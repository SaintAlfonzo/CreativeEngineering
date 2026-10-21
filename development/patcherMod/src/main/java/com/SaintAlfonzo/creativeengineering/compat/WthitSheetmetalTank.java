package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.CEConfig;

import blusunrize.immersiveengineering.common.blocks.metal.SheetmetalTankBlockEntity;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import mcp.mobius.waila.api.data.FluidData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * WTHIT plugin (server side only): an Immersive Engineering sheetmetal tank's contents on every part. IE's own line was
 * written for Jade; under WTHIT only the two fluid ports (bottom IO, top input) expose a Forge fluid handler, so
 * WTHIT's fluid bar showed on those two blocks and nowhere else. This supplies the master tank as WTHIT
 * {@link FluidData} at priority 1000, ahead of WTHIT's Forge capability provider (2000), so every part shows the same
 * bar (and the ports don't show it twice). WTHIT's own "Fluid" options still apply; off with
 * {@code [wthit] sheetmetalTank}. feature_writeup/wthit_lines.md 2.7.
 */
public final class WthitSheetmetalTank implements IWailaCommonPlugin, IDataProvider<SheetmetalTankBlockEntity> {

    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.blockData(this, SheetmetalTankBlockEntity.class, 1000);
    }

    @Override
    public void appendData(IDataWriter data, IServerAccessor<SheetmetalTankBlockEntity> accessor, IPluginConfig config) {
        SheetmetalTankBlockEntity master = accessor.getTarget().master();
        if (master == null || !CEConfig.WTHIT.sheetmetalTank.get()) {
            return;
        }
        FluidTank tank = master.tank;
        FluidStack fluid = tank.getFluid();
        data.add(FluidData.class, res -> res.add(FluidData.of(FluidData.Unit.MILLIBUCKETS, 1)
                .add(fluid.getFluid(), fluid.getTag(), fluid.getAmount(), tank.getCapacity())));
    }
}
