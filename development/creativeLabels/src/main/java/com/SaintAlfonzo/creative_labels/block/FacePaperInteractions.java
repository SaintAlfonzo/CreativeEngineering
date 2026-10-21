package com.SaintAlfonzo.creative_labels.block;

import com.SaintAlfonzo.creative_labels.CLConfig;
import com.SaintAlfonzo.creative_labels.CLRegistry;
import com.SaintAlfonzo.creative_labels.client.ClientHooks;
import com.SaintAlfonzo.creative_labels.item.EraserItem;
import com.SaintAlfonzo.creative_labels.item.LabelItem;
import com.SaintAlfonzo.creative_labels.item.NoteColours;
import com.SaintAlfonzo.creative_labels.item.StickyNoteItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;

/**
 * Everything a player does to face paper (sticky_notes.md 3.2, 3.4, 3.5, 3.10; BRIEF 3): the RightClickBlock
 * handler that lets sneak + item reach {@link FacePaperBlock#use}, placement of notes and labels from the items'
 * {@code useOn}, the per-piece actions of the use tables, piece removal, and the host-gone re-fit.
 *
 * <p>The decisions are pure and testable without a level: {@link #chooseSpot}, {@link #refit},
 * {@link #removePiece(FaceContent, PieceHit, Vec3)}, {@link #pickedUp}, {@link #erase}. The methods taking a
 * {@link Level} apply them and keep the block state's face booleans in step with the block entity's content
 * ({@link #applyState}): a face with nothing left clears its boolean, an empty cell turns back into air or water.
 */
public final class FacePaperInteractions {

    public static final long TICKS_PER_DAY = 24000L;

    private FacePaperInteractions() {}

    // ------------------------------------------------------------------------------------------- sneak clicks

