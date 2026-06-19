package ink.mingyuan.tweelix.commandhint;

import com.mojang.brigadier.tree.CommandNode;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.config.category.GenericCategory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class CommandDescriptionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(Reference.MOD_ID);
    private static final CommandDescriptionRegistry INSTANCE = new CommandDescriptionRegistry();

    // 存储命令相关的覆盖提示（例如 "tp" -> "传送到指定位置或玩家"）
    private final Map<String, Component> commandDescriptions = new HashMap<>();
    // 存储特定选择器提示（例如 "@p" -> "最近的玩家"）
    private static final Map<String, Component> SELECTOR_HINTS = new HashMap<>();

    static {
        SELECTOR_HINTS.put("@a", Component.literal("所有玩家"));
        SELECTOR_HINTS.put("@p", Component.literal("最近的玩家"));
        SELECTOR_HINTS.put("@n", Component.literal("最近的实体"));
        SELECTOR_HINTS.put("@r", Component.literal("随机玩家"));
        SELECTOR_HINTS.put("@s", Component.literal("当前实体"));
        SELECTOR_HINTS.put("@e", Component.literal("所有实体"));
    }

    public static CommandDescriptionRegistry getInstance() {
        return INSTANCE;
    }

    // 构造时直接加载
    private CommandDescriptionRegistry() {
        loadBuiltInDescriptions();

        LOGGER.info("CommandHint Registry initialized with {} entries.", commandDescriptions.size());
    }

    private void loadBuiltInDescriptions() {
        put("gamemode", "切换游戏模式");
        put("adventure", "冒险");
        put("creative", "创造");
        put("spectator", "旁观");
        put("survival", "生存");
    }

    private void put(String cmd, String desc) {
        commandDescriptions.put(cmd, Component.literal(desc));
    }

    /**
     * 根据当前候选词和输入框完整文本，提供提示描述
     *
     * @param suggestionText   当前补全列表里显示的文本（例如 "@p"、"tp"、"coordinates" 等）
     * @param inputCommandText  目前输入框里的全部文本,不一定完整（例如 "/tp @"）
     */
    public Component getHint(String suggestionText, String inputCommandText, String CommandPath) {

        if (!GenericCategory.ENABLE_COMMAND_HINT.getBooleanValue()
                || suggestionText == null
                || inputCommandText == null) {
            return null;
        }

        if (suggestionText.startsWith("@")) {
            Component selectorHint = SELECTOR_HINTS.get(suggestionText);
            if (selectorHint != null) return selectorHint;
        }

        Component directDesc = commandDescriptions.get(suggestionText);
        if (directDesc != null) return directDesc;

        // 3. 【新增】如果是物品ID，直接返回官方翻译名称（支持原版+所有模组）
        if (isItemId(suggestionText)) {
            return getItemDisplayName(suggestionText);
        }

        String fullCommandText = getFullCommandText(suggestionText, inputCommandText);

        String genericArgKey = getGenericArgKey(fullCommandText);


// 兜底：用 CommandPath + inputCommandText + suggestionText 生成 key
        if (inputCommandText.contains(" ")) {
            if (CommandPath.isEmpty()) {
                genericArgKey = "commands." + suggestionText + ".description";
            } else {
                // 提取三个来源的最后节点
                String pathLastNode = CommandPath.contains(".")
                        ? CommandPath.substring(CommandPath.lastIndexOf('.') + 1)
                        : CommandPath;

                // inputCommandText 去掉开头的 /，按空格取最后一段
                boolean pathMatchesInput = isPathMatchesInput(inputCommandText, pathLastNode);

                if (pathMatchesInput && pathLastNode.equals(suggestionText)) {
                    // 情况1：三者一致，直接用 CommandPath
                    genericArgKey = "commands." + CommandPath + ".description";
                } else if (pathMatchesInput) {
                    // 情况2：Path 和 Input 一致，但与 suggestion 不同 → 替换最后节点
                    String parentPath = CommandPath.contains(".")
                            ? CommandPath.substring(0, CommandPath.lastIndexOf('.'))
                            : "";
                    if (parentPath.isEmpty()) {
                        // 无上级节点：直接用 suggestionText
                        genericArgKey = "commands." + suggestionText + ".description";
                    } else {
                        genericArgKey = "commands." + parentPath + "." + suggestionText + ".description";
                    }
                } else {
                    // 情况3：Path 和 Input 不一致（兜底追加）
                    genericArgKey = "commands." + CommandPath + "." + suggestionText + ".description";
                }
            }
        }


        if (I18n.exists(genericArgKey)) {
            return Component.translatable(genericArgKey);
        }

        return null;
    }

    private static boolean isPathMatchesInput(String inputCommandText, String pathLastNode) {
        String inputWithoutSlash = inputCommandText.startsWith("/")
                ? inputCommandText.substring(1)
                : inputCommandText;
        String inputLastNode = inputWithoutSlash.contains(" ")
                ? inputWithoutSlash.substring(inputWithoutSlash.lastIndexOf(' ') + 1).trim()
                : inputWithoutSlash.trim();
        // 判断：CommandPath 和 inputCommandText 最后节点是否一致
        return pathLastNode.equals(inputLastNode);
    }

    private static String getFullCommandText(String suggestionText, String inputCommandText) {
        String fullCommandText = inputCommandText;

        boolean endsWithSpace = fullCommandText.trim().endsWith(" ");

        // 以空格结尾
        if (endsWithSpace) {
            fullCommandText = inputCommandText + suggestionText;
        } else {
            String withoutSlash = inputCommandText.replaceFirst("^/", "");
            if (!withoutSlash.contains(" ")) {
                fullCommandText = suggestionText;
            } else {
                fullCommandText = inputCommandText.replaceAll(" [^ ]*$", " " + suggestionText);
            }
        }
        return fullCommandText;
    }

    private String getGenericArgKey(String fullCommandText) {

        if (fullCommandText == null || fullCommandText.isEmpty()) return null;

        // 去掉开头的斜杠
        if (fullCommandText.startsWith("/")) {
            fullCommandText = fullCommandText.substring(1).trim();
        }

        fullCommandText = fullCommandText.replace(" ", ".");

        return "commands." + fullCommandText + ".description";

    }

    private boolean isItemId(String str) {
        if (str == null || !str.contains(":")) return false;
        Identifier id = Identifier.tryParse(str);
        return id != null && BuiltInRegistries.ITEM.get(id).isPresent();
    }

    private Component getItemDisplayName(String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) return Component.literal(itemId);

        return BuiltInRegistries.ITEM.get(id)
                .map(holder -> Component.translatable(holder.value().getDescriptionId()))
                .orElse(Component.literal(itemId));
    }
}