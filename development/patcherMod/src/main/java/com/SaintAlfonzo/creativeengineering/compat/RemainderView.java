package com.SaintAlfonzo.creativeengineering.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Common API for the WTHIT leftover-slot line ({@link WthitRemainderBuffer}): what a station's remainder buffer holds,
 * without Common importing Creative Delight (creative_split.md 7.6, WTHIT rows). Creative Delight's
 * {@code cooking.RemainderBuffer} implements it; a station block entity implements {@link Holder} (the oven directly,
 * the Farmer's Delight cooking pot and the Farmer's Respite kettle through CD's remainder mixins). A station class of
 * a content jar is added with {@link #addStation} from that jar's constructor; the third-party stations are registered
 * by the plugin itself.
 */
public interface RemainderView {

	int getSlots();

	ItemStack getStackInSlot(int slot);

	/** True while a craft is held back for room in the slots ({@code [cooking] stallWhenFull}). */
	boolean isStalled(long gameTime);

	/** A block entity with a remainder buffer. */
	interface Holder {
		RemainderView ce$remainderView();
	}

	/** Content-jar station classes the WTHIT line registers for, in addition to the pot and the kettle. */
	List<Class<? extends BlockEntity>> STATIONS = new CopyOnWriteArrayList<>();

	static void addStation(Class<? extends BlockEntity> station) {
		if (!STATIONS.contains(station)) STATIONS.add(station);
	}
}
