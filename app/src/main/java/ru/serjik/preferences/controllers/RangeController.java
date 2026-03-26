package ru.serjik.preferences.controllers;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import ru.serjik.preferences.PreferenceController;
import ru.serjik.preferences.values.IntegerValue;

public class RangeController extends PreferenceController {
    private TextView labelView;
    private SeekBar seekBar;
    private String label;
    private int minValue;
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            RangeController.this.updateLabel(seekBar);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            RangeController.this.preferenceEntry.set(new IntegerValue(RangeController.this.getSeekBarValue(seekBar)).toString());
        }
    };

    private int getSeekBarValue(SeekBar seekBar) {
        return seekBar.getProgress() + this.minValue;
    }

    private void updateLabel(SeekBar seekBar) {
        this.labelView.setText(String.format("%s = %d (default = %s)", this.label, Integer.valueOf(getSeekBarValue(seekBar)), this.preferenceEntry.getDefault()));
    }

    @Override
    protected View createView(String[] params) {
        this.label = params[0];
        this.minValue = Integer.parseInt(params[1]);
        int maxValue = Integer.parseInt(params[2]);
        this.seekBar = new SeekBar(this.context);
        this.seekBar.setMax(maxValue - this.minValue);
        this.seekBar.setPadding(this.seekBar.getPaddingLeft(), dp(8), this.seekBar.getPaddingRight(), dp(12));
        this.seekBar.setProgress(new IntegerValue(this.preferenceEntry.get()).value - this.minValue);
        this.labelView = new TextView(this.context);
        this.labelView.setText(this.label);
        this.labelView.setPadding(this.seekBar.getPaddingLeft(), dp(12), this.seekBar.getPaddingRight(), dp(8));
        updateLabel(this.seekBar);
        this.seekBar.setOnSeekBarChangeListener(this.seekBarChangeListener);
        LinearLayout linearLayout = new LinearLayout(this.context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(this.labelView);
        linearLayout.addView(this.seekBar);
        return linearLayout;
    }
}
