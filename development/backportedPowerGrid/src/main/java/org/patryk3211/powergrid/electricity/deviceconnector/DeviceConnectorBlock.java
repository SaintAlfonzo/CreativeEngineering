/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.electricity.deviceconnector;

import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.LangBuilder;
import com.simibubi.create.foundation.utility.VoxelShaper;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import com.mojang.math.Vector3f;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.electricity.base.IDecoratedTerminal;
import org.patryk3211.powergrid.electricity.base.Rotation4ElectricBlock;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
import org.patryk3211.powergrid.electricity.base.terminals.BlockStateTerminalCollection;
import org.patryk3211.powergrid.electricity.febridge.IFEBridgeHandler;
import org.patryk3211.powergrid.electricity.info.IHaveElectricProperties;
import org.patryk3211.powergrid.electricity.info.Resistance;
import org.patryk3211.powergrid.electricity.wire.powercord.AutoCordEndpoint;
import org.patryk3211.powergrid.electricity.wire.powercord.IAcceptCord;
import org.patryk3211.powergrid.utility.Lang;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DeviceConnectorBlock extends Rotation4ElectricBlock implements IBE<DeviceConnectorBlockEntity>, IAcceptCord, IHaveElectricProperties {
    public static final BooleanProperty POLARIZED = BooleanProperty.create("polarized");

    private final TerminalBoundingBox[] TERMINALS_DOWN = new TerminalBoundingBox[] {
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 5.5, 1, 1.5, 10.5, 4, 4.5),
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 5.5, 1, 11.5, 10.5, 4, 14.5)
    };

    // This assumes that terminal 0 is positive and terminal 1 is negative
    // which holds true for all blocks added by this mod.
    private final TerminalBoundingBox[] POLARIZED_TERMINALS_DOWN = new TerminalBoundingBox[] {
            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 5.5, 1, 1.5, 10.5, 4, 4.5)
                    .withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.NEGATIVE, 5.5, 1, 11.5, 10.5, 4, 14.5)
                    .withColor(IDecoratedTerminal.BLUE)
    };

    private static final VoxelShape SHAPE_DOWN = box(4.5, 0, 3.5, 11.5, 4, 12.5);
    private static final VoxelShape SHAPE_DOWN_2 = box(3.5, 0, 4.5, 12.5, 4, 11.5);

    public DeviceConnectorBlock(BlockBehaviour.Properties settings) {
        super(settings);

        var shaper = VoxelShaper.forDirectional(SHAPE_DOWN, Direction.DOWN);
        var shaper2 = VoxelShaper.forDirectional(SHAPE_DOWN_2, Direction.DOWN);
        setTerminalCollection(BlockStateTerminalCollection.builder(this)
                .forAllStates(state -> BlockStateTerminalCollection.each(
                        state.getValue(POLARIZED) ? POLARIZED_TERMINALS_DOWN : TERMINALS_DOWN,
                        terminal -> {
                            var facing = state.getValue(FACING);
                            terminal = switch(facing) {
                                case DOWN -> terminal;
                                case UP -> terminal.rotateAroundX(180);
                                case EAST -> terminal.rotateAroundZ(90).rotateAroundY(180);
                                case WEST -> terminal.rotateAroundZ(90);
                                case NORTH -> terminal.rotateAroundZ(90).rotateAroundY(90);
                                case SOUTH -> terminal.rotateAroundZ(90).rotateAroundY(-90);
                            };
                            var rotation = state.getValue(ROTATION);
                            terminal = terminal.rotate(facing.getAxis(), 90 * rotation - 90);
                            return terminal;
                        })
                )
                .withShapeMapper(state -> {
                    var facing = state.getValue(FACING);
                    var rotation = state.getValue(ROTATION);
                    boolean axis_along = rotation % 2 == 1;
                    var prov = (axis_along ^ facing.getAxis() == Direction.Axis.Y) ? shaper2 : shaper;
                    return prov.get(facing);
                })
                .build());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POLARIZED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var facing = ctx.getClickedFace().getOpposite();
        var neighbor = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(facing));
        var polarized = neighbor.getBlock() instanceof IAcceptConnector acceptor && acceptor.isPolarized();

        return super.getStateForPlacement(ctx)
                .setValue(POLARIZED, polarized);
    }

    // @ExpectPlatform inlined (D2): forge implementation in DeviceConnectorBlockImpl
    public static boolean hasEnergyStorage(Level world, BlockPos pos, Direction side) {
        return org.patryk3211.powergrid.electricity.deviceconnector.forge.DeviceConnectorBlockImpl.hasEnergyStorage(world, pos, side);
    }

    public static VoxelShape makeCheckShape(Direction side) {
        // Port: plain doubles instead of a joml Vector3f (1.19.2 has com.mojang.math.Vector3f without public fields).
        double minX = 6 / 16d, minY = 6 / 16d, minZ = 6 / 16d;
        double maxX = 10 / 16d, maxY = 10 / 16d, maxZ = 10 / 16d;

        switch(side.getAxis()) {
            case X -> { minX = 0; maxX = 1; }
            case Y -> { minY = 0; maxY = 1; }
            case Z -> { minZ = 0; maxZ = 1; }
        }

        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static boolean canSupport(LevelReader world, BlockPos pos, BlockState state, Direction side) {
        var connectorShape = makeCheckShape(side);
        var shape = state.getCollisionShape(world, pos);
        // Check if side of the connector is covered by the supporting block's shape.
        return Shapes.blockOccudes(connectorShape, shape, side.getOpposite());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        var facing = state.getValue(FACING);
        var neighborPos = pos.relative(facing);
        var neighbor = world.getBlockState(neighborPos);
        if(neighbor.getBlock() == this)
            return false;
        if(world instanceof Level world1) {
            if(hasEnergyStorage(world1, neighborPos, facing.getOpposite()))
                return canSupport(world, neighborPos, neighbor, facing.getOpposite());
        }
        // Port: the TFMG proxy branch is removed (TFMG compat not ported).
        if(!(neighbor.getBlock() instanceof IAcceptConnector acceptor))
            return false;
        return acceptor.canConnect(world, neighborPos, neighbor, facing.getOpposite());
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
        if(!canSurvive(state, world, pos))
            world.destroyBlock(pos, true);
    }

    @Override
    public Class<DeviceConnectorBlockEntity> getBlockEntityClass() {
        return DeviceConnectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DeviceConnectorBlockEntity> getBlockEntityType() {
        return ModdedBlockEntities.DEVICE_CONNECTOR.get();
    }

    @Override
    public @Nullable AutoCordEndpoint getEndpoint(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        var facing = state.getValue(FACING);

        var center = Vec3.atCenterOf(pos);
        var normal = facing.getNormal();
        var point = center.add(normal.getX() * 0.3125, normal.getY() * 0.3125, normal.getZ() * 0.3125);

        return new AutoCordEndpoint(context.getClickedPos(), 0, 1, point,
                renderPlug() ? context.getClickedFace() : null);
    }

    @Override
    public void appendProperties(ItemStack stack, Player player, List<Component> tooltip) {
        tooltip.add(Lang.translateDirect("tooltip.device_connector.header"));
        Resistance.maximum(IFEBridgeHandler.MAXIMUM_RESISTANCE, player, tooltip);

        Lang.translate("tooltip.device_connector.rate").style(ChatFormatting.GRAY).addTo(tooltip);
        LangBuilder valueText = Lang.builder().add(Component.nullToEmpty(" "));
        valueText.add(Lang.number(ModdedConfigs.server().electricity.forgeEnergyPerWatt.getF()))
                .add(Component.nullToEmpty(" "))
                .add(Lang.translateDirect("tooltip.device_connector.fe_w"));
        valueText.style(ChatFormatting.DARK_AQUA).addTo(tooltip);

        Lang.translate("tooltip.device_connector.max_rate").style(ChatFormatting.GRAY).addTo(tooltip);
        valueText = Lang.builder().add(Component.nullToEmpty(" "));
        valueText.add(Lang.number(ModdedConfigs.server().electricity.forgeEnergyPerVolt.getF()))
                .add(Component.nullToEmpty(" "))
                .add(Lang.translateDirect("tooltip.device_connector.fe_v"));
        valueText.style(ChatFormatting.DARK_AQUA).addTo(tooltip);
    }
}
