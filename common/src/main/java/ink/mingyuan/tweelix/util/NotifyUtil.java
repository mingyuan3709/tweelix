package ink.mingyuan.tweelix.util;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * 通知工具类 - 负责各类玩家提示消息的发送
 */
public class NotifyUtil {

    /**
     * 发送普通消息到聊天栏
     */
    public static void sendMessage(String translationKey, Object... args) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        player.displayClientMessage(Component.translatable(translationKey, args), false);
    }

    /**
     * 发送普通消息到快捷栏上方（覆盖层）
     */
    public static void sendOverlayMessage(String translationKey, Object... args) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        player.displayClientMessage(Component.translatable(translationKey, args), true);
    }

    /**
     * 发送带颜色的消息到聊天栏
     */
    public static void sendColoredMessage(String translationKey, ChatFormatting color, Object... args) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        Component message = Component.translatable(translationKey, args).withStyle(color);
        player.displayClientMessage(message, false);
    }

    /**
     * 发送成功提示（绿色）
     */
    public static void sendSuccess(String translationKey, Object... args) {
        sendColoredMessage(translationKey, ChatFormatting.GREEN, args);
    }

    /**
     * 发送警告提示（黄色）
     */
    public static void sendWarning(String translationKey, Object... args) {
        sendColoredMessage(translationKey, ChatFormatting.YELLOW, args);
    }

    /**
     * 发送错误提示（红色）
     */
    public static void sendError(String translationKey, Object... args) {
        sendColoredMessage(translationKey, ChatFormatting.RED, args);
    }

    /**
     * 发送配置值变更提示
     */
    public static void sendConfigChangeMessage(IConfigBase config, Object newValue) {
        if (Minecraft.getInstance().player == null) return;
        String message = StringUtils.translate("tweelix.config.changed",
                config.getTranslatedName(), String.valueOf(newValue));
        sendOverlayMessage("tweelix.config.changed", config.getTranslatedName(), String.valueOf(newValue));
    }

    /**
     * 发送带前缀的模组消息
     */
    public static void sendModMessage(String translationKey, Object... args) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        Component prefix = Component.literal("[Tweelix] ").withStyle(ChatFormatting.AQUA);
        Component message = Component.translatable(translationKey, args);
        player.displayClientMessage(prefix.copy().append(message), false);
    }

    /**
     * 发送带前缀的错误消息
     */
    public static void sendModError(String translationKey, Object... args) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        Component prefix = Component.literal("[Tweelix] ").withStyle(ChatFormatting.RED);
        Component message = Component.translatable(translationKey, args).withStyle(ChatFormatting.RED);
        player.displayClientMessage(prefix.copy().append(message), false);
    }

    /**
     * 发送操作结果提示（成功/失败）
     */
    public static void sendActionResult(boolean success, String successKey, String failKey, Object... args) {
        String key = success ? successKey : failKey;
        ChatFormatting color = success ? ChatFormatting.GREEN : ChatFormatting.RED;
        sendColoredMessage(key, color, args);
    }


    public static void sendToggleMessage(IConfigBase config, boolean enabled) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        String statusKey = enabled ? "tweelix.toggle.enabled" : "tweelix.toggle.disabled";
        Component message = Component.translatable("tweelix.toggle.message",
                config.getTranslatedName(), Component.translatable(statusKey));

        player.displayClientMessage(message, true);
    }


    /**
     * 发送调试信息（仅在开发环境显示）
     */
    public static void sendDebug(String message) {
        if (!Boolean.getBoolean("tweelix.debug")) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        Component debugMsg = Component.literal("[Debug] " + message).withStyle(ChatFormatting.GRAY);
        player.displayClientMessage(debugMsg, false);
    }
}