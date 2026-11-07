package com.SaintAlfonzo.creativecompat.surface;

/**
 * BlockRunner's {@code blockrunner:surface/*} speed tiers, fastest first. Each one gets a walk-speed
 * entry under CompatConfig {@code [surface.speed]}; {@link SurfaceSpeeds} feeds those values into
 * BlockRunner in place of {@code config/blockrunner.json}. The tags themselves are filled in
 * kubejs blocktags.js ({@code var runner}) - a new tier needs an entry in both places.
 */
public enum SurfaceTier {
    ASPHALT("asphalt", 1.50, "Asphalt: Immersive Petroleum asphalt, Create D&D coloured asphalt."),
    CONCRETE("concrete", 1.40, "Concrete: vanilla concrete, IE concrete and hempcrete, Engineer's Decor rebar concrete, cinder blocks."),
    GRATING("grating", 1.30, "See-through walkway floors: catwalks, floor grating, Quark grate, metal scaffolding."),
    METAL("metal", 1.25, "Solid metal: storage blocks, copper, sheet metal and plating, metal fences, gates and trapdoors."),
    SLIPPERY("slippery", 1.25, "Ice and soap: ice, packed/blue ice, soap block, thin ice."),
    CONSTRUCTED_SMOOTH("constructed_smooth", 1.25, "Cut, polished and tiled stone: smooth stone, polished stones, quartz, glazed terracotta."),
    CONSTRUCTED_BRICK("constructed_brick", 1.25, "Bricks and masonry: every brick and tile family, prismarine, purpur, shingles."),
    NATURAL_PATH("natural_path", 1.15, "Trodden ground: dirt path, coarse dirt, gravel, cobblestone families."),
    WOOD("wood", 1.10, "Wood: planks, logs, wooden slabs/stairs, fences, gates and trapdoors, scaffolding."),
    NATURAL_SMOOTH("natural_smooth", 1.05, "Raw rock: stone, deepslate, tuff, calcite, terracotta, ores, obsidian."),
    MACHINERY("machinery", 1.00, "Reserved for Create/IE machines. Not filled yet."),
    FUNCTIONAL("functional", 1.00, "Reserved for chests, furnaces and workstations. Not filled yet."),
    DECORATIVE("decorative", 1.00, "Reserved for furniture, lamps and signage. Not filled yet."),
    CAMOUFLAGE("camouflage", 1.00, "Framed / copycat blocks. Only used by an empty frame, or by every frame when [surface.camouflage] copyCamo is false."),
    GLASS("glass", 1.00, "Glass blocks, framed glass, windows, sea lantern."),
    DIRT("dirt", 1.00, "Leftover #minecraft:dirt: grass block, dirt, rooted dirt, rich soil."),
    NATURAL_ROUGH("natural_rough", 0.92, "Rough, crumbly ground: podzol, mycelium, nylium, netherrack, basalt, raw ore blocks."),
    SOFT("soft", 0.88, "Soft and fibrous: wool, hay, moss, beds, sponge, sculk. Full blocks also sink (see [surface.sinking])."),
    DUST("dust", 0.85, "Loose powder: sand, concrete powder, dust piles, sawdust. Full blocks also sink."),
    CONFECTIONERY("confectionery", 0.85, "Edible blocks: chocolate, cake, pies, melon, pumpkins."),
    TILLED("tilled", 0.80, "Crop fields: farmland, rich soil farmland, tilled end stone."),
    LEAVES("leaves", 0.78, "Leaves and hedges."),
    SNOW("snow", 0.75, "Snow block, snow layers (3+), powder snow."),
    STICKY("sticky", 0.65, "One sticky face: sticky pistons and their heads, sticky launchers (see [surface.sticky])."),
    SPONGY("spongy", 0.60, "Squelching ground: mud, clay, wet sponge, dough, flesh blocks. Full blocks also sink."),
    VERY_STICKY("very_sticky", 0.45, "Whole slime and honey blocks. Full blocks also sink.");

    private final String key;
    private final double defaultSpeed;
    private final String description;

    SurfaceTier(String key, double defaultSpeed, String description) {
        this.key = key;
        this.defaultSpeed = defaultSpeed;
        this.description = description;
    }

    /** Toml key under {@code [surface.speed]}, and the tag path after {@code blockrunner:surface/}. */
    public String key() {
        return key;
    }

    public double defaultSpeed() {
        return defaultSpeed;
    }

    public String description() {
        return description;
    }

    /** The key BlockRunner's json uses for this tier's tag. */
    public String jsonKey() {
        return "#blockrunner:surface/" + key;
    }
}
