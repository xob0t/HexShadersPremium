package ru.serjik.engine.gl;

import android.opengl.GLES20;
import ru.serjik.utils.SerjikLog;

public class GLError {
    public static void check(String tag) {
        while (true) {
            int error = GLES20.glGetError();
            if (error == 0) {
                return;
            } else {
                SerjikLog.log(tag + ": glError " + error);
            }
        }
    }
}
