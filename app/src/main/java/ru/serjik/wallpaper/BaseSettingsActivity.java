package ru.serjik.wallpaper;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;

public abstract class BaseSettingsActivity extends Activity {

    private View.OnClickListener rootClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setOnClickListener(null);
            view.setClickable(false);
            showUI();
        }
    };

    protected void showUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.show(WindowInsets.Type.systemBars());
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
        getWindow().getDecorView().findViewWithTag("ui").setVisibility(View.VISIBLE);
    }

    private void sendDropContextCommand() {
        Intent intent = new Intent(this, getWallpaperServiceClass());
        intent.putExtra("cmd", "dropContext");
        startService(intent);
    }

    private void setupHideButton() {
        View hideButton = getWindow().getDecorView().findViewWithTag("button_hide_ui");
        if (hideButton != null) {
            hideButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideUI();
                }
            });
        }
    }

    private void setupSetWallpaperButton() {
        View setWallpaperButton = getWindow().getDecorView().findViewWithTag("button_set_wallpaper");
        if (setWallpaperButton != null) {
            setWallpaperButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isTaskRoot()) {
                        setAsWallpaper();
                    }
                    finish();
                }
            });
            setWallpaperButton.setVisibility(isWallpaperAlreadySet() ? View.GONE : View.VISIBLE);
        }
    }

    private boolean isWallpaperAlreadySet() {
        WallpaperManager wm = WallpaperManager.getInstance(this);
        WallpaperInfo info = wm.getWallpaperInfo();
        if (info != null) {
            if (getClass().getCanonicalName().equals(info.getSettingsActivity())) {
                wm.forgetLoadedWallpaper();
                return true;
            }
        }
        return false;
    }

    protected void setAsWallpaper() {
        try {
            ComponentName component = new ComponentName(getWallpaperServiceClass().getPackage().getName(), getWallpaperServiceClass().getCanonicalName());
            Intent intent = new Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER");
            intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT", component);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent("android.service.wallpaper.LIVE_WALLPAPER_CHOOSER"));
            } catch (ActivityNotFoundException e2) {
                try {
                    Intent intent = new Intent();
                    intent.setAction("com.bn.nook.CHANGE_WALLPAPER");
                    startActivity(intent);
                } catch (ActivityNotFoundException e3) {
                    Toast.makeText(getBaseContext(), "something goes wrong", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    protected abstract Class<?> getWallpaperServiceClass();

    protected void hideUI() {
        getWindow().getDecorView().findViewWithTag("root").setClickable(true);
        getWindow().getDecorView().findViewWithTag("root").setOnClickListener(this.rootClickListener);
        getWindow().getDecorView().findViewWithTag("ui").setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sendDropContextCommand();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupSetWallpaperButton();
        setupHideButton();
    }
}
