package ru.serjik.hexshaders.renderer.slideshow;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.List;
import ru.serjik.engine.gl.ShaderProgram;
import ru.serjik.engine.gl.Texture;
import ru.serjik.utils.AssetsUtils;

/**
 * Per-hexagon shader program for the slideshow rendering mode.
 * Renders individual hexagons into a 256x256 render target texture,
 * supporting partial (batched) rendering across multiple frames.
 */
public class SlideShowShaderHex extends ShaderProgram {
    private int attribPos;
    private int attribTexPos;
    private int uniformOffset;
    private int uniformResolution;
    private int uniformGlobalTime;
    private int uniformTimeDelta;
    private int uniformFrame;
    private int[] uniformChannels;
    private Texture[] channelTextures;

    public SlideShowShaderHex(Context context, String shaderSource, List<String> textureNames) {
        super(shaderSource);
        this.attribPos = -1;
        this.attribTexPos = -1;
        this.uniformOffset = -1;
        this.uniformResolution = -1;
        this.uniformGlobalTime = -1;
        this.uniformTimeDelta = -1;
        this.uniformFrame = -1;
        this.uniformChannels = new int[]{-1, -1, -1};
        this.channelTextures = new Texture[3];
        AssetManager assets = context.getAssets();

        // Load channel textures
        for (int i = 0; i < textureNames.size(); i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1 + i);
            this.channelTextures[i] = new Texture(AssetsUtils.readBitmap("textures/" + textureNames.get(i), assets), true);
            Texture.setFilter(GLES20.GL_LINEAR, GLES20.GL_LINEAR);
            Texture.setWrap(GLES20.GL_REPEAT, GLES20.GL_REPEAT);
            this.uniformChannels[i] = getUniformLocation("iChannel" + i);
        }

        this.uniformResolution = getUniformLocation("iResolution");
        this.uniformGlobalTime = getUniformLocation("iGlobalTime");
        this.uniformTimeDelta = getUniformLocation("iTimeDelta");
        this.uniformFrame = getUniformLocation("iFrame");
        this.uniformOffset = getUniformLocation("u_offset");
        this.attribPos = getAttribLocation("a_pos");
        this.attribTexPos = getAttribLocation("t_pos");
    }

    /**
     * Draws a batch of hexagons into the render target.
     *
     * @param rtWidth      render target width
     * @param rtHeight     render target height
     * @param offset       wallpaper scroll offset
     * @param positions    interleaved vertex buffer
     * @param startIndex   first hexagon index in this batch
     * @param count        number of hexagons to draw in this batch
     * @param globalTime   accumulated global time
     * @param timeDelta    time delta for this frame
     * @param frameIndex   current frame index
     */
    public void draw(float rtWidth, float rtHeight, float offset, FloatBuffer positions,
                     int startIndex, int count, float globalTime, float timeDelta, int frameIndex) {
        use();
        bindPositions(positions);
        GLES20.glUniform3f(this.uniformResolution, rtWidth, rtHeight, rtWidth / rtHeight);
        GLES20.glUniform1f(this.uniformGlobalTime, globalTime);
        GLES20.glUniform1f(this.uniformTimeDelta, timeDelta);
        GLES20.glUniform1i(this.uniformFrame, frameIndex);
        GLES20.glUniform1f(this.uniformOffset, offset);
        if (this.channelTextures[0] != null) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            this.channelTextures[0].bind();
        }
        if (this.channelTextures[1] != null) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            this.channelTextures[1].bind();
        }
        GLES20.glUniform1i(this.uniformChannels[0], 1);
        GLES20.glUniform1i(this.uniformChannels[1], 2);
        GLES20.glUniform1i(this.uniformChannels[2], 3);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDrawArrays(GLES20.GL_POINTS, startIndex, count);
    }

    /**
     * Binds interleaved position buffer (position at offset 0, tex coords at offset 2, stride 16 bytes).
     */
    public void bindPositions(FloatBuffer positions) {
        positions.position(0);
        GLES20.glVertexAttribPointer(this.attribPos, 2, GLES20.GL_FLOAT, false, 16, (Buffer) positions);
        GLES20.glEnableVertexAttribArray(this.attribPos);
        positions.position(2);
        GLES20.glVertexAttribPointer(this.attribTexPos, 2, GLES20.GL_FLOAT, false, 16, (Buffer) positions);
        GLES20.glEnableVertexAttribArray(this.attribTexPos);
    }
}
