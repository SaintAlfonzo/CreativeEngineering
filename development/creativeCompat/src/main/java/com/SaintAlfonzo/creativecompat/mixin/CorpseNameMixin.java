// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.corpse.CorpseState;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

/**
 * The plain name of ancient corpses (creative-compat-publish #22). The Corpse mod overrides only
 * {@code getDisplayName}, which CorpseDecayMixin renames; WTHIT and other tooltips read {@code Entity.getName}, which
 * gave the entity type's "Corpse". This adds a {@code getName} override to CorpseEntity that returns "Ancient Corpse" /
 * "Ancient Remains" for flagged corpses (corpse/CorpseState#creativecompat$ancientName) and the inherited name
 * otherwise. Applied only with the Corpse mod (CompatMixinPlugin). The override is renamed by reobf like any Entity
 * subclass method.
 */
@Mixin(value = CorpseEntity.class, remap = false)
public abstract class CorpseNameMixin extends Entity {

	private CorpseNameMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	@Override
	public Component getName() {
		Component ancient = ((CorpseState) this).creativecompat$ancientName();
		return ancient != null ? ancient : super.getName();
	}
}
