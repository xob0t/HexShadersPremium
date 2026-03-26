package ru.serjik.engine.gl

import android.opengl.GLES20
import ru.serjik.utils.SerjikLog

abstract class ShaderProgram(combinedSourceCode: String) {

    private var programHandle: Int
    private var vertexShaderHandle: Int = 0
    private var fragmentShaderHandle: Int = 0

    init {
        val shaders = combinedSourceCode.split("====")
        if (shaders.size < 2) {
            SerjikLog.log("Shader source missing '====' delimiter between vertex and fragment shaders")
            programHandle = 0
        } else {
            programHandle = createProgram(shaders[0], shaders[1])
        }
    }

    private fun compileShader(shaderType: Int, source: String): Int {
        val shaderHandle = GLES20.glCreateShader(shaderType)
        if (shaderHandle != 0) {
            GLES20.glShaderSource(shaderHandle, source)
            GLES20.glCompileShader(shaderHandle)
            val isCompiled = IntArray(1)
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, isCompiled, 0)
            if (isCompiled[0] == 0) {
                val typeStr = if (shaderType == GLES20.GL_VERTEX_SHADER) "vertex" else "fragment"
                SerjikLog.log("Could not compile shader $typeStr:")
                SerjikLog.log(GLES20.glGetShaderInfoLog(shaderHandle))
                GLES20.glDeleteShader(shaderHandle)
                return 0
            }
        } else {
            SerjikLog.log("Could not compile shader $shaderType:")
        }
        return shaderHandle
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShaderHandle == 0) return 0

        fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShaderHandle == 0) {
            GLES20.glDeleteShader(vertexShaderHandle)
            return 0
        }

        val programHandle = GLES20.glCreateProgram()
        if (programHandle == 0) return 0

        GLES20.glAttachShader(programHandle, vertexShaderHandle)
        GLError.check("glAttachVertexShader")
        GLES20.glAttachShader(programHandle, fragmentShaderHandle)
        GLError.check("glAttachFragmentShader")
        GLES20.glLinkProgram(programHandle)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == GLES20.GL_TRUE) return programHandle

        SerjikLog.log("Could not link program: ")
        SerjikLog.log(GLES20.glGetProgramInfoLog(programHandle))
        GLES20.glDeleteProgram(programHandle)
        GLES20.glDeleteShader(fragmentShaderHandle)
        fragmentShaderHandle = 0
        GLES20.glDeleteShader(vertexShaderHandle)
        vertexShaderHandle = 0
        return 0
    }

    protected fun getUniformLocation(name: String): Int =
        GLES20.glGetUniformLocation(programHandle, name)

    fun use() {
        GLES20.glUseProgram(programHandle)
    }

    protected fun getAttribLocation(name: String): Int =
        GLES20.glGetAttribLocation(programHandle, name)

    /**
     * Releases the GL program and attached shader resources. Must be called on the GL thread.
     */
    fun release() {
        if (vertexShaderHandle != 0) {
            GLES20.glDeleteShader(vertexShaderHandle)
            vertexShaderHandle = 0
        }
        if (fragmentShaderHandle != 0) {
            GLES20.glDeleteShader(fragmentShaderHandle)
            fragmentShaderHandle = 0
        }
        if (programHandle != 0) {
            GLES20.glDeleteProgram(programHandle)
            programHandle = 0
        }
    }

    companion object {
        fun releaseCompiler() {
            GLES20.glReleaseShaderCompiler()
        }
    }
}
