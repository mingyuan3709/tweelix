package ink.mingyuan.tweelix.util;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 翻译工具类
 */
public class TranslationUtil {


    /**
     * 翻译键，找不到时返回原始键名（带安全回退）
     */
    public static String translate(String key) {
        return translate(key, new Object[0]);
    }

    /**
     * 翻译键并格式化参数，找不到时返回原始键名
     */
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

    /**
     * 检查翻译键是否存在
     */
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

    // ==================== 带默认值的翻译 ====================

    /**
     * 翻译键，找不到时返回指定默认值
     */
    public static String translateOrDefault(String key, String defaultValue) {
        String translated = translate(key);
        return translated.equals(key) ? defaultValue : translated;
    }


    /**
     * 翻译键并格式化，找不到时返回指定默认值
     */
    public static String translateOrDefault(String key, String defaultValue, Object... args) {
        String translated = translate(key, args);
        return translated.equals(key) ? defaultValue : translated;
    }

}