// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModSounds.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Tempad.MOD_ID);

    public static final RegistryObject<SoundEvent> timedoorOpen = register("entity.timedoor.open");
    public static final RegistryObject<SoundEvent> timedoorEnterMono = register("entity.timedoor.enter.mono");
    public static final RegistryObject<SoundEvent> timedoorEnterStereo = register("entity.timedoor.enter.stereo");
    public static final RegistryObject<SoundEvent> upgradePlaceMono = register("block.workstation.upgrade.place.mono");
    public static final RegistryObject<SoundEvent> upgradePlaceStereo = register("block.workstation.upgrade.place.stereo");
    public static final RegistryObject<SoundEvent> upgradeInstalling = register("block.workstation.upgrade.installing");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String path) {
        return REGISTRY.register(path, () -> new SoundEvent(Tempad.id(path)));
    }
}
