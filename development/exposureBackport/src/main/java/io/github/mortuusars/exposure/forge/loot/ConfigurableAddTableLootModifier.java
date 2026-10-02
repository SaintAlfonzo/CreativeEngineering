package io.github.mortuusars.exposure.forge.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.forge.ExposureForge;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

/**
 * Rolls an extra loot table into the generated loot when {@code Config.Common.GENERATE_LOOT} is on. Upstream
 * extends NeoForge's {@code AddTableLootModifier}, which Forge 1.19.2 doesn't have: its roll is inlined here.
 */
public class ConfigurableAddTableLootModifier extends LootModifier {
   public static final Supplier<Codec<ConfigurableAddTableLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create((instance) -> codecStart(instance).and(ResourceLocation.CODEC.fieldOf("table").forGetter(ConfigurableAddTableLootModifier::table)).apply(instance, ConfigurableAddTableLootModifier::new)));

   private final ResourceLocation table;

   public ConfigurableAddTableLootModifier(LootItemCondition[] conditions, ResourceLocation table) {
      super(conditions);
      this.table = table;
   }

   public ResourceLocation table() {
      return this.table;
   }

   protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
      if (!Config.Common.GENERATE_LOOT.get()) {
         return generatedLoot;
      }
      LootTable extraTable = context.getLootTable(this.table);
      extraTable.getRandomItemsRaw(context, LootTable.createStackSplitter(generatedLoot::add));
      return generatedLoot;
   }

   public @NotNull Codec<? extends IGlobalLootModifier> codec() {
      return ExposureForge.LootModifiers.ADD_TABLE.get();
   }
}
