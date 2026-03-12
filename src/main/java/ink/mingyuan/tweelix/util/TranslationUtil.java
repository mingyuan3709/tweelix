package ink.mingyuan.tweelix.util;

import fi.dy.masa.malilib.util.StringUtils;

public class TranslationUtil {

    public static String translateOrDefault(String key) {
        String translated = StringUtils.translate(key);
        if (!translated.equals(key)) {
            return translated; // Translation found
        }
        // Translation not found, extract the last segment from the key
        String[] parts = key.split("\\.");
        return parts.length > 0 ? parts[parts.length - 1] : key;
    }

    public static String translateOrDefault(String key, String defaultValue) {
        String translated = StringUtils.translate(key);
        return translated.equals(key) ? defaultValue : translated;
    }
}
