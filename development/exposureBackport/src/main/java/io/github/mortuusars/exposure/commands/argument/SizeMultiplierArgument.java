package io.github.mortuusars.exposure.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.mortuusars.exposure.data.export.ExportSize;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class SizeMultiplierArgument implements ArgumentType<ExportSize> {
   public ExportSize parse(StringReader reader) throws CommandSyntaxException {
      String string = reader.readString();
      ExportSize size = ExportSize.byName(string);
      if (size == null) {
         throw new SimpleCommandExceptionType(Component.translatable("argument.enum.invalid", new Object[]{string})).create();
      } else {
         return size;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return SharedSuggestionProvider.suggest(Arrays.stream(ExportSize.values()).map(ExportSize::getSerializedName), builder);
   }

   public static ExportSize getSize(CommandContext<?> context, String name) {
      return (ExportSize)context.getArgument(name, ExportSize.class);
   }
}
