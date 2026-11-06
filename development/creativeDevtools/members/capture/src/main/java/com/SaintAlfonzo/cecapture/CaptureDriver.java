package com.SaintAlfonzo.cecapture;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Runs a {@link CapturePlan} as a state machine on the client tick: boot to the title screen, load or create
 * the test world, run the setup steps, then each test (reset prelude, its steps, its captures). Never blocks the
 * render thread; commands go to the integrated server and the step waits for the result.
 *
 * <p>Log markers for the runner: {@code [cecapture] BEGIN <ID>}, {@code STEP <n> <kind> ok|fail <why>},
 * {@code END <ID> ok|fail}, {@code DONE}. One JSON line per finished test goes to the plan's results file;
 * the progress file says where the run is. At the end the driver stops the client itself.
 */
public final class CaptureDriver {

    static final Logger LOG = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    /** Ticks to wait at the title screen before loading (lets late mod screens settle). */
    private static final int TITLE_SETTLE_TICKS = 40;
    private static final int JOIN_TIMEOUT_TICKS = 20 * 60 * 10;

    private enum Phase { BOOT, CREATING, JOINING, SETUP, TESTS, DONE }

    final Minecraft mc = Minecraft.getInstance();
    final CapturePlan plan;
    final Capture capture = new Capture();
    /** Rendered frames so far; steps count settle frames with it. */
    int frames;

    private Phase phase = Phase.BOOT;
    private int phaseTicks;

    private int testIndex = -1;
    private String currentId = "setup";
    private List<Steps.Step> steps = new ArrayList<>();
    private int stepIndex;
    private int testTicks;
    private final List<String> files = new ArrayList<>();
    private final JsonArray stepLog = new JsonArray();
    private String failure;
    private boolean failShotPending;

    CaptureDriver(CapturePlan plan) {
        this.plan = plan;
        MinecraftForge.EVENT_BUS.register(capture);
        LOG.info("[cecapture] plan loaded: {} tests, world {}", plan.tests().size(), plan.worldName());
    }

    String currentId() {
        return currentId;
    }

    void addFile(String f) {
        files.add(f);
    }

