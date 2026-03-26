package ru.serjik.preferences;

import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class PreferenceParser {

    public static String substitutePreferences(String shaderSource, PreferenceStore store) {
        String[] parts = shaderSource.split("\\$", -1);
        StringBuilder sb = new StringBuilder(shaderSource.length());
        boolean isToken = false;
        for (String part : parts) {
            if (!isToken) {
                sb.append(part);
            } else if (!part.endsWith("skip")) {
                String[] keyDefault = part.split("\\|", -1);
                sb.append(new PreferenceEntry(keyDefault[0], keyDefault[1], store).get());
            }
            isToken = !isToken;
        }
        return sb.toString();
    }

    public static List<PreferenceController> createControllers(ViewGroup container, List<String> prefStrings, PreferenceStore store) {
        container.removeAllViews();
        ArrayList<PreferenceController> controllers = new ArrayList<>(prefStrings.size());
        for (String prefString : prefStrings) {
            controllers.add(createController(container, prefString, store));
        }
        return controllers;
    }

    public static List<String> extractPrefTokens(String shaderSource) {
        ArrayList<String> tokens = new ArrayList<>();
        boolean isToken = false;
        for (String part : shaderSource.split("\\$", -1)) {
            if (isToken) tokens.add(part);
            isToken = !isToken;
        }
        return tokens;
    }

    public static List<String> extractSection(String source, String startMarker, String separator, String endMarker) {
        ArrayList<String> items = new ArrayList<>();
        int startIdx = source.indexOf(startMarker);
        if (startIdx >= 0) {
            int contentStart = startIdx + startMarker.length();
            int endIdx = source.indexOf(endMarker, contentStart);
            if (endIdx > contentStart) {
                String[] parts = source.substring(contentStart, endIdx).split(Pattern.quote(separator), -1);
                for (String part : parts) {
                    items.add(part);
                }
            }
        }
        return items;
    }

    public static Map<String, PreferenceEntry> createPreferenceMap(List<String> prefTokens, PreferenceStore store) {
        HashMap<String, PreferenceEntry> map = new HashMap<>(prefTokens.size());
        for (String token : prefTokens) {
            String[] keyDefault = token.split("\\|", -1);
            String key = keyDefault[0];
            map.put(key, new PreferenceEntry(key, keyDefault[1], store));
        }
        return map;
    }

    public static PreferenceController createController(ViewGroup container, String prefString, PreferenceStore store) {
        String[] parts = prefString.split("\\|");
        PreferenceController controller = PreferenceController.create(
                parts[2], parts[3].split(";", -1),
                new PreferenceEntry(parts[0], parts[1], store),
                container.getContext());
        container.addView(controller.getView());
        return controller;
    }
}
