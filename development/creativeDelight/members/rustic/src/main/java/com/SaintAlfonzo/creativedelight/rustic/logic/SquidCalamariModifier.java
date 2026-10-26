package com.SaintAlfonzo.creativedelight.rustic.logic;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/** Loot modifier {@code rusticdelight:squids_drop_calamari_modifier}: squids also drop {@code minAmount..maxAmount} calamari. */
public class SquidCalamariModifier extends LootModifier {
    public static final Supplier<Codec<SquidCalamariModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).and(inst.group(
            ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item),
            Codec.INT.optionalFieldOf("minAmount", 1).forGetter(m -> m.min),
            Codec.INT.optionalFieldOf("maxAmount", 1).forGetter(m -> m.max)
    )).apply(inst, SquidCalamariModifier::new)));

    private final Item item;
    private final int min;
    private final int max;

    private SquidCalamariModifier(LootItemCondition[] conditions, Item item, int min, int max) {
        super(conditions);
        this.item = item;
        this.min = min;
        this.max = max;
    }

    @Override
    @NotNull
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        loot.add(new ItemStack(item, Mth.nextInt(context.getRandom(), min, max)));
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
