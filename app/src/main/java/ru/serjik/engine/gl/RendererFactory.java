package ru.serjik.engine.gl;

import android.opengl.GLSurfaceView;

public interface RendererFactory {
    GLRenderer createRenderer(GLSurfaceView surfaceView);
}
