package ru.serjik.utils

import kotlin.math.sqrt

object HexUtils {
    val SQRT3_OVER_2: Float = (sqrt(3.0) / 2.0).toFloat()

    val DIRECTION_Q: ByteArray = byteArrayOf(1, 1, 0, -1, -1, 0)

    val DIRECTION_R: ByteArray = byteArrayOf(-1, 0, 1, 1, 0, -1)

    val DIRECTION_ANGLES: FloatArray = floatArrayOf(120.0f, 180.0f, 240.0f, 300.0f, 0.0f, 60.0f)

    fun hexY(r: Int): Float = r * 0.75f

    fun hexX(q: Int, r: Int): Float = (q * SQRT3_OVER_2) + (r * SQRT3_OVER_2 * 0.5f)
}
