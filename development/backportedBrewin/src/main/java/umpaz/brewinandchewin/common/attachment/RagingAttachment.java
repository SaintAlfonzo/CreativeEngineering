package umpaz.brewinandchewin.common.attachment;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.particle.RagingParticleOptions;
import umpaz.brewinandchewin.common.mixin.LivingEntityAccessor;
import umpaz.brewinandchewin.common.network.clientbound.SyncRagingStacksClientboundPacket;
import umpaz.brewinandchewin.common.registry.BnCEffects;

public class RagingAttachment {
   public static final float RESET_TICK_MULTIPLIER = 2.5F;
   public static final ResourceLocation ID = BrewinAndChewin.asResource("raging");
   private int stacks;
   private int ticksUntilReset;
   private int previousStacks = 0;
   // Port: 1.19.2 attribute modifiers are keyed by UUID; this one is fixed (name-based on "brewinandchewin:raging").
   private static final UUID RAGING_ATTRIBUTE_ID = UUID.nameUUIDFromBytes("brewinandchewin:raging".getBytes(java.nio.charset.StandardCharsets.UTF_8));

   public RagingAttachment(int stacks, int ticksUntilReset) {
      this.stacks = stacks;
      this.ticksUntilReset = ticksUntilReset;
   }

   public int getStacks() {
      return this.stacks;
   }

   public void setStacks(int value) {
      this.stacks = value;
   }

   public int getTicksUntilReset() {
      return this.ticksUntilReset;
   }

   public void setTicksUntilReset(int value) {
      this.ticksUntilReset = value;
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putInt("stacks", this.stacks);
      tag.putInt("ticks_until_reset", this.ticksUntilReset);
      return tag;
   }

   public static RagingAttachment load(CompoundTag tag) {
      return new RagingAttachment(tag.getInt("stacks"), tag.getInt("ticks_until_reset"));
   }

   public static void tick(LivingEntity living) {
      RagingAttachment attachment = BrewinAndChewin.getHelper().getRagingAttachment(living);
      if (attachment != null) {
         if (!living.level.isClientSide()) {
            AttributeInstance attackSpeed = living.getAttribute(Attributes.ATTACK_SPEED);
            if (attachment.getTicksUntilReset() <= 0 && attachment.getStacks() > 0) {
               attachment.setStacks(attachment.getStacks() - 1);
               attachment.setTicksUntilReset(Mth.ceil(2.5F * (living instanceof Player player ? player.getCurrentItemAttackStrengthDelay() : 30.0F)));
            }

            if (attachment.getStacks() <= 0 || !living.hasEffect(BnCEffects.RAGING)) {
               if (attackSpeed != null && attackSpeed.getModifier(RAGING_ATTRIBUTE_ID) != null) {
                  attackSpeed.removeModifier(RAGING_ATTRIBUTE_ID);
               }

               if (attachment.previousStacks != 0) {
                  attachment.previousStacks = 0;
                  BrewinAndChewin.getHelper().setRagingAttachment(living, null);
                  BrewinAndChewin.getHelper().sendClientboundTracking(living, new SyncRagingStacksClientboundPacket(living.getId(), Optional.empty()));
               }

               ((LivingEntityAccessor)living).brewinandchewin$invokeUpdateEffectVisibility();
               return;
            }

            attachment.setTicksUntilReset(attachment.getTicksUntilReset() - 1);
            if (attachment.previousStacks != attachment.stacks) {
               if (attackSpeed != null) {
                  if (attackSpeed.getModifier(RAGING_ATTRIBUTE_ID) != null) {
                     attackSpeed.removeModifier(RAGING_ATTRIBUTE_ID);
                  }

                  attackSpeed.addTransientModifier(
                     new AttributeModifier(
                        RAGING_ATTRIBUTE_ID,
                        ID.toString(),
                        Math.min(
                           0.8,
                           0.05 * (double)attachment.getStacks()
                              + 0.025 * (double)living.getEffect(BnCEffects.RAGING).getAmplifier() * (double)attachment.getStacks()
                        ),
                        Operation.MULTIPLY_TOTAL
                     )
                  );
               }
               ((LivingEntityAccessor)living).brewinandchewin$invokeUpdateEffectVisibility();
               BrewinAndChewin.getHelper()
                  .sendClientboundTracking(living, new SyncRagingStacksClientboundPacket(living.getId(), Optional.of(attachment.getStacks())));
            }

            attachment.previousStacks = attachment.stacks;
         }
      }
   }

   public static ParticleOptions getParticleType(int stacks, float size) {
      return switch (stacks) {
         case 0, 1 -> new RagingParticleOptions.StageOne(size);
         case 2 -> new RagingParticleOptions.StageTwo(size);
         case 3 -> new RagingParticleOptions.StageThree(size);
         default -> new RagingParticleOptions.StageFour(size);
      };
   }
}
