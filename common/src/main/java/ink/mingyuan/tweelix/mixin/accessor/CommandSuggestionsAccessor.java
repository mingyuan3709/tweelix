package ink.mingyuan.tweelix.mixin.accessor;

import com.mojang.brigadier.ParseResults;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestions.class)
public interface CommandSuggestionsAccessor {

    @Accessor("font")
    Font getFont();

    @Accessor("suggestionLineLimit")
    int getSuggestionLineLimit();

    @Accessor("input") // 对应原版的 EditBox input 字段
    EditBox getInput();

    /**
     * 获取当前命令的 Brigadier 解析结果
     * 它是 100% 精确获取光标处处于哪个 CommandNode 的核心数据
     */
    @Accessor("currentParse")
    ParseResults<SharedSuggestionProvider> getCurrentParse();
}