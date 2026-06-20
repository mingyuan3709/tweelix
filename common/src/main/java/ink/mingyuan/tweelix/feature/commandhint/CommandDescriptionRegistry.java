package ink.mingyuan.tweelix.feature.commandhint;

import ink.mingyuan.tweelix.config.category.DisplayCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.*;

public class CommandDescriptionRegistry {

    private static final CommandDescriptionRegistry INSTANCE = new CommandDescriptionRegistry();

    public static CommandDescriptionRegistry getInstance() {
        return INSTANCE;
    }

    private CommandDescriptionRegistry() {
    }

    public Component getHint(String suggestionText, String inputCommandText, String CommandPath) {

        if (!DisplayCategory.ENABLE_COMMAND_HINT.getBooleanValue()
                || suggestionText == null
                || inputCommandText == null) {
            return null;
        }

        String genericArgKey = GetTranslationKeys(suggestionText, inputCommandText, CommandPath);

        if (I18n.exists(genericArgKey)) {
            return Component.translatable(genericArgKey);
        }

        String shortKey = getShortCommandKey(suggestionText, inputCommandText);
        if (shortKey != null && I18n.exists(shortKey)) {
            return Component.translatable(shortKey);
        }

        String genericKey = "commands.generic." + suggestionText + ".description";
        if (I18n.exists(genericKey)) {
            return Component.translatable(genericKey);
        }

        if (isItemId(suggestionText)) {
            return getItemDisplayName(suggestionText);
        }

        if (isEntityId(suggestionText)) {
            return getEntityDisplayName(suggestionText);
        }

        if (isMobEffectId(suggestionText)) {
            return getMobEffectDisplayName(suggestionText);
        }

        if (isBiomeId(suggestionText)) {
            return getBiomeDisplayName(suggestionText);
        }

        if (isSoundId(suggestionText)) {
            return getSoundDisplayName(suggestionText);
        }

        return null;
    }

   private static String GetTranslationKeys(String suggestionText, String inputCommandText, String CommandPath){

        if (!inputCommandText.contains(" ")) return "commands." + suggestionText + ".description";;

        String pathLastNode = CommandPath.contains(".")
                ? CommandPath.substring(CommandPath.lastIndexOf('.') + 1)
                : CommandPath;

        boolean pathMatchesInput = isPathMatchesInput(inputCommandText, pathLastNode);

        if (pathMatchesInput && pathLastNode.equals(suggestionText)) {
            return  "commands." + CommandPath + ".description";
        }else if (pathMatchesInput) {

            String parentPath = CommandPath.contains(".")
                    ? CommandPath.substring(0, CommandPath.lastIndexOf('.'))
                    : "";

            if (parentPath.isEmpty()) {
                return "commands." + suggestionText + ".description";
            } else {
                return "commands." + parentPath + "." + suggestionText + ".description";
            }
              }else {
           return  "commands." + CommandPath + "." + suggestionText + ".description";
        }
    }



    private static boolean isPathMatchesInput(String inputCommandText, String pathLastNode) {
        String inputWithoutSlash = inputCommandText.startsWith("/")
                ? inputCommandText.substring(1)
                : inputCommandText;
        String inputLastNode = inputWithoutSlash.contains(" ")
                ? inputWithoutSlash.substring(inputWithoutSlash.lastIndexOf(' ') + 1).trim()
                : inputWithoutSlash.trim();
        return pathLastNode.equals(inputLastNode);
    }

    private String getShortCommandKey(String suggestionText, String inputCommandText) {
        if (suggestionText == null || suggestionText.isEmpty()) {
            return null;
        }
        if (inputCommandText == null || inputCommandText.trim().isEmpty()) {
            return null;
        }
        String[] tokens = inputCommandText.trim().split("\\s+");
        String root = tokens[0];
        if (root.startsWith("/")) {
            root = root.substring(1);
        }
        if (root.isEmpty()) {
            return null;
        }
        return "commands." + root + "." + suggestionText + ".description";
    }

    private boolean isItemId(String str) {
        if (str == null || !str.contains(":")) return false;
        Identifier id = Identifier.tryParse(str);
        return id != null && BuiltInRegistries.ITEM.get(id).isPresent();
    }

    private Component getItemDisplayName(String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) return Component.literal(itemId);

        return BuiltInRegistries.ITEM.get(id)
                .map(holder -> Component.translatable(holder.value().getDescriptionId()))
                .orElse(Component.literal(itemId));
    }

    private boolean isEntityId(String str) {
        if (str == null || !str.contains(":")) return false;
        Identifier id = Identifier.tryParse(str);
        return id != null && BuiltInRegistries.ENTITY_TYPE.get(id).isPresent();
    }

    private Component getEntityDisplayName(String entityId) {
        Identifier id = Identifier.tryParse(entityId);
        if (id == null) return Component.literal(entityId);
        return BuiltInRegistries.ENTITY_TYPE.get(id)
                .map(holder -> Component.translatable(holder.value().getDescriptionId()))
                .orElse(Component.literal(entityId));
    }

    private boolean isMobEffectId(String str) {
        if (str == null || !str.contains(":")) return false;
        Identifier id = Identifier.tryParse(str);
        return id != null && BuiltInRegistries.MOB_EFFECT.get(id).isPresent();
    }

    private Component getMobEffectDisplayName(String effectId) {
        Identifier id = Identifier.tryParse(effectId);
        if (id == null) return Component.literal(effectId);
        return BuiltInRegistries.MOB_EFFECT.get(id)
                .map(holder -> Component.translatable(holder.value().getDescriptionId()))
                .orElse(Component.literal(effectId));
    }

    private boolean isBiomeId(String str) {
        if (str == null || !str.contains(":")) return false;
        Identifier id = Identifier.tryParse(str);
        if (id == null) return false;

        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            var biomeRegistry = connection.registryAccess().lookup(net.minecraft.core.registries.Registries.BIOME);
            return biomeRegistry.isPresent() && biomeRegistry.get().get(ResourceKey.create(net.minecraft.core.registries.Registries.BIOME, id)).isPresent();
        }
        return false;
    }

    private Component getBiomeDisplayName(String biomeId) {
        Identifier id = Identifier.tryParse(biomeId);
        if (id == null) return Component.literal(biomeId);
        String translationKey = "biome." + id.getNamespace() + "." + id.getPath();
        return Component.translatable(translationKey);
    }

    private boolean isSoundId(String str) {
        if (str == null || !str.contains(":")) return false;
        Identifier id = Identifier.tryParse(str);
        return id != null && BuiltInRegistries.SOUND_EVENT.get(id).isPresent();
    }

    private Component getSoundDisplayName(String soundId) {
        Identifier id = Identifier.tryParse(soundId);
        if (id == null) return null;

        String subtitleKey = "subtitles." + id.getPath();

        if (I18n.exists(subtitleKey)) {
            return Component.translatable(subtitleKey);
        }

        return null;
    }

}
