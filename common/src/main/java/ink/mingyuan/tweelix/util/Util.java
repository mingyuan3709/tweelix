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
     * 发送命令或聊天消息。
     * <ul>
     *   <li>{@code /xxx} — 作为命令发送（自动去除前导 "/"）</li>
     *   <li>{@code !!xxx} — MCDReforged 命令，通过聊天通道发送</li>
     *   <li>其他 — 默认通过 {@code sendCommand} 发送</li>
     * </ul>
     */
    public static void sendCommandOrChat(String command) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (command.startsWith("/")) {
            client.player.connection.sendCommand(command.substring(1));
        } else if (command.startsWith("!!")) {
            client.player.connection.sendChat(command);
        } else {
            client.player.connection.sendCommand(command);
        }
    }

}