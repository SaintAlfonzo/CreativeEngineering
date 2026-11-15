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
package org.patryk3211.powergrid.electricity.wireconnector;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;
import org.patryk3211.powergrid.electricity.base.DirectionalElectricBlock;
import org.patryk3211.powergrid.electricity.base.ElectricBlock;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AbstractConnectorBlock extends DirectionalElectricBlock implements IBE<ConnectorBlockEntity> {
    public AbstractConnectorBlock(Properties settings) {
        super(settings);
    }

    @Override
    public Class<ConnectorBlockEntity> getBlockEntityClass() {
        return ConnectorBlockEntity.class;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getClickedFace().getOpposite());
    }

    // Port B37 (deliberate deviation, upstream floats): the connector needs the block it is attached to (FACING points
    // at it). A sturdy face centre, as upstream's own PotatoBatteryBlock checks, or any Power Grid electric block.
    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        var facing = state.getValue(FACING);
        var supportPos = pos.relative(facing);
        if(world.getBlockState(supportPos).getBlock() instanceof ElectricBlock)
            return true;
        return canSupportCenter(world, supportPos, facing.getOpposite());
    }

    // Port B37: removing the support breaks the connector (dropped as a block); onRemove detaches and drops its wires
    // as on a hand break.
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        return direction == state.getValue(FACING) && !canSurvive(state, world, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockEntityType<? extends ConnectorBlockEntity> getBlockEntityType() {
        return ModdedBlockEntities.WIRE_CONNECTOR.get();
    }
}
