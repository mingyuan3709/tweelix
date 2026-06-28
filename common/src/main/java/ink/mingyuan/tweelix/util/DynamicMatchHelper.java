package ink.mingyuan.tweelix.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import java.util.regex.Pattern;

public class DynamicMatchHelper {
    // 缓存编译后的正则表达式，避免重复编译（可选优化）
    private static final java.util.Map<String, Pattern> patternCache = new java.util.HashMap<>();

    public static boolean matches(ItemStack stack, String rule) {
        if (stack.isEmpty()) return false;

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        // 1. 标签匹配：规则以 # 开头
        if (rule.startsWith("#")) {
            String tagName = rule.substring(1);
            return matchesTag(stack.getItem(), tagName);
        }

        // 2. 正则匹配：规则以 "regex:" 开头
        if (rule.startsWith("regex:")) {
            String regex = rule.substring(6);
            return matchesRegex(itemId, regex);
        }

        // 3. 通配符匹配：规则包含 * 或 ?
        if (rule.contains("*") || rule.contains("?")) {
            return matchesWildcard(itemId, rule);
        }

        // 4. 默认：包含匹配
        return itemId.contains(rule);
    }

    private static boolean matchesTag(Item item, String tagName) {
        ResourceLocation tagId = ResourceLocation.tryParse(tagName);
        if (tagId == null) return false;
        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
        return BuiltInRegistries.ITEM.wrapAsHolder(item).is(tagKey);
    }

    private static boolean matchesRegex(String text, String regex) {
        Pattern pattern = patternCache.computeIfAbsent(regex, Pattern::compile);
        return pattern.matcher(text).matches();
    }

    private static boolean matchesWildcard(String text, String pattern) {
        String regex = convertWildcardToRegex(pattern);
        return matchesRegex(text, regex);
    }

    private static String convertWildcardToRegex(String pattern) {
        // 转义正则特殊字符（除了 * 和 ?），然后将 * 和 ? 替换为正则通配符
        // 简单实现：对每个字符处理，更健壮的方式是使用 Pattern.quote 但需要保留 * 和 ?
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*') {
                sb.append(".*");
            } else if (c == '?') {
                sb.append(".");
            } else if (".+()[]{}|\\^$".indexOf(c) >= 0) {
                // 转义正则元字符
                sb.append('\\').append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}