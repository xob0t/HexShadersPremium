package ru.serjik.preferences

import android.view.ViewGroup
import java.util.regex.Pattern

object PreferenceParser {

    fun substitutePreferences(shaderSource: String, store: PreferenceStore): String {
        val parts = shaderSource.split("$")
        val sb = StringBuilder(shaderSource.length)
        var isToken = false
        for (part in parts) {
            if (!isToken) {
                sb.append(part)
            } else if (!part.endsWith("skip")) {
                val keyDefault = part.split("|")
                sb.append(PreferenceEntry(keyDefault[0], keyDefault[1], store).get())
            }
            isToken = !isToken
        }
        return sb.toString()
    }

    fun createControllers(
        container: ViewGroup,
        prefStrings: List<String>,
        store: PreferenceStore
    ): List<PreferenceController> {
        container.removeAllViews()
        return prefStrings.map { prefString ->
            createController(container, prefString, store)
        }
    }

    fun extractPrefTokens(shaderSource: String): List<String> {
        val tokens = mutableListOf<String>()
        var isToken = false
        for (part in shaderSource.split("$")) {
            if (isToken) tokens.add(part)
            isToken = !isToken
        }
        return tokens
    }

    fun extractSection(
        source: String,
        startMarker: String,
        separator: String,
        endMarker: String
    ): List<String> {
        val items = mutableListOf<String>()
        val startIdx = source.indexOf(startMarker)
        if (startIdx >= 0) {
            val contentStart = startIdx + startMarker.length
            val endIdx = source.indexOf(endMarker, contentStart)
            if (endIdx > contentStart) {
                val parts = source.substring(contentStart, endIdx).split(separator)
                items.addAll(parts)
            }
        }
        return items
    }

    fun createPreferenceMap(prefTokens: List<String>, store: PreferenceStore): Map<String, PreferenceEntry> {
        val map = HashMap<String, PreferenceEntry>(prefTokens.size)
        for (token in prefTokens) {
            val keyDefault = token.split("|")
            val key = keyDefault[0]
            map[key] = PreferenceEntry(key, keyDefault[1], store)
        }
        return map
    }

    fun createController(container: ViewGroup, prefString: String, store: PreferenceStore): PreferenceController {
        val parts = prefString.split("|")
        val controller = PreferenceController.create(
            parts[2], parts[3].split(";").toTypedArray(),
            PreferenceEntry(parts[0], parts[1], store),
            container.context
        )
        container.addView(controller.view)
        return controller
    }
}
