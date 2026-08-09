package ink.mingyuan.tweelix.util;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;

public class TranslationUtil {

    public static String translate(String key) {
        return translate(key, new Object[0]);
    }

    public static String translate(String key, Object... args) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        try {
            if (exists(key)) {
                return I18n.get(key, args);
            }
        } catch (Exception ignored) {
        }
        return key;
    }

    public static boolean exists(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        try {
            return Language.getInstance().has(key);
        } catch (Exception ignored) {
            return false;
        }
    }

    public static String translateOrDefault(String key, String defaultValue) {
        String translated = translate(key);
        return translated.equals(key) ? defaultValue : translated;
    }

    public static String translateOrDefault(String key, String defaultValue, Object... args) {
        String translated = translate(key, args);
        return translated.equals(key) ? defaultValue : translated;
    }

}