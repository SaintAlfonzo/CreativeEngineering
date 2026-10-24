package com.SaintAlfonzo.cecapture;

import com.SaintAlfonzo.cecapture.compat.CaptureJeiPlugin;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The step kinds of a capture spec (feature_writeup/client_capture.md 2.2) plus the driver's own {@code capture}
 * step. A step runs on the client thread, one {@link Step#tick} per client tick until it returns true; it fails
 * the test by throwing. {@link Step#onFrameEnd} is called at the end of every rendered frame while the step is
 * current (captures happen there).
 */
final class Steps {

    /** Frames rendered after a wait condition is met, before the step counts as done. */
    static final int SETTLE_FRAMES = 3;

    private Steps() {}

    abstract static class Step {
        final String kind;
        int ticks;

        Step(String kind) {
            this.kind = kind;
        }

        abstract boolean tick(CaptureDriver d) throws Exception;

        void onFrameEnd(CaptureDriver d) throws Exception {}

        /** One line for the step log; set while running (command result, wait reason). */
        String note = "";
    }

    static Step parse(JsonObject o, CaptureDriver d) {
        if (o.has("cmd")) return new Cmd(o.get("cmd").getAsString(), bool(o, "allow_fail"));
        if (o.has("look")) return new Look(o.getAsJsonObject("look"));
        if (o.has("wait")) return new Wait(o.get("wait").getAsString());
        if (o.has("slot")) return new Instant("slot", mc -> mc.player.getInventory().selected =
                Math.floorMod(o.get("slot").getAsInt(), Inventory.getSelectionSize()));
        if (o.has("use")) return new Use(o.get("use").getAsString());
        if (o.has("attack")) return new Attack();
        if (o.has("screen")) return new OpenScreen(o.get("screen").getAsString());
        if (o.has("tooltip")) return new Tooltip(o);
        if (o.has("hover")) return new Hover(o.get("hover"));
        if (o.has("key")) return new Key(o.get("key"));
        if (o.has("reload")) return new Reload();
        if (o.has("capture")) return new CaptureShot(o.getAsJsonObject("capture"));
        throw new IllegalArgumentException("unknown step " + o);
    }

    // --- helpers ----------------------------------------------------------------------------------------------

    static boolean bool(JsonObject o, String key) {
        return o.has(key) && o.get(key).getAsBoolean();
    }

    static double[] pos(String s) {
        String[] p = s.trim().split("\\s+");
        if (p.length != 3) throw new IllegalArgumentException("position needs 3 numbers: " + s);
        return new double[]{Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2])};
    }

    static ItemStack stack(String id, int count, String nbt) throws Exception {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null || ForgeRegistries.ITEMS.getKey(item) == null
                || !ForgeRegistries.ITEMS.getKey(item).equals(new ResourceLocation(id))) {
            throw new IllegalArgumentException("no item " + id);
        }
        ItemStack st = new ItemStack(item, Math.max(1, count));
        if (nbt != null && !nbt.isBlank()) st.setTag(TagParser.parseTag(nbt));
        return st;
    }

    static boolean screenIs(Screen s, String name) {
        return s != null && (s.getClass().getName().equals(name) || s.getClass().getSimpleName().equals(name));
    }

    // --- cmd ----------------------------------------------------------------------------------------------------

    /** Runs a command on the integrated server as the player (op level 4) and keeps its feedback. */
    static class Cmd extends Step {
        final String cmd;
        final boolean allowFail;
        CompletableFuture<int[]> result;
        final List<String> feedback = new ArrayList<>();
        int doneAt = -1;

        Cmd(String cmd, boolean allowFail) {
            super("cmd");
            this.cmd = cmd;
            this.allowFail = allowFail;
        }

        @Override
        boolean tick(CaptureDriver d) throws Exception {
            Minecraft mc = d.mc;
            if (result == null) {
                IntegratedServer server = mc.getSingleplayerServer();
                if (server == null || mc.player == null) throw new IllegalStateException("no integrated server");
                UUID uuid = mc.player.getUUID();
                CommandSource collector = new CommandSource() {
                    @Override
                    public void sendSystemMessage(Component c) {
                        synchronized (feedback) {
                            feedback.add(c.getString());
                        }
                    }

                    @Override
                    public boolean acceptsSuccess() {
                        return true;
                    }

                    @Override
                    public boolean acceptsFailure() {
                        return true;
                    }

                    @Override
                    public boolean shouldInformAdmins() {
                        return false;
                    }
                };
                result = CompletableFuture.supplyAsync(() -> {
                    ServerPlayer sp = server.getPlayerList().getPlayer(uuid);
                    if (sp == null) return new int[]{-1};
                    CommandSourceStack src = sp.createCommandSourceStack().withPermission(4).withSource(collector);
                    return new int[]{server.getCommands().performPrefixedCommand(src, cmd)};
                }, server);
                return false;
            }
            if (!result.isDone()) return false;
            if (doneAt < 0) {
                int r = result.get()[0];
                synchronized (feedback) {
                    note = "/" + cmd + " -> " + r + (feedback.isEmpty() ? "" : " " + String.join(" | ", feedback));
                }
                if (r <= 0 && !allowFail) throw new StepFailed(note);
                doneAt = ticks;
            }
            // Two ticks for the block / entity updates to reach the client.
            return ticks >= doneAt + 2;
        }
    }

    // --- look ---------------------------------------------------------------------------------------------------

    /**
     * {@code {"from": "x y z", "at": "x y z"}} or {@code {"from": ..., "yaw": a, "pitch": b}}. {@code from} is the
     * camera (eye) position; {@code at} aims at the centre of that block. Flies, so the player doesn't fall.
     */
    static class Look extends Step {
        final Cmd tp;
        final float yaw, pitch;

        Look(JsonObject o) {
            super("look");
            double[] from = pos(o.get("from").getAsString());
            double ex = from[0] + 0.5, ey = from[1], ez = from[2] + 0.5;
            if (o.has("at")) {
                double[] at = pos(o.get("at").getAsString());
                double dx = at[0] + 0.5 - ex, dy = at[1] + 0.5 - ey, dz = at[2] + 0.5 - ez;
                yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            } else {
                yaw = o.get("yaw").getAsFloat();
                pitch = o.get("pitch").getAsFloat();
            }
            tp = new Cmd(String.format(Locale.ROOT, "tp @s %.3f %.3f %.3f %.2f %.2f",
                    ex, ey - 1.62, ez, yaw, pitch), false);
        }

        @Override
        boolean tick(CaptureDriver d) throws Exception {
            LocalPlayer p = d.mc.player;
            if (tp.ticks == 0 && p.getAbilities().mayfly) {
                p.getAbilities().flying = true;
                p.onUpdateAbilities();
            }
            tp.ticks++;
            if (!tp.tick(d)) return false;
            p.setYRot(yaw);
            p.setXRot(pitch);
            p.yRotO = yaw;
            p.xRotO = pitch;
            note = tp.note;
            return true;
        }
    }

    // --- wait ---------------------------------------------------------------------------------------------------

    /** Ticks ({@code "20"}), {@code chunks}, {@code screen:<class>}, {@code jei}, {@code resources}; then settle frames. */
    static class Wait extends Step {
        final String what;
        int settleUntil = -1;
        int stableTicks;

        Wait(String what) {
            super("wait");
            this.what = what;
        }

        @Override
        boolean tick(CaptureDriver d) {
            if (settleUntil < 0) {
                if (!met(d)) return false;
                settleUntil = d.frames + SETTLE_FRAMES;
            }
            return d.frames >= settleUntil;
        }

        private boolean met(CaptureDriver d) {
            Minecraft mc = d.mc;
            if (what.matches("\\d+")) return ticks >= Integer.parseInt(what);
            switch (what) {
                case "chunks" -> {
                    boolean ok = ticks >= 10 && mc.player != null
                            && mc.levelRenderer.isChunkCompiled(mc.player.blockPosition())
                            && mc.levelRenderer.hasRenderedAllChunks();
                    stableTicks = ok ? stableTicks + 1 : 0;
                    return stableTicks >= 5;
                }
                case "jei" -> {
                    return !ModList.get().isLoaded("jei") || CaptureJeiPlugin.ready();
                }
                case "resources" -> {
                    return mc.getOverlay() == null;
                }
                default -> {
                    if (what.startsWith("screen:")) return screenIs(mc.screen, what.substring(7));
                    throw new IllegalArgumentException("unknown wait " + what);
                }
            }
        }
    }

    // --- instant actions ----------------------------------------------------------------------------------------

    interface McAction {
        void run(Minecraft mc) throws Exception;
    }

    static class Instant extends Step {
        final McAction action;

        Instant(String kind, McAction action) {
            super(kind);
            this.action = action;
        }

        @Override
        boolean tick(CaptureDriver d) throws Exception {
            if (ticks == 0) action.run(d.mc);
            return ticks >= 2;
        }
    }

    /** Right-click: {@code "auto"} uses the targeted block, else the held item; {@code "block"} / {@code "item"} force one. */
    static class Use extends Step {
        final String mode;

        Use(String mode) {
            super("use");
            this.mode = mode;
        }

        @Override
        boolean tick(CaptureDriver d) {
            Minecraft mc = d.mc;
            if (ticks == 0) {
                HitResult hit = mc.hitResult;
                boolean onBlock = hit instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK;
                if (mode.equals("block") && !onBlock) throw new StepFailed("use: no block targeted");
                if (onBlock && !mode.equals("item")) {
                    note = "on block " + ((BlockHitResult) hit).getBlockPos().toShortString() + " -> "
                            + mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, (BlockHitResult) hit);
                } else {
                    note = "item -> " + mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                }
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
            return ticks >= 3;
        }
    }

    static class Attack extends Step {
        Attack() {
            super("attack");
        }

        @Override
        boolean tick(CaptureDriver d) {
            Minecraft mc = d.mc;
            if (ticks == 0) {
                HitResult hit = mc.hitResult;
                if (hit instanceof EntityHitResult e) {
                    mc.gameMode.attack(mc.player, e.getEntity());
                    note = "entity " + e.getEntity().getType();
                } else if (hit instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK) {
                    mc.gameMode.startDestroyBlock(b.getBlockPos(), b.getDirection());
                    note = "block " + b.getBlockPos().toShortString();
                } else {
                    throw new StepFailed("attack: nothing targeted");
                }
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
            return ticks >= 3;
        }
    }

    // --- screens ------------------------------------------------------------------------------------------------

    private static Method selectTab;

    /** {@code inventory}, {@code creative:<tab>}, {@code jei_recipes:<id>}, {@code jei_uses:<id>}, {@code jei_category:<uid>}, {@code close}. */
    static class OpenScreen extends Step {
        final String what;
        boolean opened;

        OpenScreen(String what) {
            super("screen");
            this.what = what;
        }

        @Override
        boolean tick(CaptureDriver d) throws Exception {
            Minecraft mc = d.mc;
            String arg = what.contains(":") ? what.substring(what.indexOf(':') + 1) : "";
            if (!opened) {
                opened = true;
                if (what.equals("close")) {
                    if (mc.screen instanceof AbstractContainerScreen<?>) mc.player.closeContainer();
                    else mc.setScreen(null);
                } else if (what.equals("inventory") || what.startsWith("creative:")) {
                    if (mc.gameMode.getPlayerMode() == GameType.CREATIVE) mc.setScreen(new CreativeModeInventoryScreen(mc.player));
                    else if (what.startsWith("creative:")) throw new StepFailed("creative tab needs creative mode");
                    else mc.setScreen(new InventoryScreen(mc.player));
                } else if (what.startsWith("jei_")) {
                    if (!ModList.get().isLoaded("jei")) throw new StepFailed("JEI not loaded");
                    if (what.startsWith("jei_recipes:")) CaptureJeiPlugin.showRecipes(stack(arg, 1, null), false);
                    else if (what.startsWith("jei_uses:")) CaptureJeiPlugin.showRecipes(stack(arg, 1, null), true);
                    else if (what.startsWith("jei_category:")) CaptureJeiPlugin.showCategory(arg);
                    else throw new IllegalArgumentException("unknown screen " + what);
                } else {
                    throw new IllegalArgumentException("unknown screen " + what);
                }
                return false;
            }
            if (what.equals("close")) return mc.screen == null;
            if (what.startsWith("jei_")) return screenIs(mc.screen, "mezz.jei.gui.recipes.RecipesGui") && ticks >= 3;
            if (!(mc.screen instanceof AbstractContainerScreen<?>)) return false;
            if (what.startsWith("creative:") && ticks == 2) {
                CreativeModeTab tab = null;
                for (CreativeModeTab t : CreativeModeTab.TABS) {
                    if (t.getRecipeFolderName().equalsIgnoreCase(arg)
                            || t.getDisplayName().getString().equalsIgnoreCase(arg)) tab = t;
                }
                if (tab == null) throw new StepFailed("no creative tab " + arg);
                if (selectTab == null) {
                    selectTab = ObfuscationReflectionHelper.findMethod(CreativeModeInventoryScreen.class, "m_98560_",
                            CreativeModeTab.class);
                }
                selectTab.invoke(mc.screen, tab);
                note = "tab " + tab.getRecipeFolderName();
            }
            return ticks >= 4;
        }
    }

    /** Opens the stage screen. Options: {@code shift}, {@code ctrl}, {@code alt}, {@code advanced}, {@code count}, {@code nbt}. */
    static class Tooltip extends Step {
        final JsonObject o;

        Tooltip(JsonObject o) {
            super("tooltip");
            this.o = o;
        }

        @Override
        boolean tick(CaptureDriver d) throws Exception {
            Minecraft mc = d.mc;
            if (ticks == 0) {
                ItemStack st = stack(o.get("tooltip").getAsString(), o.has("count") ? o.get("count").getAsInt() : 1,
                        o.has("nbt") ? o.get("nbt").getAsString() : null);
                VirtualInput.shift = bool(o, "shift");
                VirtualInput.ctrl = bool(o, "ctrl");
                VirtualInput.alt = bool(o, "alt");
                mc.options.advancedItemTooltips = bool(o, "advanced");
                StageScreen stage = new StageScreen(st);
                mc.setScreen(stage);
                // The tooltip hangs off the mouse; park the virtual mouse on the stage's anchor.
                Window w = mc.getWindow();
                VirtualInput.mouseX = stage.anchorX() * (double) w.getScreenWidth() / w.getGuiScaledWidth();
                VirtualInput.mouseY = stage.anchorY() * (double) w.getScreenHeight() / w.getGuiScaledHeight();
                VirtualInput.mouseActive = true;
                return false;
            }
            return mc.screen instanceof StageScreen && ticks >= 3;
        }
    }

    /** {@code {"slot": N}} (slot index in the open container), {@code {"x": gx, "y": gy}} (GUI coords) or {@code "off"}. */
    static class Hover extends Step {
        final JsonElement arg;

        Hover(JsonElement arg) {
            super("hover");
            this.arg = arg;
        }

        @Override
        boolean tick(CaptureDriver d) {
            Minecraft mc = d.mc;
            if (ticks == 0) {
                if (arg.isJsonPrimitive() && arg.getAsString().equals("off")) {
                    VirtualInput.mouseActive = false;
                    return false;
                }
                JsonObject o = arg.getAsJsonObject();
                double gx, gy;
                if (o.has("slot")) {
                    if (!(mc.screen instanceof AbstractContainerScreen<?> acs)) throw new StepFailed("hover slot: no container screen");
                    int n = o.get("slot").getAsInt();
                    List<Slot> slots = acs.getMenu().slots;
                    if (n < 0 || n >= slots.size()) throw new StepFailed("hover: no slot " + n + " (" + slots.size() + " slots)");
                    Slot s = slots.get(n);
                    gx = acs.getGuiLeft() + s.x + 8;
                    gy = acs.getGuiTop() + s.y + 8;
                } else {
                    gx = o.get("x").getAsDouble();
                    gy = o.get("y").getAsDouble();
                }
                Window w = mc.getWindow();
                VirtualInput.mouseX = (gx + 0.5) * w.getScreenWidth() / w.getGuiScaledWidth();
                VirtualInput.mouseY = (gy + 0.5) * w.getScreenHeight() / w.getGuiScaledHeight();
                VirtualInput.mouseActive = true;
                note = "gui " + (int) gx + "," + (int) gy;
            }
            return ticks >= 2;
        }
    }

    /** Holds modifiers until the test ends: {@code "shift"}, {@code ["shift", "ctrl"]}, or {@code "none"}. */
    static class Key extends Step {
        final Set<String> keys = new HashSet<>();

        Key(JsonElement arg) {
            super("key");
            if (arg.isJsonArray()) arg.getAsJsonArray().forEach(e -> keys.add(e.getAsString()));
            else keys.add(arg.getAsString());
        }

        @Override
        boolean tick(CaptureDriver d) {
            VirtualInput.shift = keys.contains("shift");
            VirtualInput.ctrl = keys.contains("ctrl");
            VirtualInput.alt = keys.contains("alt");
            return ticks >= 1;
        }
    }

    static class Reload extends Step {
        CompletableFuture<Void> future;

        Reload() {
            super("reload");
        }

        @Override
        boolean tick(CaptureDriver d) {
            if (future == null) {
                future = d.mc.reloadResourcePacks();
                return false;
            }
            return future.isDone() && d.mc.getOverlay() == null;
        }
    }

    // --- capture ------------------------------------------------------------------------------------------------

    /**
     * {@code {"name": "", "hide": [...], "frames": [n, interval]}}. The crop rule isn't the driver's business:
     * crop.py reads it from the spec. Shots are taken at the end of a frame, after {@link #SETTLE_FRAMES}
     * frames with the hide state applied.
     */
    static class CaptureShot extends Step {
        final String name;
        final Set<String> hide = new HashSet<>();
        final int count, interval;
        int shot, settleUntil = -1, nextTick;

        CaptureShot(JsonObject o) {
            super("capture");
            name = o.has("name") ? o.get("name").getAsString() : "";
            if (o.has("hide")) o.getAsJsonArray("hide").forEach(e -> hide.add(e.getAsString()));
            JsonArray f = o.getAsJsonArray("frames");
            count = f == null ? 1 : Math.max(1, f.get(0).getAsInt());
            interval = f == null || f.size() < 2 ? 0 : f.get(1).getAsInt();
        }

        @Override
        boolean tick(CaptureDriver d) {
            if (shot >= count) {
                d.capture.clearHide(d.mc);
                return true;
            }
            if (settleUntil < 0 && ticks >= nextTick) {
                d.capture.applyHide(d.mc, hide);
                settleUntil = d.frames + SETTLE_FRAMES;
            }
            return false;
        }

        @Override
        void onFrameEnd(CaptureDriver d) throws Exception {
            if (settleUntil < 0 || d.frames < settleUntil || shot >= count) return;
            String file = d.currentId() + (name.isEmpty() ? "" : "_" + name) + (count > 1 ? "_f" + (shot + 1) : "");
            JsonObject extra = new JsonObject();
            extra.addProperty("test", d.currentId());
            extra.addProperty("name", name);
            extra.addProperty("frame_index", shot + 1);
            d.capture.shoot(d.mc, d.plan.rawDir(), file, extra);
            d.addFile(file + ".png");
            shot++;
            settleUntil = -1;
            nextTick = ticks + interval;
        }
    }

    /** A step failure with a reason for the step log (not a crash). */
    static class StepFailed extends RuntimeException {
        StepFailed(String why) {
            super(why);
        }
    }
}
