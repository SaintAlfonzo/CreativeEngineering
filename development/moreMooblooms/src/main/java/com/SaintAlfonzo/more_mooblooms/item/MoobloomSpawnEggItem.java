package com.SaintAlfonzo.more_mooblooms.item;

import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.SaintAlfonzo.more_mooblooms.entity.MMEntities;
import com.SaintAlfonzo.more_mooblooms.entity.Moobloom;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariant;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariants;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * One spawn egg item for every variant (D14): variants are data, so they can't each have an item. The variant
 * rides in {@code EntityTag.Variant}, which vanilla applies to the spawned moobloom; a plain egg spawns a random
 * variant. Creative tabs and JEI list one stack per synced variant (D14: all eggs visible in JEI), the name
 * and colours follow the variant ({@link #getName}, client/ClientSetup's colour handler).
 */
public class MoobloomSpawnEggItem extends ForgeSpawnEggItem {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MoreMooblooms.ID);

    public static final RegistryObject<MoobloomSpawnEggItem> MOOBLOOM_SPAWN_EGG = ITEMS.register("moobloom_spawn_egg",
            () -> new MoobloomSpawnEggItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public MoobloomSpawnEggItem(Item.Properties props) {
        super(MMEntities.MOOBLOOM, 0xFDD500, 0xF7EDC1, props);
    }

    public static ItemStack forVariant(ResourceLocation variant) {
        ItemStack stack = new ItemStack(MOOBLOOM_SPAWN_EGG.get());
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("Variant", variant.toString());
        stack.getOrCreateTag().put("EntityTag", entityTag);
        return stack;
    }

    @Nullable
    public static ResourceLocation variantOf(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("EntityTag", 10)) return null;
        CompoundTag entityTag = tag.getCompound("EntityTag");
        return entityTag.contains("Variant", 8) ? ResourceLocation.tryParse(entityTag.getString("Variant")) : null;
    }

    /** Client-side lookup of a stack's variant (null = plain egg or unknown variant). */
    @Nullable
    public static MoobloomVariant clientVariant(ItemStack stack) {
        ResourceLocation id = variantOf(stack);
        return id == null ? null : MoobloomVariants.all(true).get(id);
    }

    /**
     * Using the egg on a moobloom makes a calf. Vanilla asks the clicked moobloom for {@code getBreedOffspring},
     * which would roll the breeding rules, and never reads the egg's {@code EntityTag}; here the calf is the
     * egg's variant (a plain egg: the clicked moobloom's). Otherwise vanilla's steps, with the variant set before
     * the calf joins the world.
     */
    @Override
    public Optional<Mob> spawnOffspringFromSpawnEgg(Player player, Mob mob, EntityType<? extends Mob> type,
                                                    ServerLevel level, Vec3 pos, ItemStack stack) {
        if (!(mob instanceof Moobloom parent)) return super.spawnOffspringFromSpawnEgg(player, mob, type, level, pos, stack);
        if (!spawnsEntity(stack.getTag(), type)) return Optional.empty();
        Moobloom child = MMEntities.MOOBLOOM.get().create(level);
        if (child == null) return Optional.empty();
        ResourceLocation id = variantOf(stack);
        child.setVariant(MoobloomVariants.has(false, id) ? MoobloomVariants.get(false, id) : parent.getVariant());
        child.setBaby(true);
        child.moveTo(pos.x(), pos.y(), pos.z(), 0.0F, 0.0F);
        level.addFreshEntityWithPassengers(child);
        if (stack.hasCustomHoverName()) child.setCustomName(stack.getHoverName());
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return Optional.of(child);
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (!allowedIn(tab)) return;
        // The creative screen and JEI only ever run on the client, which has the synced table. Before the
        // first sync (or on a dedicated server) the plain egg stands in.
        boolean listed = false;
        if (FMLEnvironment.dist.isClient()) {
            for (MoobloomVariant v : MoobloomVariants.values(true)) {
                items.add(forVariant(v.id()));
                listed = true;
            }
        }
        if (!listed) items.add(new ItemStack(this));
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation id = variantOf(stack);
        if (id == null) return super.getName(stack);
        String key = id.getNamespace().equals(MoreMooblooms.ID) ? id.getPath() : id.getNamespace() + "." + id.getPath();
        return Component.translatable("item.more_mooblooms.moobloom_spawn_egg.variant",
                Component.translatable("entity.more_mooblooms.moobloom." + key));
    }
}
