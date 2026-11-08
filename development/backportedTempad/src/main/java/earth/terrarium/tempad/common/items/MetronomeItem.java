// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/MetronomeItem.kt. Translated to Java
// for Forge 1.19.2: the properties are passed in (ModItems adds the creative tab).
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.common.block.MetronomeBe;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.registries.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MetronomeItem extends BlockItem {
    public MetronomeItem(Properties properties) {
        super(ModBlocks.metronome.get(), properties.stacksTo(1));
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        if (level.getBlockEntity(pos) instanceof MetronomeBe blockEntity && player != null) {
            blockEntity.owner = player.getGameProfile();
            blockEntity.initialChronons = ModComponents.getChrononContent(stack);
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ChrononHandler chronons = ChrononHandler.of(stack);
        return Optional.ofNullable(chronons == null ? null : chronons.tooltip());
    }
}
