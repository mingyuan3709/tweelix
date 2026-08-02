package ink.mingyuan.tweelix.util;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import ink.mingyuan.tweelix.config.category.Generic;
import ink.mingyuan.tweelix.config.subconfig.DefaultPromptSub;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.List;
import java.util.Objects;

public final class NotifyUtil {


    private NotifyUtil() {}

    public static void sendFeatureActionbar(IConfigBase feature, String errorKey, Object... args) {
        sendFeatureActionbarInternal(feature, errorKey, true, args);
    }

    public static void sendFeatureActionbarInternal(IConfigBase feature, String errorKey, boolean useCooldown, Object... args) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (!Generic.DEFAULT_PROMPT.getBooleanValue()) return;

        if (useCooldown && TimeManager.checkAndRecordMillisCooldown(
                "prompt_" + errorKey, DefaultPromptSub.PROMPT_COOLDOWN::getIntegerValue)) return;

        String featureName = Component.translatable(feature.getTranslatedName()).getString();
        List<?> rawBlocked = DefaultPromptSub.BLOCKED_FEATURES.getStrings();

        ImmutableList<String> blockedFeatures = ImmutableList.copyOf(
                rawBlocked.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .toList()
        );

        for (String blocked : blockedFeatures) {
            if (!blocked.trim().isEmpty() && featureName.contains(blocked.trim())) {
                return;
            }
        }

        boolean shouldBold = DefaultPromptSub.BOLD_TEXT.getBooleanValue();

        TextColor prefixColor = TextColor.fromRgb(DefaultPromptSub.PREFIX_COLOR.getIntegerValue() & 0xFFFFFF);
        Style prefixStyle = Style.EMPTY
                .withColor(prefixColor)
                .withBold(shouldBold);

        int finalColorRgb = DefaultPromptSub.USE_ATTENTION_COLOR.getBooleanValue()
                ? DefaultPromptSub.ATTENTION_COLOR.getIntegerValue()
                : DefaultPromptSub.TEXT_COLOR.getIntegerValue();
        TextColor textColor = TextColor.fromRgb(finalColorRgb & 0xFFFFFF);

        Style contentStyle = Style.EMPTY
                .withColor(textColor)
                .withBold(shouldBold);

        MutableComponent finalMessage = Component.empty();

        if (DefaultPromptSub.SHOW_MOD_PREFIX.getBooleanValue()) {
            finalMessage.append(Component.literal("[Tweelix] ").withStyle(prefixStyle));
        }

        if (DefaultPromptSub.SHOW_PREFIX.getBooleanValue()) {
            finalMessage.append(Component.literal("[").append(Component.translatable(feature.getTranslatedName())).append("] ").withStyle(prefixStyle));
        }

        finalMessage.append(Component.translatable(errorKey, args).withStyle(contentStyle));
        DefaultPromptSub.PromptPosition position = (DefaultPromptSub.PromptPosition) DefaultPromptSub.PROMPT_POSITION.getOptionListValue();
        player.displayClientMessage(finalMessage, position != DefaultPromptSub.PromptPosition.CHAT);
    }

    public static void sendToggleMessage(IConfigBase config, boolean enabled) {
        String statusKey = enabled ? "tweelix.toggle.enabled" : "tweelix.toggle.disabled";
        if (DefaultPromptSub.SHOW_PREFIX.getBooleanValue()) {
            sendFeatureActionbarInternal(config, "tweelix.toggle.status", false,
                    Component.translatable(statusKey));
        } else {
            sendFeatureActionbarInternal(config, "tweelix.toggle.message", false,
                    Component.translatable(config.getTranslatedName()),
                    Component.translatable(statusKey));
        }
    }

    public static void sendConfigChangeMessage(IConfigBase config, Object newValue) {
        sendFeatureActionbarInternal(config, "tweelix.config.changed", false, config.getTranslatedName(), String.valueOf(newValue));
    }

    public static void sendDebug(String message) {
        if (!Boolean.getBoolean("tweelix.debug")) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Component debugMsg = Component.literal("[Debug] " + message).withStyle(ChatFormatting.GRAY);
        player.displayClientMessage(debugMsg, false);
    }
}