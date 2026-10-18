package net.satisfy.vinery.core.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.satisfy.vinery.core.registry.MobEffectRegistry;

public class EventHandler {
    /**
     * Party effect: every hit spawns a firework with a random colour at the target. Returns true to cancel the hit
     * (targets that aren't mobs or players). Port: called from Forge's AttackEntityEvent.
     */
    public static boolean onAttack(Player player, Level level, Entity target) {
        if (!(target instanceof LivingEntity entity) || !player.hasEffect(MobEffectRegistry.get(MobEffectRegistry.PARTY_EFFECT))) {
            return false;
        }

        if (!level.isClientSide()) {
            int color = level.random.nextInt(0xFFFFFF);
            ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
            CompoundTag explosion = new CompoundTag();
            explosion.putByte("Type", (byte) FireworkRocketItem.Shape.SMALL_BALL.getId());
            explosion.putIntArray("Colors", new int[]{color});
            ListTag explosions = new ListTag();
            explosions.add(explosion);
            CompoundTag fireworks = new CompoundTag();
            fireworks.put("Explosions", explosions);
            fireworks.putByte("Flight", (byte) 0);
            fireworkStack.getOrCreateTag().put("Fireworks", fireworks);
            FireworkRocketEntity fireworkRocket = new FireworkRocketEntity(level, fireworkStack, entity);
            fireworkRocket.setAirSupply(0);
            level.addFreshEntity(fireworkRocket);
        }
        return !(target instanceof Player || target instanceof Mob);
    }
}
