package com.SaintAlfonzo.creativecommon.compat;

import com.SaintAlfonzo.creativecommon.CommonConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITargetRedirector;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

/**
 * WTHIT plugin: invisible technical blocks (barrier, structure void, light) show in WTHIT only to players with the
 * permission level of {@code [wthit] operatorBlockLevel} (default 2, the level that can /give them). WTHIT's own
 * blacklist (config/waila/blacklist.json5) hides them from everyone by redirecting to the block behind, as a
 * redirect at priority 500; this redirect runs first and keeps the target for a permitted player. Everyone else
 * falls through to the blacklist. The client knows its own permission level (sent by the server on op changes).
 * Client-side only. Off with {@code [wthit] operatorBlocks}. feature_writeup/wthit_lines.md 2.13.
 */
public final class WthitOperatorBlocks implements IWailaClientPlugin, IBlockComponentProvider {

    /** Before WTHIT's core blacklist redirect (500). */
    private static final int PRIORITY = 400;

    private List<? extends String> cachedList;
    private Set<Block> blocks = Set.of();

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.redirect(this, Block.class, PRIORITY);
    }

    @Override
    public ITargetRedirector.Result redirect(ITargetRedirector redirect, IBlockAccessor accessor, IPluginConfig config) {
        if (!CommonConfig.WTHIT.operatorBlocks.get() || !blocks().contains(accessor.getBlock())) {
            return null;
        }
        Player player = accessor.getPlayer();
        return player.hasPermissions(CommonConfig.WTHIT.operatorBlockLevel.get()) ? redirect.toSelf() : null;
    }

    private Set<Block> blocks() {
        List<? extends String> list = CommonConfig.WTHIT.operatorBlockList.get();
        if (list != cachedList) {
            Set<Block> set = new HashSet<>();
            for (String id : list) {
                ResourceLocation rl = ResourceLocation.tryParse(id);
                if (rl != null && Registry.BLOCK.containsKey(rl)) {
                    set.add(Registry.BLOCK.get(rl));
                }
            }
            blocks = set;
            cachedList = list;
        }
        return blocks;
    }
}
