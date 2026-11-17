package com.SaintAlfonzo.creativedevtools.test;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * One {@code /cdevtest} call: where its lines go, and the lookups every verb shares (level, loaded position, item with
 * NBT, acting player). Lines always go to {@code logs/latest.log} through this class's logger (the queue judges from
 * the log); a player or RCON caller also gets them as the command reply. Nothing here loads a chunk.
 */
final class TestCtx {

    static final Logger LOGGER = LogUtils.getLogger();

    final CommandSourceStack source;
    final MinecraftServer server;
    final String verb;
    final TestArgs args;
    private final boolean reply;
    int lines;

    TestCtx(CommandSourceStack source, String verb, TestArgs args) {
        this.source = source;
        this.server = source.getServer();
        this.verb = verb;
        this.args = args;
        // the dedicated server's own console source would print every reply a second time in the log
        this.reply = source.getEntity() != null || !"Server".equals(source.getTextName());
    }

    TestLine line() {
        return new TestLine(verb);
    }

    void emit(TestLine l) {
        lines++;
        String s = l.toString();
        LOGGER.info(s);
        if (reply) source.sendSuccess(Component.literal(s), false);
    }

    /** {@code dim=<id>} or the caller's level (the console's is the overworld). */
    ServerLevel level() {
        String d = args.opt("dim", null);
        if (d == null) return source.getLevel();
        ResourceLocation id = ResourceLocation.tryParse(d);
        ServerLevel l = id == null ? null : server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, id));
        if (l == null) throw new TestArgs.Bad("no level " + d);
        return l;
    }

    BlockPos pos(String what) {
        int[] p = args.xyz(what);
        return new BlockPos(p[0], p[1], p[2]);
    }

    static BlockPos pos(int[] p) {
        return new BlockPos(p[0], p[1], p[2]);
    }

    /** Refuses an unloaded position: a command that loads (or generates) a chunk stalls the tick. */
    static void loaded(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) throw new TestArgs.Bad("unloaded " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " (forceload add first)");
    }

    static ResourceLocation id(String s) {
        ResourceLocation id = ResourceLocation.tryParse(s);
        if (id == null) throw new TestArgs.Bad("bad id " + s);
        return id;
    }

    static Item item(String s) {
        ResourceLocation id = id(s);
        if (!ForgeRegistries.ITEMS.containsKey(id)) throw new TestArgs.Bad("no such item " + s);
        return ForgeRegistries.ITEMS.getValue(id);
    }

    /** {@code id}, {@code id{snbt}} or {@code none}; count from {@code *N} at the end ({@code minecraft:apple*5}).
     * SNBT strings in a call use single quotes (the argument splitter strips double quotes). */
    static ItemStack stack(String s) {
        if (s == null || s.equals("none") || s.equals("air") || s.equals("-")) return ItemStack.EMPTY;
        int count = 1;
        int star = s.lastIndexOf('*');
        if (star > 0 && s.substring(star + 1).matches("\\d+")) {
            count = Integer.parseInt(s.substring(star + 1));
            s = s.substring(0, star);
        }
        int brace = s.indexOf('{');
        String idPart = brace < 0 ? s : s.substring(0, brace);
        ItemStack st = new ItemStack(item(idPart), count);
        if (brace >= 0) {
            try {
                CompoundTag tag = TagParser.parseTag(s.substring(brace));
                st.setTag(tag);
            } catch (Exception e) {
                throw new TestArgs.Bad("bad item nbt: " + e.getMessage());
            }
        }
        return st;
    }

    static String key(Item item) {
        return String.valueOf(ForgeRegistries.ITEMS.getKey(item));
    }

    /** {@code id*count} plus the stack's NBT, for the {@code item=} value. */
    static String describe(ItemStack st) {
        if (st.isEmpty()) return "empty";
        String s = key(st.getItem()) + "*" + st.getCount();
        return st.hasTag() ? s + st.getTag() : s;
    }

    static Direction face(String s) {
        if (s == null) return Direction.UP;
        Direction d = Direction.byName(s);
        if (d == null) throw new TestArgs.Bad("bad face " + s);
        return d;
    }

    /**
     * The acting player: {@code as=NAME} (an online player or a perfBot bot), else a Forge fake player in that level
     * standing at {@code at}. The fake player's profile is per dimension: FakePlayerFactory caches by profile and a
     * cached one keeps its first level (gotchas, SQ-SPD-08). {@code held=} replaces the main hand of either.
     */
    ServerPlayer actor(ServerLevel level, BlockPos at) {
        String name = args.opt("as", null);
        ServerPlayer p;
        if (name != null) {
            p = server.getPlayerList().getPlayerByName(name);
            if (p == null) throw new TestArgs.Bad("no player " + name);
        } else {
            String pname = "[cdevtest:" + level.dimension().location().getPath() + "]";
            UUID uuid = UUID.nameUUIDFromBytes(("cdevtest:" + level.dimension().location()).getBytes(StandardCharsets.UTF_8));
            FakePlayer fp = FakePlayerFactory.get(level, new GameProfile(uuid, pname));
            fp.getInventory().clearContent();
            fp.setShiftKeyDown(args.flag("sneak"));
            if (at != null) fp.moveTo(at.getX() + 0.5, at.getY() + 1, at.getZ() + 0.5, 0f, 90f);
            p = fp;
        }
        if (args.has("held")) p.setItemInHand(InteractionHand.MAIN_HAND, stack(args.opt("held", "none")));
        return p;
    }

    static String who(ServerPlayer p) {
        return p instanceof FakePlayer ? "fake" : p.getGameProfile().getName();
    }
}
