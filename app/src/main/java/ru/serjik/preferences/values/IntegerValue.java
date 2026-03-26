package ru.serjik.preferences.values;

public class IntegerValue {
    public int value;

    public IntegerValue() {}
    public IntegerValue(int value) { this.value = value; }
    public IntegerValue(String str) { parse(str); }

    public void parse(String str) { this.value = Integer.parseInt(str); }
    public String toString() { return Integer.toString(this.value); }
}
