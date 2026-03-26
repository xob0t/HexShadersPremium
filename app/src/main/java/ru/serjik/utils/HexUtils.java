package ru.serjik.utils;

public class HexUtils {
    public static final float SQRT3_OVER_2 = (float) (Math.sqrt(3.0d) / 2.0d);
    public static final byte[] DIRECTION_Q = {1, 1, 0, -1, -1, 0};
    public static final byte[] DIRECTION_R = {-1, 0, 1, 1, 0, -1};
    public static final float[] DIRECTION_ANGLES = {120.0f, 180.0f, 240.0f, 300.0f, 0.0f, 60.0f};

    public static float hexY(int r) {
        return r * 0.75f;
    }

    public static float hexX(int q, int r) {
        return (q * SQRT3_OVER_2) + (r * SQRT3_OVER_2 * 0.5f);
    }
}
