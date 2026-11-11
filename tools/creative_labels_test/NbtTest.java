import com.SaintAlfonzo.creativelabels.block.FaceContent;
import com.SaintAlfonzo.creativelabels.block.FacePaperBlockEntity;
import com.SaintAlfonzo.creativelabels.block.FaceSnapshot;
import com.SaintAlfonzo.creativelabels.block.LabelData;
import com.SaintAlfonzo.creativelabels.block.NoteData;
import com.SaintAlfonzo.creativelabels.block.Spots;
import com.SaintAlfonzo.creativelabels.crafting.NoteCopyRecipe;
import com.SaintAlfonzo.creativelabels.crafting.NoteDyeRecipe;
import com.SaintAlfonzo.creativelabels.crafting.NoteEraseRecipe;
import com.SaintAlfonzo.creativelabels.crafting.NoteGrid;
import com.SaintAlfonzo.creativelabels.crafting.NoteSpecialRecipe;
import com.SaintAlfonzo.creativelabels.item.ItemLabels;
import com.SaintAlfonzo.creativelabels.item.NoteColours;
import com.SaintAlfonzo.creativelabels.net.NoteTextPacket;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.util.List;

/**
 * Headless checks of the NBT model (BRIEF 6): note item NBT (display.color, CreativeLabels.Text / Special, tag
 * removed when empty), item label lines, label picture stripping (C2), face and note round trips, the Mask remap
 * (C7), colour mixing against vanilla's dyeArmor, the note text sanitiser (C6) and the four recipe matchers on a
 * 3x3 grid (stand-in vanilla items through NoteGrid.standIns: the frozen registries allow no new Item). Needs
 * Bootstrap (items). The registry-bound parts (NoteData.toItem, the block entity itself) are not constructible
 * here and are skipped.
 */
public class NbtTest {
    static int fails = 0, checks = 0;

    static void check(boolean ok, String what) {
        checks++;
        if (!ok) fails++;
        System.out.println((ok ? "ok   " : "FAIL ") + what);
    }

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        System.out.println("== note item NBT");
        ItemStack note = new ItemStack(Items.PAPER); // any item: the helpers only touch the tag
        check(NoteData.readColor(note) == NoteData.NO_COLOR && NoteData.readText(note).isEmpty() && NoteData.readSpecial(note) == null,
                "fresh stack: no colour, no text, no special");
        NoteData.writeColor(note, 0x123456);
        check(note.getTag() != null && note.getTag().getCompound("display").getInt("color") == 0x123456, "colour under display.color");
        check(NoteData.readColor(note) == 0x123456, "colour read back");
        NoteData.writeText(note, "hello");
        check(note.getTag().getCompound("CreativeLabels").getString("Text").equals("hello"), "text under CreativeLabels.Text");
        NoteData.writeSpecial(note, "gold");
        check(note.getTag().getCompound("CreativeLabels").getString("Special").equals("gold"), "special under CreativeLabels.Special");
        NoteData.writeText(note, "");
        check(!note.getTag().getCompound("CreativeLabels").contains("Text"), "empty text removes the key");
        NoteData.writeSpecial(note, null);
        check(!note.getTag().contains("CreativeLabels"), "no text and no special removes the compound");
        NoteData.writeColor(note, NoteData.NO_COLOR);
        check(note.getTag() == null, "clearing the last key removes the tag");
        ItemStack other = new ItemStack(Items.PAPER);
        check(ItemStack.isSameItemSameTags(note, other), "a cleaned stack matches a fresh one (stacks again)");

