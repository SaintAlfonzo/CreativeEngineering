package com.SaintAlfonzo.creativedelight.compat;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Method;

/**
 * Salt's COMMON settings as section {@code [salt]} of Creative Delight's config (feature_writeup/salt.md 6 S10, TODO
 * creativedelight/members #52). The Salt member's overlay {@code Configuration} defines its values into CD's builder
 * ({@code define}), takes the built spec ({@code bind}) and rereads its food list when the file loads ({@code refresh}).
 * Reflection, because CD compiles against the upstream Salt jar, which has none of the three, and so CD still boots with
 * Salt absent. The old salt-common.toml is carried over once by {@link MemberConfigMove}.
 */
public final class SaltConfigBridge {
	private SaltConfigBridge() {}

	private static final String CLASS = "io.github.mortuusars.salt.configuration.Configuration";
	private static Class<?> cls;
	private static boolean defined;

	private static Class<?> cls() {
		if (cls == null) {
			try {
				cls = Class.forName(CLASS, true, SaltConfigBridge.class.getClassLoader());
			} catch (ClassNotFoundException | LinkageError e) {
				return null;
			}
		}
		return cls;
	}

	/** Inside CDConfig's builder: pushes {@code salt} and lets Salt define its values there. */
	public static void define(ForgeConfigSpec.Builder b) {
		Class<?> c = cls();
		if (c == null) return;
		try {
			Method m = c.getMethod("define", ForgeConfigSpec.Builder.class);
			b.comment("Salt (Creative Delight). Was salt-common.toml (its values are carried over once); same sections and keys.",
					"The client settings (salted overlay, JEI categories) stay in salt-client.toml.").push("salt");
			m.invoke(null, b);
			b.pop();
			defined = true;
		} catch (ReflectiveOperationException e) {
			CreativeDelight.LOGGER.error("[creativedelight] Salt settings not defined (not the Creative Delight Salt member?): {}", e.toString());
		}
	}

	public static void bind(ForgeConfigSpec spec) {
		if (!defined) return;
		call("bind", spec);
	}

	public static void refresh() {
		if (!defined) return;
		call("refresh", null);
	}

	private static void call(String name, ForgeConfigSpec spec) {
		try {
			if (spec != null) cls.getMethod(name, ForgeConfigSpec.class).invoke(null, spec);
			else cls.getMethod(name).invoke(null);
		} catch (ReflectiveOperationException e) {
			CreativeDelight.LOGGER.error("[creativedelight] Salt config {} failed: {}", name, e.toString());
		}
	}
}
