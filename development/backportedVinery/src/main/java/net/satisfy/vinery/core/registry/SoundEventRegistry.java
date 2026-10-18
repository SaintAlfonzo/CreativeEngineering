package net.satisfy.vinery.core.registry;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.satisfy.vinery.core.Vinery;

public class SoundEventRegistry {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Vinery.MOD_ID);

    public static final RegistryObject<SoundEvent> BLOCK_GRAPEVINE_POT_SQUEEZE = create("block.grapevine_pot.squeeze");
    public static final RegistryObject<SoundEvent> CABINET_OPEN = create("cabinet_open");
    public static final RegistryObject<SoundEvent> CABINET_CLOSE = create("cabinet_close");
    public static final RegistryObject<SoundEvent> DRAWER_OPEN = create("drawer_open");
    public static final RegistryObject<SoundEvent> DRAWER_CLOSE = create("drawer_close");
    // 1.3.10 sounds of the D2 keepers (defined in the 1.3.10 jar's sounds.json)
    public static final RegistryObject<SoundEvent> KITCHEN_SINK_FAUCET = create("block.kitchen_sink.faucet");
    public static final RegistryObject<SoundEvent> WINE_RACK_3_OPEN = create("block.wine_rack_3.open");
    public static final RegistryObject<SoundEvent> WINE_RACK_3_CLOSE = create("block.wine_rack_3.close");
    public static final RegistryObject<SoundEvent> WINE_RACK_5_OPEN = create("block.wine_rack_5.open");
    public static final RegistryObject<SoundEvent> WINE_RACK_5_CLOSE = create("block.wine_rack_5.close");

    private static RegistryObject<SoundEvent> create(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(Vinery.identifier(name)));
    }
}
