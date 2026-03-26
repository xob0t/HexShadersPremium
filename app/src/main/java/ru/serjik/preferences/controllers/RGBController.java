package ru.serjik.preferences.controllers;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import ru.serjik.preferences.PreferenceController;
import ru.serjik.preferences.values.RGBValue;

public class RGBController extends PreferenceController {
    private TextView redLabel;
    private TextView greenLabel;
    private TextView blueLabel;
    private SeekBar redSeekBar;
    private SeekBar greenSeekBar;
    private SeekBar blueSeekBar;
    private String label;
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            RGBController.this.updateLabels();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            RGBController.this.preferenceEntry.set(new RGBValue(
                    RGBController.this.redSeekBar.getProgress(),
                    RGBController.this.greenSeekBar.getProgress(),
                    RGBController.this.blueSeekBar.getProgress()).toString());
        }
    };

    private LinearLayout createChannelRow(SeekBar seekBar, int value, TextView textView) {
        seekBar.setMax(100);
        seekBar.setProgress(value);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        textView.setLayoutParams(new LinearLayout.LayoutParams(dp(48), ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout linearLayout = new LinearLayout(this.context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setPadding(seekBar.getPaddingLeft(), dp(8), dp(0), dp(4));
        linearLayout.addView(textView);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.addView(seekBar);
        return linearLayout;
    }

    private void updateLabels() {
        this.redLabel.setText("R = " + this.redSeekBar.getProgress());
        this.greenLabel.setText("G = " + this.greenSeekBar.getProgress());
        this.blueLabel.setText("B = " + this.blueSeekBar.getProgress());
    }

    @Override
    protected View createView(String[] params) {
        this.label = params[0];
        RGBValue currentValue = new RGBValue(this.preferenceEntry.get());
        SeekBar seekBar = new SeekBar(this.context);
        this.redSeekBar = seekBar;
        int r = currentValue.r;
        TextView textView = new TextView(this.context);
        this.redLabel = textView;
        LinearLayout redRow = createChannelRow(seekBar, r, textView);
        SeekBar seekBar2 = new SeekBar(this.context);
        this.greenSeekBar = seekBar2;
        int g = currentValue.g;
        TextView textView2 = new TextView(this.context);
        this.greenLabel = textView2;
        LinearLayout greenRow = createChannelRow(seekBar2, g, textView2);
        SeekBar seekBar3 = new SeekBar(this.context);
        this.blueSeekBar = seekBar3;
        int b = currentValue.b;
        TextView textView3 = new TextView(this.context);
        this.blueLabel = textView3;
        LinearLayout blueRow = createChannelRow(seekBar3, b, textView3);
        TextView titleView = new TextView(this.context);
        RGBValue defaultValue = new RGBValue(this.preferenceEntry.getDefault());
        titleView.setText(String.format("%s (default R = %d, G = %d, B = %d)", this.label, Integer.valueOf(defaultValue.r), Integer.valueOf(defaultValue.g), Integer.valueOf(defaultValue.b)));
        titleView.setPadding(this.redSeekBar.getPaddingLeft(), dp(12), this.redSeekBar.getPaddingRight(), dp(8));
        updateLabels();
        this.redSeekBar.setOnSeekBarChangeListener(this.seekBarChangeListener);
        this.greenSeekBar.setOnSeekBarChangeListener(this.seekBarChangeListener);
        this.blueSeekBar.setOnSeekBarChangeListener(this.seekBarChangeListener);
        LinearLayout linearLayout = new LinearLayout(this.context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(titleView);
        linearLayout.addView(redRow);
        linearLayout.addView(greenRow);
        linearLayout.addView(blueRow);
        return linearLayout;
    }
}
