package net.satisfy.vinery.core.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.satisfy.foundation.block.FruitLeavesBlock;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.platform.PlatformHelper;

public class DarkCherryLeavesBlock extends FruitLeavesBlock {
    public static final BooleanProperty CAN_GROW_CHERRIES = BooleanProperty.create("can_grow_cherries");
    public static final BooleanProperty HAS_CHERRIES = BooleanProperty.create("has_cherries");

    public DarkCherryLeavesBlock(Properties settings) {
        super(settings, false);
    }

    @Override
    protected BooleanProperty canGrowFruitProperty() {
        return CAN_GROW_CHERRIES;
    }

    @Override
    protected BooleanProperty hasFruitProperty() {
        return HAS_CHERRIES;
    }

    @Override
    protected Item fruitItem() {
        return ObjectRegistry.CHERRY.get();
    }

    @Override
    protected double growthChance() {
        return PlatformHelper.getCherryGrowthChance();
    }

    /** Port (D10): 1.3.10's chance that picking cherries also drops a rotten cherry (1.5.5 has no source for it). */
    @Override
    public net.minecraft.world.InteractionResult useWithoutItem(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hit) {
        boolean ripe = state.getValue(HAS_CHERRIES) && state.getValue(AGE) == 3;
        net.minecraft.world.InteractionResult result = super.useWithoutItem(state, level, pos, player, hit);
        if (ripe && !level.isClientSide() && level.random.nextDouble() < PlatformHelper.getRottenCherryChance()) {
            net.satisfy.foundation.util.LibUtil.popResourceFromFace(level, pos, hit.getDirection(), new net.minecraft.world.item.ItemStack(ObjectRegistry.ROTTEN_CHERRY.get()));
        }
        return result;
    }
}
