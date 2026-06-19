package ink.mingyuan.tweelix.util;

import net.minecraft.client.resources.language.I18n;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 翻译工具类
 * 提供翻译回退、格式化、批量翻译等常用功能。
 */
public class TranslationUtil {

    // ==================== 基础翻译 ====================

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
            if (I18n.exists(key)) {
                return I18n.get(key, args);
            }
        } catch (Exception ignored) {
            // 安全回退
        }
        return key;
    }

    /**
     * 检查翻译键是否存在
     */
    public static boolean hasTranslation(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        try {
            return I18n.exists(key);
        } catch (Exception ignored) {
            return false;
        }
    }

    // ==================== 带默认值的翻译 ====================

    /**
     * 翻译键，找不到时返回最后一段（如 "item.mod.name" → "name"）
     */
    public static String translateOrDefault(String key) {
        String translated = translate(key);
        if (!translated.equals(key)) {
            return translated;
        }
        return extractLastSegment(key);
    }

    /**
     * 翻译键，找不到时返回指定默认值
     */
    public static String translateOrDefault(String key, String defaultValue) {
        String translated = translate(key);
        return translated.equals(key) ? defaultValue : translated;
    }

    /**
     * 翻译键并格式化，找不到时返回最后一段
     */
    public static String translateOrDefault(String key, Object... args) {
        String translated = translate(key, args);
        if (!translated.equals(key)) {
            return translated;
        }
        return extractLastSegment(key);
    }

    /**
     * 翻译键并格式化，找不到时返回指定默认值
     */
    public static String translateOrDefault(String key, String defaultValue, Object... args) {
        String translated = translate(key, args);
        return translated.equals(key) ? defaultValue : translated;
    }

    // ==================== 批量翻译 ====================

    /**
     * 批量翻译多个键，返回数组
     */
    public static String[] translateAll(String... keys) {
        return Arrays.stream(keys)
                .map(TranslationUtil::translate)
                .toArray(String[]::new);
    }

    /**
     * 批量翻译，用指定分隔符连接
     */
    public static String translateJoin(String delimiter, String... keys) {
        return Arrays.stream(keys)
                .map(TranslationUtil::translate)
                .collect(Collectors.joining(delimiter));
    }

    // ==================== 格式化辅助 ====================

    /**
     * 翻译并首字母大写
     */
    public static String translateCapitalized(String key) {
        String translated = translateOrDefault(key);
        if (translated.isEmpty()) {
            return translated;
        }
        return Character.toUpperCase(translated.charAt(0)) + translated.substring(1);
    }

    /**
     * 翻译键的前缀部分（如 "item.mod.name" 的 "item.mod"）
     */
    public static String translatePrefix(String key, int segments) {
        String[] parts = key.split("\\.");
        if (parts.length <= segments) {
            return translate(key);
        }
        String prefixKey = Arrays.stream(parts)
                .limit(segments)
                .collect(Collectors.joining("."));
        return translate(prefixKey);
    }

    // ==================== 私有工具方法 ====================

    /**
     * 提取键的最后一段
     */
    private static String extractLastSegment(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        String[] parts = key.split("\\.");
        return parts.length > 0 ? parts[parts.length - 1] : key;
    }
}