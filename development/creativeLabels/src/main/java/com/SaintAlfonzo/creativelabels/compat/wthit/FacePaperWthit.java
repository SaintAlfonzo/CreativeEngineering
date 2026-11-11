package com.SaintAlfonzo.creativelabels.compat.wthit;

import com.SaintAlfonzo.creativelabels.CLConfig;
import com.SaintAlfonzo.creativelabels.CLRegistry;
import com.SaintAlfonzo.creativelabels.block.FaceContent;
import com.SaintAlfonzo.creativelabels.block.FacePaperBlock;
import com.SaintAlfonzo.creativelabels.block.FacePaperBlockEntity;
import com.SaintAlfonzo.creativelabels.block.LabelData;
import com.SaintAlfonzo.creativelabels.block.NoteData;
import com.SaintAlfonzo.creativelabels.block.PieceHit;
import com.SaintAlfonzo.creativelabels.item.NoteColours;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.WailaConstants;
import mcp.mobius.waila.api.component.ItemComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * WTHIT plugin (sticky_notes.md 3.15, BRIEF 5): the piece under the crosshair.
 * <ul>
 * <li>A note: the name line becomes the note item's name ("Sticky Note", "Gold Sticky Note"), the icon the note
 * item with its colour, and the body its colour (the nearest dye's name when close, else the hex), its text in
 * quotes and "Day N, placer". Body lines obey {@code [wthit] lines}.</li>
 * <li>A label: {@link #getOverride} swaps the shown state for the host's, so WTHIT shows the host's name and icon
 * (and other plugins see the host's state; the position stays ours), plus a body line "Label: item".</li>
 * </ul>
 * Priorities: WTHIT's core name and icon run at 1100 (patcherMod's block-model icon at 1000); ours run at 900 for
 * the icon (the core icon then finds a stack already set and keeps it) and 1200 for the name (set after the core).
 * The body provider is registered on the block entity class so it still runs when the state is overridden.
 */
public final class FacePaperWthit implements IWailaClientPlugin, IBlockComponentProvider {

    private static final String LANG = "creativelabels.wthit.";
    /** Redmean distance under which a colour is called by the nearest dye's name. */
    private static final double DYE_NAME_DISTANCE = 60;

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.override(this, FacePaperBlock.class);
        registrar.icon(this, FacePaperBlock.class, 900);
        registrar.head(this, FacePaperBlock.class, 1200);
        registrar.body(this, FacePaperBlockEntity.class);
    }

    /** The piece under the crosshair, from the hit location against the cell's own block entity. */
    @Nullable
    private static PieceHit piece(IBlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (!(be instanceof FacePaperBlockEntity paper)) return null;
        BlockHitResult hit = accessor.getBlockHitResult();
        if (hit == null) return null;
        return PieceHit.resolve(paper.getBlockState(), paper, hit.getLocation());
    }

    @Override
    @Nullable
    public BlockState getOverride(IBlockAccessor accessor, IPluginConfig config) {
        PieceHit hit = piece(accessor);
        if (hit == null || !hit.isLabel()) return null;
        BlockPos hostPos = accessor.getPosition().relative(hit.face());
        BlockState host = accessor.getLevel().getBlockState(hostPos);
        return host.isAir() ? null : host;
    }

    @Override
    @Nullable
    public ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
        BlockEntity be = accessor.getBlockEntity();
        PieceHit hit = piece(accessor);
        if (hit == null || !(be instanceof FacePaperBlockEntity paper)) return null;
        if (hit.isLabel()) return new ItemComponent(new ItemStack(CLRegistry.LABEL.get()));
        NoteData note = noteOf(paper, hit);
        return note == null ? null : new ItemComponent(note.toItem());
    }

    @Override
    public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        BlockEntity be = accessor.getBlockEntity();
        PieceHit hit = piece(accessor);
        if (hit == null || !(be instanceof FacePaperBlockEntity paper)) return;
        NoteData note = noteOf(paper, hit);
        if (note == null) return;
        tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, note.toItem().getHoverName().copy().withStyle(ChatFormatting.WHITE));
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!CLConfig.on(CLConfig.WTHIT_LINES)) return;
        BlockEntity be = accessor.getBlockEntity();
        PieceHit hit = piece(accessor);
        if (hit == null || !(be instanceof FacePaperBlockEntity paper)) return;
        FaceContent face = paper.face(hit.face());
        if (hit.isLabel()) {
            LabelData label = face.label;
            if (label != null && label.hasPicture()) {
                tooltip.addLine(Component.translatable(LANG + "label", label.picture.getHoverName()).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.addLine(Component.translatable(LANG + "label.empty").withStyle(ChatFormatting.GRAY));
            }
            return;
        }
        NoteData note = noteOf(paper, hit);
        if (note == null) return;
        if (note.isDyed() && !note.isSpecial()) {
            tooltip.addLine(colourName(note.color).withStyle(ChatFormatting.GRAY));
        }
        if (note.isWritten()) {
            tooltip.addLine(Component.translatable(LANG + "note.text", note.text).withStyle(ChatFormatting.GRAY));
        }
        if (!note.placer.isEmpty()) {
            tooltip.addLine(Component.translatable(LANG + "note.day", note.day, note.placer).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /** The nearest dye's name when the colour is close to a single-dye result, else the hex value. */
    static MutableComponent colourName(int rgb) {
        DyeColor dye = NoteColours.nearestDye(rgb);
        if (NoteColours.distance(rgb, NoteColours.of(dye)) <= DYE_NAME_DISTANCE) {
            return Component.translatable("color.minecraft." + dye.getName());
        }
        return Component.literal(NoteColours.hex(rgb));
    }

    /** The note of a NOTE hit, or the nearest note of a PANEL hit (a covered face); null for labels. */
    @Nullable
    private static NoteData noteOf(FacePaperBlockEntity paper, PieceHit hit) {
        FaceContent face = paper.face(hit.face());
        if (hit.isNote()) return face.note(hit.spot());
        if (hit.isPanel()) {
            for (int spot = 0; spot < face.notes.length; spot++) {
                NoteData n = face.note(spot);
                if (n != null) return n;
            }
        }
        return null;
    }
}
