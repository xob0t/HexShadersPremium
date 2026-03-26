package ru.serjik.hexshaders.renderer.legacy;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.List;
import ru.serjik.engine.gl.ShaderProgram;
import ru.serjik.engine.gl.Texture;
import ru.serjik.utils.AssetsUtils;

/**
 * Shader program for the legacy (single-pass) hex rendering mode.
 * Renders all hexagons as point sprites in one draw call.
 */
public class LegacyShaderHex extends ShaderProgram {
    private float globalTime = 0.0f;

    public int uniformSize;
    public int uniformTexture;
    public int attribPos;

    private int uniformResolution;
    private int uniformGlobalTime;
    private int uniformTimeDelta;
    private int uniformFrame;
    private int[] uniformChannels;
    private float timeScale;
    private int frameCount;

    public LegacyShaderHex(AssetManager assetManager, String shaderSource, List<String> textureNames, float timeScale) {
        super(shaderSource);
        this.uniformChannels = new int[]{-1, -1, -1, -1};
        this.timeScale = 1.0f;
        this.frameCount = 0;
        this.timeScale = timeScale;

        // Load additional channel textures
        for (int i = 0; i < textureNames.size(); i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1 + i);
            new Texture(AssetsUtils.readBitmap("textures/" + textureNames.get(i), assetManager), true);
            Texture.setFilter(GLES20.GL_LINEAR, GLES20.GL_LINEAR);
            Texture.setWrap(GLES20.GL_REPEAT, GLES20.GL_REPEAT);
            this.uniformChannels[i] = getUniformLocation("iChannel" + i);
        }

        // Load hex point sprite texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        new Texture(AssetsUtils.readBitmap("textures/hex.png", assetManager), true);
        Texture.setFilter(GLES20.GL_LINEAR, GLES20.GL_LINEAR);
        Texture.setWrap(GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);

        this.uniformResolution = getUniformLocation("iResolution");
        this.uniformGlobalTime = getUniformLocation("iGlobalTime");
        this.uniformTimeDelta = getUniformLocation("iTimeDelta");
        this.uniformFrame = getUniformLocation("iFrame");
        this.uniformSize = getUniformLocation("u_size");
        this.uniformTexture = getUniformLocation("u_texture");
        this.attribPos = getAttribLocation("a_pos");
    }

    /**
     * Draws all hex points in a single pass.
     */
    public void draw(float deltaSeconds, float screenWidth, float screenHeight, float pointSize, FloatBuffer positions, int count) {
        use();
        bindPositions(positions);
        float scaledDelta = this.timeScale * deltaSeconds;
        this.frameCount++;
        globalTime += scaledDelta;
        if (Math.abs(globalTime) > Math.abs(60000.0f * this.timeScale) && Math.abs(this.timeScale) > 0.01f) {
            globalTime = 0.0f;
        }
        GLES20.glUniform3f(this.uniformResolution, screenWidth, screenHeight, screenWidth / screenHeight);
        GLES20.glUniform1f(this.uniformGlobalTime, globalTime);
        GLES20.glUniform1f(this.uniformTimeDelta, scaledDelta);
        GLES20.glUniform1i(this.uniformFrame, this.frameCount);
        GLES20.glUniform1i(this.uniformChannels[0], 1);
        GLES20.glUniform1i(this.uniformChannels[1], 2);
        GLES20.glUniform1i(this.uniformChannels[2], 3);
        GLES20.glUniform1i(this.uniformChannels[3], 4);
        GLES20.glUniform1f(this.uniformSize, pointSize);
        GLES20.glUniform1i(this.uniformTexture, 0);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, count);
    }

    /**
     * Binds the hex position buffer to the vertex attribute.
     */
    public void bindPositions(FloatBuffer positions) {
        positions.position(0);
        GLES20.glVertexAttribPointer(this.attribPos, 2, GLES20.GL_FLOAT, false, 8, (Buffer) positions);
        GLES20.glEnableVertexAttribArray(this.attribPos);
    }
}
