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

/**
 * 统一通知工具类 - 负责各类玩家提示消息的发送
 */
public final class NotifyUtil {


    private NotifyUtil() {}

    /**
     * 默认启用防刷屏机制的核心方法（兼容老调用，如准星复制等频繁提示）
     */
    public static void sendFeatureActionbar(IConfigBase feature, String errorKey, Object... args) {
        sendFeatureActionbarInternal(feature, errorKey, true, args);
    }

    /**
     * 核心分发（带防刷屏开关）：发送带自定义视觉样式、冷却机制、位置分发及功能黑名单的本地提示信息
     *
     * @param feature     触发该提示的功能模块配置项
     * @param errorKey    具体的提示内容翻译键
     * @param useCooldown 是否启用防刷屏冷却检查（true为启用，false为强制立刻显示）
     * @param args        动态参数
     */
    public static void sendFeatureActionbarInternal(IConfigBase feature, String errorKey, boolean useCooldown, Object... args) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // 1. 全局通知总开关拦截
        if (!Generic.DEFAULT_PROMPT.getBooleanValue()) return;

        // 2. 防刷屏冷却检查（仅在 useCooldown 为 true 时触发）
        if (useCooldown && TimeManager.checkAndRecordMillisCooldown(
                "prompt_" + errorKey, DefaultPromptSub.PROMPT_COOLDOWN::getIntegerValue)) return;

        // 3. 黑名单过滤（仅匹配功能名称）
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
                return; // 功能名称命中黑名单，静音拦截
            }
        }


        // 4. 解析文本样式与颜色
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

        // 5. 链式组装 Component
        MutableComponent finalMessage = Component.empty();

        // 拼接模组前缀：[Tweelix]
        if (DefaultPromptSub.SHOW_MOD_PREFIX.getBooleanValue()) {
            finalMessage.append(Component.literal("[Tweelix] ").withStyle(prefixStyle));
        }

        // 拼接具体功能前缀
        if (DefaultPromptSub.SHOW_PREFIX.getBooleanValue()) {
            finalMessage.append(Component.literal("[").append(Component.translatable(feature.getTranslatedName())).append("] ").withStyle(prefixStyle));
        }

        // 拼接核心内容
        finalMessage.append(Component.translatable(errorKey, args).withStyle(contentStyle));

        // 6. 根据玩家配置动态分发显示位置
        DefaultPromptSub.PromptPosition position = (DefaultPromptSub.PromptPosition) DefaultPromptSub.PROMPT_POSITION.getOptionListValue();
        player.displayClientMessage(finalMessage, position != DefaultPromptSub.PromptPosition.CHAT);
    }

    /**
     * 发送快捷键快捷开关（Toggle）切换提示
     * 💡 传入 useCooldown = false：玩家主动按键时绝对无延迟立刻显示反馈
     */
    public static void sendToggleMessage(IConfigBase config, boolean enabled) {
        String statusKey = enabled ? "tweelix.toggle.enabled" : "tweelix.toggle.disabled";
        if (DefaultPromptSub.SHOW_PREFIX.getBooleanValue()) {
            // 功能前缀已包含功能名，消息体只显示状态
            sendFeatureActionbarInternal(config, "tweelix.toggle.status", false,
                    Component.translatable(statusKey));
        } else {
            // 未显示功能前缀，显示完整：功能名称：状态
            sendFeatureActionbarInternal(config, "tweelix.toggle.message", false,
                    Component.translatable(config.getTranslatedName()),
                    Component.translatable(statusKey));
        }
    }

    /**
     * 发送配置值变更提示
     * 💡 传入 useCooldown = false：改动配置时绝对无延迟立刻反馈
     */
    public static void sendConfigChangeMessage(IConfigBase config, Object newValue) {
        sendFeatureActionbarInternal(config, "tweelix.config.changed", false, config.getTranslatedName(), String.valueOf(newValue));
    }

    /**
     * 发送调试信息（仅在开发环境显示）
     */
    public static void sendDebug(String message) {
        if (!Boolean.getBoolean("tweelix.debug")) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Component debugMsg = Component.literal("[Debug] " + message).withStyle(ChatFormatting.GRAY);
        player.displayClientMessage(debugMsg, false);
    }
}