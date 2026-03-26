package ru.serjik.engine.gl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class Texture {
    private int textureId;
    private int width;
    private int height;

    public Texture(Bitmap bitmap, boolean recycleBitmap) {
        this.textureId = genTextureId();
        bind();
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        if (recycleBitmap) {
            bitmap.recycle();
        }
    }

    public static void setFilter(int minFilter, int magFilter) {
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, minFilter);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, magFilter);
    }

    private int genTextureId() {
        int[] ids = new int[1];
        GLES20.glGenTextures(1, ids, 0);
        return ids[0];
    }

    public static void setWrap(int wrapS, int wrapT) {
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapS);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapT);
    }

    public void bind() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureId);
    }

    /**
     * Deletes the GL texture resource. Must be called on the GL thread.
     */
    public void release() {
        if (this.textureId != 0) {
            int[] ids = new int[]{this.textureId};
            GLES20.glDeleteTextures(1, ids, 0);
            this.textureId = 0;
        }
    }
}
