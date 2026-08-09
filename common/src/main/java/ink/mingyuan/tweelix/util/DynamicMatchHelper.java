package ink.mingyuan.tweelix.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import java.util.regex.Pattern;

public class DynamicMatchHelper {

    private static final java.util.Map<String, Pattern> patternCache = new java.util.HashMap<>();

    public static boolean matches(ItemStack stack, String rule) {
        if (stack.isEmpty()) return false;

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        if (rule.startsWith("#")) {
            String tagName = rule.substring(1);
            return matchesTag(stack.getItem(), tagName);
        }

        if (rule.startsWith("regex:")) {
            String regex = rule.substring(6);
            return matchesRegex(itemId, regex);
        }

        if (rule.contains("*") || rule.contains("?")) {
            return matchesWildcard(itemId, rule);
        }

        return itemId.contains(rule);
    }

    private static boolean matchesTag(Item item, String tagName) {
        Identifier tagId = Identifier.tryParse(tagName);
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

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*') {
                sb.append(".*");
            } else if (c == '?') {
                sb.append(".");
            } else if (".+()[]{}|\\^$".indexOf(c) >= 0) {
                sb.append('\\').append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}