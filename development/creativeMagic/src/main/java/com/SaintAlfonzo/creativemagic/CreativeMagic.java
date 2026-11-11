package com.SaintAlfonzo.creativemagic;

import com.SaintAlfonzo.creativecommon.swap.ConfigRename;
import com.SaintAlfonzo.creativemagic.block.DarkFireBlock;
import com.SaintAlfonzo.creativemagic.fluid.FluidBridge;
import com.SaintAlfonzo.creativemagic.item.FlintAndTerraSteelItem;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

/**
 * Creative Magic (feature_writeup/creative_magic.md): enchanting, magic recipes and the Botania tweaks, carved out of
 * patcherMod on 2026-11-04 (creative_split.md 7.6, wave 6). Dark Fire and Flint and Terrasteel with the Nether portal
 * rule (dimension/PortalFailureHandler: ordinary fire no longer opens a portal), the Nock Enough Arrows enchantments
 * taken off tables and trades, the Botania Trinket Case holding any item ({@code [botania]}), and the liquid XP bridge
 * (PneumaticCraft and Sophisticated Core use Create Enchantment Industry's experience fluid; fluid/FluidBridge, from
 * Creative Engineering 2026-11-06). Requires patcherMod
 * ({@code creativeengineering}, later Creative Common) and Creative Compat.
 *
 * Dark Fire and Flint and Terrasteel are registered under this mod's namespace ({@link #NS}); until the 2026-11-10
 * namespace move (feature_writeup/namespace_move.md) they were {@code creativeengineering}, and the swap list
 * data/creativemagic/swaps/owner_namespace.json carries old saves.
 */
@Mod(CreativeMagic.MOD_ID)
public class CreativeMagic {

    public static final String MOD_ID = "creativemagic";
    /** The namespace of every id this mod registers (was creativeengineering until the 2026-11-10 namespace move). */
    public static final String NS = MOD_ID;
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String OLD_FILE = "creativeengineering-common.toml";
    private static final String FILE = MOD_ID + "-common.toml";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, NS);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NS);

    /** The one flame that opens a Nether portal (dimension/PortalFailureHandler). */
    public static final RegistryObject<Block> DARK_FIRE = BLOCKS.register("dark_fire",
            () -> new DarkFireBlock(BlockBehaviour.Properties.of(Material.FIRE)
                    .noCollission()
                    .instabreak()
                    .lightLevel(state -> 5)
                    .sound(SoundType.WOOL)
                    .noOcclusion()));

    /** Places Dark Fire (recipe: kubejs/data/creativemagic/kubejs/magic/progression.js). */
    public static final RegistryObject<Item> FLINT_AND_TERRASTEEL = ITEMS.register("flint_and_terrasteel",
            () -> new FlintAndTerraSteelItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC).durability(65)));

    public CreativeMagic() {
        // [botania] came from patcherMod's file: the first boot copies the player's value over (then patcherMod drops
        // it from its file when Forge corrects it). Must run before Forge reads the COMMON configs.
        ConfigRename.carrySections(OLD_FILE, FILE, CMGConfig.SECTIONS);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CMGConfig.SPEC, FILE);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Same order as patcherMod's constructor had them: items, then blocks.
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        modEventBus.addListener(CreativeMagic::commonSetup);

        LOGGER.info("[creativemagic] loaded (config/{})", FILE);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        // Liquid XP bridge: log whether Create Enchantment Industry's experience fluid is there for the mixins.
        event.enqueueWork(FluidBridge::logStartupDiagnostic);
    }
}
