package ce.shared;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

/**
 * A Creative Delight member's settings, read from Creative Delight's own config (config/creativedelight-common.toml,
 * plan_creative_delight.md R3): CD's CDConfig defines the keys, comments and defaults under one section per member
 * ({@code [display]}, {@code [vintage]}); the member only reads them here, by path, so it needs no CD class. Before CD's
 * config is loaded (or without CD) every setting reads as the default given. Shared source (development/shared/cdconfig).
 */
public final class CdSettings {
	public static final String FILE = "creativedelight-common.toml";
	private static volatile ModConfig config;

	private CdSettings() {}

	/** The boolean at {@code path} (dotted, e.g. {@code display.takeAll}), or {@code fallback}. */
	public static boolean flag(String path, boolean fallback) {
		ModConfig c = config;
		if (c == null) {
			c = ConfigTracker.INSTANCE.fileMap().get(FILE);
			if (c == null) return fallback;
			config = c;
		}
		CommentedConfig data = c.getConfigData();
		if (data == null) return fallback;
		Object v = data.get(path);
		return v instanceof Boolean b ? b : fallback;
	}

	/** The whole number at {@code path} (dotted), or {@code fallback}. */
	public static int number(String path, int fallback) {
		ModConfig c = config;
		if (c == null) {
			c = ConfigTracker.INSTANCE.fileMap().get(FILE);
			if (c == null) return fallback;
			config = c;
		}
		CommentedConfig data = c.getConfigData();
		if (data == null) return fallback;
		Object v = data.get(path);
		return v instanceof Number n ? n.intValue() : fallback;
	}

	/** A setting the member reads like a ForgeConfigSpec value ({@code get()}); {@code inverted} for the old "Disable..." keys. */
	public record Flag(String path, boolean fallback, boolean inverted) {
		public Boolean get() {
			return flag(path, fallback) != inverted;
		}
	}
}
