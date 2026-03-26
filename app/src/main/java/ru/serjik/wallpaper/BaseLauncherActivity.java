package ru.serjik.wallpaper;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public abstract class BaseLauncherActivity extends Activity {

    private boolean isWallpaperAlreadySet() {
        WallpaperManager wm = WallpaperManager.getInstance(this);
        WallpaperInfo info = wm.getWallpaperInfo();
        if (info != null) {
            if (getSettingsActivityClass().getCanonicalName().equals(info.getSettingsActivity())) {
                wm.forgetLoadedWallpaper();
                return true;
            }
        }
        return false;
    }

    private void openSettings() {
        startActivity(new Intent(getApplicationContext(), getSettingsActivityClass()));
    }

    private void setWallpaper() {
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

    protected void launch() {
        if (isWallpaperAlreadySet()) {
            openSettings();
        } else {
            setWallpaper();
        }
        finish();
    }

    protected abstract Class<?> getSettingsActivityClass();
    protected abstract Class<?> getWallpaperServiceClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        launch();
    }
}
