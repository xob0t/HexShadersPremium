package ru.serjik.hexshaders.renderer.slideshow;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;
import ru.serjik.engine.gl.BufferAllocator;
import ru.serjik.engine.gl.RenderTarget;
import ru.serjik.engine.gl.ShaderProgram;
import ru.serjik.hexshaders.renderer.HexScene;
import ru.serjik.hexshaders.renderer.ShaderPreferenceStore;
import ru.serjik.preferences.PreferenceEntry;
import ru.serjik.preferences.PreferenceParser;
import ru.serjik.preferences.values.IntegerValue;
import ru.serjik.utils.AssetsUtils;
import ru.serjik.utils.DeltaTimer;
import ru.serjik.utils.FPSCallback;
import ru.serjik.utils.FPSCounter;
import ru.serjik.utils.HexUtils;
import ru.serjik.wallpaper.WallpaperOffsetsListener;

/**
 * Slideshow hex scene that incrementally renders hexagons across multiple frames.
 * Uses render-to-texture with triple buffering to distribute per-hexagon shader computation
 * across frames, achieving higher visual quality at the cost of temporal resolution.
 */
public class SlideShowHexScene implements HexScene {
    private float globalTime = 0.0f;

    private AssetManager assets;
    private Context context;
    private float pointSize;
    private int pointsInRow;
    private int totalHexCount;
    private float timeScale;
    private FloatBuffer hexPositions;
    private WallpaperOffsetsListener offsetsListener;
    private SlideShowShaderHex shaderHex;
    private SlideShowFinalShader finalShader;

    private final Object initLock = new Object();
    private RenderTarget screenTarget;
    private RenderTarget renderTargetA;
    private RenderTarget renderTargetB;
    private RenderTarget renderTargetC;

    private DeltaTimer deltaTimer = new DeltaTimer();
    private int currentHexIndex = 0;
    private int hexesPerFrame = 1;
    private int slideDivisor = 1;

    /** FPS-adaptive callback that adjusts hexes-per-frame based on performance. */
    private FPSCallback fpsAdaptiveCallback = new FPSCallback() {
        @Override
        public void onFPSUpdate(FPSCounter counter) {
            if (counter.getFPS() < 20.0f) {
                hexesPerFrame /= 2;
                hexesPerFrame++;
                return;
            }
            hexesPerFrame *= 2;
            if (hexesPerFrame > totalHexCount / slideDivisor) {
                hexesPerFrame = totalHexCount / slideDivisor;
            }
        }
    };

    private FPSCounter fpsCounter = new FPSCounter(this.fpsAdaptiveCallback, 500);
    int frameIndex = 0;
    float wallpaperOffset = 0.0f;
    int subFrameCount = 0;

    public SlideShowHexScene(Context context, WallpaperOffsetsListener offsetsListener) {
        this.offsetsListener = offsetsListener;
        this.context = context;
        this.assets = this.context.getAssets();
    }

    /**
     * Computes hex grid positions with interleaved texture coordinates.
     * Each vertex has 4 floats: x, y, texU, texV (stride 16 bytes).
     */
    private int computeHexGrid(float width, float height, float pointsInRow) {
        float aspectX;
        float aspectY;
        float cellSize = 2.0f / pointsInRow;
        if (width < height) {
            aspectX = 1.0f;
            aspectY = width / height;
            this.pointSize = (0.9f * width) / pointsInRow;
        } else {
            aspectX = height / width;
            aspectY = 1.0f;
            this.pointSize = (0.9f * height) / pointsInRow;
        }
        int cols = ((int) ((0.7f * width) / this.pointSize)) + 2;
        int rows = ((int) ((0.7f * height) / this.pointSize)) + 1;
        this.hexPositions = BufferAllocator.createFloatBuffer(((rows * 2) + 1) * 3 * ((cols * 2) + 1));
        this.hexPositions.position(0);
        int index = 0;
        for (int r = -rows; r <= rows; r++) {
            for (int q = (-cols) - (r / 2); q <= cols - (r / 2); q++) {
                float x = HexUtils.hexX(q, r) * aspectX * cellSize;
                float y = HexUtils.hexY(r) * aspectY * cellSize;
                if (Math.abs(x) < 1.0f && Math.abs(y) < 1.0f) {
                    this.hexPositions.put(x);
                    this.hexPositions.put(y);
                    this.hexPositions.put(((index % 256) + 0.5f) / 256.0f);
                    this.hexPositions.put(((index / 256) + 0.5f) / 256.0f);
                    index++;
                }
            }
        }
        return this.hexPositions.position() / 4;
    }

