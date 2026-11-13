// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3
// (LICENSE.txt). Holds the overheated device's block state and block entity data (D4) and shows them on goggles.
package org.patryk3211.powergrid.electricity.overheated;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.utility.Lang;

import java.util.List;

public class OverheatedComponentBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    /** Goggle lines of stored data, at most (the rest is summarised as a count). */
    private static final int MAX_DATA_LINES = 8;
    private static final List<String> HIDDEN_KEYS = List.of("id", "x", "y", "z", "ForgeData", "ForgeCaps");

    @Nullable
    private BlockState originalState;
    @Nullable
    private CompoundTag originalData;

    public OverheatedComponentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) { }

    public void setOriginal(BlockState state, @Nullable CompoundTag data) {
        this.originalState = state;
        this.originalData = data;
        setChanged();
        sendData();
    }

    @Nullable
    public BlockState getOriginalState() {
        return originalState;
    }

    @Nullable
    public CompoundTag getOriginalData() {
        return originalData;
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if(originalState != null)
            tag.put("Original", NbtUtils.writeBlockState(originalState));
        if(originalData != null)
            tag.put("OriginalData", originalData);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        originalState = tag.contains("Original") ? NbtUtils.readBlockState(tag.getCompound("Original")) : null;
        originalData = tag.contains("OriginalData") ? tag.getCompound("OriginalData") : null;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("gui.overheated_component.title").style(ChatFormatting.GRAY).forGoggles(tooltip);
        if(originalState == null) {
            Lang.translate("gui.overheated_component.unknown").style(ChatFormatting.DARK_GRAY).forGoggles(tooltip, 1);
            return true;
        }
        Lang.builder().add(originalState.getBlock().getName()).style(ChatFormatting.GOLD).forGoggles(tooltip, 1);
        Lang.translate("gui.overheated_component.state", originalState.toString()
                        .replaceFirst("^Block\\{[^}]*}", "").replace("[", "").replace("]", ""))
                .style(ChatFormatting.DARK_GRAY).forGoggles(tooltip, 1);
        if(originalData == null)
            return true;
        int shown = 0, total = 0;
        for(var key : originalData.getAllKeys().stream().sorted().toList()) {
            if(HIDDEN_KEYS.contains(key))
                continue;
            ++total;
            if(shown >= MAX_DATA_LINES || !isPlayerSneaking && shown >= 3)
                continue;
            ++shown;
            Lang.text(key + ": " + summary(originalData.get(key))).style(ChatFormatting.DARK_GRAY).forGoggles(tooltip, 1);
        }
        if(total > shown)
            Lang.translate("gui.overheated_component.more", total - shown).style(ChatFormatting.DARK_GRAY).forGoggles(tooltip, 1);
        return true;
    }

    private static String summary(@Nullable Tag tag) {
        if(tag == null)
            return "";
        if(tag instanceof NumericTag number)
            return String.format("%.4g", number.getAsDouble());
        if(tag instanceof StringTag string) {
            var s = string.getAsString();
            return s.length() > 24 ? s.substring(0, 24) + "..." : s;
        }
        if(tag instanceof CompoundTag compound)
            return "{" + compound.size() + "}";
        if(tag instanceof net.minecraft.nbt.CollectionTag<?> list)
            return "[" + list.size() + "]";
        return tag.getAsString();
    }
}
