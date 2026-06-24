package ink.mingyuan.tweelix.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.TweelixConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommandExporter {

    public static void exportAllCommandKeys() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            mc.gui.getChat().addMessage(
                    Component.literal("§c[Tweelix] Please join a world or server first to load the command tree!"));
            return;
        }

        CommandDispatcher<ClientSuggestionProvider> dispatcher = mc.player.connection.getCommands();
        JsonObject root = new JsonObject();

        // 元信息
        JsonObject meta = new JsonObject();
        meta.addProperty("exportTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        meta.addProperty("modVersion", Reference.MOD_VERSION != null ? Reference.MOD_VERSION : "unknown");
        root.add("_meta", meta);

        JsonObject keys = new JsonObject();

        for (CommandNode<ClientSuggestionProvider> child : dispatcher.getRoot().getChildren()) {
            String rootName = child.getName();
            // 本模组命令使用 tweelix.command.* 格式，匹配 CommandDescriptionRegistry.getModCommandHint()
            String prefix = rootName.equals("tweelix") ? "tweelix.command" : "commands." + rootName;
            walkCommandTree(child, prefix, keys);
        }

        root.add("keys", keys);

        File runDir = mc.gameDirectory;
        File exportFile = new File(runDir, "exports/command_keys.json");
        exportFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(exportFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            gson.toJson(root, writer);
            mc.gui.getChat().addMessage(
                    Component.literal("§a[Tweelix] Command keys exported to: " + exportFile.getAbsolutePath()));
        } catch (IOException e) {
            Reference.LOGGER.error("Failed to export command keys", e);
        }
    }

    private static void walkCommandTree(CommandNode<ClientSuggestionProvider> node, String currentPath,
                                        JsonObject out) {
        String descKey = currentPath + ".description";

        String label = node.getName();
        // 中间节点（有子节点、无命令、无重定向）= 参数占位符
        if (node.getRedirect() == null && node.getCommand() == null && !node.getChildren().isEmpty()) {
            label = "<" + label + ">";
        }

        out.addProperty(descKey, "Please translate: " + label);

        for (CommandNode<ClientSuggestionProvider> child : node.getChildren()) {
            walkCommandTree(child, currentPath + "." + child.getName(), out);
        }
    }
}
