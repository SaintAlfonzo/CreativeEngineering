// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 registries/BlockEntityRegistry.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.registries;

import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntity;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockEntityRegistry {
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "yuushya");
   public static final Supplier<BlockEntityType<ShowBlockEntity>> SHOW_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
      "showblockentity", () -> Builder.of(ShowBlockEntity::new, new Block[]{(Block)BlockRegistry.SHOW_BLOCK.get()}).build(null)
   );
   public static final Supplier<BlockEntityType<ItemBlockEntity>> ITEM_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
      "itemblockentity", () -> Builder.of(ItemBlockEntity::new, new Block[]{(Block)BlockRegistry.ITEM_BLOCK.get()}).build(null)
   );
   // PHASE-B: TEXT_BLOCK_ENTITY ("textblockentity") is registered with the Text Block (plan section 4, B0).

   public static void register(IEventBus bus) {
      BLOCK_ENTITY_TYPES.register(bus);
   }
}
