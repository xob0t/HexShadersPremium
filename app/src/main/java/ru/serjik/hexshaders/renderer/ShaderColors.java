package ru.serjik.hexshaders.renderer;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps each shader program to its visually dominant colors.
 * These colors are derived from analyzing the GLSL source code of each shader
 * and identifying the primary, secondary, and tertiary color contributions.
 *
 * Used to provide accurate WallpaperColors to Android (API 27+) so the system
 * theme/accent matches the actual wallpaper appearance.
 */
public class ShaderColors {

    public static class ColorSet {
        public final int primary;
        public final int secondary;
        public final int tertiary;

        public ColorSet(int primary, int secondary, int tertiary) {
            this.primary = primary;
            this.secondary = secondary;
            this.tertiary = tertiary;
        }
    }

    private static final Map<String, ColorSet> SHADER_COLORS = new HashMap<>();

    static {
        // Original shaders (01-12)
        // Flame: orange flame with blue tint
        put("01. flame", new ColorSet(0xFFFF8019, 0xFF1980FF, 0xFF1A0A00));
        // Water: deep teal/cyan caustics
        put("02. water", new ColorSet(0xFF005990, 0xFF003850, 0xFF0A1A20));
        // Rainbow: red/magenta voronoi streaks
        put("03. rainbow", new ColorSet(0xFFCC2244, 0xFFAA1166, 0xFF110022));
        // Fire: blackbody orange-red to yellow
        put("04. fire", new ColorSet(0xFFFF6600, 0xFFFFAA00, 0xFF331100));
        // Kalizyl: blue fog with warm lights
        put("05. kalizyl", new ColorSet(0xFF80B3FF, 0xFFFFCC66, 0xFF0A1020));
        // Snow: cool grey-white with slight blue tint
        put("06. snow", new ColorSet(0xFF738090, 0xFF606878, 0xFF2A2E33));
        // Galaxy: pink/red center, purple/blue halo
        put("07. galaxy", new ColorSet(0xFFCC3355, 0xFF6622AA, 0xFF2200CC));
        // Light rays: blue-green underwater rays
        put("08. light rays", new ColorSet(0xFF336688, 0xFF1A4455, 0xFF0A1A22));
        // Wave: green-cyan, blue, red bands
        put("09. wave", new ColorSet(0xFF33CC66, 0xFF3366CC, 0xFFCC3333));
        // Sea waves: deep ocean blue
        put("10. sea waves", new ColorSet(0xFF1A3038, 0xFF6699CC, 0xFFCCE6CC));
        // Space: purple/amber nebula
        put("11. space", new ColorSet(0xFF7733AA, 0xFFCC8833, 0xFF111133));
        // Metaballs: cyan/blue blobs
        put("12. metaballs", new ColorSet(0xFF3388AA, 0xFF1A4D66, 0xFF194D66));

        // Premium HS20 shaders
        // Bacterium: organic green/teal
        put("hs20. bacterium", new ColorSet(0xFF33AA66, 0xFF1A6644, 0xFF0A2211));
        // Cloud ten: soft white/blue atmospheric
        put("hs20. cloud ten", new ColorSet(0xFF6688BB, 0xFF4466AA, 0xFF223355));
        // Digital brain: electric blue/cyan neural
        put("hs20. digital brain", new ColorSet(0xFF2288CC, 0xFF115599, 0xFF0A2244));
        // Electric: bright electric blue/white
        put("hs20. electric", new ColorSet(0xFF4488FF, 0xFF2266DD, 0xFF112255));
        // Hot shower: warm orange/red particles
        put("hs20. hot shower", new ColorSet(0xFFFF6633, 0xFFCC4411, 0xFF441100));
        // Magnetismic: purple/magenta magnetic field
        put("hs20. magnetismic", new ColorSet(0xFF9933CC, 0xFF6622AA, 0xFF220044));
        // Noise 3D fly through: blue/purple fractal
        put("hs20. noise 3d fly through", new ColorSet(0xFF4455AA, 0xFF334488, 0xFF111133));
        // Perspex web: green/cyan web structure
        put("hs20. perspex web", new ColorSet(0xFF33BB88, 0xFF228866, 0xFF0A3322));
        // Protean clouds: soft blue/grey volumetric
        put("hs20. protean clouds", new ColorSet(0xFF5577AA, 0xFF334466, 0xFF1A2233));
        // Relentless: red/orange aggressive
        put("hs20. relentless", new ColorSet(0xFFCC3322, 0xFFAA2211, 0xFF330A00));
        // Spiral galaxy: warm white/orange spiral
        put("hs20. spiral galaxy", new ColorSet(0xFFCCAA77, 0xFF887744, 0xFF221100));
        // Tiny clouds: soft blue sky
        put("hs20. tiny clouds", new ColorSet(0xFF5588CC, 0xFF3366AA, 0xFF112244));
        // Zone alarm: red/orange warning
        put("hs20. zone alarm", new ColorSet(0xFFDD4422, 0xFFBB3311, 0xFF440A00));
    }

    /**
     * Registers a color set for all file variants of a shader base name.
     * Each shader has variants like .gl2n, .gl2t, .gl3t, .el2n — we match by base name.
     */
    private static void put(String baseName, ColorSet colors) {
        SHADER_COLORS.put(baseName, colors);
    }

    private static final ColorSet DEFAULT_COLORS = new ColorSet(0xFF333333, 0xFF222222, 0xFF111111);

    /**
     * Get the color set for a given shader filename.
     * Strips the file extension to match by base name.
     *
     * @param shaderName the shader asset filename, e.g. "03. rainbow.gl2n"
     * @return the matching ColorSet, or a neutral dark default if not found
     */
    public static ColorSet getColors(String shaderName) {
        if (shaderName == null) return DEFAULT_COLORS;

        // Strip extension (.gl2n, .gl2t, .gl3t, .el2n, etc.)
        String baseName = shaderName;
        int dotIdx = shaderName.lastIndexOf('.');
        if (dotIdx > 0) {
            // Find the second-to-last dot for names like "03. rainbow.gl2n"
            int prevDotIdx = shaderName.lastIndexOf('.', dotIdx - 1);
            if (prevDotIdx >= 0) {
                baseName = shaderName.substring(0, dotIdx);
            }
        }

        ColorSet colors = SHADER_COLORS.get(baseName);
        if (colors != null) return colors;

        // Fallback: try matching by prefix (number + name)
        for (Map.Entry<String, ColorSet> entry : SHADER_COLORS.entrySet()) {
            if (shaderName.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return DEFAULT_COLORS;
    }
}
