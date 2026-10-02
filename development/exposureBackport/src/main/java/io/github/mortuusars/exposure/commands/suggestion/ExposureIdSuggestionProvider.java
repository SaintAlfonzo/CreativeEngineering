package io.github.mortuusars.exposure.commands.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.mortuusars.exposure.ExposureServer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public class ExposureIdSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
   public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
      List<String> ids = ExposureServer.exposureRepository().getAllIds();
      return SharedSuggestionProvider.suggest(ids, builder);
   }
}
