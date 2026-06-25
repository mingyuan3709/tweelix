package ink.mingyuan.tweelix.feature.commandhint;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import ink.mingyuan.tweelix.config.category.Display;
import ink.mingyuan.tweelix.mixin.accessor.CommandSuggestionsAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 命令提示描述渲染的纯业务逻辑层，从 Mixin 中提取以便测试和维护。
 */
public final class CommandHintRenderer {

    private static final int DESC_COLOR = 0xFF888888;
    private static final int SEP_COLOR = 0xFF666666;
    private static final int MAX_DESC_WIDTH = 150;

    private CommandHintRenderer() {}

    /**
     * 根据文本提示动态计算补全框宽度
     */
    public static int calculateWidth(int originalK, CommandSuggestions outerInstance,
                                      List<Suggestion> list) {
        if (!Display.ENABLE_COMMAND_HINT.getBooleanValue()
                || list == null || list.isEmpty()) {
            return originalK;
        }

        CommandSuggestionsAccessor outerAcc = (CommandSuggestionsAccessor) outerInstance;
        Font font = outerAcc.getFont();
        EditBox editBox = outerAcc.getInput();
        if (editBox == null) return originalK;

        ParseResults<SharedSuggestionProvider> parseResults = outerAcc.getCurrentParse();
        String commandPath = getCommandPath(parseResults);
        CommandDescriptionRegistry registry = CommandDescriptionRegistry.getInstance();
        int maxBoxWidth = originalK;
        int sepWidth = font.width(" - ");
        String fullInputText = editBox.getValue();

        for (Suggestion suggestion : list) {
            Component desc = registry.getHint(suggestion.getText(), fullInputText, commandPath);
            if (desc != null) {
                int cmdWidth = font.width(suggestion.getText());
                int descWidth = Math.min(font.width(desc.getString()), MAX_DESC_WIDTH);
                int rowTotalWidth = cmdWidth + sepWidth + descWidth + 10;
                if (rowTotalWidth > maxBoxWidth) {
                    maxBoxWidth = rowTotalWidth;
                }
            }
        }
        return maxBoxWidth;
    }

    /**
     * 在图形界面上渲染 “ - 提示文本”
     */
    public static void renderDescriptions(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY,
                                           CommandSuggestions outerInstance, List<Suggestion> suggestionList,
                                           int offset, int rectX, int rectY, int rectWidth) {
        if (!Display.ENABLE_COMMAND_HINT.getBooleanValue()) return;
        if (outerInstance == null || suggestionList == null || suggestionList.isEmpty()) return;

        CommandSuggestionsAccessor outerAcc = (CommandSuggestionsAccessor) outerInstance;
        Font font = outerAcc.getFont();
        EditBox editBox = outerAcc.getInput();
        if (editBox == null) return;

        ParseResults<SharedSuggestionProvider> parseResults = outerAcc.getCurrentParse();
        String commandPath = getCommandPath(parseResults);
        CommandDescriptionRegistry registry = CommandDescriptionRegistry.getInstance();
        String fullInputText = editBox.getValue();

        int limit = outerAcc.getSuggestionLineLimit();
        int count = Math.min(suggestionList.size(), limit);

        for (int n = 0; n < count; n++) {
            int idx = n + offset;
            if (idx >= suggestionList.size()) break;

            Suggestion suggestion = suggestionList.get(idx);
            Component desc = registry.getHint(suggestion.getText(), fullInputText, commandPath);
            if (desc == null) continue;

            String descStr = desc.getString();
            int cmdWidth = font.width(suggestion.getText());
            int sepX = rectX + 2 + cmdWidth + 3;
            int descX = sepX + font.width(" - ");

            int availableSpace = (rectX + rectWidth) - descX - 4;
            int descWidth = font.width(descStr);
            int dotsWidth = font.width("...");

            if (descWidth > availableSpace + 1) {
                if (availableSpace > dotsWidth) {
                    descStr = font.plainSubstrByWidth(descStr, availableSpace - dotsWidth) + "...";
                } else {
                    continue;
                }
            }

            int itemY = rectY + 12 * n + 2;
            guiGraphics.text(font, " - ", sepX, itemY, SEP_COLOR, false);
            guiGraphics.text(font, descStr, descX, itemY, DESC_COLOR, false);
        }
    }

    /**
     * 通过 Brigadier 解析上下文，提取当前命令节点路径（如 "tp"、"player.attack"）
     */
    public static String getCommandPath(ParseResults<SharedSuggestionProvider> parseResults) {
        if (parseResults == null) return "";

        var contextBuilder = parseResults.getContext();
        if (contextBuilder == null) return "";

        List<ParsedCommandNode<SharedSuggestionProvider>> nodes = contextBuilder.getNodes();
        if (nodes == null || nodes.isEmpty()) return "";

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            CommandNode<SharedSuggestionProvider> node = nodes.get(i).getNode();
            String segment;

            if (node instanceof com.mojang.brigadier.tree.LiteralCommandNode<SharedSuggestionProvider> literalNode) {
                segment = literalNode.getLiteral();
            } else if (node instanceof com.mojang.brigadier.tree.ArgumentCommandNode<SharedSuggestionProvider, ?> argNode) {
                segment = argNode.getName();
            } else {
                continue;
            }

            if (i > 0) path.append('.');
            path.append(segment);
        }
        return path.toString();
    }
}
