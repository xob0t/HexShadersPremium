package ru.serjik.hexshaders.renderer.legacy;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;
import ru.serjik.engine.gl.BufferAllocator;
import ru.serjik.engine.gl.ShaderProgram;
import ru.serjik.hexshaders.renderer.HexScene;
import ru.serjik.hexshaders.renderer.ShaderPreferenceStore;
import ru.serjik.preferences.PreferenceEntry;
import ru.serjik.preferences.PreferenceParser;
import ru.serjik.preferences.values.IntegerValue;
import ru.serjik.utils.AssetsUtils;
import ru.serjik.utils.DeltaTimer;
import ru.serjik.utils.FPSCounter;
import ru.serjik.utils.HexUtils;

/**
 * Legacy hex scene that renders all hexagons in a single pass per frame.
 * Used on GPUs with fewer than 4 vertex texture units.
 */
public class LegacyHexScene implements HexScene {
    private AssetManager assets;
    private Context context;
    private float pointSize;
    private float pointsInRow;
    private int hexCount;
    private float timeScale;
    private float screenWidth;
    private float screenHeight;
    private FloatBuffer hexPositions;
    private LegacyShaderHex shaderHex;

    DeltaTimer deltaTimer = new DeltaTimer(250);
    private FPSCounter fpsCounter = new FPSCounter(null, 500);
    int frameIndex = 0;
    float wallpaperOffset = 0.0f;

    public LegacyHexScene(Context context) {
        this.context = context;
        this.assets = context.getAssets();
    }

    /**
     * Computes hex grid positions and returns the number of hex points.
     */
    private int computeHexGrid(float width, float height, float pointsInRow) {
        float aspectX;
        float aspectY;
        float cellSize = 2.0f / pointsInRow;
        if (width < height) {
            aspectY = width / height;
            this.pointSize = (0.9f * width) / pointsInRow;
            aspectX = 1.0f;
        } else {
            this.pointSize = (0.9f * height) / pointsInRow;
            aspectX = height / width;
            aspectY = 1.0f;
        }
        int cols = ((int) ((0.7f * width) / this.pointSize)) + 2;
        int rows = ((int) ((0.7f * height) / this.pointSize)) + 1;
        this.hexPositions = BufferAllocator.createFloatBuffer(((rows * 2) + 1) * 3 * ((cols * 2) + 1));
        this.hexPositions.position(0);
        for (int r = -rows; r <= rows; r++) {
            for (int q = (-cols) - (r / 2); q <= cols - (r / 2); q++) {
                float x = HexUtils.hexX(q, r) * aspectX * cellSize;
                float y = HexUtils.hexY(r) * aspectY * cellSize;
                if (Math.abs(x) < 1.0f + cellSize && Math.abs(y) < 1.0f + cellSize) {
                    this.hexPositions.put(x);
                    this.hexPositions.put(y);
                }
            }
        }
        return this.hexPositions.position() / 2;
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glViewport(0, 0, width, height);
        this.screenWidth = width;
        this.screenHeight = height;
        this.hexCount = computeHexGrid(width, height, this.pointsInRow);
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        if (visible) {
            this.deltaTimer.tick();
        }
    }

    @Override
    public String[] getInfo() {
        String[] info = new String[4];
        info[0] = "perf: ok";
        if (this.fpsCounter.getFPS() < 55.0f) {
            info[0] = "perf: good";
        }
        if (this.fpsCounter.getFPS() < 40.0f) {
            info[0] = "perf: bad";
        }
        if (this.fpsCounter.getFPS() < 20.0f) {
            info[0] = "perf: drop";
        }
        info[1] = String.format("fps: %3.1f", Float.valueOf(this.fpsCounter.getFPS()));
        info[2] = String.format("ppf: %d", Integer.valueOf(this.hexCount));
        info[3] = String.format("tpc: %d", Integer.valueOf(this.hexCount));
        return info;
    }

    @Override
    public void initialize() {
        ShaderPreferenceStore appStore = new ShaderPreferenceStore("application_store", this.context);
        if (appStore.get("reset_settings", "false").equals("true")) {
            appStore.clearAll();
        }
        appStore.put("reset_settings", "true");
        String selectedShader = appStore.get("selected_shader", "03. rainbow.gl2n").replace(".gl", ".el");
        ShaderPreferenceStore shaderStore = new ShaderPreferenceStore(selectedShader, this.context.getApplicationContext());
        String shaderSource = AssetsUtils.readText("shaders/" + selectedShader, this.assets);
        List<String> textures = PreferenceParser.extractSection(shaderSource, "textures(", ",", ")");
        Map<String, PreferenceEntry> prefMap = PreferenceParser.createPreferenceMap(PreferenceParser.extractPrefTokens(shaderSource), shaderStore);
        String substitutedSource = PreferenceParser.substitutePreferences(shaderSource, shaderStore);
        PreferenceEntry pointsEntry = prefMap.get("pointsInTheRow");
        this.pointsInRow = pointsEntry != null ? new IntegerValue(pointsEntry.get()).value : 15;
        PreferenceEntry timeScaleEntry = prefMap.get("timeScale");
        this.timeScale = timeScaleEntry != null ? new IntegerValue(timeScaleEntry.get()).value : 50;
        this.shaderHex = new LegacyShaderHex(this.assets, substitutedSource, textures, this.timeScale);
        ShaderProgram.releaseCompiler();
        appStore.put("reset_settings", "false");
    }

    @Override
    public void drawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        this.shaderHex.draw(this.deltaTimer.tick().getDeltaSeconds(), this.screenWidth, this.screenHeight, this.pointSize, this.hexPositions, this.hexCount);
        this.fpsCounter.tick();
    }
}
