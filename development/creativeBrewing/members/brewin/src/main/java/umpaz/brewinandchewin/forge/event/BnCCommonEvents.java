package umpaz.brewinandchewin.forge.event;

import java.util.Optional;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.mixin.LivingEntityAccessor;
import umpaz.brewinandchewin.common.network.clientbound.ClearKegFluidContainerComponentsClientboundPacket;
import umpaz.brewinandchewin.common.network.clientbound.SyncNumbedHeartsClientboundPacket;
import umpaz.brewinandchewin.common.network.clientbound.SyncRagingStacksClientboundPacket;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import umpaz.brewinandchewin.common.tag.BnCTags;
import umpaz.brewinandchewin.common.utility.BnCDamageSources;
import umpaz.brewinandchewin.common.utility.BnCHeatSources;
import umpaz.brewinandchewin.forge.network.BnCNetwork;
import umpaz.brewinandchewin.forge.registry.BnCAttachments;
import vectorwing.farmersdelight.common.tag.ModTags;

/** Port of BnCCommonEvents (NeoForge 1.21) onto Forge 1.19.2 events. */
@Mod.EventBusSubscriber(modid = BrewinAndChewin.MODID)
public class BnCCommonEvents {
   @SubscribeEvent
   public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
      BnCAttachments.attach(event);
   }

   @SubscribeEvent
   public static void onPlayerJoinLevel(PlayerEvent.PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         TipsyHeartsAttachment hearts = BrewinAndChewin.getHelper().getTipsyHeartsAttachment(serverPlayer);
         if (hearts != null) {
            BrewinAndChewin.getHelper()
               .sendClientboundTracking(serverPlayer, new SyncNumbedHeartsClientboundPacket(serverPlayer.getId(), hearts.getNumbedHealth(), hearts.getTicksUntilDamage()));
         }

         RagingAttachment raging = BrewinAndChewin.getHelper().getRagingAttachment(serverPlayer);
         if (raging != null) {
            BrewinAndChewin.getHelper()
               .sendClientboundTracking(serverPlayer, new SyncRagingStacksClientboundPacket(serverPlayer.getId(), Optional.of(raging.getStacks())));
         }
      }
   }

   @SubscribeEvent
   public static void onStartTracking(PlayerEvent.StartTracking event) {
      if (event.getTarget() instanceof LivingEntity living && event.getEntity() instanceof ServerPlayer serverPlayer) {
         TipsyHeartsAttachment hearts = BrewinAndChewin.getHelper().getTipsyHeartsAttachment(living);
         if (hearts != null) {
            BrewinAndChewin.getHelper()
               .sendClientbound(serverPlayer, new SyncNumbedHeartsClientboundPacket(living.getId(), hearts.getNumbedHealth(), hearts.getTicksUntilDamage()));
         }

         RagingAttachment raging = BrewinAndChewin.getHelper().getRagingAttachment(living);
         if (raging != null) {
            BrewinAndChewin.getHelper().sendClientbound(serverPlayer, new SyncRagingStacksClientboundPacket(living.getId(), Optional.of(raging.getStacks())));
         }
      }
   }

   @SubscribeEvent
   public static void onLivingTick(LivingEvent.LivingTickEvent event) {
      LivingEntity living = event.getEntity();
      TipsyHeartsAttachment attachment = BrewinAndChewin.getHelper().getTipsyHeartsAttachment(living);
      if (attachment != null && (double)attachment.getNumbedHealth() > 0.0) {
         if (attachment.getTicksUntilDamage() > 0) {
            attachment.setTicksUntilDamage(attachment.getTicksUntilDamage() - 1);
         }

         if ((attachment.getTicksUntilDamage() <= 0 || !living.hasEffect(BnCEffects.TIPSY)) && !living.level.isClientSide()) {
            float health = living.getHealth() + living.getAbsorptionAmount();
            int remainingHealth = Mth.ceil(
               Math.min(attachment.getNumbedHealth() - (float)(health % 1.0F > attachment.getNumbedHealth() % 1.0F ? 1 : 0), health)
            );
            if (remainingHealth > 0) {
               living.hurt(BnCDamageSources.CARDIAC_ARREST, attachment.getNumbedHealth());
            }

            attachment.setNumbedHealth(0.0F);
            BrewinAndChewin.getHelper()
               .sendClientboundTracking(living, new SyncNumbedHeartsClientboundPacket(living.getId(), attachment.getNumbedHealth(), attachment.getTicksUntilDamage()));
         }
      }

      RagingAttachment.tick(living);
   }

   @SubscribeEvent
   public static void mobEffectApplicable(MobEffectEvent.Applicable event) {
      // Port: 1.19.2 has no #minecraft:undead entity tag, so undead mobs are immune by mob type as well as by the tag.
      LivingEntity living = event.getEntity();
      if ((living.getType().is(BnCTags.EntityTypes.IMMUNE_TO_INTOXICATION) || living.getMobType() == MobType.UNDEAD)
         && event.getEffectInstance().getEffect() == BnCEffects.INTOXICATION) {
         event.setResult(Event.Result.DENY);
      }
   }

   /**
    * Port: 1.19.2's LivingDamageEvent amount is already past armor, enchantments, effects and absorption. Upstream (whose
    * NeoForge event also carries the reduced amount) subtracts those reductions from the numbed health a second time;
    * here the numbed health is the damage actually held back.
    */
   @SubscribeEvent(priority = EventPriority.LOW)
   public static void onLivingDamage(LivingDamageEvent event) {
      Entity attacker = event.getSource().getEntity();
      LivingEntity target = event.getEntity();
      if (target.hasEffect(BnCEffects.TIPSY) && !BnCDamageSources.isCardiacArrest(event.getSource())) {
         int amplifier = target.getEffect(BnCEffects.TIPSY).getAmplifier();
         float maximumNumbedHealth = Mth.clamp((float)(Mth.floor((2.0F + (float)amplifier * 1.6F) / 2.0F) * 2), 1.0F, target.getMaxHealth() - 2.0F);
         if (BrewinAndChewin.getHelper().getTipsyHeartsAttachment(target) == null) {
            BrewinAndChewin.getHelper().setTipsyHeartsAttachment(target, new TipsyHeartsAttachment(0.0F, 0));
         }

         TipsyHeartsAttachment attachment = BrewinAndChewin.getHelper().getTipsyHeartsAttachment(target);
         if (attachment != null) {
            float numbedHealth = Math.min(attachment.getNumbedHealth() + event.getAmount(), maximumNumbedHealth);
            if (numbedHealth - attachment.getNumbedHealth() <= target.getHealth()) {
               event.setAmount(event.getAmount() - (numbedHealth - attachment.getNumbedHealth()));
            } else {
               numbedHealth = attachment.getNumbedHealth();
            }

            int ticksUntilDamage = 200 + 20 * amplifier;
            attachment.setNumbedHealth(numbedHealth);
            attachment.setTicksUntilDamage(ticksUntilDamage);
            BrewinAndChewin.getHelper().sendClientboundTracking(target, new SyncNumbedHeartsClientboundPacket(target.getId(), numbedHealth, ticksUntilDamage));
         }
      }

      if (attacker instanceof LivingEntity living
         && (!(living instanceof Player player) || player.getAttackStrengthScale(0.0F) > 0.8F)
         && living.hasEffect(BnCEffects.RAGING)
         && BnCDamageSources.triggersRaging(event.getSource())) {
         if (BrewinAndChewin.getHelper().getRagingAttachment(living) == null) {
            BrewinAndChewin.getHelper().setRagingAttachment(living, new RagingAttachment(0, 0));
         }

         RagingAttachment attachment = BrewinAndChewin.getHelper().getRagingAttachment(living);
         if (attachment == null) {
            return;
         }
         int stacks = Math.min(4, attachment.getStacks() + 1);
         if (stacks != attachment.getStacks() && target.level instanceof ServerLevel serverLevel) {
            double heightAddition = living.getY(1.0) - living.getY(0.5);
            serverLevel.sendParticles(
               RagingAttachment.getParticleType(stacks, 0.5F),
               target.getX(),
               target.getY(0.5),
               target.getZ(),
               12,
               target.getRandom().nextDouble() * 0.4 - 0.2,
               target.getRandom().nextDouble() * heightAddition * 2.0 - heightAddition,
               target.getRandom().nextDouble() * 0.4 - 0.2,
               0.0
            );
         }

         attachment.setStacks(stacks);
         ((LivingEntityAccessor)living).brewinandchewin$invokeUpdateEffectVisibility();
         attachment.setTicksUntilReset(Mth.ceil(2.5F * (living instanceof Player playerx ? playerx.getCurrentItemAttackStrengthDelay() : 30.0F)));
      }
   }

   @SubscribeEvent
   public static void sendClearFluidContainerTextComponents(OnDatapackSyncEvent event) {
      ClearKegFluidContainerComponentsClientboundPacket packet = new ClearKegFluidContainerComponentsClientboundPacket();
      if (event.getPlayer() != null) {
         BnCNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), packet);
      } else {
         BnCNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
      }
   }

   // --- Port: "place a temperature block near a keg" (4.5.0 does this with loot conditions on placed_block) ---

   private static final ResourceLocation TEMPERATURE_ADVANCEMENT = BrewinAndChewin.asResource("main/place_temperature_block_near_keg");

   @SubscribeEvent
   public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && event.getLevel() instanceof Level level) {
         checkTemperatureAdvancement(player, level, event.getPos());
      }
   }

   @SubscribeEvent
   public static void onItemUsedOnBlock(PlayerInteractEvent.RightClickBlock event) {
      if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
         BlockPos pos = event.getPos().immutable();
         Level level = event.getLevel();
         // Runs after the use resolves, so a campfire lit by this click counts.
         player.getServer().execute(() -> checkTemperatureAdvancement(player, level, pos));
      }
   }

   private static void checkTemperatureAdvancement(ServerPlayer player, Level level, BlockPos pos) {
      Advancement advancement = player.server.getAdvancements().getAdvancement(TEMPERATURE_ADVANCEMENT);
      if (advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone()) {
         return;
      }
      BlockState state = level.getBlockState(pos);
      boolean found;
      if (state.is(BnCBlocks.KEG)) {
         found = BlockPos.betweenClosedStream(pos.offset(-KegBlockEntity.RANGE, -KegBlockEntity.RANGE, -KegBlockEntity.RANGE),
               pos.offset(KegBlockEntity.RANGE, KegBlockEntity.RANGE, KegBlockEntity.RANGE))
            .anyMatch(p -> isTemperatureSource(level.getBlockState(p)));
      } else if (isTemperatureSource(state)) {
         found = BlockPos.betweenClosedStream(pos.offset(-KegBlockEntity.RANGE, -KegBlockEntity.RANGE, -KegBlockEntity.RANGE),
               pos.offset(KegBlockEntity.RANGE, KegBlockEntity.RANGE, KegBlockEntity.RANGE))
            .anyMatch(p -> level.getBlockState(p).is(BnCBlocks.KEG));
      } else {
         return;
      }
      if (found) {
         for (String criterion : player.getAdvancements().getOrStartProgress(advancement).getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
         }
      }
   }

   private static boolean isTemperatureSource(BlockState state) {
      BlockState source = BnCHeatSources.withoutIdleBurner(state);
      return (source.is(ModTags.HEAT_SOURCES) || source.is(BnCTags.Blocks.FREEZE_SOURCES)) && BnCHeatSources.isActive(source);
   }
}
