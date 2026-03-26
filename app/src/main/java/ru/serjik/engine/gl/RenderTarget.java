package ru.serjik.engine.gl;

import android.opengl.GLES20;
import ru.serjik.utils.SerjikLog;

public class RenderTarget {
    private int textureId;
    private int framebufferId;
    private int width;
    private int height;
    private boolean hasFBO;
    private final int[] tempIntArray = new int[1];
    private boolean hasBeenRenderedTo = false;

    public RenderTarget(int width, int height, boolean createFBO) {
        this.textureId = 0;
        this.framebufferId = 0;
        this.width = width;
        this.height = height;
        this.hasFBO = createFBO;
        if (createFBO) {
            GLES20.glGenTextures(1, this.tempIntArray, 0);
            this.textureId = this.tempIntArray[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureId);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glGenFramebuffers(1, this.tempIntArray, 0);
            this.framebufferId = this.tempIntArray[0];
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.framebufferId);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, this.textureId, 0);
            int fbStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
            if (fbStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                SerjikLog.log("Framebuffer incomplete, status: 0x" + Integer.toHexString(fbStatus));
            }
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
    }

    public void bind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.framebufferId);
        GLES20.glViewport(0, 0, this.width, this.height);
        if (!this.hasBeenRenderedTo) {
            this.hasBeenRenderedTo = true;
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
    }

    public int getTextureId() {
        if (this.hasBeenRenderedTo) {
            return this.textureId;
        }
        return 0;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    /**
     * Releases the GL framebuffer and texture resources associated with this render target.
     * Must be called on the GL thread before discarding the object.
     */
    public void release() {
        if (this.hasFBO) {
            if (this.framebufferId != 0) {
                this.tempIntArray[0] = this.framebufferId;
                GLES20.glDeleteFramebuffers(1, this.tempIntArray, 0);
                this.framebufferId = 0;
            }
            if (this.textureId != 0) {
                this.tempIntArray[0] = this.textureId;
                GLES20.glDeleteTextures(1, this.tempIntArray, 0);
                this.textureId = 0;
            }
        }
        this.hasBeenRenderedTo = false;
    }
}
