package com.SaintAlfonzo.baby_mobs;

import net.minecraftforge.common.ForgeConfigSpec;

/** {@code config/baby_mobs-common.toml}. Defaults are vanilla 1.21.2 / 26.1 values. */
public final class BMConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue SQUID_BABY_CHANCE;
    public static final ForgeConfigSpec.DoubleValue GLOW_SQUID_BABY_CHANCE;
    public static final ForgeConfigSpec.DoubleValue DOLPHIN_BABY_CHANCE;
    public static final ForgeConfigSpec.IntValue GROW_TICKS;
    public static final ForgeConfigSpec.BooleanValue DOLPHIN_FISH_FEEDING;
    public static final ForgeConfigSpec.BooleanValue BABY_MODELS;
    public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> KEEP_VANILLA_BABIES;
    public static final ForgeConfigSpec.EnumValue<VariantBabies> VARIANT_BABIES;

    /** How babies of variant mobs render (a coat from another mod: More Mob Variants, Quark, Cozy's Improved Cats). */
    public enum VariantBabies { OLD_MODELS, NEW_MODELS }

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Which natural spawns are babies. The first mob of a spawn group is always an adult; each further",
                "member rolls this chance (vanilla's AgeableMob rule). A spawn egg used on an adult gives a baby",
                "(vanilla); otherwise spawn eggs and /summon give adults unless the NBT says Age:-24000.").push("spawns");
        SQUID_BABY_CHANCE = b.comment("Squid (vanilla 0.05)").defineInRange("squidBabyChance", 0.05, 0.0, 1.0);
        GLOW_SQUID_BABY_CHANCE = b.comment("Glow squid (vanilla 0.05)").defineInRange("glowSquidBabyChance", 0.05, 0.0, 1.0);
        DOLPHIN_BABY_CHANCE = b.comment("Dolphin (vanilla 0.10)").defineInRange("dolphinBabyChance", 0.10, 0.0, 1.0);
        b.pop();

        b.comment("Growing up.").push("growth");
        GROW_TICKS = b.comment("Ticks a baby takes to grow up (vanilla 24000 = 20 minutes)").defineInRange("growTicks", 24000, 1, 1_000_000);
        DOLPHIN_FISH_FEEDING = b.comment("Feeding a baby dolphin any #minecraft:fishes item ages it up by 10% of the time left (vanilla)")
                .define("dolphinFishFeeding", true);
        b.pop();

        b.comment("Client.").push("client");
        BABY_MODELS = b.comment("Use the 26.1 (Tiny Takeover) baby models and textures for every remodelled baby",
                "(squid, dolphins, farm animals, pets, horses, undead and the rest); false = vanilla 1.19.2 babies,",
                "the adult model at half size").define("babyModels", true);
        VARIANT_BABIES = b.comment("Babies of variant mobs. OLD_MODELS: the species in keepVanillaBabies, and any baby whose",
                        "coat comes from another mod with no 26.1 baby (Quark pride and shiny animals, Cozy's end and",
                        "nether cats), keep the 1.19.2 baby with that coat, mixed in with remodelled babies of the same",
                        "mob. NEW_MODELS: every baby that can be remodelled is; a coat with a matching 26.1 baby gets it",
                        "(More Mob Variants ashen wolf -> ashen baby), any other coat uses the mob's regular baby.")
                .defineEnum("variantBabies", VariantBabies.OLD_MODELS);
        KEEP_VANILLA_BABIES = b.comment("With variantBabies = OLD_MODELS: entity ids whose babies all keep the 1.19.2 baby (the adult",
                        "model at half size, with the mob's own coat). Default: the mobs More Mob Variants gives coats",
                        "(owner, 2026-11-01). Cats stay remodelled; their More Mob Variants coats use the baby of the",
                        "cat's own vanilla coat.")
                .defineListAllowEmpty(java.util.List.of("keepVanillaBabies"), () -> java.util.List.of(
                        "minecraft:wolf", "minecraft:cow", "minecraft:pig", "minecraft:chicken", "minecraft:sheep", "minecraft:zombie"),
                        o -> o instanceof String);
        b.pop();

        SPEC = b.build();
    }

    private BMConfig() {}
}
