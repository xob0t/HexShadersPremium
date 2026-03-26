package ru.serjik.hexshaders.premium;

import android.opengl.GLSurfaceView;
import ru.serjik.engine.gl.GLRenderer;
import ru.serjik.hexshaders.renderer.HexRendererWrapper;
import ru.serjik.wallpaper.GLWallpaperService;

/**
 * Live wallpaper service for HexShaders Premium.
 * Creates a HexRendererWrapper as the GL renderer, passing itself as the wallpaper offsets listener.
 */
public class HexShadersService extends GLWallpaperService {
    @Override
    public GLRenderer createRenderer(GLSurfaceView surfaceView) {
        return new HexRendererWrapper(surfaceView, this);
    }
}
