package ink.mingyuan.tweelix.mixin.accessor;

import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public interface SuggestionsListAccessor {

    @Accessor("rect")
    Rect2i getRect();

    @Accessor("suggestionList")
    List<Suggestion> getSuggestionList();

    @Accessor("offset")
    int getOffset();

    @Accessor("current")
    int getCurrent();
}