package ink.mingyuan.tweelix;

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
            System.out.println("请先进入一个单人世界或服务器以加载完整的命令树！");
            return;
        }

        // 1. 直接使用 ClientSuggestionProvider 泛型接收，类型完全对齐
        CommandDispatcher<ClientSuggestionProvider> dispatcher = mc.player.connection.getCommands();
        Map<String, String> translationMap = new LinkedHashMap<>();

        // 2. 遍历子节点
        for (CommandNode<ClientSuggestionProvider> child : dispatcher.getRoot().getChildren()) {
            walkCommandTree(child, "commands." + child.getName(), translationMap);
        }

        // 3. 导出为 JSON 文件
        File runDir = mc.gameDirectory;
        File exportFile = new File(runDir, "exports/command_keys.json");
        exportFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(exportFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            gson.toJson(translationMap, writer);
            System.out.println("【Tweelix】成功导出所有命令 Key 到: " + exportFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 4. 同步将这里的泛型也改为 ClientSuggestionProvider
    private static void walkCommandTree(CommandNode<ClientSuggestionProvider> node, String currentPath, Map<String, String> map) {
        String descKey = currentPath + ".description";

        String cleanNodeName = node.getName();
        if (node.getRedirect() == null && node.getCommand() == null && !node.getChildren().isEmpty()) {
            cleanNodeName = "<" + cleanNodeName + ">";
        }

        map.put(descKey, "请翻译此参数/命令的用途: " + cleanNodeName);

        for (CommandNode<ClientSuggestionProvider> child : node.getChildren()) {
            walkCommandTree(child, currentPath + "." + child.getName(), map);
        }
    }
}