        System.out.println("== NoteData round trips");
        NoteData n = new NoteData(0xAABBCC, "smile", "buy milk", 42, "Steve");
        CompoundTag saved = n.save();
        check(saved.getCompound("display").getInt("color") == 0xAABBCC && saved.getCompound("CreativeLabels").getString("Text").equals("buy milk")
                && saved.getCompound("CreativeLabels").getString("Special").equals("smile") && saved.getLong("Day") == 42
                && saved.getString("Placer").equals("Steve"), "NoteData.save shape: " + saved);
        check(NoteData.load(saved).equals(n), "NoteData load(save) round trip");
        NoteData plain = new NoteData();
        check(plain.save().isEmpty(), "plain unwritten note saves an empty compound");
        check(NoteData.load(plain.save()).equals(plain) && NoteData.load(plain.save()).isPlain(), "plain note round trip");
        ItemStack carried = new ItemStack(Items.PAPER);
        n.writeTo(carried);
        NoteData back = NoteData.fromItem(carried, 7, "Alex");
        check(back.color == n.color && back.special.equals(n.special) && back.text.equals(n.text) && back.day == 7 && back.placer.equals("Alex"),
                "writeTo / fromItem round trip: " + back);
        check(carried.getTag().getCompound("display").getInt("color") == 0xAABBCC && !carried.getTag().contains("Day"),
                "writeTo writes the item shape only (no Day/Placer)");

        System.out.println("== item labels (CreativeLabels.Lines)");
        ItemStack sword = new ItemStack(Items.STICK);
        check(ItemLabels.read(sword).isEmpty() && !ItemLabels.has(sword), "no lines on a fresh item");
        check(ItemLabels.add(sword, "one"), "add first line");
        check(sword.getTag().getCompound("CreativeLabels").getList("Lines", 8).size() == 1, "Lines list has one entry");
        check(ItemLabels.add(sword, "two") && ItemLabels.add(sword, "three"), "add second and third");
        check(!ItemLabels.add(sword, "four"), "fourth line refused");
        check(ItemLabels.read(sword).equals(List.of("one", "two", "three")), "lines in order: " + ItemLabels.read(sword));
        check(ItemLabels.isFull(sword), "full at three");
        check(ItemLabels.removeLast(sword) && ItemLabels.read(sword).equals(List.of("one", "two")), "removeLast");
        ItemLabels.write(sword, List.of("", "x", ""));
        check(ItemLabels.read(sword).equals(List.of("x")), "write drops empty lines");
        check(ItemLabels.removeLast(sword) && !ItemLabels.removeLast(sword), "removeLast on the last line, then nothing");
        check(sword.getTag() == null, "removing the last line removes Lines, the compound and the tag");
        ItemStack enchanted = new ItemStack(Items.IRON_SWORD);
        enchanted.getOrCreateTag().putInt("Damage", 5);
        ItemLabels.add(enchanted, "keep");
        ItemLabels.removeLast(enchanted);
        check(enchanted.getTag() != null && enchanted.getTag().getInt("Damage") == 5 && !enchanted.getTag().contains("CreativeLabels"),
                "other NBT survives label removal");

