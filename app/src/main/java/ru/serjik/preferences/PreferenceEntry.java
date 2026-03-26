package ru.serjik.preferences;

public class PreferenceEntry {
    private String key;
    private String defaultValue;
    private PreferenceStore store;
    private PreferenceListenerSet listeners = new PreferenceListenerSet();

    public PreferenceEntry(String key, String defaultValue, PreferenceStore store) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.store = store;
    }

    public String getDefault() {
        return this.defaultValue;
    }

    public void set(String value) {
        if (get().equals(value)) return;
        this.store.put(this.key, value);
        this.listeners.notifyListeners(this);
    }

    public String get() {
        return this.store.get(this.key, this.defaultValue);
    }

    public void reset() {
        set(getDefault());
    }

    public PreferenceListenerSet getListeners() {
        return this.listeners;
    }
}
