package de.cokechat.mixin;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;
@Mixin(CommandSuggestions.SuggestionsList.class)
public interface SuggestionsListAccess {
    @Accessor("suggestionList") List<Suggestion> cokechat$options();
    @Accessor("current") int cokechat$selected();
}
