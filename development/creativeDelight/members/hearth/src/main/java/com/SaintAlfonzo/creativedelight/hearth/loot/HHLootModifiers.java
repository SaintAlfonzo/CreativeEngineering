package com.SaintAlfonzo.creativedelight.hearth.loot;

import com.SaintAlfonzo.creativedelight.hearth.HHConfig;
import com.SaintAlfonzo.creativedelight.hearth.HHTags;
import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.registry.ModItems;

/** Loot hooks (N23-1, N23-17, plan P5). 1.19.2 has no block-drops event, so these go through Forge's global loot modifiers. */
public final class HHLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, HearthAndHarvestMod.MODID);
    public static final RegistryObject<Codec<CornHusk>> CORN_HUSK = SERIALIZERS.register("corn_husk", CornHusk.CODEC);
    public static final RegistryObject<Codec<ReplaceItem>> REPLACE_ITEM = SERIALIZERS.register("replace_item", ReplaceItem.CODEC);
    public static final RegistryObject<Codec<PitchforkStraw>> PITCHFORK_STRAW = SERIALIZERS.register("pitchfork_straw", PitchforkStraw.CODEC);

    private HHLootModifiers() {
    }

    /** A Corn Husk by chance when the drops hold corn: hand, hoe and Create harvesters all pass through the loot table. */
    public static final class CornHusk extends LootModifier {
        static final Supplier<Codec<CornHusk>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(i -> codecStart(i).apply(i, CornHusk::new)));

        CornHusk(LootItemCondition[] conditions) {
            super(conditions);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            if (loot.stream().anyMatch(stack -> stack.is(HHTags.CORN)) && context.getRandom().nextDouble() < HHConfig.CORN_HUSK_HARVEST_CHANCE.get()) {
                loot.add(new ItemStack(HHItems.CORN_HUSK.get()));
            }
            return loot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }

    /** Swaps one item for another, same count: the crow feather on Alex's Mobs' crow. */
    public static final class ReplaceItem extends LootModifier {
        static final Supplier<Codec<ReplaceItem>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(i -> codecStart(i).and(i.group(
                ForgeRegistries.ITEMS.getCodec().fieldOf("from").forGetter(m -> m.from),
                ForgeRegistries.ITEMS.getCodec().fieldOf("to").forGetter(m -> m.to))).apply(i, ReplaceItem::new)));
        private final Item from;
        private final Item to;

        ReplaceItem(LootItemCondition[] conditions, Item from, Item to) {
            super(conditions);
            this.from = from;
            this.to = to;
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            for (int n = 0; n < loot.size(); n++) {
                if (loot.get(n).is(from)) loot.set(n, new ItemStack(to, loot.get(n).getCount()));
            }
            return loot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }
    }

    /** N23-17, fix B8: grass and ferns give straw instead of their drops; a mature tagged crop gives straw on top of them. */
    public static final class PitchforkStraw extends LootModifier {
        static final Supplier<Codec<PitchforkStraw>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(i -> codecStart(i).apply(i, PitchforkStraw::new)));

        PitchforkStraw(LootItemCondition[] conditions) {
            super(conditions);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
            BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
            if (tool == null || state == null || !tool.is(HHItems.PITCHFORK.get())) return loot;
            boolean grass = state.is(HHTags.PITCHFORK_GRASS);
            if (!grass && !(state.is(HHTags.PITCHFORK_CROPS) && isMature(state))) return loot;
            if (grass) loot.clear();
            loot.add(new ItemStack(ModItems.STRAW.get(), 2 + context.getRandom().nextInt(3)));
            return loot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC.get();
        }

        /** By the block's "age" property, so modded crops that are not CropBlocks (hemp, rice) work; no age counts as mature. */
        private static boolean isMature(BlockState state) {
            for (Property<?> property : state.getProperties()) {
                if (property instanceof IntegerProperty age && "age".equals(property.getName())) {
                    return state.getValue(age) >= age.getPossibleValues().stream().max(Integer::compare).orElse(0);
                }
            }
            return true;
        }
    }
}
