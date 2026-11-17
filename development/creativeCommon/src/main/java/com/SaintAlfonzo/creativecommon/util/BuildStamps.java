package com.SaintAlfonzo.creativecommon.util;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One boot line naming the exact build of every own jar (plan_process_overhaul.md P1.2 item 4):
 * {@code [creativecommon] builds: creativecommon 1.2.3+abc1234.2611162122, creativedelight ...}.
 * Reads META-INF/ce-build.json, which build-logic writes into every jar it builds (bundle members are their own mod
 * files once Forge has extracted them). A run doc, a crash report and the owner's latest.log then name the builds.
 */
public final class BuildStamps {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Pattern MOD_ID = Pattern.compile("\"mod_id\"\\s*:\\s*\"([^\"]*)\"");
	private static final Pattern STAMP = Pattern.compile("\"stamp\"\\s*:\\s*\"([^\"]*)\"");

	private BuildStamps() {}

	public static void log() {
		List<String> out = new ArrayList<>();
		try {
			for (IModFileInfo info : ModList.get().getModFiles()) {
				try {
					Path p = info.getFile().findResource("META-INF", "ce-build.json");
					if (p == null || !Files.exists(p)) continue;
					String json = Files.readString(p);
					Matcher id = MOD_ID.matcher(json), st = STAMP.matcher(json);
					out.add((id.find() ? id.group(1) : info.getFile().getFileName()) + " " + (st.find() ? st.group(1) : "?"));
				} catch (Exception e) {
					out.add(info.getFile().getFileName() + " unreadable");
				}
			}
		} catch (Exception e) {
			LOGGER.warn("[creativecommon] builds: could not list mod files", e);
			return;
		}
		out.sort(null);
		LOGGER.info("[creativecommon] builds: {}", out.isEmpty() ? "none stamped" : String.join(", ", out));
	}
}