    // --- events -------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        try {
            tick();
        } catch (Exception ex) {
            LOG.error("[cecapture] driver error in phase {}", phase, ex);
            if (phase == Phase.TESTS || phase == Phase.SETUP) fail("driver error: " + ex);
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            capture.beginFrame();
            return;
        }
        frames++;
        if (failShotPending) {
            failShotPending = false;
            try {
                capture.shoot(mc, plan.rawDir(), currentId + "__failed", new JsonObject());
                files.add(currentId + "__failed.png");
            } catch (Exception ex) {
                LOG.warn("[cecapture] failed-frame shot for {} not saved: {}", currentId, ex.toString());
            }
            finishTest();
            return;
        }
        if ((phase == Phase.TESTS || phase == Phase.SETUP) && failure == null && stepIndex < steps.size()) {
            try {
                steps.get(stepIndex).onFrameEnd(this);
            } catch (Exception ex) {
                fail(steps.get(stepIndex).kind + ": " + ex);
            }
        }
    }

    // --- phases -------------------------------------------------------------------------------------------------

    private void setPhase(Phase p) {
        phase = p;
        phaseTicks = 0;
        LOG.info("[cecapture] phase {}", p);
    }

    private void tick() throws Exception {
        phaseTicks++;
        switch (phase) {
            case BOOT -> {
                boolean idle = mc.getOverlay() == null && mc.level == null && mc.screen != null;
                if (!idle) phaseTicks = 0;
                if (phaseTicks < TITLE_SETTLE_TICKS) return;
                writeProgress("loading");
                if (TestWorld.exists(mc, plan.worldName())) {
                    LOG.info("[cecapture] loading world {}", plan.worldName());
                    TestWorld.load(mc, plan.worldName());
                    setPhase(Phase.JOINING);
                } else {
                    LOG.info("[cecapture] creating flat world {} ({})", plan.worldName(), plan.worldBiome());
                    TestWorld.openCreateScreen(mc);
                    setPhase(Phase.CREATING);
                }
            }
            case CREATING -> {
                if (mc.screen instanceof CreateWorldScreen cws) {
                    TestWorld.createFlat(mc, cws, plan);
                    setPhase(Phase.JOINING);
                } else if (phaseTicks > JOIN_TIMEOUT_TICKS) {
                    abort("create world screen never opened");
                }
            }
            case JOINING -> {
                confirmDialogs();
                if (mc.player != null && mc.level != null && mc.screen == null && phaseTicks > 20) {
                    setPhase(Phase.SETUP);
                    currentId = "setup";
                    beginSteps(setupSteps());
                } else if (phaseTicks > JOIN_TIMEOUT_TICKS) {
                    abort("world never joined (screen " + (mc.screen == null ? "none" : mc.screen.getClass().getName()) + ")");
                }
            }
            case SETUP, TESTS -> runSteps();
            case DONE -> {
                if (phaseTicks == 20) mc.stop();
            }
        }
    }

    private List<Steps.Step> setupSteps() {
        List<Steps.Step> s = new ArrayList<>();
        for (JsonObject o : plan.setup()) s.add(Steps.parse(o, this));
        s.add(new Steps.Wait("jei"));
        s.add(new Steps.Wait("chunks"));
        return s;
    }

    /** Experimental-settings and backup prompts on world load: press the proceed button. */
    private void confirmDialogs() {
        Screen s = mc.screen;
        if (!(s instanceof ConfirmScreen || s instanceof BackupConfirmScreen) || phaseTicks % 20 != 0) return;
        Set<String> proceed = Set.of("selectWorld.backupJoinSkipButton", "gui.proceed", "gui.yes", "gui.continue");
        for (GuiEventListener l : s.children()) {
            if (l instanceof Button b && b.getMessage().getContents() instanceof TranslatableContents t
                    && proceed.contains(t.getKey())) {
                LOG.info("[cecapture] confirming {} ({})", s.getClass().getSimpleName(), t.getKey());
                b.onPress();
                return;
            }
        }
    }

    // --- tests --------------------------------------------------------------------------------------------------

    private void beginSteps(List<Steps.Step> list) {
        steps = list;
        stepIndex = 0;
        testTicks = 0;
        files.clear();
        while (stepLog.size() > 0) stepLog.remove(0);
        failure = null;
    }

    private void nextTest() {
        testIndex++;
        if (testIndex >= plan.tests().size()) {
            currentId = "done";
            writeProgress("done");
            LOG.info("[cecapture] DONE");
            setPhase(Phase.DONE);
            return;
        }
        CapturePlan.Test t = plan.tests().get(testIndex);
        currentId = t.id();
        writeProgress("running");
        LOG.info("[cecapture] BEGIN {}", t.id());
        List<Steps.Step> list = new ArrayList<>(resetSteps());
        try {
            for (JsonObject o : t.steps()) list.add(Steps.parse(o, this));
            for (JsonObject o : t.capture()) {
                JsonObject wrap = new JsonObject();
                wrap.add("capture", o);
                list.add(Steps.parse(wrap, this));
            }
        } catch (RuntimeException ex) {
            beginSteps(List.of());
            fail("bad step: " + ex.getMessage());
            return;
        }
        beginSteps(list);
    }

    /** Before every test: close the screen, release keys, clear inventory and effects, noon, clear weather, creative. */
    private List<Steps.Step> resetSteps() {
        List<Steps.Step> s = new ArrayList<>();
        s.add(new Steps.Instant("reset", m -> {
            VirtualInput.releaseAll();
            capture.clearHide(m);
            m.options.advancedItemTooltips = false;
            m.options.renderDebug = false;
            if (m.screen instanceof AbstractContainerScreen<?>) m.player.closeContainer();
            else if (m.screen != null) m.setScreen(null);
            m.gui.getChat().clearMessages(false);
            m.getToasts().clear();
        }));
        for (String c : new String[]{"gamemode creative @s", "clear @s", "effect clear @s", "time set 6000", "weather clear"}) {
            s.add(new Steps.Cmd(c, true));
        }
        return s;
    }

    private void runSteps() {
        if (failure != null) return; // waiting for the failed-frame shot
        testTicks++;
        if (stepIndex >= steps.size()) {
            finishTest();
            return;
        }
        Steps.Step step = steps.get(stepIndex);
        try {
            boolean done = step.tick(this);
            step.ticks++;
            if (done) {
                logStep(step, true, step.note);
                stepIndex++;
            } else if (step.ticks > plan.stepTimeoutTicks()) {
                fail(step.kind + ": timeout after " + step.ticks + " ticks" + (step.note.isEmpty() ? "" : " (" + step.note + ")"));
            } else if (phase == Phase.TESTS && testTicks > plan.testTimeoutTicks()) {
                fail("test timeout after " + testTicks + " ticks");
            }
        } catch (Exception ex) {
            String why = ex instanceof Steps.StepFailed ? ex.getMessage() : ex.toString();
            fail(step.kind + ": " + why);
        }
    }

    private void logStep(Steps.Step step, boolean ok, String why) {
        LOG.info("[cecapture] STEP {} {} {} {}", stepIndex, step.kind, ok ? "ok" : "fail", why);
        JsonObject o = new JsonObject();
        o.addProperty("n", stepIndex);
        o.addProperty("kind", step.kind);
        o.addProperty("ok", ok);
        o.addProperty("ticks", step.ticks);
        if (!why.isEmpty()) o.addProperty("note", why);
        stepLog.add(o);
    }

    private void fail(String why) {
        if (failure != null) return;
        failure = why;
        if (stepIndex < steps.size()) logStep(steps.get(stepIndex), false, why);
        else LOG.info("[cecapture] STEP {} - fail {}", stepIndex, why);
        failShotPending = true; // finishTest runs after the next frame is saved
    }

    private void finishTest() {
        boolean ok = failure == null;
        capture.clearHide(mc);
        if (phase == Phase.SETUP) {
            if (!ok) LOG.warn("[cecapture] setup failed: {} (running the tests anyway)", failure);
            setPhase(Phase.TESTS);
            nextTest();
            return;
        }
        LOG.info("[cecapture] END {} {}", currentId, ok ? "ok" : "fail");
        JsonObject r = new JsonObject();
        r.addProperty("id", currentId);
        r.addProperty("status", ok ? "ok" : "fail");
        if (!ok) r.addProperty("error", failure);
        JsonArray f = new JsonArray();
        files.forEach(f::add);
        r.add("files", f);
        r.add("steps", stepLog.deepCopy());
        r.addProperty("ticks", testTicks);
        try {
            Files.createDirectories(plan.results().getParent());
            Files.writeString(plan.results(), GSON.toJson(r) + "\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            LOG.error("[cecapture] can't write results", ex);
        }
        nextTest();
    }

    private void abort(String why) {
        LOG.error("[cecapture] ABORT {}", why);
        writeProgress("aborted: " + why);
        setPhase(Phase.DONE);
    }

    private void writeProgress(String state) {
        JsonObject o = new JsonObject();
        o.addProperty("state", state);
        o.addProperty("current", currentId);
        o.addProperty("index", Math.max(testIndex, 0));
        o.addProperty("total", plan.tests().size());
        o.addProperty("time", System.currentTimeMillis());
        try {
            Path p = plan.progress();
            Files.createDirectories(p.getParent());
            Files.writeString(p, GSON.toJson(o), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOG.error("[cecapture] can't write progress", ex);
        }
    }
}
