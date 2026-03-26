package ru.serjik.engine.gl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils

class Texture(bitmap: Bitmap, recycleBitmap: Boolean) {

    private var textureId: Int = genTextureId()
    private val width: Int
    private val height: Int

    init {
        bind()
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        width = bitmap.width
        height = bitmap.height
        if (recycleBitmap) {
            bitmap.recycle()
        }
    }

    private fun genTextureId(): Int {
        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        return ids[0]
    }

    fun bind() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
    }

    /**
     * Deletes the GL texture resource. Must be called on the GL thread.
     */
    fun release() {
        if (textureId != 0) {
            val ids = intArrayOf(textureId)
            GLES20.glDeleteTextures(1, ids, 0)
            textureId = 0
        }
    }

    companion object {
        fun setFilter(minFilter: Int, magFilter: Int) {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, minFilter)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, magFilter)
        }

        fun setWrap(wrapS: Int, wrapT: Int) {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapS)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapT)
        }
    }
}
