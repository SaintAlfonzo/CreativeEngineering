package com.SaintAlfonzo.creativedelight.oceanic.registry;

import com.SaintAlfonzo.creativedelight.oceanic.OceanicDelightMod;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * The loot modifier type upstream's data uses ({@code oceanic_delight:oceanic_delight_loot_modifier}, field
 * {@code lootTable}): where its conditions pass, it rolls that loot table into the drops. Upstream's six modifiers (squid
 * and glow squid tentacles, elder guardian eye, salmon eggs, the shipwreck and buried treasure eggs) are its data.
 */
public final class OceanicLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, OceanicDelightMod.MODID);

    public static final RegistryObject<Codec<AddTable>> ADD_TABLE = SERIALIZERS.register("oceanic_delight_loot_modifier", AddTable.CODEC);

    private OceanicLootModifiers() {
    }

    public static class AddTable extends LootModifier {
        public static final Supplier<Codec<AddTable>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(instance -> codecStart(instance)
                .and(ResourceLocation.CODEC.fieldOf("lootTable").forGetter(m -> m.table))
                .apply(instance, AddTable::new)));

        private final ResourceLocation table;

        public AddTable(LootItemCondition[] conditions, ResourceLocation table) {
            super(conditions);
            this.table = table;
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            context.getLootTable(table).getRandomItemsRaw(context, loot::add);
            return loot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }
}
