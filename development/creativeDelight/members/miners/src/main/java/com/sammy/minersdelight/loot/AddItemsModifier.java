package com.sammy.minersdelight.loot;

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

/**
 * Loot modifiers {@code miners_delight:add_items} and {@code miners_delight:replace_items}: add {@code min..max} of
 * {@code item} to the drops, or replace the drops with them. Fields as the upstream data writes them.
 */
public class AddItemsModifier extends LootModifier {
    public static final Supplier<Codec<AddItemsModifier>> ADD = Suppliers.memoize(() -> codec(false));
    public static final Supplier<Codec<AddItemsModifier>> REPLACE = Suppliers.memoize(() -> codec(true));

    private final Item item;
    private final int min;
    private final int max;
    private final boolean replace;

    private AddItemsModifier(LootItemCondition[] conditions, Item item, int min, int max, boolean replace) {
        super(conditions);
        this.item = item;
        this.min = min;
        this.max = max;
        this.replace = replace;
    }

    private static Codec<AddItemsModifier> codec(boolean replace) {
        return RecordCodecBuilder.create(inst -> codecStart(inst).and(inst.group(
                ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item),
                Codec.INT.optionalFieldOf("min", 1).forGetter(m -> m.min),
                Codec.INT.optionalFieldOf("max", 1).forGetter(m -> m.max)
        )).apply(inst, (conditions, item, min, max) -> new AddItemsModifier(conditions, item, min, max, replace)));
    }

    @Override
    @NotNull
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (replace) {
            loot.clear();
        }
        int count = Mth.nextInt(context.getRandom(), min, max);
        int maxStack = new ItemStack(item).getMaxStackSize();
        while (count > 0) {
            int size = Math.min(count, maxStack);
            loot.add(new ItemStack(item, size));
            count -= size;
        }
        return loot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return replace ? REPLACE.get() : ADD.get();
    }
}
