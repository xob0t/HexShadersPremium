package ru.serjik.wallpaper

import android.app.WallpaperColors
import android.content.SharedPreferences
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import ru.serjik.engine.gl.GLRenderer
import ru.serjik.engine.gl.RendererFactory
import ru.serjik.hexshaders.renderer.ShaderColors
import ru.serjik.utils.SerjikLog
import kotlin.math.abs

abstract class GLWallpaperService : WallpaperService(), RendererFactory, WallpaperOffsetsListener {

    private val engines = mutableListOf<GLWallpaperEngine>()
    private var wallpaperOffset = 0.0f

    override fun getOffset(): Float = wallpaperOffset

    override fun onCreateEngine(): Engine {
        val engine = GLWallpaperEngine()
        engines.add(engine)
        return engine
    }

    /**
     * Returns the current shader name from shared preferences.
     * Subclasses can override if they store the shader name differently.
     */
    protected open fun getCurrentShaderName(): String {
        val prefs = getSharedPreferences("application_store", MODE_PRIVATE)
        return prefs.getString("selected_shader", "03. rainbow.gl2n") ?: "03. rainbow.gl2n"
    }

    private inner class GLWallpaperEngine : Engine() {
        var offsetsActivated = false
        var renderer: GLRenderer? = null
        private var lastOffsetValue = 0.0f
        private var offsetChangeCounter = 0
        private var lastKnownShaderName: String? = null

        /**
         * SharedPreferences listener that triggers context reload when settings change.
         * Replaces the old startService() IPC mechanism.
         */
        private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "reload_signal") {
                renderer?.resetContext()
                // Also update wallpaper colors in case user changed RGB preferences
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    lastKnownShaderName = getCurrentShaderName()
                    notifyColorsChanged()
                }
            }
        }

        private var prefs: SharedPreferences? = null

        private fun resetOffsetTracking() {
            offsetsActivated = false
            lastOffsetValue = 0.0f
            offsetChangeCounter = 0
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            lastKnownShaderName = getCurrentShaderName()
            val surfaceView = WallpaperGLSurfaceView(this@GLWallpaperService)
            renderer = this@GLWallpaperService.createRenderer(surfaceView)
            SerjikLog.logWithCaller(renderer.toString())

            // Register SharedPreferences listener for reload signals
            prefs = getSharedPreferences("application_store", MODE_PRIVATE)
            prefs?.registerOnSharedPreferenceChangeListener(prefsListener)
        }

        override fun onDestroy() {
            SerjikLog.logWithCaller(" $renderer")
            prefs?.unregisterOnSharedPreferenceChangeListener(prefsListener)
            renderer?.let { r ->
                r.pause()
                r.getSurfaceView().onPause()
                (r.getSurfaceView() as WallpaperGLSurfaceView).detach()
            }
            engines.remove(this)
            super.onDestroy()
        }

        override fun onOffsetsChanged(
            xOffset: Float, yOffset: Float,
            xStep: Float, yStep: Float,
            xPixelOffset: Int, yPixelOffset: Int
        ) {
            if (offsetsActivated) {
                wallpaperOffset = when {
                    xOffset < 0.0f -> 0.0f
                    xOffset > 1.0f -> 1.0f
                    else -> xOffset - 0.5f
                }
            } else {
                if (offsetChangeCounter > 3) {
                    offsetsActivated = true
                    wallpaperOffset = xOffset - 0.5f
                } else if (abs(xOffset - lastOffsetValue) <= 0.001f) {
                    offsetChangeCounter = 0
                } else {
                    offsetChangeCounter++
                    lastOffsetValue = xOffset
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            SerjikLog.logWithCaller("visible = $visible $renderer count = ${engines.size}")
            super.onVisibilityChanged(visible)
            renderer?.let { r ->
                if (!visible) {
                    r.pause()
                } else {
                    resetOffsetTracking()
                    r.resume()

                    // Check if the shader changed while invisible
                    val currentShader = getCurrentShaderName()
                    if (currentShader != lastKnownShaderName) {
                        lastKnownShaderName = currentShader
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            notifyColorsChanged()
                        }
                    }
                }
            }
        }

        /**
         * Called by Android (API 27+) to get the wallpaper's dominant colors.
         * Returns colors matching the active shader so the system theme/accent
         * matches the wallpaper appearance.
         */
        override fun onComputeColors(): WallpaperColors? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                val colors = ShaderColors.getColors(lastKnownShaderName, applicationContext)
                return WallpaperColors(
                    Color.valueOf(colors.primary),
                    Color.valueOf(colors.secondary),
                    Color.valueOf(colors.tertiary)
                )
            }
            return super.onComputeColors()
        }

        private inner class WallpaperGLSurfaceView(context: android.content.Context) : GLSurfaceView(context) {
            fun detach() {
                super.onDetachedFromWindow()
            }

            override fun getHolder(): SurfaceHolder {
                return this@GLWallpaperEngine.surfaceHolder
            }
        }
    }
}
