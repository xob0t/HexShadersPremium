package ru.serjik.hexshaders.premium

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import ru.serjik.wallpaper.BaseLauncherActivity

/**
 * Launcher activity for HexShaders Premium.
 * Checks if the wallpaper is already set and either opens settings or prompts to set it.
 */
class HexShadersActivity : BaseLauncherActivity() {

    override fun launch() {
        super.launch()
    }

    override val settingsActivityClass: Class<*>
        get() = HexShadersSettings::class.java

    override val wallpaperServiceClass: Class<*>
        get() = HexShadersService::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<android.view.View>(R.id.button_open_play_store_link)?.setOnClickListener {
            openPlayStore()
        }
    }

    private fun openPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ru.serjik.hexshaders.premium")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.serjik.hexshaders.premium")))
        }
    }
}
