package ink.mingyuan.tweelix.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandExporter {

    public static void exportAllCommandKeys() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            mc.gui.getChat().addMessage(
                    net.minecraft.network.chat.Component.literal("§c[Tweelix] Please join a world or server first to load the command tree!"));
            return;
        }

        CommandDispatcher<ClientSuggestionProvider> dispatcher = mc.player.connection.getCommands();
        Map<String, String> translationMap = new LinkedHashMap<>();

        for (CommandNode<ClientSuggestionProvider> child : dispatcher.getRoot().getChildren()) {
            walkCommandTree(child, "commands." + child.getName(), translationMap);
        }

        File runDir = mc.gameDirectory;
        File exportFile = new File(runDir, "exports/command_keys.json");
        exportFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(exportFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            gson.toJson(translationMap, writer);
            mc.gui.getChat().addMessage(
                    net.minecraft.network.chat.Component.literal("§a[Tweelix] Command keys exported to: " + exportFile.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void walkCommandTree(CommandNode<ClientSuggestionProvider> node, String currentPath,
                                        Map<String, String> map) {
        String descKey = currentPath + ".description";

        String cleanNodeName = node.getName();
        if (node.getRedirect() == null && node.getCommand() == null && !node.getChildren().isEmpty()) {
            cleanNodeName = "<" + cleanNodeName + ">";
        }

        map.put(descKey, "Please translate: " + cleanNodeName);

        for (CommandNode<ClientSuggestionProvider> child : node.getChildren()) {
            walkCommandTree(child, currentPath + "." + child.getName(), map);
        }
    }
}
