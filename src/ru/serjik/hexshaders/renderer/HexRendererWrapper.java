package ru.serjik.hexshaders.renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import ru.serjik.engine.gl.GLRenderer;
import ru.serjik.hexshaders.renderer.legacy.LegacyHexScene;
import ru.serjik.hexshaders.renderer.slideshow.SlideShowHexScene;
import ru.serjik.wallpaper.WallpaperOffsetsListener;

/**
 * GLRenderer wrapper that delegates to either a LegacyHexScene or SlideShowHexScene
 * based on the GPU's vertex texture fetch capability (GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS).
 */
public class HexRendererWrapper extends GLRenderer {
    private Context context;
    private WallpaperOffsetsListener offsetsListener;
    private HexScene scene;

    public HexRendererWrapper(GLSurfaceView surfaceView, WallpaperOffsetsListener offsetsListener) {
        super(surfaceView);
        this.offsetsListener = offsetsListener;
        this.context = surfaceView.getContext().getApplicationContext();
    }

    private int getMaxVertexTextureUnits() {
        int[] result = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, result, 0);
        return result[0];
    }

    @Override
    protected void onDrawFrame(float deltaSeconds) {
        this.scene.drawFrame();
    }

    @Override
    protected void onSurfaceChanged(int width, int height) {
        this.scene.onSurfaceChanged(width, height);
    }

    @Override
    protected void onSurfaceCreated(EGLConfig config) {
        setFrameDelay(16);
        if (this.scene == null) {
            this.scene = getMaxVertexTextureUnits() < 4
                    ? new LegacyHexScene(this.context)
                    : new SlideShowHexScene(this.context, this.offsetsListener);
        }
        this.scene.initialize();
    }

    @Override
    protected void onVisibilityChanged(boolean visible) {
        if (this.scene != null) {
            this.scene.onVisibilityChanged(visible);
        }
    }

    /**
     * Returns performance/debug info from the current scene.
     */
    public String[] getInfo() {
        if (this.scene == null) {
            return new String[]{"", "", "", ""};
        }
        return this.scene.getInfo();
    }
}
