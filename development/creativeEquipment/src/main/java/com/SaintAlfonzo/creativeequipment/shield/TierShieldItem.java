package com.SaintAlfonzo.creativeequipment.shield;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeequipment.shield.client.TierShieldRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * One of the seven DEDICATED tier shields ({@code creativeequipment:<tier>_shield}) - every
 * {@link ShieldTier} except {@link ShieldTier#IRON}, which is folded into the player's real
 * vanilla shield instead (see {@link ShieldTier#of}); that one has no class of its own.
 *
 * <p>A real {@link ShieldItem} subclass (not a KubeJS item) so mods that special-case
 * {@code instanceof ShieldItem} - MineColonies guards only equip / raise those - treat it as a
 * shield. It blocks through vanilla's own machinery; what makes it a tier shield is
 * {@code mixin/LivingEntityShieldMixin} (which damage it can block, and from where) and
 * {@link ShieldBlockHandler} (how much it absorbs, wear, on-block effects), both keyed off
 * {@link ShieldTier#of}, not this class specifically. It uses vanilla's shield model with its own
 * base texture per tier - see {@link TierShieldRenderer} / {@code TierShieldMaterials}.
 */
public class TierShieldItem extends ShieldItem {

    private final ShieldTier tier;

    public TierShieldItem(ShieldTier tier) {
        super(properties(tier));
        this.tier = tier;
    }

    private static Item.Properties properties(ShieldTier tier) {
        // durability() only has to be positive here (it makes the item damageable and single-stack);
        // the real maximum is read live from config in getMaxDamage(ItemStack).
        Item.Properties p = new Item.Properties()
                .tab(CreativeModeTab.TAB_COMBAT)
                .durability(tier.defaultDurability())
                .rarity(tier.rarity());
        return tier.fireproof() ? p.fireResistant() : p;
    }

    public ShieldTier tier() {
        return tier;
    }

    /** Live from config. {@code ItemStack#getMaxDamage} routes through this Forge hook. */
    @Override
    public int getMaxDamage(ItemStack stack) {
        return ShieldConfig.durability(tier);
    }

    /**
     * Vanilla's shield appends the banner colour ({@code item.minecraft.shield.white}); there is no
     * such lang key for these, so a bannered tier shield (crafting grid, via
     * {@code mixin/ShieldDecorationRecipeMixin}) keeps its plain tier name.
     */
    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId();
    }

    /** Anvil repair uses the tier's configured material instead of vanilla's planks. */
    @Override
    public boolean isValidRepairItem(ItemStack shield, ItemStack repair) {
        String id = ShieldConfig.repairItem(tier);
        if (repair.isEmpty() || id == null || id.isBlank()) {
            return false;
        }
        ResourceLocation rl = ResourceLocation.tryParse(id.trim());
        return rl != null && ForgeRegistries.ITEMS.getValue(rl) == repair.getItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag); // banner pattern lines
        ShieldTooltip.appendLines(tier, flag, tooltip);
    }

    /**
     * Forge calls this from the Item constructor on the client, before {@link #tier} is assigned,
     * so nothing here may touch it. The renderer is created lazily on first draw because
     * Minecraft's model set doesn't exist yet at registration time.
     */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    Minecraft mc = Minecraft.getInstance();
                    renderer = new TierShieldRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
                }
                return renderer;
            }
        });
    }
}
