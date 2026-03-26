package ru.serjik.preferences.values;

public class BooleanValue {
    public boolean value;

    public BooleanValue() {}
    public BooleanValue(String str) { parse(str); }
    public BooleanValue(boolean value) { this.value = value; }

    public void parse(String str) { this.value = str.equals("true"); }
    public String toString() { return this.value ? "true" : "false"; }
}
