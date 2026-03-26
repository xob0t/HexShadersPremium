package ru.serjik.hexshaders.premium;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ru.serjik.hexshaders.premium.databinding.ActivitySettingsBinding;
import ru.serjik.hexshaders.renderer.HexRendererWrapper;
import ru.serjik.hexshaders.renderer.ShaderPreferenceStore;
import ru.serjik.preferences.PreferenceChangeListener;
import ru.serjik.preferences.PreferenceController;
import ru.serjik.preferences.PreferenceEntry;
import ru.serjik.preferences.PreferenceParser;
import ru.serjik.wallpaper.BaseSettingsActivity;
import ru.serjik.wallpaper.WallpaperOffsetsListener;
import ru.serjik.utils.AssetsUtils;

/**
 * Settings activity for HexShaders Premium.
 * Provides shader selection, per-shader preference controls, preview rendering, and performance info overlay.
 */
public class HexShadersSettings extends BaseSettingsActivity {
    private ActivitySettingsBinding binding;
    private List<String> shaderList;
    private HexRendererWrapper renderer;
    private ShaderPreferenceStore appStore;
    private List<PreferenceController> controllers = new ArrayList<>();
    private String cachedGLVersion = null;

    /** OnClickListener for buy premium, reset defaults, and shader live buttons. */
    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.button_buy_premium) {
                String premiumId = String.valueOf(getPackageName()) + ".premium";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + premiumId)));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + premiumId)));
                }
            } else if (view.getId() == R.id.button_shader_live) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ru.serjik.shaderlive")));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.serjik.shaderlive")));
                }
            } else if (view.getId() == R.id.button_reset_to_defaults) {
                resetToDefaults();
            }
        }
    };

    /** OnItemSelectedListener for the shader selection spinner. */
    private AdapterView.OnItemSelectedListener shaderSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            reloadShaderSettings();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    /** PreferenceChangeListener that triggers renderer reload when any preference changes. */
    private PreferenceChangeListener preferenceChangeListener = new PreferenceChangeListener() {
        @Override
        public void onPreferenceChanged(PreferenceEntry entry) {
            renderer.resetContext();
        }
    };

    /** WallpaperOffsetsListener returning 0.0 for static preview offset. */
    private WallpaperOffsetsListener previewOffsetsListener = new WallpaperOffsetsListener() {
        @Override
        public float getOffset() {
            return 0.0f;
        }
    };

    /** Runnable that periodically updates the info overlay (fps, performance labels). */
    private Runnable infoUpdater = new Runnable() {
        @Override
        public void run() {
            String[] info = renderer.getInfo();
            if (info[0].contains("ok")) {
                binding.layoutInfo.setBackgroundColor(Color.argb(128, 0, 128, 0));
            }
            if (info[0].contains("good")) {
                binding.layoutInfo.setBackgroundColor(Color.argb(128, 128, 128, 0));
            }
            if (info[0].contains("bad")) {
                binding.layoutInfo.setBackgroundColor(Color.argb(128, 128, 0, 0));
            }
            if (info[0].contains("drop")) {
                binding.layoutInfo.setBackgroundColor(Color.argb(128, 255, 0, 0));
            }
            for (int i = 0; i < 4; i++) {
                ((TextView) binding.layoutInfo.getChildAt(i)).setText(info[i]);
            }
            binding.layoutInfo.postDelayed(this, 500L);
        }
    };

    /**
     * Resets all preference controllers to default values and reloads settings.
     */
    private void resetToDefaults() {
        for (PreferenceController controller : this.controllers) {
            controller.getPreferenceEntry().getListeners().clear();
            controller.getPreferenceEntry().reset();
        }
        reloadShaderSettings();
    }

    /**
     * Reloads settings for the currently selected shader: creates preference controls and triggers renderer reload.
     */
    private void reloadShaderSettings() {
        String selectedShader = (String) this.binding.spinnerShader.getSelectedItem();
        this.appStore.put("selected_shader", selectedShader);
        ShaderPreferenceStore shaderStore = new ShaderPreferenceStore(selectedShader, this);
        List<String> prefTokens = PreferenceParser.extractPrefTokens(AssetsUtils.readText("shaders/" + selectedShader, getAssets()));
        if (!isPremium() && prefTokens.size() > 4) {
            prefTokens = prefTokens.subList(0, 4);
        }
        this.controllers = PreferenceParser.createControllers(this.binding.layoutSettings, prefTokens, shaderStore);
        for (PreferenceController controller : this.controllers) {
            controller.getPreferenceEntry().getListeners().addListener(this.preferenceChangeListener);
        }
        this.renderer.resetContext();
    }

    /**
     * Returns true if this is the premium variant of the app.
     */
    private boolean isPremium() {
        return getPackageName().endsWith(".premium");
    }

    /**
     * Starts the preview renderer and schedules info overlay updates.
     */
    private void startPreview() {
        if (this.renderer == null) {
            this.renderer = new HexRendererWrapper(new GLSurfaceView(this), this.previewOffsetsListener);
        }
        this.binding.layoutRoot.addView(this.renderer.getSurfaceView(), 0);
        this.renderer.resume();
        this.binding.layoutInfo.postDelayed(this.infoUpdater, 500L);
    }

    /**
     * Stops the preview renderer and cancels info overlay updates.
     */
    private void stopPreview() {
        this.binding.layoutInfo.removeCallbacks(this.infoUpdater);
        this.renderer.pause();
        this.binding.layoutRoot.removeView(this.renderer.getSurfaceView());
    }

    /**
     * Lists available shader files filtered by GL capabilities.
     */
    private List<String> listAvailableShaders() {
        String glSuffix = detectGLVersion();
        String ext = glSuffix.endsWith("n") ? ".el" : ".gl";
        ArrayList<String> shaders = new ArrayList<>();
        try {
            for (String filename : getAssets().list("shaders")) {
                if (filename.contains(". ") && filename.contains(ext) && filename.substring(filename.length() - 4).compareTo(glSuffix) <= 0) {
                    shaders.add(filename);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return shaders;
    }

    /**
     * Detects the GL version and vertex texture support by creating a temporary EGL context.
     * Returns a suffix like "gl2n", "gl2t", "gl3n", or "gl3t".
     */
    private String detectGLVersion() {
        if (this.cachedGLVersion == null) {
            EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            int[] version = new int[2];
            EGL14.eglInitialize(display, version, 0, version, 1);
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            EGL14.eglChooseConfig(display, new int[]{
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_DEPTH_SIZE, 0,
                    EGL14.EGL_CONFIG_CAVEAT, EGL14.EGL_NONE,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_NONE
            }, 0, configs, 0, 1, numConfigs, 0);
            EGLConfig config = configs[0];
            EGLSurface surface = EGL14.eglCreatePbufferSurface(display, config, new int[]{
                    EGL14.EGL_WIDTH, 8, EGL14.EGL_HEIGHT, 8, EGL14.EGL_NONE
            }, 0);
            EGLContext eglContext = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, new int[]{
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE
            }, 0);
            EGL14.eglMakeCurrent(display, surface, surface, eglContext);

            int[] maxVertexTexUnits = new int[1];
            GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, maxVertexTexUnits, 0);
            String glVersionString = GLES20.glGetString(GLES20.GL_VERSION);

            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(display, surface);
            EGL14.eglDestroyContext(display, eglContext);
            EGL14.eglTerminate(display);

            this.cachedGLVersion = "gl" + (glVersionString.contains("3.") ? "3" : "2") + (maxVertexTexUnits[0] > 4 ? "t" : "n");
        }
        return this.cachedGLVersion;
    }

    @Override
    protected Class<?> getWallpaperServiceClass() {
        return HexShadersService.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.appStore = new ShaderPreferenceStore("application_store", this);
        this.shaderList = listAvailableShaders();
        this.binding.spinnerShader.setAdapter((SpinnerAdapter) new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, this.shaderList));
        for (int i = 0; i < this.shaderList.size(); i++) {
            if (this.appStore.get("selected_shader", this.shaderList.get(2)).equals(this.shaderList.get(i))) {
                this.binding.spinnerShader.setSelection(i);
            }
        }
        this.binding.spinnerShader.setOnItemSelectedListener(this.shaderSelectedListener);
        if (isPremium()) {
            this.binding.layoutAdvice.setVisibility(View.GONE);
            this.binding.buttonResetToDefaults.setOnClickListener(this.buttonClickListener);
        } else {
            this.binding.buttonResetToDefaults.setVisibility(View.GONE);
            this.binding.buttonBuyPremium.setOnClickListener(this.buttonClickListener);
            this.binding.buttonShaderLive.setOnClickListener(this.buttonClickListener);
        }
    }

    @Override
    protected void onPause() {
        stopPreview();
        super.onPause();
    }

    @Override
    protected void onResume() {
        startPreview();
        super.onResume();
    }
}
