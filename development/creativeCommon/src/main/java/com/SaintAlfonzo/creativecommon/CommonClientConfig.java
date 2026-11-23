package com.SaintAlfonzo.creativecommon;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Per-client settings for {@code creativecommon}, backing {@code config/creativecommon-client.toml}. Each player keeps
 * their own values; the file is never read on a dedicated server. Registered in {@link CreativeCommon}'s constructor,
 * for the same reason as {@link CommonConfig}.
 */
public final class CommonClientConfig {

	public static final ForgeConfigSpec CLIENT_SPEC;
	public static final LoadingTips LOADING_TIPS;
	public static final Privacy PRIVACY;

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		LOADING_TIPS = new LoadingTips(builder);
		PRIVACY = new Privacy(builder);
		CLIENT_SPEC = builder.build();
	}

	private CommonClientConfig() {}

	/** Loading screen tips ({@code loadingtips/} package, feature_writeup/loading_tips.md). */
	public static final class LoadingTips {
		public final ForgeConfigSpec.BooleanValue enabled;
		public final ForgeConfigSpec.IntValue cycleSeconds;

		private LoadingTips(ForgeConfigSpec.Builder b) {
			b.comment("Tip bar on the world loading screens. The tips come from the built-in resource pack",
					"Loading Tips and any resource pack with assets/<namespace>/loading_tips/*.json."
			).push("loading_tips");

			enabled = b.comment("Show the tip bar while a world or dimension loads.")
					.define("enabled", true);
			cycleSeconds = b.comment("Seconds each tip stays before the next one rolls in.")
					.defineInRange("cycleSeconds", 10, 2, 120);

			b.pop(); // loading_tips
		}
	}

	/**
	 * Chat reporting and Mojang's user API ({@code privacy.MojangReporting}, feature_writeup/privacy.md 4.2). The
	 * value is read straight from this file at game start, before Forge loads configs; the spec only writes the key.
	 */
	public static final class Privacy {
		public final ForgeConfigSpec.BooleanValue blockMojangReporting;

		private Privacy(ForgeConfigSpec.Builder b) {
			b.comment("Chat reporting and Mojang's user API.").push("privacy");

			blockMojangReporting = b.comment(
					"true: the game uses Mojang's offline user API. It never gets a chat signing key, so nothing you",
					"write in chat is signed or can be reported to Mojang; no abuse reports or telemetry are sent; your",
					"Microsoft account block list is not loaded. The \"Not Secure\" chat bar and the \"Chat messages can't",
					"be verified\" toast are hidden. Servers that enforce secure chat refuse players without a key: set",
					"false to play on those. Login and skins are unaffected. Applies on the next game start."
			).define("blockMojangReporting", true);

			b.pop(); // privacy
		}
	}
}
