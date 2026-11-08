// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/TimeTwisterItem.kt. Translated to Java
// for Forge 1.19.2: sendToClient -> ModNetworking.sendToPlayer; player.travelHistory -> TempadPlayerData.
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.context.InventoryContext;
import earth.terrarium.tempad.common.data.TempadPlayerData;
import earth.terrarium.tempad.common.network.s2c.OpenTimeTwister;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TimeTwisterItem extends ChrononItem {
    public TimeTwisterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide) {
            InventoryContext ctx = new InventoryContext(player, TempadItem.getSlot(usedHand, player));
            if (!player.isCreative() && ModComponents.getChrononContent(ctx.getStack()) < 1000) {
                player.displayClientMessage(Component.translatable("message.tempad.not_enough_chronons")
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Tempad.ORANGE.getValue() & 0xFFFFFF))), true);
                return InteractionResultHolder.fail(player.getItemInHand(usedHand));
            }
            if (player instanceof ServerPlayer serverPlayer) {
                ModNetworking.sendToPlayer(new OpenTimeTwister(TempadPlayerData.get(player).getTravelHistory().getRelevantHistory(), ctx.getHolder()),
                        serverPlayer);
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }
}
