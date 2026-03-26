package ru.serjik.preferences.controllers;

import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.serjik.preferences.PreferenceController;
import ru.serjik.preferences.values.IntegerValue;

public class IntegerController extends PreferenceController {
    private TextView labelView;
    private String label;
    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (IntegerController.this.preferenceEntry.get().equals(textView.getText().toString())) {
                return false;
            }
            IntegerController.this.preferenceEntry.set(textView.getText().toString());
            return false;
        }
    };

    @Override
    protected View createView(String[] params) {
        this.label = params[0];
        this.labelView = new TextView(this.context);
        this.labelView.setText(this.label);
        this.labelView.setPadding(dp(8), 0, dp(8), 0);
        EditText editText = new EditText(this.context);
        try {
            editText.setText(new IntegerValue(this.preferenceEntry.get()).toString());
        } catch (Exception e) {
            editText.setText(new IntegerValue(this.preferenceEntry.getDefault()).toString());
        }
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        editText.setOnEditorActionListener(this.editorActionListener);
        LinearLayout linearLayout = new LinearLayout(this.context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setPadding(dp(4), dp(8), dp(4), 0);
        linearLayout.addView(this.labelView);
        linearLayout.addView(editText);
        return linearLayout;
    }
}
