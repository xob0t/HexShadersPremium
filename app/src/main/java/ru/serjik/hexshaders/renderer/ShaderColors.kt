package ru.serjik.hexshaders.renderer

import android.content.Context
import android.graphics.Color
import ru.serjik.preferences.values.RGBValue

/**
 * Maps each shader program to its visually dominant colors.
 * These colors are derived from analyzing the GLSL source code of each shader
 * and identifying the primary, secondary, and tertiary color contributions.
 *
 * Used to provide accurate WallpaperColors to Android (API 27+) so the system
 * theme/accent matches the actual wallpaper appearance.
 *
 * For shaders with user-customizable RGB colors, the stored preference values
 * are read and used to derive the wallpaper accent colors dynamically.
 */
object ShaderColors {

    data class ColorSet(
        val primary: Int,
        val secondary: Int,
        val tertiary: Int
    )

    /**
     * Defines how to derive wallpaper colors for a shader.
     * @param fallback the hardcoded default ColorSet (used when no user prefs exist)
     * @param colorKeys preference keys for user-customizable RGB values (e.g. "color", "attenuate")
     *                  If non-empty, colors are dynamically derived from stored preferences.
     */
    private data class ShaderColorConfig(
        val fallback: ColorSet,
        val colorKeys: List<String> = emptyList()
    )

    private val SHADER_CONFIGS = HashMap<String, ShaderColorConfig>().apply {
        // Original shaders (01-12)
        put("01. flame", ShaderColorConfig(
            ColorSet(0xFFFF8019.toInt(), 0xFF1980FF.toInt(), 0xFF1A0A00.toInt()),
            colorKeys = listOf("col1", "col2")
        ))
        put("02. water", ShaderColorConfig(ColorSet(0xFF005990.toInt(), 0xFF003850.toInt(), 0xFF0A1A20.toInt())))
        put("03. rainbow", ShaderColorConfig(ColorSet(0xFFCC2244.toInt(), 0xFFAA1166.toInt(), 0xFF110022.toInt())))
        put("04. fire", ShaderColorConfig(
            ColorSet(0xFFFF6600.toInt(), 0xFFFFAA00.toInt(), 0xFF331100.toInt()),
            colorKeys = listOf("wavelenths")
        ))
        put("05. kalizyl", ShaderColorConfig(
            ColorSet(0xFF80B3FF.toInt(), 0xFFFFCC66.toInt(), 0xFF0A1020.toInt()),
            colorKeys = listOf("lightColor", "fogColor")
        ))
        put("06. snow", ShaderColorConfig(ColorSet(0xFF738090.toInt(), 0xFF606878.toInt(), 0xFF2A2E33.toInt())))
        put("07. galaxy", ShaderColorConfig(ColorSet(0xFFCC3355.toInt(), 0xFF6622AA.toInt(), 0xFF2200CC.toInt())))
        put("08. light rays", ShaderColorConfig(
            ColorSet(0xFF336688.toInt(), 0xFF1A4455.toInt(), 0xFF0A1A22.toInt()),
            colorKeys = listOf("frc", "src", "attc")
        ))
        put("09. wave", ShaderColorConfig(ColorSet(0xFF33CC66.toInt(), 0xFF3366CC.toInt(), 0xFFCC3333.toInt())))
        put("10. sea waves", ShaderColorConfig(ColorSet(0xFF1A3038.toInt(), 0xFF6699CC.toInt(), 0xFFCCE6CC.toInt())))
        put("11. space", ShaderColorConfig(ColorSet(0xFF7733AA.toInt(), 0xFFCC8833.toInt(), 0xFF111133.toInt())))
        put("12. metaballs", ShaderColorConfig(
            ColorSet(0xFF3388AA.toInt(), 0xFF1A4D66.toInt(), 0xFF194D66.toInt()),
            colorKeys = listOf("color", "attenuate")
        ))

        // Premium HS20 shaders
        put("hs20. bacterium", ShaderColorConfig(
            ColorSet(0xFF33AA66.toInt(), 0xFF1A6644.toInt(), 0xFF0A2211.toInt()),
            colorKeys = listOf("greenc", "dotsc")
        ))
        put("hs20. cloud ten", ShaderColorConfig(ColorSet(0xFF6688BB.toInt(), 0xFF4466AA.toInt(), 0xFF223355.toInt())))
        put("hs20. digital brain", ShaderColorConfig(ColorSet(0xFF2288CC.toInt(), 0xFF115599.toInt(), 0xFF0A2244.toInt())))
        put("hs20. electric", ShaderColorConfig(
            ColorSet(0xFF4488FF.toInt(), 0xFF2266DD.toInt(), 0xFF112255.toInt()),
            colorKeys = listOf("colorrr")
        ))
        put("hs20. hot shower", ShaderColorConfig(
            ColorSet(0xFFFF6633.toInt(), 0xFFCC4411.toInt(), 0xFF441100.toInt()),
            colorKeys = listOf("planetcolor")
        ))
        put("hs20. magnetismic", ShaderColorConfig(ColorSet(0xFF9933CC.toInt(), 0xFF6622AA.toInt(), 0xFF220044.toInt())))
        put("hs20. noise 3d fly through", ShaderColorConfig(
            ColorSet(0xFF4455AA.toInt(), 0xFF334488.toInt(), 0xFF111133.toInt()),
            colorKeys = listOf("color")
        ))
        put("hs20. perspex web", ShaderColorConfig(ColorSet(0xFF33BB88.toInt(), 0xFF228866.toInt(), 0xFF0A3322.toInt())))
        put("hs20. protean clouds", ShaderColorConfig(ColorSet(0xFF5577AA.toInt(), 0xFF334466.toInt(), 0xFF1A2233.toInt())))
        put("hs20. relentless", ShaderColorConfig(ColorSet(0xFFCC3322.toInt(), 0xFFAA2211.toInt(), 0xFF330A00.toInt())))
        put("hs20. spiral galaxy", ShaderColorConfig(
            ColorSet(0xFFCCAA77.toInt(), 0xFF887744.toInt(), 0xFF221100.toInt()),
            colorKeys = listOf("galaxy_col", "bulb_col", "sky_col")
        ))
        put("hs20. tiny clouds", ShaderColorConfig(ColorSet(0xFF5588CC.toInt(), 0xFF3366AA.toInt(), 0xFF112244.toInt())))
        put("hs20. zone alarm", ShaderColorConfig(ColorSet(0xFFDD4422.toInt(), 0xFFBB3311.toInt(), 0xFF440A00.toInt())))
    }

