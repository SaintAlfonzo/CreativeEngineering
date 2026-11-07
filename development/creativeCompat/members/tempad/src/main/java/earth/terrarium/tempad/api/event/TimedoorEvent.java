// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/event/TimedoorEvent.kt. Translated to Java for Forge 1.19.2:
// ICancellableEvent -> @Cancelable; posted on MinecraftForge.EVENT_BUS.
package earth.terrarium.tempad.api.event;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TimedoorEvent extends EntityEvent {
    public TimedoorEvent(TimedoorEntity timedoor) {
        super(timedoor);
    }

    @Override
    public TimedoorEntity getEntity() {
        return (TimedoorEntity) super.getEntity();
    }

    @Cancelable
    public static class Open extends TimedoorEvent {
        @Nullable
        public final ResourceLocation provider;
        @Nullable
        public final UUID locationId;
        public final GameProfile opener;
        @Nullable
        public Component errorMessage;

        public Open(TimedoorEntity timedoor, @Nullable ResourceLocation provider, @Nullable UUID locationId, GameProfile opener) {
            super(timedoor);
            this.provider = provider;
            this.locationId = locationId;
            this.opener = opener;
        }

        /** If you cancel the event, you should provide a reason why. */
        public void fail(Component message) {
            setCanceled(true);
            errorMessage = message;
        }
    }

    @Cancelable
    public static class OpenWithItem extends Open {
        public final SyncableContext<?> tempadCtx;

        public OpenWithItem(TimedoorEntity timedoor, GameProfile opener, SyncableContext<?> tempadCtx,
                            @Nullable ResourceLocation provider, @Nullable UUID locationId) {
            super(timedoor, provider, locationId, opener);
            this.tempadCtx = tempadCtx;
        }
    }

    @Cancelable
    public static class OpenWithBlock extends Open {
        public final BlockEntity block;

        public OpenWithBlock(TimedoorEntity timedoor, GameProfile opener, BlockEntity block,
                             @Nullable ResourceLocation provider, @Nullable UUID locationId) {
            super(timedoor, provider, locationId, opener);
            this.block = block;
        }
    }

    public static class Close extends TimedoorEvent {
        public Close(TimedoorEntity timedoor) {
            super(timedoor);
        }
    }

    @Cancelable
    public static class Enter extends TimedoorEvent {
        public final Entity teleportee;

        public Enter(TimedoorEntity timedoor, Entity teleportee) {
            super(timedoor);
            this.teleportee = teleportee;
        }
    }

    public static class Exit extends TimedoorEvent {
        public final Entity teleportee;

        public Exit(TimedoorEntity timedoor, Entity teleportee) {
            super(timedoor);
            this.teleportee = teleportee;
        }
    }
}
