package ru.serjik.preferences.values;

public class RGBValue {
    public int r;
    public int g;
    public int b;

    public RGBValue() {}
    public RGBValue(int r, int g, int b) { this.r = r; this.g = g; this.b = b; }
    public RGBValue(String str) { parse(str); }

    public void parse(String str) {
        String[] parts = str.split(",");
        this.r = Integer.parseInt(parts[0]);
        this.g = Integer.parseInt(parts[1]);
        this.b = Integer.parseInt(parts[2]);
    }

    public String toString() { return this.r + "," + this.g + "," + this.b; }
}
