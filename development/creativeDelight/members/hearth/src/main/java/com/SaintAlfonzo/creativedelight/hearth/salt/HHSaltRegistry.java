package com.SaintAlfonzo.creativedelight.hearth.salt;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vectorwing.farmersdelight.common.registry.ModItems;

/** Registration of the salt lick (plan_hearth_crow_salt.md section 2): the worn and waxed stages, their items, the lick sound and the POI type. */
public final class HHSaltRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("HearthAndHarvest/Salt");
    private static final String MODID = HearthAndHarvestMod.MODID;

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, MODID);

    public static final RegistryObject<Block> LIGHTLY_LICKED = stage("lightly_licked_salt_block");
    public static final RegistryObject<Block> WELL_LICKED = stage("well_licked_salt_block");
    public static final RegistryObject<Block> HEAVILY_LICKED = stage("heavily_licked_salt_block");
    public static final RegistryObject<Block> WAXED_SALT = stage("waxed_salt_block");
    public static final RegistryObject<Block> WAXED_LIGHTLY_LICKED = stage("waxed_lightly_licked_salt_block");
    public static final RegistryObject<Block> WAXED_WELL_LICKED = stage("waxed_well_licked_salt_block");
    public static final RegistryObject<Block> WAXED_HEAVILY_LICKED = stage("waxed_heavily_licked_salt_block");

    public static final RegistryObject<SoundEvent> LICK = SOUNDS.register("entities.lick",
            () -> new SoundEvent(new ResourceLocation(MODID, "entities.lick")));

    /** Unwaxed lick blocks only: waxing takes a block out of the index, unwaxing puts it back. */
    public static final RegistryObject<PoiType> SALT_LICK_POI = POI_TYPES.register("salt_lick", HHSaltRegistry::createPoi);

    private HHSaltRegistry() {
    }

    private static RegistryObject<Block> stage(String id) {
        RegistryObject<Block> block = BLOCKS.register(id, () -> new LickableSaltBlock(Properties.copy(Blocks.GRAVEL).strength(2.0F, 3.0F).sound(SoundType.TUFF)));
        ITEMS.register(id, () -> new BlockItem(block.get(), ModItems.basicItem()));
        return block;
    }

    private static PoiType createPoi() {
        Set<BlockState> states = new HashSet<>();
        Block salt = SaltLick.saltBlock();
        if (salt != null && salt != Blocks.AIR) {
            states.addAll(salt.getStateDefinition().getPossibleStates());
        } else {
            LOGGER.warn("salt:salt_block is not registered; the salt lick POI covers the worn stages only");
        }
        states.addAll(LIGHTLY_LICKED.get().getStateDefinition().getPossibleStates());
        states.addAll(WELL_LICKED.get().getStateDefinition().getPossibleStates());
        states.addAll(HEAVILY_LICKED.get().getStateDefinition().getPossibleStates());
        // Forge stops the boot if a state already belongs to another POI type. Drop those states and say so instead.
        states.removeIf(state -> {
            boolean claimed = PoiTypes.forState(state).isPresent();
            if (claimed) {
                LOGGER.error("Salt lick POI clash: {} already belongs to another POI type; animals will not find this block", state);
            }
            return claimed;
        });
        return new PoiType(states, 0, 1);
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        SOUNDS.register(bus);
        POI_TYPES.register(bus);
        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(SaltEvents::registerDispenser));
        MinecraftForge.EVENT_BUS.addListener(SaltEvents::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(SaltEvents::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(SaltEvents::onTooltip);
    }
}
