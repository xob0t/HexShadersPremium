package ru.serjik.engine.gl;

import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class GLRenderer {
    private GLSurfaceView surfaceView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int frameDelayMillis = 16;
    private int maxDeltaMillis = 250;

    private Runnable renderRequester = new Runnable() {
        @Override
        public void run() {
            surfaceView.requestRender();
            handler.postDelayed(renderRequester, frameDelayMillis);
        }
    };

    private GLSurfaceView.Renderer rendererImpl = new GLSurfaceView.Renderer() {
        private int frameCounter = 0;
        private boolean needsResize = true;
        private int viewWidth, viewHeight;
        private long lastTimestamp = SystemClock.elapsedRealtime();

        @Override
        public void onDrawFrame(GL10 gl) {
            if (needsResize) {
                frameCounter++;
                if (frameCounter <= 3 || viewWidth <= 0 || viewHeight <= 0) return;
                GLRenderer.this.onSurfaceChanged(viewWidth, viewHeight);
                needsResize = false;
                return;
            }
            long now = SystemClock.elapsedRealtime();
            int deltaMillis = (int) (now - lastTimestamp);
            lastTimestamp = now;
            if (deltaMillis < 0 || deltaMillis > maxDeltaMillis) {
                deltaMillis = maxDeltaMillis;
            }
            GLRenderer.this.onDrawFrame(deltaMillis / 1000.0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            needsResize = true;
            frameCounter = 0;
            viewWidth = width;
            viewHeight = height;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLRenderer.this.onSurfaceCreated(config);
        }
    };

    public GLRenderer(GLSurfaceView surfaceView) {
        onInit(surfaceView);
        this.surfaceView = surfaceView;
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setRenderer(this.rendererImpl);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        this.handler.postDelayed(this.renderRequester, this.frameDelayMillis);
    }

    public GLSurfaceView getSurfaceView() {
        return this.surfaceView;
    }

    protected abstract void onDrawFrame(float deltaSeconds);

    public void setFrameDelay(int millis) {
        if (millis < 0) throw new IllegalArgumentException("frameDelayMillis = " + millis);
        this.frameDelayMillis = millis;
    }

    protected abstract void onSurfaceChanged(int width, int height);

    protected void onInit(GLSurfaceView surfaceView) {}

    protected abstract void onSurfaceCreated(EGLConfig config);

    protected abstract void onVisibilityChanged(boolean visible);

    public void resume() {
        onVisibilityChanged(true);
        this.handler.postDelayed(this.renderRequester, this.frameDelayMillis);
    }

    public void pause() {
        this.handler.removeCallbacks(this.renderRequester);
        onVisibilityChanged(false);
    }

    public void resetContext() {
        this.surfaceView.onPause();
        this.surfaceView.onResume();
    }
}