    /** Vanilla skips a block's use when sneaking with an item; paper still wants it (dye, eraser, another note). */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().getBlockState(event.getPos()).is(CLRegistry.FACE_PAPER.get())) {
            event.setUseBlock(Event.Result.ALLOW);
        }
    }

    // ------------------------------------------------------------------------------------------- pure decisions

    /** The spot under the point when it is free, else the nearest free spot; -1 when nothing is free. */
    public static int chooseSpot(Direction face, @Nullable Direction rotation, Vec3 local, int freeMask) {
        if (freeMask == 0) return -1;
        int at = Spots.spotAt(face, rotation, local);
        if (at >= 0 && (freeMask & (1 << at)) != 0) return at;
        return Spots.nearestSpot(face, rotation, local, freeMask);
    }

    /**
     * Re-checks a face against its host's current fit: every note whose spot no longer fits and a label that no
     * longer fits are removed and returned (as a FaceContent, so {@link FaceContent#drops()} gives their items);
     * the inset follows the host. An emptied face is cleared (rotation and inset reset).
     */
    public static FaceContent refit(FaceContent c, Spots.Fit fit) {
        FaceContent removed = new FaceContent();
        if (c.isEmpty()) return removed;
        if (c.kind == FaceContent.Kind.LABEL) {
            if (!fit.labelFits()) {
                LabelData l = c.removeLabel();
                if (l != null) removed.setLabel(l);
            } else {
                c.inset = fit.inset();
            }
            return removed;
        }
        for (int spot = 0; spot < Spots.COUNT; spot++) {
            if (c.notes[spot] == null || fit.allows(spot)) continue;
            NoteData n = c.removeNote(spot);
            if (n != null) removed.putNote(spot, n);
        }
        if (!c.isEmpty()) c.inset = fit.inset();
        return removed;
    }

    /**
     * Takes one piece off a face: the hit note, the label, or (panel of a covered face) the note nearest to the
     * cell-local point. Returns the removed piece as a FaceContent (empty when nothing was there).
     */
    public static FaceContent removePiece(FaceContent c, PieceHit piece, Vec3 local) {
        FaceContent removed = new FaceContent();
        switch (piece.kind()) {
            case LABEL -> {
                LabelData l = c.removeLabel();
                if (l != null) removed.setLabel(l);
            }
            case NOTE -> {
                NoteData n = c.removeNote(piece.spot());
                if (n != null) removed.putNote(piece.spot(), n);
            }
            case PANEL -> {
                int spot = Spots.nearestSpot(piece.face(), c.rotation, local, c.noteMask());
                if (spot >= 0) {
                    NoteData n = c.removeNote(spot);
                    if (n != null) removed.putNote(spot, n);
                }
            }
        }
        return removed;
    }

    /** The note a sneak + empty-hand pick-up gives back: a copy; a special loses its look unless keepSpecial. */
    public static NoteData pickedUp(NoteData note, boolean keepSpecial) {
        NoteData out = note.copy();
        if (!keepSpecial && out.isSpecial()) {
            out.special = null;
            out.color = NoteData.NO_COLOR;
        }
        return out;
    }

    /** The eraser on a placed note: text first; a blank note goes back to plain. False when nothing changed. */
    public static boolean erase(NoteData note) {
        if (note.isWritten()) {
            note.text = "";
            return true;
        }
        if (!note.isPlain()) {
            note.color = NoteData.NO_COLOR;
            note.special = null;
            return true;
        }
        return false;
    }

    /** The face rotation a piece placed now would use: the existing one, else the player's facing on floors and ceilings. */
    @Nullable
    public static Direction rotationFor(Direction face, @Nullable FaceContent existing, Player player) {
        if (existing != null && !existing.isEmpty()) return existing.rotation;
        return face.getAxis().isVertical() ? player.getDirection() : null;
    }

    /** A cell that can take paper: air, water or paper already. */
    public static boolean canHold(BlockState cell) {
        return cell.is(CLRegistry.FACE_PAPER.get()) || cell.isAir() || cell.is(Blocks.WATER);
    }

    public static long day(Level level) {
        return level.getDayTime() / TICKS_PER_DAY;
    }

    // ------------------------------------------------------------------------------------------- placement

    /** Both items' useOn: the cell and face from the click, then {@link #placeNote} or {@link #placeLabel}. */
    public static InteractionResult placeFromContext(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = ctx.getLevel();
        BlockPos clicked = ctx.getClickedPos();
        BlockState clickedState = level.getBlockState(clicked);
        BlockPos cell;
        Direction face;
        if (clickedState.is(CLRegistry.FACE_PAPER.get())) {
            FacePaperBlockEntity be = FacePaperBlock.entity(level, clicked);
            PieceHit piece = be == null ? null : PieceHit.resolve(clickedState, be, ctx.getClickLocation());
            if (piece == null) return InteractionResult.FAIL; // paper clicked but no piece under the hit: nothing
            cell = clicked;
            face = piece.face();
        } else {
            cell = clicked.relative(ctx.getClickedFace());
            face = ctx.getClickedFace().getOpposite();
        }
        return place(level, player, ctx.getItemInHand(), cell, face, ctx.getClickLocation());
    }

    /** Dispatches on the held item: a note or a label; anything else PASS. */
    public static InteractionResult place(Level level, Player player, ItemStack stack, BlockPos cell, Direction face, Vec3 hit) {
        if (stack.getItem() instanceof StickyNoteItem) return placeNote(level, player, stack, cell, face, hit);
        if (stack.getItem() instanceof LabelItem) return placeLabel(level, player, stack, cell, face, hit);
        return InteractionResult.PASS;
    }

    /**
     * Places one note from {@code stack} on face {@code face} of {@code cell} (host at cell.relative(face)): the
     * free allowed spot under the hit, else the nearest free one; nothing free -> FAIL (3.4 row 1 "nothing": FAIL
     * stops vanilla from going on to the in-air use, PASS would open the note screen or label the off hand). PASS
     * only when the stack is not a note at all. The first piece on a floor or ceiling stores the player's facing.
     * A plain note rolls for gold on the server (C13).
     */
    public static InteractionResult placeNote(Level level, Player player, ItemStack stack, BlockPos cell, Direction face, Vec3 hit) {
        if (!(stack.getItem() instanceof StickyNoteItem) || stack.isEmpty()) return InteractionResult.PASS;
        BlockState cellState = level.getBlockState(cell);
        if (!canHold(cellState)) return InteractionResult.FAIL;
        BlockPos hostPos = cell.relative(face);
        BlockState host = level.getBlockState(hostPos);
        FacePaperBlockEntity be = FacePaperBlock.entity(level, cell);
        FaceContent existing = be == null ? null : be.face(face);
        if (existing != null && existing.kind == FaceContent.Kind.LABEL) return InteractionResult.FAIL;
        Direction rotation = rotationFor(face, existing, player);
        Spots.Fit fit = Spots.fit(host, level, hostPos, face, rotation);
        int free = fit.free(existing == null ? 0 : existing.noteMask());
        if (free == 0) return InteractionResult.FAIL;
        int spot = chooseSpot(face, rotation, local(cell, hit), free);
        if (spot < 0) return InteractionResult.FAIL;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        be = ensureCell(level, cell, cellState, face);
        if (be == null) return InteractionResult.FAIL;
        FaceContent c = be.face(face);
        NoteData note = NoteData.fromItem(stack, day(level), player.getGameProfile().getName());
        boolean gold = false;
        if (note.isPlain() && level.random.nextDouble() < CLConfig.num(CLConfig.GOLD_CHANCE)) {
            note.special = NoteColours.GOLD.name();
            gold = true;
        }
        boolean first = c.isEmpty();
        c.putNote(spot, note);
        if (first) {
            c.rotation = face.getAxis().isVertical() ? rotation : null;
            c.inset = fit.inset();
        }
        applyState(level, cell, be);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        sound(level, cell, SoundEvents.BOOK_PAGE_TURN);
        if (gold && level instanceof ServerLevel server) {
            Vec3 p = Spots.noteBox(face, c.rotation, spot, c.inset).getCenter();
            server.sendParticles(ParticleTypes.FIREWORK, cell.getX() + p.x, cell.getY() + p.y, cell.getZ() + p.z,
                    6, 0.1, 0.1, 0.1, 0.03);
        }
        return InteractionResult.CONSUME;
    }

    /** Places a blank label on an empty five-spot face at the boundary or within the inset limit (3.10); no fit -> FAIL. */
    public static InteractionResult placeLabel(Level level, Player player, ItemStack stack, BlockPos cell, Direction face, Vec3 hit) {
        if (!(stack.getItem() instanceof LabelItem) || stack.isEmpty()) return InteractionResult.PASS;
        BlockState cellState = level.getBlockState(cell);
        if (!canHold(cellState)) return InteractionResult.FAIL;
        BlockPos hostPos = cell.relative(face);
        BlockState host = level.getBlockState(hostPos);
        FacePaperBlockEntity be = FacePaperBlock.entity(level, cell);
        FaceContent existing = be == null ? null : be.face(face);
        if (existing != null && !existing.isEmpty()) return InteractionResult.FAIL;
        Direction rotation = rotationFor(face, null, player);
        Spots.Fit fit = Spots.fit(host, level, hostPos, face, rotation);
        if (!fit.labelFits()) return InteractionResult.FAIL;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        be = ensureCell(level, cell, cellState, face);
        if (be == null) return InteractionResult.FAIL;
        FaceContent c = be.face(face);
        c.setLabel(new LabelData());
        c.rotation = face.getAxis().isVertical() ? rotation : null;
        c.inset = fit.inset();
        applyState(level, cell, be);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        sound(level, cell, SoundEvents.BOOK_PAGE_TURN);
        return InteractionResult.CONSUME;
    }

    /** Server: makes sure the cell is paper with face {@code face} set and returns its block entity. */
    @Nullable
    private static FacePaperBlockEntity ensureCell(Level level, BlockPos cell, BlockState cellState, Direction face) {
        if (cellState.is(CLRegistry.FACE_PAPER.get())) {
            if (!FacePaperBlock.hasFace(cellState, face)) {
                level.setBlock(cell, FacePaperBlock.withFace(cellState, face, true), Block.UPDATE_ALL);
            }
        } else {
            boolean water = cellState.getFluidState().is(Fluids.WATER) && cellState.getFluidState().isSource();
            BlockState paper = CLRegistry.FACE_PAPER.get().defaultBlockState()
                    .setValue(FacePaperBlock.WATERLOGGED, water);
            level.setBlock(cell, FacePaperBlock.withFace(paper, face, true), Block.UPDATE_ALL);
        }
        return FacePaperBlock.entity(level, cell);
    }

    // ------------------------------------------------------------------------------------------- use

    /** {@link FacePaperBlock#use}: the per-piece tables of 3.4 (notes) and 3.10 (labels). */
    public static InteractionResult use(Level level, BlockPos pos, BlockState state, FacePaperBlockEntity be, PieceHit piece,
                                        Player player, InteractionHand hand, BlockHitResult hit) {
        FaceContent c = be.face(piece.face());
        ItemStack held = player.getItemInHand(hand);
        if (piece.isPanel()) return InteractionResult.CONSUME; // between the notes of a covered face: nothing
        if (piece.isLabel()) return useLabel(level, pos, be, c, piece, player, hand, held, hit);
        NoteData note = c.note(piece.spot());
        if (note == null) return InteractionResult.PASS;
        boolean sneak = player.isSecondaryUseActive();
        boolean server = !level.isClientSide;

        if (held.getItem() instanceof DyeItem dye) {
            if (note.isSpecial()) return InteractionResult.CONSUME;
            if (server) {
                note.color = NoteColours.mix(note.color, dye.getDyeColor());
                be.sync();
                if (!player.getAbilities().instabuild) held.shrink(1);
                sound(level, pos, SoundEvents.DYE_USE);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.getItem() instanceof StickyNoteItem) {
            // CONSUME / SUCCESS when placed; FAIL when nothing fits (3.4 row 1: nothing, and no note screen)
            return placeNote(level, player, held, pos, piece.face(), hit.getLocation());
        }
        if (held.getItem() instanceof EraserItem) {
            if (server && erase(note)) {
                be.sync();
                sound(level, pos, SoundEvents.WOOL_HIT);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty()) {
            if (sneak) {
                if (server) {
                    NoteData taken = pickedUp(note, CLConfig.on(CLConfig.KEEP_SPECIAL_COLOR));
                    c.removeNote(piece.spot());
                    applyState(level, pos, be);
                    player.getInventory().placeItemBackInInventory(taken.toItem());
                    sound(level, pos, SoundEvents.BOOK_PAGE_TURN);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (level.isClientSide) {
                Direction face = piece.face();
                int spot = piece.spot();
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openNoteScreen(pos, face, spot));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (c.isCovered()) return InteractionResult.CONSUME;
        if (sneak) return InteractionResult.PASS; // vanilla would not have used the host either; the item's useOn runs
        return forwardToHost(level, pos, piece.face(), player, hand, hit);
    }

    /** 3.10: the label's own actions, else the host gets the click moved onto its face. */
    private static InteractionResult useLabel(Level level, BlockPos pos, FacePaperBlockEntity be, FaceContent c, PieceHit piece,
                                              Player player, InteractionHand hand, ItemStack held, BlockHitResult hit) {
        LabelData label = c.label;
        if (label == null) return InteractionResult.PASS;
        boolean server = !level.isClientSide;
        boolean creative = player.getAbilities().instabuild;
        if (player.isSecondaryUseActive()) {
            if (server) {
                label.setPicture(held); // empty hand clears; an item is shown, not used up
                be.sync();
                sound(level, pos, SoundEvents.BOOK_PAGE_TURN);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.getItem() instanceof DyeItem dye) {
            if (server) {
                label.dye = dye.getDyeColor();
                be.sync();
                if (!creative) held.shrink(1);
                sound(level, pos, SoundEvents.DYE_USE);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(Items.GLOW_INK_SAC) || held.is(Items.INK_SAC)) {
            boolean glow = held.is(Items.GLOW_INK_SAC);
            if (server && label.glow != glow) {
                label.glow = glow;
                be.sync();
                if (!creative) held.shrink(1);
                sound(level, pos, glow ? SoundEvents.GLOW_INK_SAC_USE : SoundEvents.INK_SAC_USE);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(Items.FEATHER)) {
            if (server) {
                label.showName = !label.showName;
                be.sync();
                sound(level, pos, SoundEvents.BOOK_PAGE_TURN);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return forwardToHost(level, pos, piece.face(), player, hand, hit);
    }

    /** The click goes to the host block with the hit moved onto its face (chests open through paper). */
    public static InteractionResult forwardToHost(Level level, BlockPos cell, Direction face, Player player, InteractionHand hand,
                                                  BlockHitResult hit) {
        BlockPos hostPos = cell.relative(face);
        BlockState host = level.getBlockState(hostPos);
        if (host.isAir()) return InteractionResult.PASS;
        return host.use(level, player, hand, new BlockHitResult(hit.getLocation(), face.getOpposite(), hostPos, false));
    }

    // ------------------------------------------------------------------------------------------- breaking

    /** Where the player's current pick lands (both sides; the centre of the cell when the pick gives nothing). */
    public static Vec3 pickLocation(Player player, BlockPos pos) {
        HitResult pick = player.pick(player.getReachDistance(), 1.0f, false);
        return pick == null ? Vec3.atCenterOf(pos) : pick.getLocation();
    }

    /**
     * {@link FacePaperBlock#onDestroyedByPlayer}: removes the piece under the player's pick, drops it on the server
     * when harvesting, and keeps the block. Returns false when the cell still holds something after the removal;
     * the caller breaks the whole cell (vanilla path, drops through getDrops) when this returns true.
     */
    public static boolean breakPiece(Level level, BlockPos pos, BlockState state, FacePaperBlockEntity be, Player player, boolean willHarvest) {
        if (be.pieceCount() <= 1) return true;
        Vec3 at = pickLocation(player, pos);
        PieceHit piece = PieceHit.resolve(state, be, at);
        if (piece == null) return true;
        FaceContent removed = removePiece(be.face(piece.face()), piece, local(pos, at));
        if (removed.isEmpty()) return true;
        if (!level.isClientSide && willHarvest) {
            for (ItemStack drop : removed.drops()) Block.popResource(level, pos, drop);
        }
        applyState(level, pos, be);
        return false;
    }

    // ------------------------------------------------------------------------------------------- host gone

    /**
     * The host on face {@code face} changed: re-fit that face, drop what no longer fits (server only) and return the
     * block state the cell should now have (the same instance when nothing changed).
     */
    public static BlockState hostChanged(LevelAccessor level, BlockPos pos, BlockState state, FacePaperBlockEntity be,
                                         Direction face, BlockState host, BlockPos hostPos) {
        FaceContent c = be.face(face);
        if (c.isEmpty()) return stateFor(state, be.contentMask());
        float insetBefore = c.inset;
        Spots.Fit fit = Spots.fit(host, level, hostPos, face, c.rotation);
        FaceContent removed = refit(c, fit);
        if (removed.isEmpty() && c.inset == insetBefore) return state;
        if (level instanceof ServerLevel server) {
            for (ItemStack drop : removed.drops()) Block.popResource(server, pos, drop);
        }
        be.sync();
        return stateFor(state, be.contentMask());
    }

    // ------------------------------------------------------------------------------------------- state upkeep

    /** The state a paper cell with this content mask should have: booleans per face, or air / water when empty. */
    public static BlockState stateFor(BlockState state, int contentMask) {
        if (contentMask == 0) {
            return state.hasProperty(FacePaperBlock.WATERLOGGED) && state.getValue(FacePaperBlock.WATERLOGGED)
                    ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }
        BlockState out = state;
        for (Direction d : Direction.values()) out = FacePaperBlock.withFace(out, d, (contentMask & (1 << d.ordinal())) != 0);
        return out;
    }

    /** After a content change: sync the block entity and set the matching block state (air / water when empty). */
    public static void applyState(Level level, BlockPos pos, FacePaperBlockEntity be) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(CLRegistry.FACE_PAPER.get())) return;
        BlockState next = stateFor(state, be.contentMask());
        be.sync();
        if (next != state) level.setBlock(pos, next, Block.UPDATE_ALL);
    }

    // ------------------------------------------------------------------------------------------- small helpers

    public static Vec3 local(BlockPos cell, Vec3 world) {
        return world.subtract(cell.getX(), cell.getY(), cell.getZ());
    }

    private static void sound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}
