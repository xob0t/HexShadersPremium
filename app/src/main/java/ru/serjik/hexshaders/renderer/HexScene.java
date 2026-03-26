package ru.serjik.hexshaders.renderer;

/**
 * Interface for hex shader rendering scenes.
 * Defines the lifecycle and rendering contract for both legacy and slideshow hex scenes.
 */
public interface HexScene {

    /**
     * Called when the surface dimensions change.
     */
    void onSurfaceChanged(int width, int height);

    /**
     * Called when visibility changes.
     */
    void onVisibilityChanged(boolean visible);

    /**
     * Returns performance/debug info labels.
     * [0] = perf label, [1] = fps, [2] = points per frame, [3] = total point count
     */
    String[] getInfo();

    /**
     * Initializes the scene: loads shader preferences, compiles shaders, loads textures.
     */
    void initialize();

    /**
     * Draws a single frame.
     */
    void drawFrame();
}
