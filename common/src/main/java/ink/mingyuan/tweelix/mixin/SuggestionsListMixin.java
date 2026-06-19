package ink.mingyuan.tweelix.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import ink.mingyuan.tweelix.commandhint.CommandDescriptionRegistry;
import ink.mingyuan.tweelix.config.category.GenericCategory;
import ink.mingyuan.tweelix.mixin.accessor.CommandSuggestionsAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class SuggestionsListMixin {

    @Shadow @Final private Rect2i rect;
    @Shadow @Final private List<Suggestion> suggestionList;
    @Shadow private int offset;

    private CommandSuggestions tweelit$outer;

    @Unique private static final int DESC_COLOR = 0xFF888888;
    @Unique private static final int SEP_COLOR = 0xFF666666;
    @Unique private static final int MAX_DESC_WIDTH = 150;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CommandSuggestions outerInstance, int i, int j, int k, List<Suggestion> list, boolean bl, CallbackInfo ci) {
        this.tweelit$outer = outerInstance;
    }

    /**
     * 重构 1：根据文本提示动态扩宽渲染提示框宽度
     */
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private static int widenSuggestionBox(int originalK, CommandSuggestions outerInstance, int i, int j, int k, List<Suggestion> list) {
        if (!GenericCategory.ENABLE_COMMAND_HINT.getBooleanValue() || list == null || list.isEmpty()) return originalK;

        CommandSuggestionsAccessor outerAcc = (CommandSuggestionsAccessor) outerInstance;
        Font font = outerAcc.getFont();
        EditBox editBox = outerAcc.getInput();
        if (editBox == null) return originalK;

        ParseResults<SharedSuggestionProvider> parseResults = outerAcc.getCurrentParse();

        String CommandPath = tweelit$getCommandPath(parseResults);

        CommandDescriptionRegistry registry = CommandDescriptionRegistry.getInstance();
        int maxBoxWidth = originalK;
        int sepWidth = font.width(" - ");
        String fullInputText = editBox.getValue(); // 确认这里是否真的拿到了完整的 "/carpet"

        for (Suggestion suggestion : list) {
            Component desc = registry.getHint(suggestion.getText(), fullInputText,CommandPath);
            if (desc != null) {
                int cmdWidth = font.width(suggestion.getText());
                int descWidth = font.width(desc.getString());

                // 限制最大宽度，但如果没超过，必须是真实的宽度
                if (descWidth > MAX_DESC_WIDTH) {
                    descWidth = MAX_DESC_WIDTH;
                }

                // 注意：原生补全框两边有 padding，额外 +6 甚至 +10 确保安全
                int rowTotalWidth = cmdWidth + sepWidth + descWidth + 10;
                if (rowTotalWidth > maxBoxWidth) {
                    maxBoxWidth = rowTotalWidth;
                }
            }
        }
        return maxBoxWidth;
    }

    /**
     * 重构 2：在图形界面上渲染 “ - 提示文本”
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void renderDescriptions(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (!GenericCategory.ENABLE_COMMAND_HINT.getBooleanValue()) return;
        if (tweelit$outer == null || rect == null || suggestionList == null || suggestionList.isEmpty()) return;

        CommandSuggestionsAccessor outerAcc = (CommandSuggestionsAccessor) tweelit$outer;
        Font font = outerAcc.getFont();
        EditBox editBox = outerAcc.getInput();
        if (editBox == null) return;

        ParseResults<SharedSuggestionProvider> parseResults = outerAcc.getCurrentParse();

        String CommandPath = tweelit$getCommandPath(parseResults);

        CommandNode<SharedSuggestionProvider> currentNode = tweelit$getCurrentNode(parseResults);

        CommandDescriptionRegistry registry = CommandDescriptionRegistry.getInstance();
        String fullInputText = editBox.getValue();

        int limit = outerAcc.getSuggestionLineLimit();
        int k = Math.min(suggestionList.size(), limit);

        for (int n = 0; n < k; n++) {
            int idx = n + offset;
            if (idx >= suggestionList.size()) break;

            Suggestion suggestion = suggestionList.get(idx);
            // 直接传入 候选词文本 和 输入框完整文本
            Component desc = registry.getHint(suggestion.getText(), fullInputText,CommandPath);
            if (desc == null) continue;

            String descStr = desc.getString();
            int cmdWidth = font.width(suggestion.getText());

            int sepX = rect.getX() + 2 + cmdWidth + 3;
            int descX = sepX + font.width(" - ");

// 1. 计算理论上留给描述文本的实际可用像素宽度
            int availableSpace = (rect.getX() + rect.getWidth()) - descX - 4;

            int descWidth = font.width(descStr);
            int dotsWidth = font.width("...");

// 2. 优化裁剪判断：引入 1 像素的容错缓冲
// 只有当文本宽度确实明显大于可用空间时，才执行裁剪
            if (descWidth > availableSpace + 1) {
                // 确保空间至少能放下 "..."，否则裁剪没有意义
                if (availableSpace > dotsWidth) {
                    // 裁剪时减去省略号宽度
                    descStr = font.plainSubstrByWidth(descStr, availableSpace - dotsWidth) + "...";
                } else {
                    // 如果连省略号都放不下，说明空间极度被挤压，直接不渲染该条提示
                    continue;
                }
            }

            int itemY = rect.getY() + 12 * n + 2;

// 3. 最终渲染
            guiGraphics.drawString(font, " - ", sepX, itemY, SEP_COLOR, false);
            guiGraphics.drawString(font, descStr, descX, itemY, DESC_COLOR, false);
        }
    }

    /**
     * 辅助方法：通过解析上下文获取光标处处于哪一个具体的 CommandNode
     */
    @Unique
    private static CommandNode<SharedSuggestionProvider> tweelit$getCurrentNode(ParseResults<SharedSuggestionProvider> parseResults) {
        if (parseResults == null) return null;

        com.mojang.brigadier.context.CommandContextBuilder<SharedSuggestionProvider> contextBuilder = parseResults.getContext();
        if (contextBuilder == null) return null;

        List<ParsedCommandNode<SharedSuggestionProvider>> nodes = contextBuilder.getNodes();

        // 如果 nodes 为空，说明 Brigadier 还没有把当前输入的文本认作任何有效的命令节点（即正在输入根命令）
        if (nodes == null || nodes.isEmpty()) {
            return null; // 返回 null，让 Registry 触发极其稳健的“不带空格纯文本匹配”
        }

        // 正常返回当前最末尾的有效节点
        return nodes.get(nodes.size() - 1).getNode();
    }

    @Unique
    private static String tweelit$getCommandPath(ParseResults<SharedSuggestionProvider> parseResults) {
        if (parseResults == null) return "";

        com.mojang.brigadier.context.CommandContextBuilder<SharedSuggestionProvider> contextBuilder = parseResults.getContext();
        if (contextBuilder == null) return "";

        List<ParsedCommandNode<SharedSuggestionProvider>> nodes = contextBuilder.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return "";
        }

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            CommandNode<SharedSuggestionProvider> node = nodes.get(i).getNode();
            String segment;

            if (node instanceof com.mojang.brigadier.tree.LiteralCommandNode<SharedSuggestionProvider> literalNode) {
                segment = literalNode.getLiteral();          // "player", "attack"
            } else if (node instanceof com.mojang.brigadier.tree.ArgumentCommandNode<SharedSuggestionProvider, ?> argNode) {
                segment = argNode.getName();                 // "player"
            } else {
                continue; // 跳过 RootCommandNode 等不可预期节点
            }

            if (i > 0) {
                path.append('.');
            }
            path.append(segment);
        }

        return path.toString(); // 结果: "player.player.attack"
    }
}