package net.satisfy.foundation.recipe.book;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: recipe clicked in the station recipe book, fill the inputs. */
public record PlaceRecipePacket(int containerId, ResourceLocation recipe, boolean max) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeResourceLocation(recipe);
        buf.writeBoolean(max);
    }

    public static PlaceRecipePacket decode(FriendlyByteBuf buf) {
        return new PlaceRecipePacket(buf.readVarInt(), buf.readResourceLocation(), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            handle(this, player);
        }
        context.get().setPacketHandled(true);
    }

    private static void handle(PlaceRecipePacket packet, ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu.containerId != packet.containerId || !(menu instanceof StationRecipeBookMenu book) || !menu.stillValid(player)) {
            return;
        }
        Recipe<?> recipe = player.getLevel().getRecipeManager().byKey(packet.recipe).orElse(null);
        if (recipe == null || !book.recipeBookTypes().contains(recipe.getType())) {
            return;
        }
        RecipePlacer.place(player.getInventory(), menu, book.recipeBookPlacementSlots(recipe), book.recipeBookPlacementIngredients(recipe), packet.max);
        menu.broadcastChanges();
    }
}