        System.out.println("== label picture stripping (C2 / Q5)");
        ItemStack shulker = new ItemStack(Items.SHULKER_BOX, 3);
        CompoundTag bet = new CompoundTag();
        ListTag items = new ListTag();
        for (int i = 0; i < 27; i++) {
            CompoundTag it = new CompoundTag();
            it.putString("id", "minecraft:diamond");
            it.putByte("Count", (byte) 64);
            it.putByte("Slot", (byte) i);
            items.add(it);
        }
        bet.put("Items", items);
        shulker.getOrCreateTag().put("BlockEntityTag", bet);
        shulker.getOrCreateTag().putInt("CustomModelData", 9);
        shulker.getOrCreateTag().putInt("Damage", 3);
        ItemStack stripped = LabelData.strip(shulker);
        check(stripped.getCount() == 1 && stripped.getItem() == Items.SHULKER_BOX, "stripped: count 1, same item");
        check(stripped.getTag() != null && !stripped.getTag().contains("BlockEntityTag") && !stripped.getTag().contains("Damage"),
                "stripped: no BlockEntityTag, no Damage");
        check(stripped.getTag().getInt("CustomModelData") == 9, "stripped: CustomModelData kept");
        ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HEALING);
        check(LabelData.strip(potion).getTag().getString("Potion").equals("minecraft:healing"), "stripped: Potion kept");
        ItemStack leather = new ItemStack(Items.LEATHER_CHESTPLATE);
        ((DyeableLeatherItem) Items.LEATHER_CHESTPLATE).setColor(leather, 0xFF00FF);
        leather.getOrCreateTag().putInt("RepairCost", 4);
        ItemStack strippedLeather = LabelData.strip(leather);
        check(strippedLeather.getTag().getCompound("display").getInt("color") == 0xFF00FF && !strippedLeather.getTag().contains("RepairCost"),
                "stripped: display.color kept, RepairCost dropped");
        ItemStack enchantedBook = new ItemStack(Items.DIAMOND_PICKAXE);
        enchantedBook.enchant(net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY, 3);
        check(LabelData.strip(enchantedBook).isEnchanted(), "stripped: Enchantments kept (glint)");
        check(LabelData.strip(new ItemStack(Items.STONE)).getTag() == null, "stripped: a bare item has no tag");
        ItemStack huge = new ItemStack(Items.STONE);
        ListTag ench = new ListTag();
        for (int i = 0; i < 200; i++) {
            CompoundTag e = new CompoundTag();
            e.putString("id", "minecraft:some_very_long_enchantment_name_" + i);
            e.putShort("lvl", (short) 1);
            ench.add(e);
        }
        huge.getOrCreateTag().put("Enchantments", ench);
        check(LabelData.strip(huge).getTag() == null, "stripped: an oversized kept tag is dropped (id only)");
        LabelData label = new LabelData();
        label.setPicture(shulker);
        label.dye = DyeColor.LIME;
        label.glow = true;
        label.showName = true;
        LabelData loadedLabel = LabelData.load(label.save());
        check(loadedLabel.equals(label), "LabelData round trip: " + loadedLabel);
        check(new LabelData().save().isEmpty(), "blank label saves an empty compound");
        check(LabelData.load(new CompoundTag()).equals(new LabelData()), "blank label round trip");

        System.out.println("== face round trips");
        FaceContent face = new FaceContent();
        face.inset = 1f / 16;
        face.putNote(Spots.TL, new NoteData(0x112233, null, "a", 1, "p"));
        face.putNote(Spots.BR, new NoteData(NoteData.NO_COLOR, "creeper", "", 2, "q"));
        face.rotation = Direction.WEST;
        FaceContent loaded = FaceContent.load(face.save());
        check(loaded.kind == FaceContent.Kind.NOTES && loaded.noteMask() == face.noteMask() && loaded.rotation == Direction.WEST
                && Math.abs(loaded.inset - face.inset) < 1e-6 && loaded.notes[Spots.TL].equals(face.notes[Spots.TL])
                && loaded.notes[Spots.BR].equals(face.notes[Spots.BR]), "notes face round trip: " + loaded);
        FaceContent labelFace = new FaceContent();
        labelFace.setLabel(label);
        FaceContent loadedLabelFace = FaceContent.load(labelFace.save());
        check(loadedLabelFace.kind == FaceContent.Kind.LABEL && loadedLabelFace.label.equals(label), "label face round trip");
        check(FaceContent.load(new FaceContent().save()).isEmpty(), "empty face round trip");
        face.removeNote(Spots.TL);
        check(face.kind == FaceContent.Kind.NOTES && face.noteCount() == 1, "removing one note keeps the face");
        face.removeNote(Spots.BR);
        check(face.isEmpty() && face.rotation == null && face.inset == 0, "removing the last note clears the face");
        FaceSnapshot snap = loaded.snapshot();
        check(snap.kind() == FaceContent.Kind.NOTES && snap.note(Spots.TL).color() == 0x112233 && snap.note(Spots.TL).written()
                && snap.note(Spots.BR).special().equals("creeper") && !snap.note(Spots.BR).written() && snap.note(Spots.C) == null
                && snap.rotation() == Direction.WEST, "snapshot mirrors the face: " + snap);
        check(new FaceContent().snapshot() == FaceSnapshot.EMPTY, "empty snapshot is the shared EMPTY");

        System.out.println("== Mask remap (C7)");
        FaceContent[] faces = new FaceContent[6];
        for (int i = 0; i < 6; i++) faces[i] = new FaceContent();
        faces[Direction.NORTH.ordinal()].putNote(Spots.C, new NoteData(1, null, "n", 0, ""));
        faces[Direction.DOWN.ordinal()].putNote(Spots.TL, new NoteData(2, null, "d", 0, ""));
        faces[Direction.DOWN.ordinal()].rotation = Direction.NORTH;
        int savedMask = bit(Direction.NORTH) | bit(Direction.DOWN);
        FaceContent[] same = FacePaperBlockEntity.remap(faces, savedMask, savedMask);
        check(same == faces, "same mask: untouched");
        FaceContent[] r90 = FacePaperBlockEntity.remap(faces, savedMask, bit(Direction.EAST) | bit(Direction.DOWN));
        check(r90[Direction.EAST.ordinal()].note(Spots.C) != null && r90[Direction.EAST.ordinal()].note(Spots.C).text.equals("n")
                && r90[Direction.NORTH.ordinal()].isEmpty(), "rotated 90: north content now east");
        check(r90[Direction.DOWN.ordinal()].rotation == Direction.EAST, "rotated 90: floor rotation turned to EAST");
        FaceContent[] r180 = FacePaperBlockEntity.remap(faces, savedMask, bit(Direction.SOUTH) | bit(Direction.DOWN));
        check(r180[Direction.SOUTH.ordinal()].note(Spots.C) != null && r180[Direction.DOWN.ordinal()].rotation == Direction.SOUTH,
                "rotated 180: north -> south, floor rotation SOUTH");
        FaceContent[] two = new FaceContent[6];
        for (int i = 0; i < 6; i++) two[i] = new FaceContent();
        two[Direction.NORTH.ordinal()].putNote(Spots.C, new NoteData(1, null, "n", 0, ""));
        two[Direction.EAST.ordinal()].putNote(Spots.C, new NoteData(1, null, "e", 0, ""));
        // {N,E} -> {N,W} is both a FRONT_BACK mirror and a 270 rotation (every mirrored horizontal mask is also some
        // rotated one, D4 orbit); the mask can't tell them apart and rotations are tried first, so rotation wins.
        FaceContent[] mirrored = FacePaperBlockEntity.remap(two, bit(Direction.NORTH) | bit(Direction.EAST), bit(Direction.NORTH) | bit(Direction.WEST));
        check(mirrored[Direction.WEST.ordinal()].note(Spots.C).text.equals("n") && mirrored[Direction.NORTH.ordinal()].note(Spots.C).text.equals("e"),
                "{N,E} -> {N,W}: resolved as a 270 rotation (north -> west, east -> north)");
        FaceContent[] none = FacePaperBlockEntity.remap(two, bit(Direction.NORTH) | bit(Direction.EAST), bit(Direction.UP));
        check(none == two, "no transform matches: faces kept as saved");

        System.out.println("== colour mixing against vanilla dyeArmor");
        for (DyeColor dye : DyeColor.values()) {
            ItemStack helmet = DyeableLeatherItem.dyeArmor(new ItemStack(Items.LEATHER_HELMET), List.of((DyeItem) DyeItem.byColor(dye)));
            int vanilla = ((DyeableLeatherItem) Items.LEATHER_HELMET).getColor(helmet);
            int ours = NoteColours.mix(NoteData.NO_COLOR, dye);
            check(ours == vanilla, "single dye " + dye.getName() + ": " + NoteColours.hex(ours));
        }
        int[] starts = {0xFF0000, 0x00FF00, 0x123456, 0xFFFFFF, 0x010101};
        for (int start : starts) {
            for (DyeColor dye : new DyeColor[]{DyeColor.BLUE, DyeColor.YELLOW, DyeColor.BLACK, DyeColor.PINK}) {
                ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
                ((DyeableLeatherItem) Items.LEATHER_HELMET).setColor(helmet, start);
                helmet = DyeableLeatherItem.dyeArmor(helmet, List.of((DyeItem) DyeItem.byColor(dye)));
                int vanilla = ((DyeableLeatherItem) Items.LEATHER_HELMET).getColor(helmet);
                check(NoteColours.mix(start, dye) == vanilla, "mix " + NoteColours.hex(start) + " + " + dye.getName());
            }
            ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
            ((DyeableLeatherItem) Items.LEATHER_HELMET).setColor(helmet, start);
            helmet = DyeableLeatherItem.dyeArmor(helmet, List.of((DyeItem) DyeItem.byColor(DyeColor.RED), (DyeItem) DyeItem.byColor(DyeColor.CYAN)));
            check(NoteColours.mix(start, DyeColor.RED, DyeColor.CYAN) == ((DyeableLeatherItem) Items.LEATHER_HELMET).getColor(helmet),
                    "mix " + NoteColours.hex(start) + " + red + cyan");
        }
        check(NoteColours.nearestDye(NoteColours.of(DyeColor.LIME)) == DyeColor.LIME, "nearestDye round trip (lime)");
        check(NoteColours.special("gold") == NoteColours.GOLD && NoteColours.special("nope") == null, "special lookup");
        check(NoteColours.specialFor(Items.GUNPOWDER) == NoteColours.CREEPER && NoteColours.specialFor(Items.STONE) == null, "specialFor ingredient");
        check(NoteColours.look(NoteData.NO_COLOR, null) == 0 && NoteColours.look(5, null) == 1 && NoteColours.look(5, "gold") == 5, "item look values");

        System.out.println("== note text sanitiser (C6)");
        check(NoteTextPacket.sanitize("hello", 32).equals("hello"), "plain text kept");
        check(NoteTextPacket.sanitize("  padded  ", 32).equals("padded"), "blanks trimmed");
        check(NoteTextPacket.sanitize("first\nsecond", 32).equals("first"), "first line only");
        check(NoteTextPacket.sanitize("a\u00a7cred\u00a7r b", 32).equals("ared b"), "section codes stripped with their code char");
        check(NoteTextPacket.sanitize("tab\there", 32).equals("tabhere"), "disallowed characters dropped");
        check(NoteTextPacket.sanitize("0123456789abcdef", 8).equals("01234567"), "capped at the length");
        check(NoteTextPacket.sanitize("abc   def", 4).equals("abc"), "cap then trim");
        check(NoteTextPacket.sanitize(null, 32).isEmpty() && NoteTextPacket.sanitize("\u00a7", 32).isEmpty(), "null and a lone section sign give empty");
        ItemStack handNote = new ItemStack(Items.PAPER);
        NoteData.writeText(handNote, "x");
        check(ItemLabels.canLabel(handNote, new ItemStack(Items.STONE)) && !ItemLabels.canLabel(new ItemStack(Items.PAPER), new ItemStack(Items.STONE))
                && !ItemLabels.canLabel(handNote, ItemStack.EMPTY), "canLabel: written note on something");

        System.out.println("== recipe matchers (3x3 grid, stand-in items: paper = note, dried kelp = eraser)");
        net.minecraft.world.item.Item noteItem = Items.PAPER;
        net.minecraft.world.item.Item eraserItem = Items.DRIED_KELP;
        NoteGrid.standIns(noteItem, eraserItem);
        check(NoteGrid.isNote(new ItemStack(Items.PAPER)) && NoteGrid.isEraser(new ItemStack(Items.DRIED_KELP)) && !NoteGrid.isNote(new ItemStack(Items.STONE)),
                "stand-ins installed");
        AbstractContainerMenu menu = new AbstractContainerMenu(null, 0) {
            @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }
            @Override public boolean stillValid(Player p) { return true; }
        };
        NoteDyeRecipe dye = new NoteDyeRecipe(new ResourceLocation("creative_labels", "t_dye"));
        NoteSpecialRecipe special = new NoteSpecialRecipe(new ResourceLocation("creative_labels", "t_special"));
        NoteCopyRecipe copy = new NoteCopyRecipe(new ResourceLocation("creative_labels", "t_copy"));
        NoteEraseRecipe erase = new NoteEraseRecipe(new ResourceLocation("creative_labels", "t_erase"));
        ItemStack plainNote = new ItemStack(noteItem);
        ItemStack writtenNote = new ItemStack(noteItem);
        NoteData.writeText(writtenNote, "milk");
        ItemStack redNote = new ItemStack(noteItem);
        NoteData.writeColor(redNote, NoteColours.of(DyeColor.RED));
        ItemStack goldNote = new ItemStack(noteItem);
        NoteData.writeSpecial(goldNote, "gold");

        // note_dye: N identical notes + 1 dye
        CraftingContainer g = grid(menu, plainNote, plainNote, plainNote, new ItemStack(Items.BLUE_DYE));
        check(dye.matches(g, null), "dye: 3 plain + blue matches");
        ItemStack dyed = dye.assemble(g);
        check(dyed.getCount() == 3 && NoteData.readColor(dyed) == NoteColours.of(DyeColor.BLUE), "dye: 3 blue notes: " + NoteColours.hex(NoteData.readColor(dyed)));
        g = grid(menu, redNote, redNote, new ItemStack(Items.BLUE_DYE));
        check(dye.matches(g, null) && NoteData.readColor(dye.assemble(g)) == NoteColours.mix(NoteColours.of(DyeColor.RED), DyeColor.BLUE), "dye: red notes + blue mix like leather");
        g = grid(menu, writtenNote, writtenNote, new ItemStack(Items.LIME_DYE));
        check(dye.matches(g, null) && NoteData.readText(dye.assemble(g)).equals("milk") && dye.assemble(g).getCount() == 2, "dye: text kept");
        check(!dye.matches(grid(menu, plainNote, redNote, new ItemStack(Items.BLUE_DYE)), null), "dye: mixed notes refused");
        check(!dye.matches(grid(menu, plainNote, new ItemStack(Items.BLUE_DYE), new ItemStack(Items.RED_DYE)), null), "dye: two dyes refused (vanilla's armour dye handles 1 note + N dyes)");
        check(!dye.matches(grid(menu, goldNote, new ItemStack(Items.BLUE_DYE)), null), "dye: special refused");
        check(!dye.matches(grid(menu, plainNote, new ItemStack(Items.BLUE_DYE), new ItemStack(Items.STONE)), null), "dye: a stray item refused");
        check(!dye.matches(grid(menu, new ItemStack(Items.BLUE_DYE)), null), "dye: no note refused");
        check(dye.canCraftInDimensions(2, 1) && !dye.canCraftInDimensions(1, 1), "dye: needs two slots");

        // note_special: N plain notes + 1 ingredient
        g = grid(menu, plainNote, writtenNote, new ItemStack(Items.GUNPOWDER));
        check(!special.matches(g, null), "special: notes must be identical (plain + written refused)");
        g = grid(menu, writtenNote, writtenNote, new ItemStack(Items.GUNPOWDER));
        check(special.matches(g, null), "special: 2 written plain notes + gunpowder matches");
        ItemStack creeper = special.assemble(g);
        check(creeper.getCount() == 2 && "creeper".equals(NoteData.readSpecial(creeper)) && NoteData.readText(creeper).equals("milk"), "special: 2 creeper notes, text kept");
        g = grid(menu, plainNote, new ItemStack(Items.INK_SAC));
        check(special.matches(g, null) && "inverted".equals(NoteData.readSpecial(special.assemble(g))), "special: ink sac -> inverted");
        g = grid(menu, plainNote, new ItemStack(Items.GLOW_BERRIES));
        check(special.matches(g, null) && "smile".equals(NoteData.readSpecial(special.assemble(g))), "special: glow berries -> smile");
        g = grid(menu, plainNote, new ItemStack(Items.GOLD_NUGGET));
        check(special.matches(g, null) && "gold".equals(NoteData.readSpecial(special.assemble(g))), "special: gold nugget -> gold");
        check(!special.matches(grid(menu, redNote, new ItemStack(Items.GUNPOWDER)), null), "special: dyed note refused");
        check(!special.matches(grid(menu, goldNote, new ItemStack(Items.GUNPOWDER)), null), "special: special note refused");
        check(!special.matches(grid(menu, plainNote, new ItemStack(Items.GUNPOWDER), new ItemStack(Items.GUNPOWDER)), null), "special: two ingredients refused");
        check(!special.matches(grid(menu, plainNote, new ItemStack(Items.GUNPOWDER), new ItemStack(Items.BLUE_DYE)), null), "special: ingredient + dye refused");

        // note_copy: 1 written + 8 blank
        g = grid(menu, writtenNote, plainNote, plainNote, plainNote, redNote, plainNote, goldNote, plainNote, plainNote);
        check(copy.matches(g, null), "copy: 1 written + 8 blank (mixed colours) matches");
        ItemStack copies = copy.assemble(g);
        check(copies.getCount() == 9 && NoteData.readText(copies).equals("milk") && NoteData.readSpecial(copies) == null, "copy: 9 copies of the written note");
        ItemStack writtenRed = redNote.copy();
        NoteData.writeText(writtenRed, "eggs");
        g = grid(menu, plainNote, plainNote, plainNote, plainNote, writtenRed, plainNote, plainNote, plainNote, plainNote);
        check(copy.matches(g, null) && NoteData.readColor(copy.assemble(g)) == NoteData.readColor(redNote) && NoteData.readText(copy.assemble(g)).equals("eggs"),
                "copy: colour and text of the written note copied (written in the middle)");
        check(!copy.matches(grid(menu, writtenNote, writtenRed, plainNote, plainNote, plainNote, plainNote, plainNote, plainNote, plainNote), null), "copy: two written refused");
        check(!copy.matches(grid(menu, writtenNote, plainNote, plainNote, plainNote, plainNote, plainNote, plainNote, plainNote), null), "copy: only 7 blanks refused");
        check(!copy.matches(grid(menu, plainNote, plainNote, plainNote, plainNote, plainNote, plainNote, plainNote, plainNote, plainNote), null), "copy: no written refused");
        check(copy.canCraftInDimensions(3, 3) && !copy.canCraftInDimensions(2, 2), "copy: needs nine slots");

        // note_erase: note + eraser
        g = grid(menu, writtenRed, new ItemStack(eraserItem));
        check(erase.matches(g, null), "erase: written red note + eraser matches");
        ItemStack erased = erase.assemble(g);
        check(erased.getCount() == 1 && NoteData.readText(erased).isEmpty() && NoteData.readColor(erased) == NoteData.readColor(redNote), "erase: text wiped first, colour kept");
        g = grid(menu, redNote, new ItemStack(eraserItem));
        check(erase.matches(g, null) && NoteData.readColor(erase.assemble(g)) == NoteData.NO_COLOR && erase.assemble(g).getTag() == null, "erase: blank red note -> plain, tag gone");
        g = grid(menu, goldNote, new ItemStack(eraserItem));
        check(erase.matches(g, null) && NoteData.readSpecial(erase.assemble(g)) == null, "erase: blank gold note -> plain");
        check(!erase.matches(grid(menu, plainNote, new ItemStack(eraserItem)), null), "erase: plain blank note has nothing to erase");
        check(!erase.matches(grid(menu, writtenNote, writtenNote, new ItemStack(eraserItem)), null), "erase: two notes refused");
        check(!erase.matches(grid(menu, writtenNote, new ItemStack(eraserItem), new ItemStack(Items.STONE)), null), "erase: a stray item refused");
        check(!dye.matches(grid(menu, writtenNote, new ItemStack(eraserItem)), null) && !special.matches(grid(menu, writtenNote, new ItemStack(eraserItem)), null),
                "erase grid matches neither dye nor special");
        NoteGrid.standIns(null, null);
        check(!NoteGrid.isNote(new ItemStack(Items.PAPER)), "stand-ins removed");

        System.out.println(fails == 0 ? "ALL " + checks + " CHECKS PASSED" : fails + " FAILED of " + checks);
        System.exit(fails == 0 ? 0 : 1);
    }

    static int bit(Direction d) {
        return 1 << d.ordinal();
    }

    /** A 3x3 crafting grid holding copies of the given stacks in order (count 1 each). */
    static CraftingContainer grid(AbstractContainerMenu menu, ItemStack... stacks) {
        CraftingContainer g = new CraftingContainer(menu, 3, 3);
        for (int i = 0; i < stacks.length; i++) {
            ItemStack s = stacks[i].copy();
            s.setCount(1);
            g.setItem(i, s);
        }
        return g;
    }
}
