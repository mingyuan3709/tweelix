package ink.mingyuan.tweelix.util;

import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigString;
import ink.mingyuan.tweelix.config.TweelixConfig;
import net.minecraft.client.Minecraft;

public class Util {

    public static void handleToggle(IConfigBoolean config) {

        boolean newValue = !config.getBooleanValue();
        config.setBooleanValue(newValue);
        TweelixConfig.INSTANCE.save();
        if (Minecraft.getInstance().player != null) {
            NotifyUtil.sendToggleMessage(config, newValue);
        }

    }

    public static boolean isConfigStringNotEmpty(ConfigString config) {
        String value = config.getStringValue();
        return value != null && !value.isEmpty();
    }


    /**
     * 智能发送命令或聊天消息
     * <ul>
     *   <li>以 {@code /} 开头：视为命令，以命令形式发送 {@code /}</li>
     *   <li>其他情况：直接以聊天消息形式发送</li>
     * </ul>
     *
     * @param command 待发送的指令字符串，如 {@code "/gamemode creative"}、{@code "!!tp 100 64 200"}
     */
    public static void sendCommandOrChat(String command) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (command.startsWith("/")) {
            client.player.connection.sendCommand(command.substring(1));
        } else{
            client.player.connection.sendChat(command);
        }
    }


    /**
     * 将延迟时间字符串转换为游戏刻(tick)数。
     * <p>
     * 支持格式：
     * <ul>
     *   <li>{@code 5t}  - 5 ticks (0.25秒)</li>
     *   <li>{@code 2s}  - 2秒 (40 ticks)</li>
     *   <li>{@code 1m}  - 1分钟 (1200 ticks)</li>
     *   <li>纯数字默认视为 ticks</li>
     * </ul>
     *
     * @param raw 用户输入的时间字符串，如 "3s"、"10t"
     * @return 对应的 tick 数；格式错误时返回 0
     */
    public static int parseDelayTicks(String raw) {
        if (raw == null || raw.isEmpty()) return 0;
        raw = raw.toLowerCase().trim();
        char last = raw.charAt(raw.length() - 1);
        String numPart;
        int multiplier;
        switch (last) {
            case 't' -> { numPart = raw.substring(0, raw.length() - 1); multiplier = 1; }
            case 's' -> { numPart = raw.substring(0, raw.length() - 1); multiplier = 20; }
            case 'm' -> { numPart = raw.substring(0, raw.length() - 1); multiplier = 1200; }
            default -> { numPart = raw; multiplier = 1; }
        }
        try {
            int val = Integer.parseInt(numPart);
            return val * multiplier;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}