package com.SaintAlfonzo.creativedelight.treatedstick;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.block.TomatoVineBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

/**
 * Placeable treated sticks (feature_writeup/treated_sticks.md): Immersive Engineering's treated stick
 * ({@code immersiveengineering:stick_treated}) is placed like Supplementaries' sticks, as
 * {@code creativedelight:treated_stick}, and Farmer's Delight tomatoes climb it as
 * {@code creativedelight:treated_stick_tomatoes}, the same way they climb Supplementaries' sticks.
 *
 * Own code (Supplementaries is ARR): the blocks re-implement the behaviour of Supplementaries' StickBlock and
 * TomatoStickBlock; the block models name Supplementaries' models as parents. Hooks:
 *  - placement: {@code mixin.immersiveengineering.TreatedStickPlaceMixin} (Item#useOn, so the clicked block's own
 *    use runs first, as for Supplementaries' sticks) calls {@link #useOn};
 *  - climbing: FD's TomatoVineBlock and Supplementaries' stick/rope tomatoes call {@link #tryClimb} first
 *    ({@code mixin.farmersdelight.TomatoClimbTreatedStickMixin}, {@code mixin.supplementaries.SuppTomatoClimbTreatedStickMixin});
 *    {@link TreatedStickTomatoBlock} climbs on its own.
 */
public final class TreatedSticks {

	public static final ResourceLocation ITEM_ID = new ResourceLocation("immersiveengineering", "stick_treated");

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeDelight.MOD_ID);

	public static final RegistryObject<Block> TREATED_STICK = BLOCKS.register("treated_stick",
			() -> new TreatedStickBlock(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN).noOcclusion()
					.strength(0.25F, 0.0F).sound(SoundType.WOOD)));

	public static final RegistryObject<Block> TREATED_STICK_TOMATOES = BLOCKS.register("treated_stick_tomatoes",
			() -> new TreatedStickTomatoBlock(Block.Properties.copy(Blocks.WHEAT).sound(SoundType.WOOD)));

	private static Item item;

	private TreatedSticks() {}

	public static void register(IEventBus modBus) {
		BLOCKS.register(modBus);
	}

	/** The treated stick item, or air without Immersive Engineering. */
	public static Item item() {
		if (item == null) {
			Item found = ForgeRegistries.ITEMS.getValue(ITEM_ID);
			item = found == null ? Items.AIR : found;
		}
		return item;
	}

	public static boolean isTreatedStick(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() == item() && item() != Items.AIR;
	}

	/**
	 * Right-click with a treated stick on a block whose own use passed: on a bottom FD tomato vine (not on a rope) it
	 * becomes a vine on a treated stick, otherwise a treated stick is placed (or added to the clicked stick block on a
	 * new axis), like Supplementaries' sticks. PASS when nothing could be placed.
	 */
	public static InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos clicked = ctx.getClickedPos();
		BlockState clickedState = level.getBlockState(clicked);
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();

		if (clickedState.is(ModBlocks.TOMATO_CROP.get()) && !clickedState.getValue(TomatoVineBlock.ROPELOGGED)
				&& (player == null || !player.isSecondaryUseActive())) {
			BlockState vine = TREATED_STICK_TOMATOES.get().defaultBlockState()
					.setValue(TomatoVineBlock.VINE_AGE, clickedState.getValue(TomatoVineBlock.VINE_AGE));
			return finish(level, clicked, vine, player, stack);
		}

		BlockPlaceContext pc = new BlockPlaceContext(ctx);
		if (!pc.canPlace()) return InteractionResult.PASS;
		BlockPos pos = pc.getClickedPos();
		BlockState state = TREATED_STICK.get().getStateForPlacement(pc);
		if (state == null || !state.canSurvive(level, pos)
				|| !level.isUnobstructed(state, pos, player == null ? CollisionContext.empty() : CollisionContext.of(player))) {
			return InteractionResult.PASS;
		}
		return finish(level, pos, state, player, stack);
	}

	private static InteractionResult finish(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
		if (!level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE)) return InteractionResult.FAIL;
		SoundType sound = SoundType.WOOD;
		level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F,
				sound.getPitch() * 0.8F);
		level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state));
		if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	/**
	 * Tomato climbing onto a treated stick above {@code pos}. Returns false when the block above is not a treated
	 * stick (the caller's own climbing runs); true when it is (the caller stops). Same rule as FD and
	 * Supplementaries: 30% per call, at most three vines high, only with FD's tagged-rope climbing on.
	 */
	public static boolean tryClimb(ServerLevel level, BlockPos pos, RandomSource random) {
		BlockPos above = pos.above();
		BlockState stateAbove = level.getBlockState(above);
		if (!stateAbove.is(TREATED_STICK.get())) return false;
		if (!Configuration.ENABLE_TOMATO_VINE_CLIMBING_TAGGED_ROPES.get()) return true;
		if (random.nextFloat() < 0.3F && vineHeight(level, pos) < 3) {
			level.setBlockAndUpdate(above, TREATED_STICK_TOMATOES.get().withPropertiesOf(stateAbove));
		}
		return true;
	}

	static int vineHeight(Level level, BlockPos pos) {
		int h = 1;
		while (level.getBlockState(pos.below(h)).getBlock() instanceof TomatoVineBlock) h++;
		return h;
	}
}
