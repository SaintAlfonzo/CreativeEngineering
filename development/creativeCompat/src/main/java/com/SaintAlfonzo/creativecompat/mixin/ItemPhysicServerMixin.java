package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.itemphysic.AllurementItemCurses;
import com.SaintAlfonzo.creativecompat.itemphysic.ItemBurnTransform;
import com.SaintAlfonzo.creativecompat.itemphysic.ItemPhysicSplit;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.creativecore.common.util.type.list.SortingList;
import team.creative.itemphysic.ItemPhysic;
import team.creative.itemphysic.ItemPhysicConfig;
import team.creative.itemphysic.server.ItemPhysicServer;

/**
 * Splits ItemPhysic's single "burningItems" bucket into a fire-vs-lava-aware decision and
 * makes its "swimmingItems" bucket additive, both driven by {@link ItemPhysicSplit}
 * (populated from KubeJS).
 *
 * <p>ItemPhysic funnels every relevant decision through one call:
 * {@code SortingList.canPass(ItemStack)} — {@code burningItems.canPass(...)} in
 * {@code fireImmune}, {@code hurt} and {@code updatePre}, and {@code swimmingItems.canPass(...)}
 * in {@code updatePre} and {@code updateFluidHeightAndDoFluidPushing}. {@code hurt} also calls
 * {@code undestroyableItems.canPass(...)}. Each redirect below re-dispatches on the list
 * identity so only the two lists we care about are touched; everything else keeps ItemPhysic's
 * own answer.
 *
 * <p>{@code canPass == true} always means "this hazard is allowed to destroy the item".
 *
 * <p>{@code ItemPhysicServer} / {@code SortingList} method names are not obfuscated at runtime,
 * so {@code remap = false}; the {@code ItemStack} in the {@code @At} descriptor is a class ref
 * (unchanged under Mojmap-runtime Forge) and needed only to pick {@code canPass(ItemStack)}
 * over the {@code canPass(Object)} overload.
 */
@Mixin(value = ItemPhysicServer.class, remap = false)
public class ItemPhysicServerMixin {

    private static final String CAN_PASS =
            "Lteam/creative/creativecore/common/util/type/list/SortingList;canPass(Lnet/minecraft/world/item/ItemStack;)Z";

    /**
     * Allurement's Curse of Ascension / Fleeting hook sits in the vanilla gravity branch that
     * ItemPhysic bypasses; run them after ItemPhysic's own gravity instead.
     */
    @Inject(method = "updatePre", at = @At("TAIL"), remap = false)
    private static void creativeengineering$allurementCurses(ItemEntity item, RandomSource rand, CallbackInfo ci) {
        AllurementItemCurses.tick(item);
    }

    @Redirect(method = "fireImmune", at = @At(value = "INVOKE", target = CAN_PASS, remap = false), remap = false)
    private static boolean creativeengineering$fireImmune(SortingList list, ItemStack stack, ItemEntity item) {
        return creativeengineering$route(list, stack, item);
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = CAN_PASS, remap = false), remap = false)
    private static boolean creativeengineering$hurt(SortingList list, ItemStack stack,
                                                   ItemEntity item, DamageSource source, float amount) {
        return creativeengineering$route(list, stack, item);
    }

    @Redirect(method = "updatePre", at = @At(value = "INVOKE", target = CAN_PASS, remap = false), remap = false)
    private static boolean creativeengineering$updatePre(SortingList list, ItemStack stack,
                                                        ItemEntity item, RandomSource rand) {
        return creativeengineering$route(list, stack, item);
    }

    @Redirect(method = "updateFluidHeightAndDoFluidPushing", at = @At(value = "INVOKE", target = CAN_PASS, remap = false), remap = false)
    private static boolean creativeengineering$fluidPush(SortingList list, ItemStack stack,
                                                        ItemEntity item, TagKey<Fluid> fluidTag, double scale) {
        return creativeengineering$route(list, stack, item);
    }

    /**
     * {@code ItemPhysicServer.hurt} destroys a dropped item with
     * {@code stack.onDestroyed(item); item.discard();} once its health hits 0. When that
     * killing blow is a fire/lava source, give {@link ItemBurnTransform} a chance to leave
     * something behind (dirty glass, ash) or go out with a bang (tnt) before the entity is
     * discarded. {@code discard()} is matched by SRG name with {@code remap = false} (no
     * refmap in this project). A {@code @Redirect} handler takes the redirected call's
     * receiver followed by <em>all</em> of the enclosing method's params, so the signature is
     * {@code (receiver, item, source, amount)} — {@code receiver} and {@code item} are the
     * same {@link ItemEntity} here.
     */
    @Redirect(
            method = "hurt",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/entity/item/ItemEntity;m_146870_()V",
                     remap = false),
            remap = false
    )
    private static void creativeengineering$onBurnDeath(ItemEntity discardTarget,
                                                       ItemEntity item, DamageSource source, float amount) {
        if (source != null && source.isFire() && item.level != null && !item.level.isClientSide
                && !item.getTags().contains(ItemBurnTransform.PRODUCT_TAG)) {
            try {
                ItemStack stack = item.getItem();
                ItemBurnTransform.Rule rule = ItemBurnTransform.ruleFor(stack);
                if (rule != null) {
                    ItemBurnTransform.apply(item, stack.copy(), rule);
                }
            } catch (Throwable t) {
                // never let a transform failure stop the item from being cleaned up
            }
        }
        discardTarget.discard();
    }

    @Unique
    private static boolean creativeengineering$route(SortingList list, ItemStack stack, ItemEntity item) {
        ItemPhysicConfig.General cfg = ItemPhysic.CONFIG.general;

        if (list == cfg.burningItems) {
            Boolean decision = ItemPhysicSplit.burnDecision(stack, ItemPhysicSplit.touchingLava(item));
            return (decision != null) ? decision.booleanValue()
                                      : list.canPass(stack);          // BURN — honour ItemPhysic's list
        }

        if (list == cfg.swimmingItems) {
            if (ItemPhysicSplit.forcedSink(stack)) return false;   // physics/sinks tag wins
            return ItemPhysicSplit.isFloat(stack) || list.canPass(stack);
        }

        // undestroyableItems, or any list we don't manage — untouched
        return list.canPass(stack);
    }
}
