// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 registries/BlockRegistry.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.registries;

import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.blockentity.itemblock.ItemBlock;
import com.yuushya.modelling.blockentity.showblock.ShowBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistry {
   public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "yuushya");
   public static final RegistryObject<Block> SHOW_BLOCK = BLOCKS.register(
      "showblock",
      () -> new ShowBlock(
            Properties.of(Material.METAL).noOcclusion().strength(4.0F).lightLevel(blockState -> (Integer)blockState.getValue(YuushyaBlockStates.LIT)), 0
         )
   );
   public static final RegistryObject<Block> ITEM_BLOCK = BLOCKS.register(
      "itemblock",
      () -> new ItemBlock(
            Properties.of(Material.METAL).noOcclusion().strength(4.0F).lightLevel(blockState -> (Integer)blockState.getValue(YuushyaBlockStates.LIT)), 1
         )
   );
   // PHASE-B: TEXT_BLOCK ("textblock", TextBlock) is registered with the Text Block (plan section 4, B0).

   public static void register(IEventBus bus) {
      BLOCKS.register(bus);
   }
}
