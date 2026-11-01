package umpaz.brewinandchewin.common.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.entity.container.KegTransfer;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.network.BnCPacket;

/**
 * JEI [+] on the keg (alcohol.md 10.3). Port: upstream sends the client's slot-by-slot plan; here only the recipe and
 * shift, and the server plans again on its own state ({@link KegTransfer}), so a stale or forged client can't move
 * anything the server wouldn't.
 */
public record JEITransferKegRecipeServerboundPacket(ResourceLocation recipe, boolean maxTransfer) implements BnCPacket {
   public JEITransferKegRecipeServerboundPacket(FriendlyByteBuf buf) {
      this(buf.readResourceLocation(), buf.readBoolean());
   }

   @Override
   public void encode(FriendlyByteBuf buf) {
      buf.writeResourceLocation(this.recipe);
      buf.writeBoolean(this.maxTransfer);
   }

   @Override
   public void handle(@Nullable ServerPlayer sender) {
      if (sender == null) {
         return;
      }
      sender.getServer().getRecipeManager().byKey(this.recipe)
         .filter(KegFermentingRecipe.class::isInstance)
         .map(KegFermentingRecipe.class::cast)
         .ifPresent(recipe -> KegTransfer.apply(sender, recipe, this.maxTransfer));
   }
}
