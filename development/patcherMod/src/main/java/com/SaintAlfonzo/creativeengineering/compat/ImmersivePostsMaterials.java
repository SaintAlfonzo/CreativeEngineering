package com.SaintAlfonzo.creativeengineering.compat;

import net.minecraft.world.item.ItemStack;
import twistedgate.immersiveposts.api.posts.IPostMaterial;
import twistedgate.immersiveposts.enums.EnumPostMaterial;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * The Immersive Posts materials the pack keeps. Every material registers a post block (1,920 states)
 * and a truss block (3,072 states); all 15 came to about 75k block states. The rest are never
 * registered, and their fence items no longer count as post materials, so a post base ignores them
 * instead of looking up a block that does not exist.
 *
 * <p>Registration runs in the mod's own class init, before any config is read, so the list is fixed
 * here rather than in CEConfig.
 */
public final class ImmersivePostsMaterials {

    public static final Set<EnumPostMaterial> KEPT = EnumSet.of(
            EnumPostMaterial.WOOD,
            EnumPostMaterial.ALUMINIUM,
            EnumPostMaterial.STEEL,
            EnumPostMaterial.IRON,
            EnumPostMaterial.COPPER,
            EnumPostMaterial.NICKEL);

    /** Post and truss block ids of the dropped materials, which the mod's own tag files still list. */
    private static final Set<String> DROPPED_BLOCKS = new HashSet<>();

    static {
        for (EnumPostMaterial material : EnumPostMaterial.values()) {
            if (KEPT.contains(material)) continue;
            for (String id : new String[] {"immersiveposts:" + material.getBlockName(), "immersiveposts:" + material.getBlockName() + "_truss"}) {
                DROPPED_BLOCKS.add(id);
                DROPPED_BLOCKS.add(id + "?");
            }
        }
    }

    private ImmersivePostsMaterials() {}

    /** {@code entry} is a tag file entry as written: "id", "id?" (optional) or "#tag". */
    public static boolean isDroppedBlock(String entry) {
        return DROPPED_BLOCKS.contains(entry);
    }

    /** Replaces {@code EnumPostMaterial.values()} in the post and truss registration loops. */
    public static EnumPostMaterial[] kept() {
        return KEPT.toArray(new EnumPostMaterial[0]);
    }

    /** {@code IPostMaterial.isValidItem}, false for the fence item of a dropped material. */
    public static boolean isValidItem(ItemStack stack) {
        if (!IPostMaterial.isValidItem(stack)) return false;
        return !(IPostMaterial.getPostMaterial(stack) instanceof EnumPostMaterial material) || KEPT.contains(material);
    }
}
