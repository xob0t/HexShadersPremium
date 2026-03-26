package ru.serjik.engine.gl;

import android.opengl.GLES20;
import ru.serjik.utils.SerjikLog;

public abstract class ShaderProgram {
    private int programHandle;
    private int vertexShaderHandle;
    private int fragmentShaderHandle;

    public ShaderProgram(String combinedSourceCode) {
        String[] shaders = combinedSourceCode.split("====");
        if (shaders.length < 2) {
            SerjikLog.log("Shader source missing '====' delimiter between vertex and fragment shaders");
            this.programHandle = 0;
            return;
        }
        this.programHandle = createProgram(shaders[0], shaders[1]);
    }

    private int compileShader(int shaderType, String source) {
        int shaderHandle = GLES20.glCreateShader(shaderType);
        if (shaderHandle != 0) {
            GLES20.glShaderSource(shaderHandle, source);
            GLES20.glCompileShader(shaderHandle);
            int[] isCompiled = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, isCompiled, 0);
            if (isCompiled[0] == 0) {
                SerjikLog.log("Could not compile shader " + (shaderType == GLES20.GL_VERTEX_SHADER ? "vertex" : "fragment") + ":");
                SerjikLog.log(GLES20.glGetShaderInfoLog(shaderHandle));
                GLES20.glDeleteShader(shaderHandle);
                return 0;
            }
        } else {
            SerjikLog.log("Could not compile shader " + shaderType + ":");
        }
        return shaderHandle;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        this.vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (this.vertexShaderHandle == 0) return 0;
        this.fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (this.fragmentShaderHandle == 0) {
            GLES20.glDeleteShader(this.vertexShaderHandle);
            return 0;
        }
        int programHandle = GLES20.glCreateProgram();
        if (programHandle == 0) return 0;
        GLES20.glAttachShader(programHandle, this.vertexShaderHandle);
        GLError.check("glAttachVertexShader");
        GLES20.glAttachShader(programHandle, this.fragmentShaderHandle);
        GLError.check("glAttachFragmentShader");
        GLES20.glLinkProgram(programHandle);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == GLES20.GL_TRUE) return programHandle;
        SerjikLog.log("Could not link program: ");
        SerjikLog.log(GLES20.glGetProgramInfoLog(programHandle));
        GLES20.glDeleteProgram(programHandle);
        GLES20.glDeleteShader(this.fragmentShaderHandle);
        this.fragmentShaderHandle = 0;
        GLES20.glDeleteShader(this.vertexShaderHandle);
        this.vertexShaderHandle = 0;
        return 0;
    }

    public static void releaseCompiler() {
        GLES20.glReleaseShaderCompiler();
    }

    protected int getUniformLocation(String name) {
        return GLES20.glGetUniformLocation(this.programHandle, name);
    }

    public void use() {
        GLES20.glUseProgram(this.programHandle);
    }

    protected int getAttribLocation(String name) {
        return GLES20.glGetAttribLocation(this.programHandle, name);
    }

    /**
     * Releases the GL program and attached shader resources. Must be called on the GL thread.
     */
    public void release() {
        if (this.vertexShaderHandle != 0) {
            GLES20.glDeleteShader(this.vertexShaderHandle);
            this.vertexShaderHandle = 0;
        }
        if (this.fragmentShaderHandle != 0) {
            GLES20.glDeleteShader(this.fragmentShaderHandle);
            this.fragmentShaderHandle = 0;
        }
        if (this.programHandle != 0) {
            GLES20.glDeleteProgram(this.programHandle);
            this.programHandle = 0;
        }
    }
}
