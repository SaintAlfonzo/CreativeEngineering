package com.SaintAlfonzo.creative_labels.item;

import com.SaintAlfonzo.creative_labels.block.FacePaperInteractions;
import com.SaintAlfonzo.creative_labels.block.NoteData;
import com.SaintAlfonzo.creative_labels.client.ClientHooks;
import com.SaintAlfonzo.creative_labels.crafting.NoteDyeRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.List;

/**
 * {@code creative_labels:sticky_note} (sticky_notes.md 3.6 to 3.8, BRIEF 5). Colour = vanilla {@code display.color}
 * ({@link DyeableLeatherItem}); text and special under {@code CreativeLabels} ({@link NoteData}). Stack 64; notes
 * stack only with identical NBT, so plain notes stack and written ones stack with identical copies. Specials are
 * never dyed: {@link NoteDyeRecipe} refuses them and {@code ArmorDyeRecipeMixin} keeps vanilla's armour-dye recipe
 * (which accepts any {@link DyeableLeatherItem}) off them too.
 *
 * <ul>
 *   <li>{@link #useOn}: placement on a host face or on paper already there (3.4)</li>
 *   <li>{@link #use} in the air: sneaking with an item in the off hand labels that stack (server); otherwise the
 *       note screen opens on the client and its Done sends {@code NoteTextPacket} (3.7)</li>
 *   <li>{@link #overrideStackedOnOther}: a written note on the cursor right-clicked onto a slot labels the whole
 *       stack there, one note used, gated server side by {@link LabelGate} (3.8, Q3, Q4)</li>
 * </ul>
 */
public class StickyNoteItem extends Item implements DyeableLeatherItem {

    public static final int TOOLTIP_LINES = 4;

    public StickyNoteItem(Properties properties) {
        super(properties.stacksTo(64));
    }

    /** "Sticky Note", or the special's own name ("Gold Sticky Note") when {@code Special} is set. */
    @Override
    public Component getName(ItemStack stack) {
        NoteColours.Special s = NoteColours.special(NoteData.readSpecial(stack));
        return s == null ? super.getName(stack) : Component.translatable(s.nameKey());
    }

    /** Vanilla's leather colour read, but {@link NoteData#NO_COLOR} (not leather brown) when undyed. */
    @Override
    public int getColor(ItemStack stack) {
        return NoteData.readColor(stack);
    }

    @Override
    public boolean hasCustomColor(ItemStack stack) {
        return NoteData.readColor(stack) != NoteData.NO_COLOR;
    }

    @Override
    public void setColor(ItemStack stack, int color) {
        NoteData.writeColor(stack, color);
    }

    @Override
    public void clearColor(ItemStack stack) {
        NoteData.writeColor(stack, NoteData.NO_COLOR);
    }

    /** The creative tab (and so JEI) lists the plain note and the four specials (3.6). */
    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (!this.allowedIn(tab)) return;
        items.add(new ItemStack(this));
        for (NoteColours.Special s : NoteColours.SPECIALS) items.add(special(s));
    }

    /** A blank note with a special look. */
    public ItemStack special(NoteColours.Special s) {
        ItemStack stack = new ItemStack(this);
        NoteData.writeSpecial(stack, s.name());
        return stack;
    }

    /** Placement (3.4): on a host face or on paper already there; chests take notes with sneak + right-click. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        return FacePaperInteractions.placeFromContext(context);
    }

    /**
     * In the air (3.7, 3.8): sneak with the note in the main hand and an item in the off hand labels that item
     * (server); not sneaking opens the note screen (client). A sneak with nothing to label does nothing.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack note = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(note);
            ItemStack target = player.getOffhandItem();
            if (!ItemLabels.canLabel(note, target)) return InteractionResultHolder.pass(note);
            // both sides: the client's copy is a prediction the server's inventory sync confirms; the sound
            // goes through Player.playSound, which plays once on each side
            ItemLabels.labelWith(player, note, target);
            return InteractionResultHolder.sidedSuccess(note, level.isClientSide);
        }
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openHandNoteScreen(hand));
        }
        return InteractionResultHolder.sidedSuccess(note, level.isClientSide);
    }

    /**
     * A written note on the cursor, right-clicked onto a slot (3.8): the line goes on the whole stack there, one
     * note used. Works in every container, including the creative inventory tab; a backpack or shulker slot is
     * labelled, not opened (Q4: the carried stack's override runs before the slot item's). Blank notes, note slots
     * and slots that can't be modified fall through to vanilla. Server side one labelling per 5 ticks
     * ({@link LabelGate}): later drag clicks are swallowed and the slot correction resyncs the client.
     */
    @Override
    public boolean overrideStackedOnOther(ItemStack note, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;
        ItemStack target = slot.getItem();
        if (!ItemLabels.canLabel(note, target)) return false;
        if (!slot.allowModification(player)) return false;
        if (!LabelGate.allow(player)) return true;
        if (ItemLabels.labelWith(player, note, target)) slot.setChanged();
        return true;
    }

    /** The colour a tooltip or tint should show: the note's RGB or plain yellow. */
    public static int displayColor(ItemStack stack) {
        int c = NoteData.readColor(stack);
        return c == NoteData.NO_COLOR ? NoteColours.PLAIN_YELLOW : c;
    }

    /** Spec 3.9: the text in the note's colour (if written), then four grey lines, one per lang key. */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String text = NoteData.readText(stack);
        if (!text.isEmpty()) {
            tooltip.add(Component.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(displayColor(stack)))));
        }
        for (int i = 1; i <= TOOLTIP_LINES; i++) {
            tooltip.add(Component.translatable("creative_labels.tooltip.note." + i).withStyle(ChatFormatting.GRAY));
        }
    }
}
