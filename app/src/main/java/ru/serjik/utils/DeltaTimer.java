package ru.serjik.utils;

import android.os.SystemClock;

public class DeltaTimer {
    private int maxDeltaMillis;
    private int rawDeltaMillis;
    private float deltaSeconds;
    private long lastTimestamp;

    public DeltaTimer() {
        this(0);
    }

    public DeltaTimer(int maxDeltaMillis) {
        this.rawDeltaMillis = 0;
        this.deltaSeconds = 0.0f;
        this.maxDeltaMillis = maxDeltaMillis;
        this.lastTimestamp = SystemClock.elapsedRealtime();
    }

    public DeltaTimer tick() {
        long now = SystemClock.elapsedRealtime();
        this.rawDeltaMillis = (int) (now - this.lastTimestamp);
        if (this.maxDeltaMillis > 0 && (this.rawDeltaMillis > this.maxDeltaMillis || this.rawDeltaMillis < 0)) {
            this.rawDeltaMillis = this.maxDeltaMillis;
        }
        this.lastTimestamp = now;
        this.deltaSeconds = this.rawDeltaMillis * 0.001f;
        return this;
    }

    public float getDeltaSeconds() {
        return this.deltaSeconds;
    }
}
