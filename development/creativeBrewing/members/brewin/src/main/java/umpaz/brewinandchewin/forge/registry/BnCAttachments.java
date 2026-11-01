package umpaz.brewinandchewin.forge.registry;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;

/**
 * Port: NeoForge data attachments -> one Forge capability on every LivingEntity that holds both upstream attachments.
 * Each slot is null when the entity has none (upstream's "no data"). Saved as {"raging": {...}, "tipsy_hearts": {...}};
 * like the upstream attachments, nothing is copied to a respawned player.
 */
public class BnCAttachments {
   public static final Capability<Data> DATA = CapabilityManager.get(new CapabilityToken<>() {
   });
   private static final ResourceLocation KEY = BrewinAndChewin.asResource("entity_data");

   public static void registerCapabilities(RegisterCapabilitiesEvent event) {
      event.register(Data.class);
   }

   public static void attach(AttachCapabilitiesEvent<Entity> event) {
      if (event.getObject() instanceof LivingEntity) {
         Provider provider = new Provider();
         event.addCapability(KEY, provider);
         event.addListener(provider.optional::invalidate);
      }
   }

   @Nullable
   public static Data get(LivingEntity entity) {
      return entity.getCapability(DATA).resolve().orElse(null);
   }

   public static class Data {
      @Nullable
      public RagingAttachment raging;
      @Nullable
      public TipsyHeartsAttachment tipsyHearts;
   }

   private static class Provider implements ICapabilitySerializable<CompoundTag> {
      private final Data data = new Data();
      private final LazyOptional<Data> optional = LazyOptional.of(() -> this.data);

      @NotNull
      @Override
      public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
         return cap == DATA ? this.optional.cast() : LazyOptional.empty();
      }

      @Override
      public CompoundTag serializeNBT() {
         CompoundTag tag = new CompoundTag();
         if (this.data.raging != null) {
            tag.put("raging", this.data.raging.save());
         }
         if (this.data.tipsyHearts != null) {
            tag.put("tipsy_hearts", this.data.tipsyHearts.save());
         }
         return tag;
      }

      @Override
      public void deserializeNBT(CompoundTag tag) {
         this.data.raging = tag.contains("raging", 10) ? RagingAttachment.load(tag.getCompound("raging")) : null;
         this.data.tipsyHearts = tag.contains("tipsy_hearts", 10) ? TipsyHeartsAttachment.load(tag.getCompound("tipsy_hearts")) : null;
      }
   }
}
