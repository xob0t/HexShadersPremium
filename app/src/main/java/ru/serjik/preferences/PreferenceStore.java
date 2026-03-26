package ru.serjik.preferences;

public interface PreferenceStore {
    void put(String key, String value);
    String get(String key, String defaultValue);
}
