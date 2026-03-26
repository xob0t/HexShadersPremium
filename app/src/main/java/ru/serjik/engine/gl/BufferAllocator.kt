package ru.serjik.engine.gl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object BufferAllocator {
    fun createFloatBuffer(floatCount: Int): FloatBuffer {
        return ByteBuffer.allocateDirect(floatCount * 4).apply {
            order(ByteOrder.nativeOrder())
        }.asFloatBuffer().apply {
            position(0)
        }
    }
}