    private val DEFAULT_COLORS = ColorSet(0xFF333333.toInt(), 0xFF222222.toInt(), 0xFF111111.toInt())

    /**
     * Resolves the base name from a shader filename by stripping the file extension.
     * e.g. "12. metaballs.gl2n" -> "12. metaballs"
     */
    private fun resolveBaseName(shaderName: String): String {
        val dotIdx = shaderName.lastIndexOf('.')
        if (dotIdx > 0) {
            val prevDotIdx = shaderName.lastIndexOf('.', dotIdx - 1)
            if (prevDotIdx >= 0) {
                return shaderName.substring(0, dotIdx)
            }
        }
        return shaderName
    }

    /**
     * Finds the config for a shader by exact base name or prefix match.
     */
    private fun findConfig(shaderName: String): ShaderColorConfig? {
        val baseName = resolveBaseName(shaderName)
        SHADER_CONFIGS[baseName]?.let { return it }
        for ((key, config) in SHADER_CONFIGS) {
            if (shaderName.startsWith(key)) return config
        }
        return null
    }

    /**
     * Converts an RGBValue (0-100 range per channel, as used by the shader preferences)
     * to an ARGB color int (0-255 range).
     */
    private fun rgbValueToColor(rgb: RGBValue): Int {
        val r = (rgb.r * 255 / 100).coerceIn(0, 255)
        val g = (rgb.g * 255 / 100).coerceIn(0, 255)
        val b = (rgb.b * 255 / 100).coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    /**
     * Get the color set for a given shader, taking user preferences into account.
     *
     * For shaders with user-customizable RGB colors (like "12. metaballs"),
     * reads the stored preference values and derives wallpaper accent colors from them.
     *
     * @param shaderName the shader asset filename, e.g. "12. metaballs.gl2n"
     * @param context    application context for reading SharedPreferences; if null, uses hardcoded fallback
     * @return the ColorSet for Android WallpaperColors
     */
    fun getColors(shaderName: String?, context: Context? = null): ColorSet {
        if (shaderName == null) return DEFAULT_COLORS

        val config = findConfig(shaderName) ?: return DEFAULT_COLORS

        // If this shader has user-customizable colors and we have a context, read them
        if (config.colorKeys.isNotEmpty() && context != null) {
            val prefs = context.getSharedPreferences(shaderName, Context.MODE_PRIVATE)
            val userColors = config.colorKeys.mapNotNull { key ->
                prefs.getString(key, null)?.let { value ->
                    try {
                        rgbValueToColor(RGBValue(value))
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            if (userColors.isNotEmpty()) {
                return ColorSet(
                    primary = userColors[0],
                    secondary = userColors.getOrElse(1) { darken(userColors[0]) },
                    tertiary = userColors.getOrElse(2) { darken(darken(userColors[0])) }
                )
            }
        }

        return config.fallback
    }

    /**
     * Darkens a color by 40% for generating secondary/tertiary variants.
     */
    private fun darken(color: Int): Int {
        val r = ((color shr 16) and 0xFF) * 6 / 10
        val g = ((color shr 8) and 0xFF) * 6 / 10
        val b = (color and 0xFF) * 6 / 10
        return Color.rgb(r, g, b)
    }
}
