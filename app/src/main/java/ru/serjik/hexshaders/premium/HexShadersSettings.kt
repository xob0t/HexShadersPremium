package ru.serjik.hexshaders.premium

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import ru.serjik.hexshaders.premium.databinding.ActivitySettingsBinding
import ru.serjik.hexshaders.renderer.HexRendererWrapper
import ru.serjik.hexshaders.renderer.ShaderPreferenceStore
import ru.serjik.preferences.PreferenceController
import ru.serjik.preferences.PreferenceParser
import ru.serjik.wallpaper.BaseSettingsActivity
import ru.serjik.wallpaper.WallpaperOffsetsListener
import ru.serjik.utils.AssetsUtils
import java.io.IOException

/**
 * Settings activity for HexShaders Premium.
 * Provides shader selection, per-shader preference controls, preview rendering, and performance info overlay.
 */
class HexShadersSettings : BaseSettingsActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var shaderList: List<String>
    private var renderer: HexRendererWrapper? = null
    private lateinit var appStore: ShaderPreferenceStore
    private var controllers: MutableList<PreferenceController> = mutableListOf()
    private var cachedGLVersion: String? = null

    /** OnClickListener for buy premium, reset defaults, and shader live buttons. */
    private val buttonClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.button_buy_premium -> {
                val premiumId = "${packageName}.premium"
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$premiumId")))
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$premiumId")))
                }
            }
            R.id.button_shader_live -> {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ru.serjik.shaderlive")))
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.serjik.shaderlive")))
                }
            }
            R.id.button_reset_to_defaults -> resetToDefaults()
        }
    }

    /** OnItemSelectedListener for the shader selection spinner. */
    private val shaderSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
            reloadShaderSettings()
        }

        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
    }

    /** PreferenceChangeListener that triggers renderer reload when any preference changes. */
    private val preferenceChangeListener = ru.serjik.preferences.PreferenceChangeListener {
        renderer?.resetContext()
    }

    /** WallpaperOffsetsListener returning 0.0 for static preview offset. */
    private val previewOffsetsListener = WallpaperOffsetsListener { 0.0f }

    /** Runnable that periodically updates the info overlay (fps, performance labels). */
    private val infoUpdater: Runnable = object : Runnable {
        override fun run() {
            val info = renderer?.getInfo() ?: return
            val bgColor = when {
                info[0].contains("drop") -> Color.argb(128, 255, 0, 0)
                info[0].contains("bad") -> Color.argb(128, 128, 0, 0)
                info[0].contains("good") -> Color.argb(128, 128, 128, 0)
                info[0].contains("ok") -> Color.argb(128, 0, 128, 0)
                else -> Color.argb(128, 0, 128, 0)
            }
            binding.layoutInfo.setBackgroundColor(bgColor)
            for (i in 0 until 4) {
                (binding.layoutInfo.getChildAt(i) as TextView).text = info[i]
            }
            binding.layoutInfo.postDelayed(this, 500L)
        }
    }

    /** Resets all preference controllers to default values and reloads settings. */
    private fun resetToDefaults() {
        for (controller in controllers) {
            controller.preferenceEntry?.getListeners()?.clear()
            controller.preferenceEntry?.reset()
        }
        reloadShaderSettings()
    }

    /** Reloads settings for the currently selected shader: creates preference controls and triggers renderer reload. */
    private fun reloadShaderSettings() {
        val selectedShader = binding.spinnerShader.selectedItem as String
        appStore.put("selected_shader", selectedShader)
        val shaderStore = ShaderPreferenceStore(selectedShader, this)
        var prefTokens = PreferenceParser.extractPrefTokens(
            AssetsUtils.readText("shaders/$selectedShader", assets)
        )
        if (!isPremium() && prefTokens.size > 4) {
            prefTokens = prefTokens.subList(0, 4)
        }
        controllers = PreferenceParser.createControllers(binding.layoutSettings, prefTokens, shaderStore).toMutableList()
        for (controller in controllers) {
            controller.preferenceEntry?.getListeners()?.addListener(preferenceChangeListener)
        }
        renderer?.resetContext()
    }

    /** Returns true if this is the premium variant of the app. */
    private fun isPremium(): Boolean = packageName.endsWith(".premium")

    /** Starts the preview renderer and schedules info overlay updates. */
    private fun startPreview() {
        if (renderer == null) {
            renderer = HexRendererWrapper(GLSurfaceView(this), previewOffsetsListener)
        }
        binding.layoutRoot.addView(renderer!!.getSurfaceView(), 0)
        renderer!!.resume()
        binding.layoutInfo.postDelayed(infoUpdater, 500L)
    }

    /** Stops the preview renderer and cancels info overlay updates. */
    private fun stopPreview() {
        binding.layoutInfo.removeCallbacks(infoUpdater)
        renderer?.pause()
        renderer?.let { binding.layoutRoot.removeView(it.getSurfaceView()) }
    }

    /** Lists available shader files filtered by GL capabilities. */
    private fun listAvailableShaders(): List<String> {
        val glSuffix = detectGLVersion()
        val ext = if (glSuffix.endsWith("n")) ".el" else ".gl"
        val shaders = mutableListOf<String>()
        try {
            assets.list("shaders")?.forEach { filename ->
                if (filename.contains(". ") &&
                    filename.contains(ext) &&
                    filename.substring(filename.length - 4).compareTo(glSuffix) <= 0
                ) {
                    shaders.add(filename)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return shaders
    }

    /**
     * Detects the GL version and vertex texture support by creating a temporary EGL context.
     * Returns a suffix like "gl2n", "gl2t", "gl3n", or "gl3t".
     */
    private fun detectGLVersion(): String {
        if (cachedGLVersion == null) {
            val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            EGL14.eglInitialize(display, version, 0, version, 1)
            val configs = arrayOfNulls<android.opengl.EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(
                display, intArrayOf(
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_DEPTH_SIZE, 0,
                    EGL14.EGL_CONFIG_CAVEAT, EGL14.EGL_NONE,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_NONE
                ), 0, configs, 0, 1, numConfigs, 0
            )
            val config = configs[0]!!
            val surface = EGL14.eglCreatePbufferSurface(
                display, config, intArrayOf(
                    EGL14.EGL_WIDTH, 8, EGL14.EGL_HEIGHT, 8, EGL14.EGL_NONE
                ), 0
            )
            val eglContext = EGL14.eglCreateContext(
                display, config, EGL14.EGL_NO_CONTEXT, intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE
                ), 0
            )
            EGL14.eglMakeCurrent(display, surface, surface, eglContext)

            val maxVertexTexUnits = IntArray(1)
            GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, maxVertexTexUnits, 0)
            val glVersionString = GLES20.glGetString(GLES20.GL_VERSION)

            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(display, surface)
            EGL14.eglDestroyContext(display, eglContext)
            EGL14.eglTerminate(display)

            val glMajor = if (glVersionString.contains("3.")) "3" else "2"
            val texSupport = if (maxVertexTexUnits[0] > 4) "t" else "n"
            cachedGLVersion = "gl$glMajor$texSupport"
        }
        return cachedGLVersion!!
    }

    override val wallpaperServiceClass: Class<*>
        get() = HexShadersService::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appStore = ShaderPreferenceStore("application_store", this)
        shaderList = listAvailableShaders()
        binding.spinnerShader.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, shaderList
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val defaultShader = appStore.get("selected_shader", shaderList.getOrElse(2) { shaderList[0] })
        for (i in shaderList.indices) {
            if (defaultShader == shaderList[i]) {
                binding.spinnerShader.setSelection(i)
            }
        }
        binding.spinnerShader.onItemSelectedListener = shaderSelectedListener
        if (isPremium()) {
            binding.layoutAdvice.visibility = View.GONE
            binding.buttonResetToDefaults.setOnClickListener(buttonClickListener)
        } else {
            binding.buttonResetToDefaults.visibility = View.GONE
            binding.buttonBuyPremium.setOnClickListener(buttonClickListener)
            binding.buttonShaderLive.setOnClickListener(buttonClickListener)
        }
    }

    override fun onPause() {
        stopPreview()
        super.onPause()
    }

    override fun onResume() {
        startPreview()
        super.onResume()
    }
}
