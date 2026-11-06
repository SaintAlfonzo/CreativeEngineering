package com.SaintAlfonzo.creativecompat.cats;

import com.github.alexthe666.citadel.server.entity.IComandableMob;
import net.cozy.cozysimprovedcats.entity.custom.ImprovedCatEntity;
import net.cozy.cozysimprovedcats.entity.state.CatMode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Cozy's Improved Cats in the pack (feature_writeup/cozy_cats.md section 10). Only loaded when Cozy is.
 *
 * <ul>
 *   <li>Cats have no gender: the config turns Cozy's gender system off ({@code genderSystemEnabled = false}), which
 *       leaves a "Gender: Unknown" line in the collar tooltip and the stats chat; {@link #isGenderLine} drops both
 *       ({@code mixin/CozyMessageGenderMixin}, {@link #onTooltip}).</li>
 *   <li>Cozy owns cat modes (decision D2): Domestication Innovation's follow / wander / stay right-click is skipped on
 *       Cozy cats ({@code mixin/CatCozyCommandMixin}), so a click toggles sitting as vanilla does and Cozy's Follow,
 *       Wander, Guard and Stay (Cat Bed) are the only modes. A DI command saved on a cat before that is cleared on
 *       load ({@link #clearDiCommand}).</li>
 *   <li>No ancient fish: Cozy's raw and cooked Ancient Cod and Salmon merge into the vanilla fish (Creative Delight,
 *       {@code kubejs_startup/cozys_improved_cats/item_merges.js}), the config turns off the fishing swap and the mob
 *       drops and lets vanilla cod and salmon tame, and {@link #onTrades} drops the two fisherman trades (Cozy has no
 *       switch for them; its trade validator refuses 0 uses).</li>
 *   <li>Feather on a Stick: a cat in Stay mode ignores the feather, as a sitting vanilla cat does ({@link #inStay}).</li>
 * </ul>
 */
public final class CozyCats {

    private static final String COZY_TRADES = "net.cozy.cozysimprovedcats.util.ModVillagerTrades";
    private static final String GENDER_KEY = "tooltip.cozys-improved-cats.cat_collar.gender";

    private CozyCats() {}

    public static boolean isCozy(Entity entity) {
        return entity instanceof ImprovedCatEntity;
    }

    /** Cozy's Stay mode (a home is required for it; without one Cozy falls back to Follow). */
    public static boolean inStay(Entity entity) {
        return entity instanceof ImprovedCatEntity cat && cat.hasReadableData()
                && cat.featureState().mode() == CatMode.STAY;
    }

    /** DI command 0 (follow, DI's default) on a Cozy cat, so DI's follow goal mixin never holds it in DI stay / wander. */
    static void clearDiCommand(Cat cat) {
        if (cat instanceof ImprovedCatEntity && cat instanceof IComandableMob commandable && commandable.getCommand() != 0) {
            commandable.setCommand(0);
        }
    }

    public static boolean isGenderLine(Component line) {
        return line != null && line.getContents() instanceof TranslatableContents t && GENDER_KEY.equals(t.getKey());
    }

    /** Client: the collar's "Gender:" line (Cozy adds it on the normal-priority tooltip event). */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        event.getToolTip().removeIf(CozyCats::isGenderLine);
    }

    /** Both sides' trade setup: Cozy's ancient fish listings are lambdas declared in ModVillagerTrades. */
    public static void onTrades(VillagerTradesEvent event) {
        event.getTrades().values().forEach(list ->
                list.removeIf(listing -> listing.getClass().getName().startsWith(COZY_TRADES)));
    }
}
