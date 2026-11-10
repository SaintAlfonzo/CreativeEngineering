package com.SaintAlfonzo.creativedelight.hearth.event;

import com.SaintAlfonzo.creativedelight.hearth.HHConfig;
import com.SaintAlfonzo.creativedelight.hearth.HHSounds;
import com.SaintAlfonzo.creativedelight.hearth.HHTags;
import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

/**
 * Animals drop one manure 30 to 60 s after a successful feeding or, for sheep, grazing (3.2). Upstream ticks a counter on
 * every living entity; here only the animals with a drop pending are checked, once a second. A pending drop is lost if
 * the animal unloads or the server stops, which costs at most one manure.
 */
public final class ManureDrops {
    private static final Map<Animal, Long> PENDING = new HashMap<>();
    private static int ticks;

    private ManureDrops() {
    }

    private static boolean canDrop(Animal animal) {
        return !animal.isBaby() && !animal.hasCustomName() && !animal.getType().is(HHTags.DOES_NOT_POOP);
    }

    public static void schedule(Animal animal) {
        if (animal.level.isClientSide || !HHConfig.MANURE_FROM_FED_ANIMALS.get() || !canDrop(animal)) return;
        PENDING.putIfAbsent(animal, animal.level.getGameTime() + 600L + animal.getRandom().nextInt(601));
    }

    /** Only food the animal will actually eat counts: upstream also fires on an animal still on its breeding cooldown. */
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().level.isClientSide || !(event.getTarget() instanceof Animal animal)) return;
        ItemStack held = event.getItemStack();
        if (animal.isFood(held) && !animal.isInLove() && animal.getAge() == 0 && animal.canFallInLove()) schedule(animal);
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty() || ++ticks % 20 != 0) return;
        PENDING.entrySet().removeIf(entry -> {
            Animal animal = entry.getKey();
            if (animal.isRemoved() || !animal.isAlive()) return true;
            if (animal.level.getGameTime() < entry.getValue()) return false;
            animal.spawnAtLocation(HHItems.MANURE.get());
            animal.level.playSound(null, animal.getX(), animal.getY(), animal.getZ(), HHSounds.FART.get(), SoundSource.NEUTRAL,
                    0.5F, 0.5F + animal.getRandom().nextFloat() * 1.5F);
            return true;
        });
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }
}
