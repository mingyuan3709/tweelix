package ink.mingyuan.tweelix.util;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigString;
import ink.mingyuan.tweelix.config.TweelixConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class Util {

    public static void sendToggleMessage(IConfigBase config, boolean enabled) {
        if (MinecraftClient.getInstance().player == null) return;
        String statusKey = enabled ? "tweelix.toggle.enabled" : "tweelix.toggle.disabled";
        Text message = Text.translatable("tweelix.toggle.message",
                config.getTranslatedName(), Text.translatable(statusKey));
        MinecraftClient.getInstance().player.sendMessage(message, true);
    }

    public static void sendDefaultPrompt(PlayerEntity player, String translationKey) {

        if (TweelixConfig.Generic.DEFAULT_PROMPT.getBooleanValue()) {
            player.sendMessage(Text.translatable(translationKey), true);
        }
    }

    public static void handleToggle(IConfigBoolean config) {

        boolean newValue = !config.getBooleanValue();
        config.setBooleanValue(newValue);
        TweelixConfig.INSTANCE.save();
        if (MinecraftClient.getInstance().player != null) {
            Util.sendToggleMessage(config, newValue);
        }

    }

    public static boolean isConfigStringNotEmpty(ConfigString config) {
        String value = config.getStringValue();
        return value != null && !value.isEmpty();
    }

    public static void sendCommandOrChat(String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) return;

        if (command.startsWith("/")) {
            client.player.networkHandler.sendChatCommand(command.substring(1));
        } else {
            client.player.networkHandler.sendChatMessage(command);
        }
    }
}
