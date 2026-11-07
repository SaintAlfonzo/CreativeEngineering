package com.SaintAlfonzo.creativemagic;

import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Creative Magic's COMMON config, {@code config/creativemagic-common.toml}. {@code [botania]} moved out of patcherMod's
 * creativeengineering-common.toml on 2026-11-04 (creative_split.md 7.6, wave 6) with its key, comment and default
 * unchanged. The first boot with Magic copies a player's value over ({@code swap.ConfigRename.carrySections}, called
 * from {@link CreativeMagic}). The static field name is the pre-carve CEConfig's, so {@code global.NCH.cfg(..., 'BOTANIA')} finds it
 * once the scripts register this class.
 */
public final class CMGConfig {

    private CMGConfig() {}

    public static final ForgeConfigSpec SPEC;
    public static final Botania BOTANIA;

    /** The toml tables carried over from creativeengineering-common.toml. */
    public static final List<String> SECTIONS = List.of("botania");

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        BOTANIA = new Botania(builder);
        SPEC = builder.build();
    }

    /** Botania tweaks ({@code compat/TrinketCaseRules} and the {@code TrinketCase*Mixin}s). */
    public static final class Botania {
        public final ForgeConfigSpec.BooleanValue trinketCaseAnyItem;

        private Botania(ForgeConfigSpec.Builder b) {
            b.comment("Botania tweaks.").push("botania");

            trinketCaseAnyItem = b.comment(
                    "Let the Trinket Case hold any item, not just trinkets, rods and mana items.",
                    "Other trinket cases and items that can't go inside container items (shulker",
                    "boxes) are still refused. Only real trinkets equip from the case. Restart to apply."
            ).define("trinketCaseAnyItem", true);

            b.pop(); // botania
        }
    }
}
