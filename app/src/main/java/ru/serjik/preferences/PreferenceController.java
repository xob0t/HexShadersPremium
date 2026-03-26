package ru.serjik.preferences;

import android.content.Context;
import android.view.View;

public abstract class PreferenceController {
    protected PreferenceEntry preferenceEntry;
    protected Context context;
    protected View view;
    private float density = -1.0f;

    public static PreferenceController create(String typeName, String[] params, PreferenceEntry entry, Context context) {
        String className = "ru.serjik.preferences.controllers." + typeName + "Controller";
        try {
            PreferenceController controller = (PreferenceController) Class.forName(className).getDeclaredConstructor().newInstance();
            controller.preferenceEntry = entry;
            controller.context = context;
            controller.density = context.getResources().getDisplayMetrics().density;
            controller.view = controller.createView(params);
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("can't instantiate: " + className);
        }
    }

    protected int dp(int px) {
        return (int) ((px * this.density) + 0.5f);
    }

    public View getView() {
        return this.view;
    }

    protected abstract View createView(String[] params);

    public PreferenceEntry getPreferenceEntry() {
        return this.preferenceEntry;
    }
}
