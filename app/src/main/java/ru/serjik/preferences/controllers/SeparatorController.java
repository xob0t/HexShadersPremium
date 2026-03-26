package ru.serjik.preferences.controllers;

import android.view.View;
import android.view.ViewGroup;
import ru.serjik.preferences.PreferenceController;

public class SeparatorController extends PreferenceController {
    @Override
    protected View createView(String[] params) {
        View view = new View(this.context);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(8)));
        return view;
    }
}