    /**
     * Rotates the triple-buffered render targets.
     */
    private void rotateRenderTargets() {
        RenderTarget temp = this.renderTargetB;
        this.renderTargetB = this.renderTargetC;
        this.renderTargetC = this.renderTargetA;
        this.renderTargetA = temp;
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        this.totalHexCount = computeHexGrid(width, height, this.pointsInRow);
        this.hexesPerFrame = this.totalHexCount / this.slideDivisor;
        this.currentHexIndex = 0;
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        // Release old render targets to prevent GPU memory leaks
        if (this.screenTarget != null) this.screenTarget.release();
        if (this.renderTargetA != null) this.renderTargetA.release();
        if (this.renderTargetB != null) this.renderTargetB.release();
        if (this.renderTargetC != null) this.renderTargetC.release();
        this.screenTarget = new RenderTarget(width, height, false);
        this.renderTargetA = new RenderTarget(256, 256, true);
        this.renderTargetB = new RenderTarget(256, 256, true);
        this.renderTargetC = new RenderTarget(256, 256, true);
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
        info[2] = String.format("ppf: %d", Integer.valueOf(this.hexesPerFrame));
        info[3] = String.format("tpc: %d", Integer.valueOf(this.totalHexCount));
        return info;
    }

    @Override
    public void initialize() {
        ShaderPreferenceStore appStore = new ShaderPreferenceStore("application_store", this.context);
        synchronized (this.initLock) {
            if (appStore.get("reset_settings", "false").equals("true")) {
                appStore.clearAll();
            }
            appStore.put("reset_settings", "true");
            String selectedShader = appStore.get("selected_shader", "03. rainbow.gl2n");
            ShaderPreferenceStore shaderStore = new ShaderPreferenceStore(selectedShader, this.context.getApplicationContext());
            String shaderSource = AssetsUtils.readText("shaders/" + selectedShader, this.assets);
            List<String> textures = PreferenceParser.extractSection(shaderSource, "textures(", ",", ")");
            Map<String, PreferenceEntry> prefMap = PreferenceParser.createPreferenceMap(PreferenceParser.extractPrefTokens(shaderSource), shaderStore);
            String substitutedSource = PreferenceParser.substitutePreferences(shaderSource, shaderStore);
            PreferenceEntry pointsEntry = prefMap.get("pointsInTheRow");
            this.pointsInRow = pointsEntry != null ? new IntegerValue(pointsEntry.get()).value : 15;
            PreferenceEntry timeScaleEntry = prefMap.get("timeScale");
            this.timeScale = timeScaleEntry != null ? new IntegerValue(timeScaleEntry.get()).value : 50;
            if (prefMap.containsKey("slides")) {
                int[] fibonacciSequence = {1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144};
                int slidesIndex = new IntegerValue(((PreferenceEntry) prefMap.get("slides")).get()).value;
                if (slidesIndex >= 12) {
                    slidesIndex = 4;
                }
                this.slideDivisor = fibonacciSequence[slidesIndex];
            }
            this.shaderHex = new SlideShowShaderHex(this.context.getApplicationContext(), substitutedSource, textures);
            this.finalShader = new SlideShowFinalShader(this.assets);
            ShaderProgram.releaseCompiler();
            appStore.put("reset_settings", "false");
        }
    }

    @Override
    public void drawFrame() {
        // Bind render target for per-hexagon shader computation
        this.renderTargetA.bind();
        if (this.currentHexIndex == 0) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }

        // Determine batch size for this frame
        int batchSize = this.hexesPerFrame + 1;
        if (this.currentHexIndex + batchSize > this.totalHexCount) {
            batchSize = this.totalHexCount - this.currentHexIndex;
        }

        // Render batch of hexagons into 256x256 texture
        GLES20.glViewport(0, 0, 256, 256);
        this.shaderHex.draw(this.screenTarget.getWidth(), this.screenTarget.getHeight(),
                this.wallpaperOffset, this.hexPositions, this.currentHexIndex, batchSize,
                globalTime, this.deltaTimer.getDeltaSeconds() * this.timeScale, this.frameIndex);

        // Bind screen and composite final output
        this.screenTarget.bind();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        this.finalShader.draw(this.renderTargetB.getTextureId(), this.renderTargetC.getTextureId(),
                (float) this.currentHexIndex / this.totalHexCount, this.pointSize,
                this.hexPositions, this.totalHexCount);

        this.currentHexIndex += batchSize;
        this.subFrameCount++;

        // Check if all hexagons have been rendered for this cycle
        if (this.currentHexIndex >= this.totalHexCount) {
            this.wallpaperOffset = this.offsetsListener.getOffset();
            this.currentHexIndex = 0;
            this.frameIndex++;
            globalTime += this.deltaTimer.tick().getDeltaSeconds() * this.timeScale;
            if (Math.abs(globalTime) > Math.abs(60000.0f * this.timeScale) && Math.abs(this.timeScale) > 0.01f) {
                globalTime = 0.0f;
            }
            rotateRenderTargets();
        }
        this.fpsCounter.tick();
    }
}
