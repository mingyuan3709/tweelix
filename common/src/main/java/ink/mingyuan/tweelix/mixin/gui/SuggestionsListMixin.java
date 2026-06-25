package ink.mingyuan.tweelix.mixin.gui;

import ink.mingyuan.tweelix.feature.commandhint.CommandHintRenderer;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 命令补全提示增强 — 薄注入层，业务逻辑委托给 {@link CommandHintRenderer}。
 */
@Mixin(CommandSuggestions.SuggestionsList.class)
public class SuggestionsListMixin {

    @Shadow @Final private Rect2i rect;
    @Shadow @Final private List<Suggestion> suggestionList;
    @Shadow private int offset;

    @Unique
    private CommandSuggestions tweelix$outer;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CommandSuggestions outerInstance, int i, int j, int k,
                        List<Suggestion> list, boolean bl, CallbackInfo ci) {
        this.tweelix$outer = outerInstance;
    }

    /** 动态扩宽补全框宽度 */
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private static int widenSuggestionBox(int originalK, CommandSuggestions outerInstance,
                                          int i, int j, int k, List<Suggestion> list) {
        return CommandHintRenderer.calculateWidth(originalK, outerInstance, list);
    }

    /** 渲染命令描述提示 */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void renderDescriptions(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        CommandHintRenderer.renderDescriptions(
                guiGraphics, mouseX, mouseY,
                tweelix$outer, suggestionList, offset,
                rect.getX(), rect.getY(), rect.getWidth());
    }
}
