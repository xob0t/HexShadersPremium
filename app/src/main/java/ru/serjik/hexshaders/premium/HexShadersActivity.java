package ru.serjik.hexshaders.premium;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import ru.serjik.wallpaper.BaseLauncherActivity;

/**
 * Launcher activity for HexShaders Premium.
 * Checks if the wallpaper is already set and either opens settings or prompts to set it.
 */
public class HexShadersActivity extends BaseLauncherActivity {

    @Override
    protected void launch() {
        super.launch();
    }

    @Override
    protected Class<?> getSettingsActivityClass() {
        return HexShadersSettings.class;
    }

    /**
     * Opens the Play Store page for this app (called from XML onClick).
     */
    public void buttonOpenPlayStoreClick(View view) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ru.serjik.hexshaders.premium")));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.serjik.hexshaders.premium")));
        }
    }

    @Override
    protected Class<?> getWallpaperServiceClass() {
        return HexShadersService.class;
    }
}
