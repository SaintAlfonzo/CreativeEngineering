package com.SaintAlfonzo.solraspberrypie.mixin;

import com.SaintAlfonzo.solraspberrypie.data.UnlockOrder;
import com.cazsius.solcarrot.tracking.FoodInstance;
import com.cazsius.solcarrot.tracking.FoodList;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Food Dictionary unlock order (plan_diet_books.md 2.3, {@link UnlockOrder}): a food Carrot adds to its eaten set is
 * appended; cleared with Carrot's list; saved beside it in Carrot's NBT (key {@code solrp_unlockOrder}), which Carrot
 * also syncs to the client and copies on respawn. Gated on solcarrot (RpMixinPlugin).
 */
@Mixin(value = FoodList.class, remap = false)
public abstract class CarrotFoodListMixin implements UnlockOrder {
	@Unique
	private static final String SOLRP_ORDER = "solrp_unlockOrder";

	@Shadow
	@Final
	private Set<FoodInstance> foods;

	@Unique
	private final List<ResourceLocation> solrp$order = new ArrayList<>();

	@Override
	public List<ResourceLocation> solrp$unlockOrder() {
		return solrp$order;
	}

	@Inject(method = "addFood", at = @At("HEAD"))
	private void solrp$recordUnlock(Item food, CallbackInfoReturnable<Boolean> cir) {
		if (food == null || foods.contains(new FoodInstance(food))) return;
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(food);
		if (id != null && !solrp$order.contains(id)) solrp$order.add(id);
	}

	@Inject(method = "clearFood", at = @At("RETURN"))
	private void solrp$clearOrder(CallbackInfo ci) {
		solrp$order.clear();
	}

	@Inject(method = "serializeNBT()Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
	private void solrp$save(CallbackInfoReturnable<CompoundTag> cir) {
		ListTag list = new ListTag();
		for (ResourceLocation id : solrp$order) list.add(StringTag.valueOf(id.toString()));
		cir.getReturnValue().put(SOLRP_ORDER, list);
	}

	@Inject(method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
	private void solrp$load(CompoundTag tag, CallbackInfo ci) {
		solrp$order.clear();
		for (Tag el : tag.getList(SOLRP_ORDER, Tag.TAG_STRING)) {
			ResourceLocation id = ResourceLocation.tryParse(el.getAsString());
			if (id != null) solrp$order.add(id);
		}
	}
}
