package io.github.mortuusars.exposure.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.mortuusars.exposure.data.export.ExportLook;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class ExposureLookArgument implements ArgumentType<ExportLook> {
   public ExportLook parse(StringReader reader) throws CommandSyntaxException {
      String string = reader.readString();
      ExportLook look = ExportLook.byName(string);
      if (look == null) {
         throw new SimpleCommandExceptionType(Component.translatable("argument.enum.invalid", new Object[]{string})).create();
      } else {
         return look;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return SharedSuggestionProvider.suggest(
         Arrays.stream(ExportLook.values()).filter(l -> l != ExportLook.REGULAR).map(ExportLook::getSerializedName), builder
      );
   }

   public static ExportLook getLook(CommandContext<?> context, String name) {
      return (ExportLook)context.getArgument(name, ExportLook.class);
   }
}
