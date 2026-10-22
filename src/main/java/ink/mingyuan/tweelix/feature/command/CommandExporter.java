package ink.mingyuan.tweelix.feature.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import ink.mingyuan.tweelix.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 导出命令树的翻译 key 骨架。
 *
 * <p>注意：这里只遍历静态命令树，产出的是「命令名 / 参数槽位」这一类 key，
 * 不包含参数候选值（如 {@code time set day}、gamerule 的具体值），
 * 候选值需要在值层单独维护。</p>
 */
public final class CommandExporter {

    private CommandExporter() {}

    /**
     * @return true 表示文件已写入；false 表示未连接世界或写入失败
     */
    public static boolean exportAllCommandKeys() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.connection == null) {
            return false;
        }

        CommandDispatcher<ClientSuggestionProvider> dispatcher = mc.player.connection.getCommands();
        JsonObject keys = new JsonObject();
        int[] counts = new int[2]; // [0] literal, [1] argument slot

        for (CommandNode<ClientSuggestionProvider> child : dispatcher.getRoot().getChildren()) {
            String rootName = child.getName();
            String prefix = rootName.equals("tweelix") ? "tweelix.command" : "commands." + rootName;
            walkCommandTree(child, prefix, keys, counts);
        }

        JsonObject meta = new JsonObject();
        meta.addProperty("exportTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        meta.addProperty("modVersion", Reference.MOD_VERSION != null ? Reference.MOD_VERSION : "unknown");
        meta.addProperty("keyCount", keys.size());
        meta.addProperty("literalCount", counts[0]);
        meta.addProperty("slotCount", counts[1]);
        meta.addProperty("note",
                "Only command-tree nodes are exported (command names and argument slots). "
                        + "Suggestion values such as 'time set day' or gamerule values are not tree nodes "
                        + "and must be maintained in the value layer separately.");

        JsonObject root = new JsonObject();
        root.add("_meta", meta);
        root.add("keys", keys);

        Path exportDir = mc.gameDirectory.toPath().resolve("exports");
        Path exportFile = exportDir.resolve("command_keys.json");

        try {
            Files.createDirectories(exportDir);
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            try (var writer = Files.newBufferedWriter(exportFile, StandardCharsets.UTF_8)) {
                gson.toJson(root, writer);
            }
            return true;
        } catch (IOException e) {
            Reference.LOGGER.error("Failed to export command keys to " + exportFile, e);
            return false;
        }
    }

    private static void walkCommandTree(CommandNode<ClientSuggestionProvider> node, String currentPath,
                                        JsonObject out, int[] counts) {
        boolean argument = node instanceof ArgumentCommandNode<ClientSuggestionProvider, ?>;
        String label = argument ? "<" + node.getName() + ">" : node.getName();

        out.addProperty(currentPath + ".description", "Please translate: " + label);
        counts[argument ? 1 : 0]++;

        for (CommandNode<ClientSuggestionProvider> child : node.getChildren()) {
            walkCommandTree(child, currentPath + "." + child.getName(), out, counts);
        }
    }
}
