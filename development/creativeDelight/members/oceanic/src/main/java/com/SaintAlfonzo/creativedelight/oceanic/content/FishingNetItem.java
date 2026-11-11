package com.SaintAlfonzo.creativedelight.oceanic.content;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * The Fishing Net (ocean_delight.md 5.1, 5.6). Used on a kelp stalk with more kelp above it, or on tall seagrass, it
 * searches the plant: one roll of a loot table at the plant, dropped there. Blocks count through two block tags the pack
 * fills (Creative Delight {@code oceanic_delight/fishing_net_tags.js}): {@code #creativedelight:fishing_net/kelp} as
 * kelp, {@code #creativedelight:fishing_net/sea_grass} as tall seagrass; vanilla kelp and tall seagrass count without
 * them. The tables are the pack's ({@code creativedelight:gameplay/fishing_net/kelp} and {@code .../sea_grass}).
 * <p>Any use: 10 tick cooldown. A search: 50 ticks, one durability (none in creative). A kelp search breaks the stalk 1
 * time in 5, and 1 such break in 5 lets a pufferfish out; a seagrass search breaks it 1 time in 20. 92 uses, not
 * repairable, takes enchantments like a stone tool; a crafting ingredient that comes back one durability worse.
 */
public class FishingNetItem extends Item {
    public static final TagKey<Block> KELP = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("creativedelight", "fishing_net/kelp"));
    public static final TagKey<Block> SEA_GRASS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("creativedelight", "fishing_net/sea_grass"));
    public static final ResourceLocation KELP_TABLE = new ResourceLocation("creativedelight", "gameplay/fishing_net/kelp");
    public static final ResourceLocation SEA_GRASS_TABLE = new ResourceLocation("creativedelight", "gameplay/fishing_net/sea_grass");

    private final Multimap<Attribute, AttributeModifier> mainHand;

    public FishingNetItem(Properties properties) {
        super(properties.durability(92));
        mainHand = ImmutableMultimap.of(
                Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 5.0, AttributeModifier.Operation.ADDITION),
                Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.8, AttributeModifier.Operation.ADDITION));
    }

    static boolean isKelp(BlockState state) {
        return state.is(Blocks.KELP_PLANT) || state.is(KELP);
    }

    static boolean isSeaGrass(BlockState state) {
        return state.is(Blocks.TALL_SEAGRASS) || state.is(SEA_GRASS);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack net = context.getItemInHand();
        if (player != null) player.getCooldowns().addCooldown(this, 10);
        if (level.isClientSide) return InteractionResult.SUCCESS;
        level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1.0F, 1.1F);
        BlockState state = level.getBlockState(pos);
        if (isKelp(state)) {
            BlockState above = level.getBlockState(pos.above());
            if (!isKelp(above) && !above.is(Blocks.KELP)) return InteractionResult.SUCCESS;
            search(level, pos, player, net, KELP_TABLE);
            if (level.random.nextFloat() < 0.2F) {
                level.destroyBlock(pos, false);
                if (level.random.nextFloat() < 0.2F) releasePufferfish((ServerLevel) level, pos);
            }
        } else if (isSeaGrass(state)) {
            search(level, pos, player, net, SEA_GRASS_TABLE);
            if (level.random.nextFloat() < 0.05F) level.destroyBlock(pos, false);
        }
        return InteractionResult.SUCCESS;
    }

    private void search(Level level, BlockPos pos, Player player, ItemStack net, ResourceLocation table) {
        if (player != null) {
            player.getCooldowns().addCooldown(this, 50);
            net.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        ServerLevel server = (ServerLevel) level;
        Vec3 origin = Vec3.atCenterOf(pos);
        LootContext.Builder context = new LootContext.Builder(server).withParameter(LootContextParams.ORIGIN, origin)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player).withRandom(server.random);
        if (player != null) context.withLuck(player.getLuck());
        for (ItemStack drop : server.getServer().getLootTables().get(table).getRandomItems(context.create(LootContextParamSets.CHEST))) {
            ItemEntity item = new ItemEntity(server, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, drop);
            item.setPickUpDelay(10);
            server.addFreshEntity(item);
        }
    }

    private static void releasePufferfish(ServerLevel level, BlockPos pos) {
        Pufferfish fish = EntityType.PUFFERFISH.create(level);
        if (fish == null) return;
        fish.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        fish.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
        level.addFreshEntity(fish);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 1.0F;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, attacker, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 4;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? mainHand : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack back = stack.copy();
        back.setCount(1);
        back.setDamageValue(stack.getDamageValue() + 1);
        return back.getDamageValue() >= back.getMaxDamage() ? ItemStack.EMPTY : back;
    }
}
