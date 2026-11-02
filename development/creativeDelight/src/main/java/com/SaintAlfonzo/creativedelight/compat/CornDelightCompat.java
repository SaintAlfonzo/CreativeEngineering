package com.SaintAlfonzo.creativedelight.compat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

/**
 * Corn Delight 1.2.11 behaviour on the installed 1.0.3 (feature_writeup/corn_delight.md 2.3, 2.6). The items are KubeJS
 * ({@code creativeengineering:cob_pipe}, Creative Delight startup corn_delight/corn_items.js); this is the code they
 * need.
 * <ul>
 * <li>Cob Pipe: goes in the vanilla head slot ({@code mixin.MobCobPipeSlotMixin}). A right click puts it in a Curios
 * mouth or face slot if the player has one, else the empty head slot, else a free Curios head slot (1.2.11: the head
 * curio only while a helmet is worn). Curios part: {@link CornDelightCurios}.</li>
 * <li>Villagers: corn joins the items they pick up and share, and counts as breeding food (1 point, the carrot's),
 * combined with the current {@code Villager.WANTED_ITEMS} / {@code FOOD_POINTS} the way the Vintage and Veggies ports
 * add theirs.</li>
 * </ul>
 * Registered only when Corn Delight is loaded.
 */
public final class CornDelightCompat {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation COB_PIPE = new ResourceLocation("creativeengineering", "cob_pipe");
    private static final ResourceLocation CORN = new ResourceLocation("corn_delight", "corn");

    /** The registered pipe, looked up once (Mob.getEquipmentSlotForItem asks for every stack). */
    private static volatile Item pipe;

    private CornDelightCompat() {}

    public static boolean isCobPipe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item p = pipe;
        if (p == null) {
            if (!COB_PIPE.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) return false;
            pipe = p = stack.getItem();
        }
        return stack.getItem() == p;
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!isCobPipe(stack)) return;
        Player player = event.getEntity();
        boolean curios = ModList.get().isLoaded("curios");
        boolean equipped = (curios && (CornDelightCurios.equip(player, "mouth", stack) || CornDelightCurios.equip(player, "face", stack)))
                || equipHead(player, stack)
                || (curios && CornDelightCurios.equip(player, "head", stack));
        if (!equipped) return;
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_GENERIC,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level.isClientSide));
    }

    private static boolean equipHead(Player player, ItemStack stack) {
        if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) return false;
        player.setItemSlot(EquipmentSlot.HEAD, stack.split(1));
        return true;
    }

    /**
     * Common setup, main thread (enqueueWork). The two fields are static final in vanilla (the Veggies and Vintage ports
     * make them public non-final by AT); written with {@code sun.misc.Unsafe} either way, as util.ModDisplayNames.
     */
    @SuppressWarnings("unchecked")
    public static void addVillagerFood() {
        Item corn = ForgeRegistries.ITEMS.getValue(CORN);
        if (corn == null || !CORN.equals(ForgeRegistries.ITEMS.getKey(corn))) {
            LOGGER.warn("[corn-delight] {} not registered; villager food unchanged", CORN);
            return;
        }
        try {
            Field wantedField = ObfuscationReflectionHelper.findField(Villager.class, "f_35372_");   // WANTED_ITEMS
            Field pointsField = ObfuscationReflectionHelper.findField(Villager.class, "f_35369_");   // FOOD_POINTS
            Set<Item> wanted = new HashSet<>((Set<Item>) wantedField.get(null));
            wanted.add(corn);
            Map<Item, Integer> points = new HashMap<>();
            points.put(corn, 1);
            points.putAll((Map<Item, Integer>) pointsField.get(null));   // a value already set elsewhere wins
            setStatic(wantedField, ImmutableSet.copyOf(wanted));
            setStatic(pointsField, ImmutableMap.copyOf(points));
            LOGGER.info("[corn-delight] corn is villager food ({} wanted items, {} food items)", wanted.size(), points.size());
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("[corn-delight] could not add corn to the villager food", e);
        }
    }

    private static void setStatic(Field field, Object value) throws ReflectiveOperationException {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) f.get(null);
        unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), value);
    }
}
