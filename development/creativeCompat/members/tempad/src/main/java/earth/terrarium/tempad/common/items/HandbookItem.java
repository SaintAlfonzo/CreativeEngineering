// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/HandbookItem.kt. Translated to Java for Forge 1.19.2
// (item tempad:knowledge_projector): the client call goes through DistExecutor so a dedicated server never loads the screen.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.client.TempadClientExtras;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class HandbookItem extends Item {
    public HandbookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide) DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> TempadClientExtras::openGuide);
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }
}
