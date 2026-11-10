package com.SaintAlfonzo.creativedelight.hearth.machine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;

/** A half crate with its bottles in the block entity tag; a second half can be added onto a placed one. */
public class CrateBlockItem extends BlockItem {
    private static final int MAX_TOOLTIP_LINES = 5;

    public CrateBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static void registerDispense(Item crate) {
        DispenserBlock.registerBehavior(crate, new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos target = source.getPos().relative(facing);
                InteractionResult result = ((CrateBlockItem) stack.getItem()).place(new DirectionalPlaceContext(source.getLevel(), target, facing, stack, facing.getOpposite()));
                setSuccess(result.consumesAction());
                return stack;
            }
        });
    }

    @Nullable
    private static CompoundTag blockTag(ItemStack stack) {
        return BlockItem.getBlockEntityData(stack);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState existing = level.getBlockState(pos);
        boolean willBeDouble = existing.is(getBlock()) && existing.getValue(CrateBlock.TYPE) != SlabType.DOUBLE;
        NonNullList<ItemStack> bottom = NonNullList.withSize(CrateBlockEntity.PER_HALF, ItemStack.EMPTY);
        NonNullList<ItemStack> top = NonNullList.withSize(CrateBlockEntity.PER_HALF, ItemStack.EMPTY);
        boolean server = willBeDouble && !level.isClientSide;
        if (server && level.getBlockEntity(pos) instanceof CrateBlockEntity crate) {
            for (int i = 0; i < CrateBlockEntity.PER_HALF; i++) bottom.set(i, crate.getItem(i).copy());
        }
        if (server) {
            CompoundTag tag = blockTag(context.getItemInHand());
            if (tag != null) ContainerHelper.loadAllItems(tag, top);
        }
        InteractionResult result = super.place(context);
        if (server && result.consumesAction() && level.getBlockEntity(pos) instanceof CrateBlockEntity crate) {
            crate.clearContent();
            for (int i = 0; i < CrateBlockEntity.PER_HALF; i++) {
                crate.items().set(i, bottom.get(i));
                crate.items().set(CrateBlockEntity.PER_HALF + i, top.get(i));
            }
            crate.sync();
        }
        return result;
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = blockTag(stack);
        if (tag == null) return super.getName(stack);
        NonNullList<ItemStack> items = NonNullList.withSize(CrateBlockEntity.TOTAL, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        ItemStack first = ItemStack.EMPTY;
        for (int i = 0; i < CrateBlockEntity.PER_HALF && first.isEmpty(); i++) first = items.get(i);
        if (first.isEmpty()) return super.getName(stack);
        for (int i = 0; i < CrateBlockEntity.PER_HALF; i++) {
            if (!items.get(i).isEmpty() && !items.get(i).is(first.getItem())) return Component.translatable("block.hearthandharvest.crate.mixed");
        }
        return Component.translatable("block.hearthandharvest.crate.named", first.getHoverName());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag tag = blockTag(stack);
        if (tag == null) return;
        NonNullList<ItemStack> items = NonNullList.withSize(CrateBlockEntity.TOTAL, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        Map<Item, Integer> grouped = new LinkedHashMap<>();
        Map<Item, ItemStack> rep = new LinkedHashMap<>();
        for (ItemStack s : items) {
            if (s.isEmpty()) continue;
            rep.putIfAbsent(s.getItem(), s);
            grouped.merge(s.getItem(), s.getCount(), Integer::sum);
        }
        if (grouped.isEmpty()) return;
        List<Map.Entry<Item, Integer>> entries = new ArrayList<>(grouped.entrySet());
        int shown = Math.min(entries.size(), MAX_TOOLTIP_LINES);
        for (int i = 0; i < shown; i++) {
            MutableComponent line = Component.literal("  ").append(rep.get(entries.get(i).getKey()).getHoverName());
            if (entries.get(i).getValue() > 1) line.append(Component.literal(" x " + entries.get(i).getValue()).withStyle(style -> style.withColor(11184810)));
            tooltip.add(line);
        }
        if (entries.size() > shown) {
            tooltip.add(Component.translatable("container.shulkerBox.more", entries.size() - shown).withStyle(style -> style.withColor(8947848)));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return com.SaintAlfonzo.creativedelight.hearth.machine.client.CrateItemRenderer.instance();
            }
        });
    }
}
