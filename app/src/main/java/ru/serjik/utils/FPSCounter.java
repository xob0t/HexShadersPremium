package ru.serjik.utils;

import android.os.SystemClock;

public class FPSCounter {
    private FPSCallback callback;
    private int deltaMillis;
    private float smoothedDelta;
    private long lastTimestamp;
    private long nextCallbackTime;
    private int callbackIntervalMillis;

    public FPSCounter() {
        this(null);
    }

    public FPSCounter(FPSCallback callback) {
        this(callback, 3000);
    }

    public FPSCounter(FPSCallback callback, int intervalMillis) {
        this.callback = null;
        this.deltaMillis = 0;
        this.smoothedDelta = 0.0f;
        this.lastTimestamp = now();
        this.callback = callback;
        this.callbackIntervalMillis = intervalMillis;
        this.nextCallbackTime = now() + this.callbackIntervalMillis;
    }

    private static long now() {
        return SystemClock.elapsedRealtime();
    }

    public void tick() {
        long currentTime = now();
        this.deltaMillis = (int) (currentTime - this.lastTimestamp);
        this.lastTimestamp = currentTime;
        if (this.deltaMillis > 250) this.deltaMillis = 250;
        if (this.deltaMillis < 1) this.deltaMillis = 1;
        this.smoothedDelta += (this.deltaMillis - this.smoothedDelta) / 8.0f;
        if (currentTime > this.nextCallbackTime) {
            this.nextCallbackTime = currentTime + this.callbackIntervalMillis;
            if (this.callback != null) this.callback.onFPSUpdate(this);
        }
    }

    public float getFPS() {
        return (float) (1000.0d / (double) this.smoothedDelta);
    }
}
