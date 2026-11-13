// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.general.ceilingtile.lamp;

import com.simibubi.create.content.schematics.requirement.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.electricity.base.ElectricBlock;
import org.patryk3211.powergrid.electricity.base.IDecoratedTerminal;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
import org.patryk3211.powergrid.electricity.base.terminals.BlockStateTerminalCollection;
import org.patryk3211.powergrid.electricity.light.bulb.ILightBulb;
import org.patryk3211.powergrid.electricity.wire.powercord.IAcceptCord;
import org.patryk3211.powergrid.general.ceilingtile.CeilingBlock;

import java.util.ArrayList;
import java.util.List;

public class CeilingTileLampBlock extends ElectricBlock implements IBE<CeilingTileLampBlockEntity>, IAcceptCord, CeilingBlock, ISpecialBlockItemRequirement {
    public static final EnumProperty<State> STATE = EnumProperty.create("state", State.class);

    private static final VoxelShape SHAPE = Shapes.or(
            box(0, 0, 0, 16, 2, 16),
            box(1, 2, 1, 15, 3, 15),
            box(2, 3, 2, 14, 9, 14)
    );

    private static final TerminalBoundingBox[] TERMINALS = new TerminalBoundingBox[] {
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 3, 9, 3, 6, 11, 6),
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 10, 9, 10, 13, 11, 13)
    };

    public CeilingTileLampBlock(Properties properties) {
        super(properties.lightLevel(state -> switch(state.getValue(STATE)) {
            case LAMP_LOW_POWER -> ILightBulb.LIGHT_LEVEL_LOW_POWER;
            case LAMP_ON -> ILightBulb.LIGHT_LEVEL_FULL_POWER;
            default -> 0;
        }));
        registerDefaultState(defaultBlockState().setValue(STATE, State.EMPTY));
        setTerminalCollection(BlockStateTerminalCollection.builder(this)
                .forAllStates(state -> TERMINALS)
                .withShapeMapper(state -> SHAPE)
                .build());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(placementHelper(state, level, pos, player, hand, hit) == InteractionResult.SUCCESS)
            return InteractionResult.SUCCESS;
        var stack = player.getMainHandItem();
        if(stack.isEmpty() || stack.getItem() instanceof ILightBulb) {
            return onBlockEntityUse(level, pos, be ->
                    be.replaceBulb(player, hand, stack)
                            ? InteractionResult.SUCCESS
                            : InteractionResult.FAIL);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        // Port (B32): the bulb comes back with the factory light (upstream deleted it).
        var player = context.getPlayer();
        var level = context.getLevel();
        if(!level.isClientSide && player != null && !player.isCreative()
                && level.getBlockEntity(context.getClickedPos()) instanceof CeilingTileLampBlockEntity lamp) {
            var bulb = lamp.getBulbState();
            if(bulb != null && !bulb.isBurned())
                player.addItem(new ItemStack(bulb.getItem(), 1));
        }
        return removeCeilingAttachment(context, ModdedBlocks.FACTORY_LIGHT.asStack());
    }

    // Port (B32): drop the bulb, as LightFixtureBlock does (upstream's lamp lost it when broken).
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder params) {
        var be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if(be instanceof CeilingTileLampBlockEntity lamp) {
            var bulb = lamp.getBulbState();
            if(bulb != null && !bulb.isBurned()) {
                var drops = new ArrayList<>(super.getDrops(state, params));
                drops.add(new ItemStack(bulb.getItem(), 1));
                return drops;
            }
        }
        return super.getDrops(state, params);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATE);
    }

    @Override
    public Class<CeilingTileLampBlockEntity> getBlockEntityClass() {
        return CeilingTileLampBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CeilingTileLampBlockEntity> getBlockEntityType() {
        return ModdedBlockEntities.CEILING_TILE_LAMP.get();
    }

    @Override
    public boolean renderPlug() {
        return true;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState blockState, @Nullable BlockEntity blockEntity) {
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, List.of(
                ModdedBlocks.CEILING_TILE.asStack(),
                ModdedBlocks.FACTORY_LIGHT.asStack()
        ));
    }

    public enum State implements StringRepresentable {
        EMPTY,
        LAMP,
        LAMP_LOW_POWER,
        LAMP_ON;

        @Override
        public String getSerializedName() {
            return switch(this) {
                case EMPTY -> "empty";
                case LAMP -> "lamp";
                case LAMP_LOW_POWER -> "lamp_low_power";
                case LAMP_ON -> "lamp_on";
            };
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ModdedBlocks.FACTORY_LIGHT.asStack();
    }
}
