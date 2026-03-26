package ru.serjik.preferences.controllers;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import ru.serjik.preferences.PreferenceController;
import ru.serjik.preferences.values.BooleanValue;

public class CheckBoxController extends PreferenceController {
    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            CheckBoxController.this.preferenceEntry.set(new BooleanValue(isChecked).toString());
        }
    };

    @Override
    protected View createView(String[] params) {
        String label = params[0];
        CheckBox checkBox = new CheckBox(this.context);
        checkBox.setChecked(new BooleanValue(this.preferenceEntry.get()).value);
        checkBox.setText(label);
        checkBox.setPadding(checkBox.getPaddingLeft(), dp(12), checkBox.getPaddingRight(), dp(12));
        checkBox.setOnCheckedChangeListener(this.checkedChangeListener);
        LinearLayout linearLayout = new LinearLayout(this.context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(dp(12), 0, 0, 0);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(checkBox);
        return linearLayout;
    }
}
