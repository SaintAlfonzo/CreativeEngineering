/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
// Port state: phases P2 (grid base), P3 (devices, transformers, storage, FE bridge, magnet), P4 (kinetics), P6 (movement checks) P7 (equipment), P9 (components, boost serializer) and P10 (string light cord recipe): complete, the upstream copy is
// deleted. Removed for good: NativeMNA.tryLoad (D4),
// ModdedAdvancements (pack drops advancements), getPlatform / forPlatform (Forge only, D2).
package org.patryk3211.powergrid;

import com.simibubi.create.content.contraptions.BlockMovementChecks;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.solarpanel.SolarPanelBlock;
import com.simibubi.create.infrastructure.config.AllConfigs;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.collections.*;
import org.patryk3211.powergrid.electricity.GlobalElectricNetworks;
import org.patryk3211.powergrid.commands.DebugCommand;
import org.patryk3211.powergrid.electricity.deviceconnector.DeviceConnectorBlockEntity;
import org.patryk3211.powergrid.electricity.electromagnet.recipe.MagnetizingRecipe;
import org.patryk3211.powergrid.electricity.febridge.FEInverterBlockEntity;
import org.patryk3211.powergrid.electricity.redstoneconverter.RedstoneConverterRegistry;
import org.patryk3211.powergrid.utility.proxy.SubstituteBlockEntityProvider;
import org.patryk3211.powergrid.electricity.sim.ElectricalNetwork;
import org.patryk3211.powergrid.electricity.sim.GraphedElectricalNetwork;
import org.patryk3211.powergrid.electricity.wire.EntityWireInteraction;
import org.patryk3211.powergrid.electricity.wire.WireItem;
import org.patryk3211.powergrid.network.packets.NegotiateSyncC2SPacket;
import org.patryk3211.powergrid.utility.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerGrid {
	public static final String MOD_ID = "powergrid";

	public static final Logger LOGGER = LoggerFactory.getLogger(PowerGrid.class);

	public static AbstractPowerGridRegistrate REGISTRATE;

	// Phase P3: the magnetization recipe type (the boost recipe P9d and the string light cord P10 add their serializers here)
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(MOD_ID, Registry.RECIPE_SERIALIZER_REGISTRY);
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(MOD_ID, Registry.RECIPE_TYPE_REGISTRY);

	public static void init() {
		LOGGER.info("Power grid starting, prepare to be electrocuted (1.19.2 port, phase {})", PortPhase.CURRENT);
		// Without a logger the solver prints every convergence to stdout (its test mode); PORT_NOTES 9.
		ElectricalNetwork.LOGGER = LOGGER;
		// Port: upstream's solver calls DebugCommand.pushProblems directly; the P1 core reports through this hook.
		GraphedElectricalNetwork.PROBLEM_SINK = DebugCommand::pushProblems;

		ModdedSoundEvents.prepare();

		REGISTRATE = createRegistrate();

		register();

		registerArchitecturyEvents();

		ModdedPackets.registerPackets();
	}

	public static void registerArchitecturyEvents() {
		TickEvent.ServerLevelTick.SERVER_LEVEL_PRE.register(GlobalElectricNetworks::preTick);
		TickEvent.ServerLevelTick.SERVER_LEVEL_POST.register(GlobalElectricNetworks::postTick);
		TickEvent.SERVER_POST.register(EntityWireInteraction::postTick);
		LifecycleEvent.SERVER_LEVEL_UNLOAD.register(GlobalElectricNetworks::unloadWorld);
		CommandRegistrationEvent.EVENT.register(ModdedCommands::register);
		PlayerEvent.PLAYER_JOIN.register(PowerGrid::playerJoin);
		PlayerEvent.PLAYER_QUIT.register(PowerGrid::playerQuit);
		PlayerEvent.CHANGE_DIMENSION.register(PowerGrid::playerChangeDimension);
		LifecycleEvent.SETUP.register(PowerGrid::setup);
		InteractionEvent.RIGHT_CLICK_BLOCK.register(WireItem::useOn);
		InteractionEvent.RIGHT_CLICK_ITEM.register(WireItem::use);
		// Phase P7: the portable battery charges the held item from the chest slot (vanilla armor slot; no Curios).
		TickEvent.PLAYER_PRE.register(PowerGrid::playerPre);
	}

	private static void playerPre(net.minecraft.world.entity.player.Player player) {
		if(player.level.isClientSide())
			return;

		var chestStack = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
		if(!(chestStack.getItem() instanceof org.patryk3211.powergrid.equipment.portablebattery.PortableBatteryItem battery))
			return;

		battery.onWornTick(chestStack, player);
	}

	// Phase P7: lightning rods on a bearing contraption (lightning attractor). Called from the common setup's
	// enqueueWork (PowerGridImpl.commonSetup; Create 0.5.1 AllMovementBehaviours keeps a plain AttachedRegistry),
	// upstream from init through Create 6's MovementBehaviour.REGISTRY.
	public static void registerMovementBehaviours() {
		com.simibubi.create.AllMovementBehaviours.registerBehaviour(net.minecraft.world.level.block.Blocks.LIGHTNING_ROD,
				new org.patryk3211.powergrid.equipment.thunder.LightningRodMovementBehaviour());
	}

	private static void setup() {
		RedstoneConverterRegistry.init();
	}

	private static void playerQuit(ServerPlayer player) {
		NegotiateSyncC2SPacket.SYNC_TYPES.remove(player);
	}

	private static void playerJoin(ServerPlayer player) {
		if(player.hasPermissions(3) && !ModdedConfigs.server().isUpToDate()) {
			player.sendSystemMessage(Lang.translateDirect("message.outdated_configs"));
			player.sendSystemMessage(Component.empty()
					.append(Lang.translateDirect("message.reset_configs")
							.withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)
									.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/powergrid reset_configs")))
					).append(Component.literal(" "))
					.append(Lang.translateDirect("message.disable_config_message")
							.withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)
									.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/powergrid ignore_configs")))
					)
			);
		}
	}

	private static void playerChangeDimension(ServerPlayer player, ResourceKey<Level> oldDim, ResourceKey<Level> newDim) {
		GlobalElectricNetworks.dropTrackers(player, oldDim);
	}

	private static void register() {
		// Upstream order: blocks register first so their builder transforms fill the per-block config defaults
		// (stressValues, thermal, resistance) before ModdedConfigs.register() builds the specs.
		registerRecipes();

		// Phase P3: block entity factories that the forge side may replace (FEInverterBlockEntityImpl, PowerGridImpl)
		SubstituteBlockEntityProvider.INSTANCE.registerDefault(DeviceConnectorBlockEntity.class, DeviceConnectorBlockEntity::new);
		// Phase P8: the forge punch card reader (item handler capability) replaces the common block entity
		SubstituteBlockEntityProvider.INSTANCE.registerDefault(org.patryk3211.powergrid.kinetics.punchcard.PunchCardReaderBlockEntity.class, org.patryk3211.powergrid.kinetics.punchcard.PunchCardReaderBlockEntity::new);
		SubstituteBlockEntityProvider.INSTANCE.registerDefault(FEInverterBlockEntity.class, FEInverterBlockEntity::new);
		SubstituteBlockEntityProvider.INSTANCE.lock();

		ModdedDisplaySources.register();
		ModdedBlocks.register();
		ModdedItems.register();
		// Phase P9c: the acid fluid, its block and bucket (upstream order: after the items)
		if(PortPhase.CURRENT >= 9.3)
			org.patryk3211.powergrid.collections.ModdedFluids.register();
		ModdedBlockEntities.register();
		ModdedEntities.register();
		ModdedConfigs.register();
		ModdedMenus.register();
		// Phase P9a: circuit board components (forge registry powergrid:components, PowerGridImpl.newRegistry)
		if(PortPhase.CURRENT >= 9.1)
			org.patryk3211.powergrid.circuits.components.Components.register();

		ModdedParticles.register();
		RECIPE_SERIALIZERS.register();
		RECIPE_TYPES.register();
		ModdedParticles.PARTICLE_TYPES.register();

		finalizeRegistrate();
	}

	// Phase P6: solar panels carry their neighbours on contraptions. Called from the common setup's enqueueWork
	// (PowerGridImpl.commonSetup; Create 0.5.1 keeps the checks in plain lists), upstream from init.
	public static void registerBlockMovementChecks(){
		BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
			if (!(state.getBlock() instanceof SolarPanelBlock))
				return BlockMovementChecks.CheckResult.PASS;
			BlockState neighbor = world.getBlockState(pos.relative(direction));
			if (neighbor.getBlock() instanceof SolarPanelBlock)
				return BlockMovementChecks.CheckResult.SUCCESS;

			return BlockMovementChecks.CheckResult.PASS;
		});
	}

	public static void registerRecipes() {
		var magnetizing = MagnetizingRecipe.TYPE_INFO;
		RECIPE_SERIALIZERS.register(magnetizing.getId(), magnetizing::getSerializer);
		RECIPE_TYPES.register(magnetizing.getId(), magnetizing::getType);
		// Phase P9d: the boost recipe (Create's deploying type, Power Grid's serializer; no recipe type of its own)
		RECIPE_SERIALIZERS.register("boost_recipe", () -> org.patryk3211.powergrid.equipment.BoostRecipe.SERIALIZER);
		// Phase P10: the string light cord's dye pattern recipe (crafting, special)
		RECIPE_SERIALIZERS.register("crafting_special_string_light_cord", () -> org.patryk3211.powergrid.electricity.light.string.StringLightCordRecipe.SERIALIZER);
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	public static ResourceLocation texture(String path) {
		return asResource("textures/" + path + ".png");
	}

	// @ExpectPlatform inlined (D2): forge implementation in PowerGridImpl
	public static AbstractPowerGridRegistrate createRegistrate() {
		return org.patryk3211.powergrid.forge.PowerGridImpl.createRegistrate();
	}

	// @ExpectPlatform inlined (D2): forge implementation in PowerGridImpl
	public static void finalizeRegistrate() {
		org.patryk3211.powergrid.forge.PowerGridImpl.finalizeRegistrate();
	}

	public static int maxRPM() {
		return AllConfigs.server().kinetics.maxRotationSpeed.get();
	}
}
