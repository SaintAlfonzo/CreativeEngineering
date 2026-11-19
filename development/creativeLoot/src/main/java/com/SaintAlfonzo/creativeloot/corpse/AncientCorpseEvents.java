package com.SaintAlfonzo.creativeloot.corpse;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;

/**
 * Event wiring for ancient corpses: the config is re-read on server start and on /reload; with the Corpse mod loaded,
 * {@link CorpseHooks} rolls the loot on first open and breaks the remains. CorpseHooks is the only class that touches
 * Corpse classes and is loaded only when the mod is.
 */
public final class AncientCorpseEvents {
	private AncientCorpseEvents() {}

	static void onReloadListeners(AddReloadListenerEvent event) {
		event.addListener((ResourceManagerReloadListener) rm -> AncientCorpseConfig.reload());
	}

	static void onServerAboutToStart(ServerAboutToStartEvent event) {
		AncientCorpseConfig.reload();
	}

	static void registerCorpseListeners() {
		MinecraftForge.EVENT_BUS.addListener(CorpseHooks::onInteract);
		MinecraftForge.EVENT_BUS.addListener(CorpseHooks::onAttack);
	}
}
