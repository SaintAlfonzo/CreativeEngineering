package com.SaintAlfonzo.creativecompat.surface;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CompatConfig.SpeedFormat;
import com.SaintAlfonzo.creativecompat.CreativeCompat;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;

/**
 * WTHIT plugin: a "Run Speed" line in the look-at overlay, the walk speed that block gives
 * ({@link RunSpeed}), tuned by CompatConfig {@code [surface.wthit]}. Pass-through blocks get Walk Through,
 * Fall Through and Neighbors lines instead ({@link PassThroughFormat}, switch {@code passThrough}). Client-only, declared in
 * {@code waila_plugins.json} (loaded only when BlockRunner is present). No server data: frames
 * sync their camo to clients already.
 */
public final class WthitRunSpeed implements IWailaClientPlugin, IBlockComponentProvider {

	private static final ResourceLocation LINE = new ResourceLocation(CreativeCompat.MOD_ID, "run_speed");

	private static boolean listening;

	@Override
	public void register(IClientRegistrar registrar) {
		registrar.body(this, Block.class);
		if (!listening) {
			listening = true;
			// tier names are cached per block; a tag reload (join, /reload) can move blocks between tiers
			MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TagsUpdatedEvent.class,
					event -> RunSpeed.clearTierCache());
		}
	}

	@Override
	public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		CompatConfig.Surface.Wthit cfg = CompatConfig.SURFACE.wthit;
		if (!cfg.enabled.get()) {
			return;
		}
		if (cfg.passThrough.get() && passThrough(tooltip, accessor, cfg)) {
			return;
		}
		RunSpeed.Info info = RunSpeed.at(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState(),
				accessor.getPlayer());
		if (info == null
				|| cfg.hideUntyped.get() && !info.typed()
				|| cfg.hideNormal.get() && RunSpeed.isNormal(info.factor())) {
			return;
		}
		MutableComponent line = Component.translatable("creativeengineering.wthit.run_speed",
				RunSpeed.format(info.factor(), cfg.format.get()));
		if (cfg.showTier.get()) {
			List<Component> parts = new ArrayList<>();
			if (info.tier() != null) {
				parts.add(Component.translatable("creativeengineering.surface.tier." + info.tier().key()));
			}
			if (info.source() == RunSpeed.Source.NATIVE || info.source() == RunSpeed.Source.IN_BLOCK) {
				parts.add(Component.translatable("creativeengineering.wthit.run_speed.source."
						+ info.source().name().toLowerCase(Locale.ROOT)));
			}
			for (RunSpeed.Note note : info.notes()) {
				parts.add(Component.translatable("creativeengineering.wthit.run_speed."
						+ note.name().toLowerCase(Locale.ROOT)));
			}
			if (!parts.isEmpty()) {
				MutableComponent detail = Component.literal(" (");
				for (int i = 0; i < parts.size(); i++) {
					if (i > 0) {
						detail.append(", ");
					}
					detail.append(parts.get(i));
				}
				line.append(detail.append(")").withStyle(ChatFormatting.GRAY));
			}
		}
		tooltip.setLine(LINE, line);
	}

	private static final ResourceLocation WALK = new ResourceLocation(CreativeCompat.MOD_ID, "pass_walk");
	private static final ResourceLocation FALL = new ResourceLocation(CreativeCompat.MOD_ID, "pass_fall");
	private static final ResourceLocation NEIGHBOURS = new ResourceLocation(CreativeCompat.MOD_ID, "pass_neighbours");
	private static final String KEY = "creativecompat.wthit.pass_through.";

	/**
	 * Pass-through lines (plan_passthrough.md 6, {@link PassThroughFormat}) for a block in a non-inert tier, with the
	 * server's values. True when the block is in such a tier: it then gets these lines or none (below its growth rule,
	 * the viewer exempt or the owner of a SecurityCraft web), never the Run Speed line. Counts neighbors once per
	 * overlay refresh, against the looked-at block.
	 */
	private static boolean passThrough(ITooltip tooltip, IBlockAccessor accessor, CompatConfig.Surface.Wthit cfg) {
		PassValues values = PassValues.of(true);
		if (!values.enabled) return false;
		BlockState state = accessor.getBlockState();
		PassTier tier = PassThrough.tierOf(state);
		if (tier == null) return false;
		PassTier.Values v = values.tier(tier);
		if (v.inert()) return false;
		Level level = accessor.getLevel();
		BlockPos pos = accessor.getPosition();
		Player player = accessor.getPlayer();
		PassRule.Cell cell = PassThrough.cellAt(level, pos, state, values);
		// a layer height block too high (acts whole) or too low to walk through: the normal Run Speed line
		if (cell == null && tier.layered()) return false;
		if (player != null && (PassThrough.exempt(player, tier, v) || PassOwner.ownerPasses(level, pos, state, player))) {
			return true;
		}
		if (cell == null) return true;
		int[] layers = tier.layered() ? PassThrough.layers(state) : new int[]{0, 0};

		RunSpeed.Soil soil = RunSpeed.soil(level, pos, player);
		double soilFactor = soil != null ? soil.info().factor() : 1.0;
		SpeedFormat format = cfg.format.get();
		for (PassThroughFormat.Line l : PassThroughFormat.lines(cell, v, soilFactor, cfg.hideNormal.get(),
				layers[0], layers[1])) {
			switch (l.kind()) {
				case WALK -> {
					MutableComponent line = Component.translatable(KEY + "walk", RunSpeed.format(l.value(), format));
					notes(line, l);
					if (l.soil() && soil != null) {
						MutableComponent on = Component.translatable(KEY + "on_soil", soil.state().getBlock().getName());
						if (cfg.showTier.get() && soil.info().tier() != null) {
							on.append(" (").append(Component.translatable(
									"creativeengineering.surface.tier." + soil.info().tier().key())).append(")");
						}
						line.append(Component.literal(" ").append(on).withStyle(ChatFormatting.GRAY));
					}
					tooltip.setLine(WALK, line);
				}
				case FALL -> {
					MutableComponent line = Component.translatable(KEY + "fall", l.stopped()
							? Component.translatable(KEY + "stopped").withStyle(ChatFormatting.RED)
							: RunSpeed.format(l.value(), format));
					notes(line, l);
					tooltip.setLine(FALL, line);
				}
				case NEIGHBOURS -> tooltip.setLine(NEIGHBOURS,
						Component.translatable(KEY + "neighbours", (int) l.value(), l.of()));
			}
		}
		return true;
	}

	/** " (Dense neighbors, while moving through, 3 of 8 layers)" in gray, when any applies. */
	private static void notes(MutableComponent line, PassThroughFormat.Line l) {
		boolean layers = l.kind() == PassThroughFormat.Kind.WALK && l.of() > 0;
		if (!l.dense() && !l.whileMoving() && !layers) return;
		MutableComponent d = Component.literal(" (");
		boolean first = true;
		if (l.dense()) {
			d.append(Component.translatable(KEY + "dense"));
			first = false;
		}
		if (l.whileMoving()) {
			if (!first) d.append(", ");
			d.append(Component.translatable(KEY + "while_moving"));
			first = false;
		}
		if (layers) {
			if (!first) d.append(", ");
			d.append(Component.translatable(KEY + "layers", l.layers(), l.of()));
		}
		line.append(d.append(")").withStyle(ChatFormatting.GRAY));
	}
}
