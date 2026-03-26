package ru.serjik.preferences;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PreferenceListenerSet {
    private Set<PreferenceChangeListener> listeners = new HashSet<>();

    public void clear() {
        this.listeners.clear();
    }

    public void addListener(PreferenceChangeListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void notifyListeners(PreferenceEntry entry) {
        Iterator<PreferenceChangeListener> it = this.listeners.iterator();
        while (it.hasNext()) {
            it.next().onPreferenceChanged(entry);
        }
    }
}
