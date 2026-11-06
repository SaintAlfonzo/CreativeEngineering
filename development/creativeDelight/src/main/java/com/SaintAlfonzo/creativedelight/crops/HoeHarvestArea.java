package com.SaintAlfonzo.creativedelight.crops;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.tweaks.module.HoeHarvestingModule;

/**
 * Square area for Quark's hoe harvesting (right-click replant and the area break), sized per hoe.
 * Quark only knows odd squares around the block (3x3, 5x5); this adds 1x1 to 9x9, even sizes
 * included. The size comes from the item tags {@code creativeengineering:hoe_harvest/<N>x<N>}
 * (kubejs equipmenttags.js "Hoe harvest area"); when a hoe is in several, the largest wins. A hoe in
 * none keeps Quark's own size (its {@code Regular / High Tier Hoe Radius} config).
 *
 * <p>An odd area is centred on the block. An even area can't be, so it reaches one block further
 * on the side the player is looking toward, on each axis (a 2x2 is the block plus the ones ahead).
 *
 * <p>Used by {@code mixin/QuarkHoeAreaBreakMixin}, {@code mixin/QuarkHoeAreaHarvestMixin} and the
 * tooltip ({@link HoeHarvestTooltip}).
 */
public final class HoeHarvestArea {

    private HoeHarvestArea() {}

    public static final int MAX_SIZE = 9;

    /** Most extra levels a hoe area click climbs up a column of own-pick crops (tomatoes on ropes). */
    public static final int MAX_CLIMB = 6;

    /** TAGS[n] = {@code creativeengineering:hoe_harvest/nxn}; index 0 unused. */
    private static final TagKey<Item>[] TAGS = makeTags();

    @SuppressWarnings("unchecked")
    private static TagKey<Item>[] makeTags() {
        TagKey<Item>[] tags = new TagKey[MAX_SIZE + 1];
        for (int n = 1; n <= MAX_SIZE; n++) {
            tags[n] = ItemTags.create(new ResourceLocation(CreativeDelight.NS, "hoe_harvest/" + n + "x" + n));
        }
        return tags;
    }

    /** Side length of the hoe's square area. 1 for anything that isn't a hoe or with Quark's module off. */
    public static int size(ItemStack stack) {
        if (!ModuleLoader.INSTANCE.isModuleEnabled(HoeHarvestingModule.class) || !HoeHarvestingModule.isHoe(stack)) {
            return 1;
        }
        for (int n = MAX_SIZE; n >= 1; n--) {
            if (stack.is(TAGS[n])) return n;
        }
        return 2 * HoeHarvestingModule.getRange(stack) - 1;
    }

    /** Offsets from the target block, inclusive on both ends. */
    public record Area(int size, int minX, int maxX, int minZ, int maxZ) {}

    public static Area of(Player player, ItemStack stack) {
        int size = size(stack);
        Vec3 look = player.getLookAngle();
        int[] x = span(size, look.x);
        int[] z = span(size, look.z);
        return new Area(size, x[0], x[1], z[0], z[1]);
    }

    /** {min, max} along one axis: centred when odd, the extra block on the {@code look} side when even. */
    private static int[] span(int size, double look) {
        int back = (size - 1) / 2;
        int ahead = size - 1 - back;
        return look < 0 ? new int[] {-ahead, back} : new int[] {-back, ahead};
    }
}
