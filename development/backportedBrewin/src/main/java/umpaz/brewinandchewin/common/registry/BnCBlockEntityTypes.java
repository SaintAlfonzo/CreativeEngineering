package umpaz.brewinandchewin.common.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;

public class BnCBlockEntityTypes {
   public static final BlockEntityType<KegBlockEntity> KEG = Builder.of(BrewinAndChewin.getHelper().supplyBlockEntity(), BnCBlocks.KEG).build(null);
   public static final BlockEntityType<CoasterBlockEntity> COASTER = Builder.of(CoasterBlockEntity::new, BnCBlocks.COASTER).build(null);

   public static void registerAll(RegisterHelper<BlockEntityType<?>> helper) {
      helper.register(BrewinAndChewin.asResource("keg"), KEG);
      helper.register(BrewinAndChewin.asResource("coaster"), COASTER);
   }
}
