package ru.serjik.hexshaders.renderer.slideshow;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import ru.serjik.engine.gl.ShaderProgram;
import ru.serjik.engine.gl.Texture;
import ru.serjik.utils.AssetsUtils;

/**
 * Final compositing shader for the slideshow hex renderer.
 * Combines the current and previous rendered textures with the hex point sprite texture
 * to produce the final output.
 */
public class SlideShowFinalShader extends ShaderProgram {
    private int uniformRenderedTexture1;
    private int uniformRenderedTexture2;
    private int uniformHexagonTexture;
    private int uniformPointSize;
    private int uniformStep;
    private int attribPointPosition;
    private int attribTexPointPosition;
    private Texture hexTexture;

    public SlideShowFinalShader(AssetManager assetManager) {
        super(AssetsUtils.readText("final_hex_slide_show.glsl", assetManager));
        this.uniformRenderedTexture1 = getUniformLocation("u_RenderedTexture1");
        this.uniformRenderedTexture2 = getUniformLocation("u_RenderedTexture2");
        this.uniformHexagonTexture = getUniformLocation("u_HexagonTexture");
        this.uniformPointSize = getUniformLocation("u_PointSize");
        this.uniformStep = getUniformLocation("u_Step");
        this.attribPointPosition = getAttribLocation("a_PointPosition");
        this.attribTexPointPosition = getAttribLocation("t_PointPosition");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        this.hexTexture = new Texture(AssetsUtils.readBitmap("textures/hex.png", assetManager), true);
        Texture.setFilter(GLES20.GL_LINEAR, GLES20.GL_LINEAR);
        Texture.setWrap(GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);
    }

    /**
     * Draws the final composited frame.
     *
     * @param textureId1 texture ID from the first render target
     * @param textureId2 texture ID from the second render target
     * @param step       interpolation step (progress through hexagons)
     * @param pointSize  hex point sprite size
     * @param positions  interleaved vertex buffer (position + tex coords, stride 16)
     * @param count      number of hexagon points to draw
     */
    public void draw(int textureId1, int textureId2, float step, float pointSize, FloatBuffer positions, int count) {
        use();
        bindPositions(positions);

        // Bind hex texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        this.hexTexture.bind();
        Texture.setFilter(GLES20.GL_LINEAR, GLES20.GL_LINEAR);
        Texture.setWrap(GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glUniform1i(this.uniformHexagonTexture, 2);

        // Bind rendered texture 1
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId1);
        Texture.setFilter(GLES20.GL_NEAREST, GLES20.GL_NEAREST);
        Texture.setWrap(GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glUniform1i(this.uniformRenderedTexture1, 0);

        // Bind rendered texture 2
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId2);
        Texture.setFilter(GLES20.GL_NEAREST, GLES20.GL_NEAREST);
        Texture.setWrap(GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glUniform1i(this.uniformRenderedTexture2, 1);

        GLES20.glUniform1f(this.uniformPointSize, pointSize);
        GLES20.glUniform1f(this.uniformStep, step);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, count);
    }

    /**
     * Binds interleaved position buffer (position at offset 0, tex coords at offset 2, stride 16 bytes).
     */
    public void bindPositions(FloatBuffer positions) {
        positions.position(0);
        GLES20.glVertexAttribPointer(this.attribPointPosition, 2, GLES20.GL_FLOAT, false, 16, (Buffer) positions);
        GLES20.glEnableVertexAttribArray(this.attribPointPosition);
        positions.position(2);
        GLES20.glVertexAttribPointer(this.attribTexPointPosition, 2, GLES20.GL_FLOAT, false, 16, (Buffer) positions);
        GLES20.glEnableVertexAttribArray(this.attribTexPointPosition);
    }
}
