package ru.serjik.engine.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BufferAllocator {
    public static FloatBuffer createFloatBuffer(int floatCount) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(floatCount * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.position(0);
        return floatBuffer;
    }
}
