package ru.serjik.wallpaper;

import android.app.WallpaperColors;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.List;
import ru.serjik.engine.gl.GLRenderer;
import ru.serjik.engine.gl.RendererFactory;
import ru.serjik.hexshaders.renderer.ShaderColors;
import ru.serjik.utils.SerjikLog;

public abstract class GLWallpaperService extends WallpaperService implements RendererFactory, WallpaperOffsetsListener {
    private List<GLWallpaperEngine> engines = new ArrayList<>();
    private float wallpaperOffset = 0.0f;

    @Override
    public float getOffset() {
        return this.wallpaperOffset;
    }

    @Override
    public Engine onCreateEngine() {
        GLWallpaperEngine engine = new GLWallpaperEngine();
        this.engines.add(engine);
        return engine;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras;
        if (intent != null && (extras = intent.getExtras()) != null && "dropContext".equals(extras.getString("cmd"))) {
            for (GLWallpaperEngine engine : this.engines) {
                if (engine.renderer != null) {
                    engine.renderer.resetContext();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Returns the current shader name from shared preferences.
     * Subclasses can override if they store the shader name differently.
     */
    protected String getCurrentShaderName() {
        SharedPreferences prefs = getSharedPreferences("application_store", MODE_PRIVATE);
        return prefs.getString("selected_shader", "03. rainbow.gl2n");
    }

    private class GLWallpaperEngine extends Engine {
        boolean offsetsActivated;
        GLRenderer renderer;
        private float lastOffsetValue;
        private int offsetChangeCounter;
        private String lastKnownShaderName;

        GLWallpaperEngine() {
            this.offsetsActivated = false;
            this.lastOffsetValue = 0.0f;
            this.offsetChangeCounter = 0;
        }

        private void resetOffsetTracking() {
            this.offsetsActivated = false;
            this.lastOffsetValue = 0.0f;
            this.offsetChangeCounter = 0;
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.lastKnownShaderName = getCurrentShaderName();
            WallpaperGLSurfaceView surfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);
            this.renderer = GLWallpaperService.this.createRenderer(surfaceView);
            SerjikLog.logWithCaller(this.renderer.toString());
        }

        @Override
        public void onDestroy() {
            SerjikLog.logWithCaller(" " + this.renderer);
            this.renderer.pause();
            this.renderer.getSurfaceView().onPause();
            ((WallpaperGLSurfaceView) this.renderer.getSurfaceView()).detach();
            engines.remove(this);
            super.onDestroy();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixelOffset, int yPixelOffset) {
            if (this.offsetsActivated) {
                if (xOffset < 0.0f) {
                    wallpaperOffset = 0.0f;
                } else if (xOffset > 1.0f) {
                    wallpaperOffset = 1.0f;
                } else {
                    wallpaperOffset = xOffset - 0.5f;
                }
            } else {
                if (this.offsetChangeCounter > 3) {
                    this.offsetsActivated = true;
                    wallpaperOffset = xOffset - 0.5f;
                } else if (Math.abs(xOffset - this.lastOffsetValue) <= 0.001f) {
                    this.offsetChangeCounter = 0;
                } else {
                    this.offsetChangeCounter++;
                    this.lastOffsetValue = xOffset;
                }
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            SerjikLog.logWithCaller("visible = " + visible + " " + this.renderer + " count = " + engines.size());
            super.onVisibilityChanged(visible);
            if (this.renderer != null) {
                if (!visible) {
                    this.renderer.pause();
                } else {
                    resetOffsetTracking();
                    this.renderer.resume();

                    // Check if the shader changed while invisible
                    String currentShader = getCurrentShaderName();
                    if (currentShader != null && !currentShader.equals(this.lastKnownShaderName)) {
                        this.lastKnownShaderName = currentShader;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            notifyColorsChanged();
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
        @Override
        public WallpaperColors onComputeColors() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                ShaderColors.ColorSet colors = ShaderColors.getColors(this.lastKnownShaderName);
                return new WallpaperColors(
                        Color.valueOf(colors.primary),
                        Color.valueOf(colors.secondary),
                        Color.valueOf(colors.tertiary));
            }
            return super.onComputeColors();
        }

        private class WallpaperGLSurfaceView extends GLSurfaceView {
            WallpaperGLSurfaceView(android.content.Context context) {
                super(context);
            }

            public void detach() {
                super.onDetachedFromWindow();
            }

            @Override
            public SurfaceHolder getHolder() {
                return GLWallpaperEngine.this.getSurfaceHolder();
            }
        }
    }
}
