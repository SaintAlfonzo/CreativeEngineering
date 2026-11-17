package com.SaintAlfonzo.creativedelight.hearth.gear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

/**
 * Dispenser planting from a seed container (plan_bugfix_1114 section 1). The container always stays in its slot: success
 * plays the dispense click, failure the fail click, and nothing is ejected. Each seed is planted through its own
 * {@code useOn} on the soil block, face UP, held by a fake player, so every seed keeps its own placement rules (farmland
 * crops, nether wart, berry bushes, End crops).
 * <p>
 * Target rule (pouch and bundle alike): the block in front of the dispenser; if it is air or replaceable the soil layer is
 * the layer below it, otherwise the front block is the soil layer and seeds go on top of it. The Quark seed pouch plants a
 * 5x5 on that layer (up to 25 seeds); the vanilla bundle plants its first entry once on the centre.
 */
public final class SeedContainerDispense {
    /** Half width of the pouch pattern: 5x5, matching Quark's player Shift Range of 5. */
    public static final int POUCH_RADIUS = 2;
    private static final TagKey<net.minecraft.world.item.Item> FORGE_SEEDS = ItemTags.create(new ResourceLocation("forge", "seeds"));
    private static final TagKey<net.minecraft.world.item.Item> POUCH_HOLDABLE = ItemTags.create(new ResourceLocation("quark", "seed_pouch_holdable"));

    private SeedContainerDispense() {
    }

    /** Container-specific part: plant from {@code container} at this soil center, return whether anything planted. */
    public interface Planter {
        boolean plant(ServerLevel level, BlockPos soilCentre, ItemStack container);
    }

    /** Registers the container behavior; it never ejects the container. */
    public static void register(net.minecraft.world.level.ItemLike item, Planter planter) {
        DispenserBlock.registerBehavior(item, new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack container) {
                ServerLevel level = source.getLevel();
                Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
                setSuccess(planter.plant(level, soilCentre(level, source.getPos().relative(facing)), container));
                return container;
            }
        });
    }

    /** The vanilla bundle: registered with or without Quark (only its recipe comes from Quark). */
    public static void registerBundle() {
        register(Items.BUNDLE, SeedContainerDispense::plantFromBundle);
    }

    /** Front air or replaceable: soil is the block below it; otherwise the front block is the soil. */
    static BlockPos soilCentre(ServerLevel level, BlockPos front) {
        BlockState state = level.getBlockState(front);
        return state.isAir() || state.getMaterial().isReplaceable() ? front.below() : front;
    }

    /** A seed for the bundle: Quark's pouch tag, {@code #forge:seeds}, or a block item whose block is plantable. */
    static boolean isSeed(ItemStack stack) {
        return stack.is(POUCH_HOLDABLE) || stack.is(FORGE_SEEDS)
                || stack.getItem() instanceof BlockItem item && item.getBlock() instanceof IPlantable;
    }

    /** Plants one of {@code seed} on top of {@code soil} through the seed's own useOn; true when it placed. */
    static boolean plantOne(ServerLevel level, BlockPos soil, ItemStack seed) {
        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        ItemStack held = seed.copy();
        held.setCount(1);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        try {
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(soil).add(0, 0.5, 0), Direction.UP, soil, false);
            // Explicit level: the fake player is cached by profile (the overworld one in every dimension), and the public
            // UseOnContext constructor takes player.level, so Nether seeds were tried in the overworld (SQ-SPD-08).
            UseOnContext context = new UseOnContext(level, player, InteractionHand.MAIN_HAND, held, hit) {
            };
            boolean placed = held.getItem().useOn(context).consumesAction();
            // A placed seed shrinks the held copy; count either sign so a planted seed is never handed back.
            return placed || held.isEmpty();
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    /** Plants up to {@code count} seeds over the 5x5 centered on {@code centre}; returns how many planted. */
    public static int plantPattern(ServerLevel level, BlockPos centre, ItemStack seed, int count) {
        int planted = 0;
        for (int dx = -POUCH_RADIUS; dx <= POUCH_RADIUS && planted < count; dx++) {
            for (int dz = -POUCH_RADIUS; dz <= POUCH_RADIUS && planted < count; dz++) {
                if (plantOne(level, centre.offset(dx, 0, dz), seed)) {
                    planted++;
                }
            }
        }
        return planted;
    }

    /** Vanilla bundle: the first entry of {@code Items} (vanilla's remove-one order), one seed on the centre. */
    private static boolean plantFromBundle(ServerLevel level, BlockPos centre, ItemStack bundle) {
        CompoundTag tag = bundle.getTag();
        if (tag == null || !tag.contains("Items", Tag.TAG_LIST)) {
            return false;
        }
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        if (items.isEmpty()) {
            return false;
        }
        CompoundTag first = items.getCompound(0);
        ItemStack seed = ItemStack.of(first);
        if (seed.isEmpty() || !isSeed(seed) || !plantOne(level, centre, seed)) {
            return false;
        }
        seed.shrink(1);
        if (seed.isEmpty()) {
            items.remove(0);
            if (items.isEmpty()) {
                bundle.removeTagKey("Items");
            }
        } else {
            CompoundTag rewritten = new CompoundTag();
            seed.save(rewritten);
            items.set(0, rewritten);
        }
        return true;
    }
}
