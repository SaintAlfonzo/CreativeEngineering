package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.excavation.VeinUndoAccess;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import oreexcavation.handlers.MiningAgent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Ore Excavation's "Auto Pickup" only spawns a vein mine's drops at the player's feet. With it on, the drops now go
 * straight into the player's inventory, and only what doesn't fit is spawned there, so {@code /undo_excavation}
 * (which takes the exact drops back from the inventory) finds them. The target is the lambda in
 * {@code dropEverything} that spawns each item stack. Unobfuscated target, no refmap: {@code remap = false}.
 */
@Mixin(value = MiningAgent.class, remap = false)
public class OreExcavationMiningAgentMixin {

    @Shadow(remap = false)
    @Final
    public ServerPlayer player;

    @Redirect(method = "lambda$dropEverything$1", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean creativeengineering$dropsIntoInventory(Level level, Entity entity) {
        if (VeinUndoAccess.autoPickup() && entity instanceof ItemEntity item) {
            ItemStack stack = item.getItem().copy();
            player.getInventory().add(stack);
            if (stack.isEmpty()) return true;
            item.setItem(stack);
        }
        return level.addFreshEntity(entity);
    }
}
