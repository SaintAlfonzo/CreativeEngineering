// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/compat/CuriosCompat.kt. Translated to Java for Forge 1.19.2
// against Curios 5.1 (curios-forge-1.19.2-5.1.6.4): CuriosApi.getCuriosInventory(player) -> CuriosApi.getCuriosHelper()
// .getCuriosHandler(player); ObjectByteCodec -> BufCodec. Upstream's data/tempad/curios/ slot and entity files (the
// 1.20+ data format, ignored by Curios 5.1) become the IMC slot requests in enqueueSlots.
// Loaded only when Curios is (ModContext and Tempad check ModList first), TP-D8.
package earth.terrarium.tempad.common.compat;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.Priority;
import earth.terrarium.tempad.api.PriorityId;
import earth.terrarium.tempad.api.context.ContextRegistry;
import earth.terrarium.tempad.api.context.ContextType;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.common.utils.BufCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;

public final class CuriosCompat {
    private CuriosCompat() {
    }

    public record CuriosSlotInfo(String identifier, int index) {
        public static final BufCodec<CuriosSlotInfo> CODEC = BufCodec.of(
                (buf, info) -> {
                    buf.writeUtf(info.identifier());
                    buf.writeInt(info.index());
                },
                buf -> new CuriosSlotInfo(buf.readUtf(), buf.readInt()));
    }

    public static class CuriosContext implements SyncableContext<CuriosSlotInfo> {
        public static final ResourceLocation ID = Tempad.id("curios");
        public static final ContextType<CuriosSlotInfo> TYPE = new ContextType<>(ID, CuriosSlotInfo.CODEC);

        public final Player player;
        private final CuriosSlotInfo data;
        public final Map<String, ICurioStacksHandler> inventory;

        public CuriosContext(Player player, CuriosSlotInfo data) {
            this.player = player;
            this.data = data;
            this.inventory = handler(player).orElseThrow().getCurios();
        }

        @Override
        public ContextType<CuriosSlotInfo> getType() {
            return TYPE;
        }

        @Override
        public CuriosSlotInfo getData() {
            return data;
        }

        @Override
        public ItemStack getStack() {
            return inventory.get(data.identifier()).getStacks().getStackInSlot(data.index());
        }

        @Override
        public void setStack(ItemStack stack) {
            inventory.get(data.identifier()).getStacks().setStackInSlot(data.index(), stack);
        }

        @Override
        public void addStack(ItemStack stack) {
            player.getInventory().placeItemBackInInventory(stack);
        }
    }

    private static java.util.Optional<ICuriosItemHandler> handler(Player player) {
        return CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve();
    }

    /** Upstream {@code initCuriosCompat()} (common). */
    public static void init() {
        ContextRegistry.register(CuriosContext.TYPE, CuriosContext::new);
        ContextRegistry.registerLocator(new PriorityId(CuriosContext.ID, Priority.HIGH), (player, filter) -> {
            // Upstream calls orElseThrow() here; a player without the Curios capability (a fake player) finds nothing instead.
            ICuriosItemHandler curios = handler(player).orElse(null);
            if (curios == null) return null;
            for (Map.Entry<String, ICurioStacksHandler> entry : curios.getCurios().entrySet()) {
                ICurioStacksHandler handler = entry.getValue();
                for (int index = 0; index < handler.getStacks().getSlots(); index++) {
                    ItemStack stack = handler.getStacks().getStackInSlot(index);
                    if (filter.test(stack)) return new CuriosContext(player, new CuriosSlotInfo(entry.getKey(), index));
                }
            }
            return null;
        });
    }

    /**
     * Upstream {@code data/tempad/curios/entities/tempad.json} (players get bracelet, charm, belt) and
     * {@code data/tempad/curios/slots/charm.json} (charm size 3). Curios 5.1 takes slot types only by IMC; with several
     * requests for one slot it keeps the largest size. The item tags data/curios/tags/item/{belt, bracelet, charm} come
     * from the upstream jar (UpstreamTranslator).
     */
    public static void enqueueSlots(InterModEnqueueEvent event) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BRACELET.getMessageBuilder().build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.CHARM.getMessageBuilder().size(3).build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BELT.getMessageBuilder().build());
    }
}
