package com.SaintAlfonzo.creativecommon.mixin;

import de.cadentem.create_gravity.config.ServerConfig;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Create: Gravity's raw biome_configs strings (private). The client parses them itself (LowGravity), because the mod's
 * parsed list is empty on a dedicated client. Forge syncs the server's copy of this SERVER config at login.
 */
@Mixin(value = ServerConfig.class, remap = false)
public interface CreateGravityConfigAccessor {

	@Accessor(value = "BIOME_CONFIGS_INTERNAL", remap = false)
	static ForgeConfigSpec.ConfigValue<List<? extends String>> creativecommon$biomeConfigs() {
		throw new AssertionError();
	}
}
