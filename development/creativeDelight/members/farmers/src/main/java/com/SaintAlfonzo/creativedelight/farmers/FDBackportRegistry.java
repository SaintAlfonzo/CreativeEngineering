package com.SaintAlfonzo.creativedelight.farmers;

import com.SaintAlfonzo.creativedelight.farmers.block.GleamingSaladBlock;
import com.SaintAlfonzo.creativedelight.farmers.block.HangingCanvasSignBlock;
import com.SaintAlfonzo.creativedelight.farmers.block.PumpkinPieBlock;
import com.SaintAlfonzo.creativedelight.farmers.block.RopeFenceBlock;
import com.SaintAlfonzo.creativedelight.farmers.block.RopeFenceGateBlock;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.FoodValues;
import vectorwing.farmersdelight.common.item.ConsumableItem;
import vectorwing.farmersdelight.common.item.FuelBlockItem;
import vectorwing.farmersdelight.common.registry.ModEffects;
import vectorwing.farmersdelight.common.registry.ModItems;

/**
 * The backported blocks, items, particle and sounds, in FD's namespace ({@code farmersdelight:<id>}). Ids, block
 * properties, food values and the stack sizes are 1.3.4's, rebuilt with 1.19.2 calls (material and colour for
 * 1.20's MapColor, Blocks.CAKE's sound for the feast).
 */
public final class FDBackportRegistry {

	private static final String NS = FarmersDelight.MODID;

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, NS);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NS);
	public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, NS);
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NS);

	/** Gleaming Salad serving: 14 / 0.75, Nourishment 5 min (1.3.4 FoodValues.GLEAMING_SALAD). */
	public static final FoodProperties GLEAMING_SALAD_FOOD = new FoodProperties.Builder().nutrition(14).saturationMod(0.75F)
			.effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F).build();

	public static final RegistryObject<SimpleParticleType> SPARKLE = PARTICLES.register("sparkle", () -> new SimpleParticleType(false));

	public static final RegistryObject<SoundEvent> ROPE_FENCE_GATE_OPEN = sound("block.rope_fence_gate.open");
	public static final RegistryObject<SoundEvent> ROPE_FENCE_GATE_CLOSE = sound("block.rope_fence_gate.close");

	public static final RegistryObject<Block> ROPE_FENCE = BLOCKS.register("rope_fence",
			() -> new RopeFenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE).strength(1.0F)));
	public static final RegistryObject<Block> ROPE_FENCE_GATE = BLOCKS.register("rope_fence_gate",
			() -> new RopeFenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE).strength(1.0F)));
	public static final RegistryObject<Block> PUMPKIN_PIE = BLOCKS.register("pumpkin_pie",
			() -> new PumpkinPieBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), FDBackportRegistry.PUMPKIN_PIE_SLICE));
	public static final RegistryObject<Block> GLEAMING_SALAD_BLOCK = BLOCKS.register("gleaming_salad_block",
			() -> new GleamingSaladBlock(BlockBehaviour.Properties.of(Material.CAKE, MaterialColor.COLOR_LIGHT_GREEN).instabreak()
					.strength(0.5F).sound(SoundType.WOOL).lightLevel(state -> state.getValue(GleamingSaladBlock.SERVINGS) * 3),
					FDBackportRegistry.GLEAMING_SALAD, true));

	/** Undyed ceiling Hanging Canvas Sign: no text, block entity, dyes or wall variant (farmers_delight.md). */
	public static final RegistryObject<Block> HANGING_CANVAS_SIGN = BLOCKS.register("hanging_canvas_sign",
			() -> new HangingCanvasSignBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).noCollission()
					.strength(1.0F).sound(SoundType.WOOD)));

	public static final RegistryObject<Item> HANGING_CANVAS_SIGN_ITEM = ITEMS.register("hanging_canvas_sign",
			() -> new BlockItem(HANGING_CANVAS_SIGN.get(), ModItems.basicItem().stacksTo(16)));
	public static final RegistryObject<Item> ROPE_FENCE_ITEM = ITEMS.register("rope_fence",
			() -> new FuelBlockItem(ROPE_FENCE.get(), ModItems.basicItem(), 200));
	public static final RegistryObject<Item> ROPE_FENCE_GATE_ITEM = ITEMS.register("rope_fence_gate",
			() -> new FuelBlockItem(ROPE_FENCE_GATE.get(), ModItems.basicItem(), 200));
	public static final RegistryObject<Item> PUMPKIN_PIE_SLICE = ITEMS.register("pumpkin_pie_slice",
			() -> new ConsumableItem(ModItems.foodItem(FoodValues.PIE_SLICE)));
	public static final RegistryObject<Item> GLEAMING_SALAD_BLOCK_ITEM = ITEMS.register("gleaming_salad_block",
			() -> new BlockItem(GLEAMING_SALAD_BLOCK.get(), ModItems.basicItem().stacksTo(1)));
	public static final RegistryObject<Item> GLEAMING_SALAD = ITEMS.register("gleaming_salad",
			() -> new ConsumableItem(ModItems.bowlFoodItem(GLEAMING_SALAD_FOOD), true));

	private FDBackportRegistry() {}

	private static RegistryObject<SoundEvent> sound(String name) {
		return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(NS, name)));
	}

	static void register(IEventBus modBus) {
		BLOCKS.register(modBus);
		ITEMS.register(modBus);
		PARTICLES.register(modBus);
		SOUNDS.register(modBus);
	}
}
