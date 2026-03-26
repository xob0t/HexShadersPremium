package ru.serjik.hexshaders.renderer;

import android.content.Context;
import android.content.SharedPreferences;
import ru.serjik.preferences.PreferenceStore;

/**
 * PreferenceStore implementation backed by Android SharedPreferences.
 * Used to persist shader configuration per-shader or for the application store.
 */
public class ShaderPreferenceStore implements PreferenceStore {
    private SharedPreferences sharedPreferences;
    private Context context;

    public ShaderPreferenceStore(String name, Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(name, 0);
    }

    /**
     * Clears all stored preferences.
     */
    public void clearAll() {
        this.sharedPreferences.edit().clear().apply();
    }

    @Override
    public void put(String key, String value) {
        this.sharedPreferences.edit().putString(key, value).apply();
    }

    @Override
    public String get(String key, String defaultValue) {
        return this.sharedPreferences.getString(key, defaultValue);
    }
}
