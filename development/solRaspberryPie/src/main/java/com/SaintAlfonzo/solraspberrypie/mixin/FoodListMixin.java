package com.SaintAlfonzo.solraspberrypie.mixin;

import com.SaintAlfonzo.solraspberrypie.data.GateFoods;
import com.SaintAlfonzo.solraspberrypie.logic.Drinks;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.tarinoita.solsweetpotato.tracking.FoodList;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Drinks leave the food diet (7): with shouldForbiddenCount on, Apple Pie would still age every remembered food and
 * bump foodsEaten for a blacklisted drink. Returning early keeps the food memory and the counter untouched.
 * <p>
 * Also keeps the unique-food gate counter ({@link GateFoods}, 12.4): filled on a counted eat, cleared with Apple Pie's
 * own counter, saved beside it in Apple Pie's NBT (key {@code solrp_gateFoods}). A list saved before the key existed is
 * backfilled with the remembered foods eaten since the counter last reset.
 */
@Mixin(value = FoodList.class, remap = false)
public abstract class FoodListMixin implements GateFoods {
    @Unique
    private static final String SOLRP_KEY = "solrp_gateFoods";

    @Shadow
    private int foodsEaten;

    @Shadow
    @Final
    private Map<FoodInstance, Integer> uniqueFoods;

    @Unique
    private final Set<ResourceLocation> solrp$gate = new LinkedHashSet<>();

    @Override
    public Set<ResourceLocation> solrp$gateFoods() {
        return solrp$gate;
    }

    @Inject(method = "addFood(Lnet/minecraft/world/item/Item;)V", at = @At("HEAD"), cancellable = true)
    private void solrp$skipDrinks(Item item, CallbackInfo ci) {
        if (Drinks.isDrink(item)) ci.cancel();
    }

    @Inject(method = "addFood(Lnet/minecraft/world/item/Item;)V", at = @At("RETURN"))
    private void solrp$countUnique(Item item, CallbackInfo ci) {
        if (!SOLSweetPotatoConfig.shouldCount(item)) return;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id != null) solrp$gate.add(id);
    }

    @Inject(method = "resetFoodsEaten", at = @At("RETURN"))
    private void solrp$resetUnique(CallbackInfo ci) {
        solrp$gate.clear();
    }

    @Inject(method = "serializeNBT()Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void solrp$save(CallbackInfoReturnable<CompoundTag> cir) {
        ListTag list = new ListTag();
        for (ResourceLocation id : solrp$gate) list.add(StringTag.valueOf(id.toString()));
        cir.getReturnValue().put(SOLRP_KEY, list);
    }

    @Inject(method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void solrp$load(CompoundTag tag, CallbackInfo ci) {
        solrp$gate.clear();
        if (tag.contains(SOLRP_KEY, Tag.TAG_LIST)) {
            for (Tag el : tag.getList(SOLRP_KEY, Tag.TAG_STRING)) {
                ResourceLocation id = ResourceLocation.tryParse(el.getAsString());
                if (id != null) solrp$gate.add(id);
            }
            return;
        }
        // Saved before the counter existed: a food with fewer meals since than foodsEaten was eaten after the reset.
        for (Map.Entry<FoodInstance, Integer> e : uniqueFoods.entrySet()) {
            if (e.getValue() >= foodsEaten || e.getKey().getItem() == null) continue;
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(e.getKey().getItem());
            if (id != null) solrp$gate.add(id);
        }
    }
}
