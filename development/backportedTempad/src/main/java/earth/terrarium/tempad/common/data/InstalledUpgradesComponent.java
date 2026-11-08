// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/data/InstalledUpgradesComponent.kt. Translated to Java for
// Forge 1.19.2: Codec -> NBT {upgrades: [string]}, ByteCodec -> FriendlyByteBuf.
package earth.terrarium.tempad.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.ArrayList;
import java.util.List;

public record InstalledUpgradesComponent(List<ResourceLocation> upgrades) implements TooltipComponent {
    public static final InstalledUpgradesComponent EMPTY = new InstalledUpgradesComponent(List.of());

    public InstalledUpgradesComponent plus(ResourceLocation upgrade) {
        List<ResourceLocation> list = new ArrayList<>(upgrades);
        list.add(upgrade);
        return new InstalledUpgradesComponent(List.copyOf(list));
    }

    public InstalledUpgradesComponent minus(ResourceLocation upgrade) {
        List<ResourceLocation> list = new ArrayList<>(upgrades);
        list.remove(upgrade);
        return new InstalledUpgradesComponent(List.copyOf(list));
    }

    public boolean contains(ResourceLocation upgrade) {
        return upgrades.contains(upgrade);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ResourceLocation id : upgrades) list.add(StringTag.valueOf(id.toString()));
        tag.put("upgrades", list);
        return tag;
    }

    public static InstalledUpgradesComponent load(CompoundTag tag) {
        List<ResourceLocation> list = new ArrayList<>();
        for (Tag entry : tag.getList("upgrades", Tag.TAG_STRING)) {
            ResourceLocation id = ResourceLocation.tryParse(entry.getAsString());
            if (id != null) list.add(id);
        }
        return new InstalledUpgradesComponent(List.copyOf(list));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(upgrades, FriendlyByteBuf::writeResourceLocation);
    }

    public static InstalledUpgradesComponent read(FriendlyByteBuf buf) {
        return new InstalledUpgradesComponent(List.copyOf(buf.readList(FriendlyByteBuf::readResourceLocation)));
    }
}
