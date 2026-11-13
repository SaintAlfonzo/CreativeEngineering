// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3.
// Phase P9a gate (feature_writeup/plan_power_grid.md P9a, power_grid.md SQ-PWG-06): the solver's non-linear models as
// the circuit board builds them. Each test makes the wires a board component's bake() makes, with that component's
// default properties (the constants below; source: DiodeComponent, BJTComponent, ElectronTubeComponent,
// PentodeComponent, ThyratronComponent, RegulatorTubeComponent at v0.6.2), puts them on the network the game builds
// (GraphedElectricalNetwork, G_MIN on, Java backend), drives them from ideal sources and checks that it settles (solver converged and nodes steady)
// within 2 s and the operating point is physical. The component classes themselves cannot load here (their static init reaches
// PowerGrid's Architectury DeferredRegisters and Create's SRG-named Lang), so the parameters are copied; a hot tube
// gets the saturation current or emission the component's thermal callback sets at its operating temperature (1400 C),
// a cold one zero. Terminals sit on their own nodes joined to the sources by 1 mohm, as upstream's tube tests do (the
// junction wires do not take a null node, PortGateTests gate 4). Run by tools/test_power_grid_solver.sh.
package org.patryk3211.electricity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.patryk3211.powergrid.config.CSolver;
import org.patryk3211.powergrid.electricity.sim.GraphedElectricalNetwork;
import org.patryk3211.powergrid.electricity.sim.NetworkGraph;
import org.patryk3211.powergrid.electricity.sim.node.FloatingNode;
import org.patryk3211.powergrid.electricity.sim.node.IElectricNode;
import org.patryk3211.powergrid.electricity.sim.special.BJTWire;
import org.patryk3211.powergrid.electricity.sim.special.ColdCathodeRegulatorTubeWire;
import org.patryk3211.powergrid.electricity.sim.special.ElectronTubeWire;
import org.patryk3211.powergrid.electricity.sim.special.PNJunctionWire;
import org.patryk3211.powergrid.electricity.sim.special.PentodeWire;
import org.patryk3211.powergrid.electricity.sim.special.ThyratronWire;

import java.io.OutputStream;
import java.io.PrintStream;

public class BoardComponentTests extends TestHelper {
    private static final int MAX_TICKS = 200;
    /** Ticks the nodes must stay steady to count as settled (longer than a cut-off tube's stall before it walks). */
    private static final int STABLE_TICKS = 60;
    /** Ticks a stage may take to settle (2 s at 20 ticks per second). */
    private static final int SETTLE_LIMIT = 40;
    private static final float R_TINY = 0.001f;

    private static void report(String format, Object... args) {
        PortGateTests.REPORT.add(String.format(format, args));
    }

    private static Network game() {
        var net = new Network();
        net.network = new GraphedElectricalNetwork(new NetworkGraph(), true, CSolver.SolverBackend.JAVA::create);
        net.network.warmUp(-1);
        // WorldNetworks.createNetwork copies the solver config's tube step limits (default 0.5; the class default is 0.8)
        net.network.triodeLimAnode = 0.5;
        net.network.triodeLimCathode = 0.5;
        net.network.triodeLimGrid = 0.5;
        return net;
    }

    /** A terminal node tied to {@code source} by 1 mohm. */
    private static FloatingNode tie(Network net, IElectricNode source) {
        var n = net.N();
        net.W(R_TINY, source, n);
        return n;
    }

    /**
     * Solves until the solver reports convergence and the watched nodes have then stayed within 1 uV for STABLE_TICKS
     * ticks; returns the tick at which that steady stretch began, or -1 when it does not settle within MAX_TICKS. The
     * tubes' Newton step limiter (network triodeLim*, upstream) moves a tube's evaluation point by at most 0.5 * 0.5 V
     * per iteration (config default), so a tube that starts in cut-off stalls, then walks, for about 35 ticks (triode
     * at -4 V) while isConverged() already reports true (power_grid.md 3.5, B18): the steady stretch is longer than that
     * stall, so the walk counts.
     */
    private static int settle(Network net, FloatingNode... watched) {
        var out = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        try {
            double[] last = new double[watched.length];
            int stable = 0;
            for(int t = 0; t < MAX_TICKS; ++t) {
                net.calculate();
                boolean same = t > 0;
                for(int i = 0; i < watched.length; ++i) {
                    double v = watched[i].getVoltage();
                    if(Math.abs(v - last[i]) > 1e-6)
                        same = false;
                    last[i] = v;
                }
                stable = same && net.network.isConverged() ? stable + 1 : 0;
                if(stable >= STABLE_TICKS)
                    return t + 1 - STABLE_TICKS;
            }
            return -1;
        } finally {
            System.setOut(out);
        }
    }

    private static boolean within(double ticks) {
        return ticks > 0 && ticks <= SETTLE_LIMIT;
    }

    private static void finite(String name, double... values) {
        for(double v : values)
            Assertions.assertTrue(Double.isFinite(v), name + ": non-finite value " + v);
    }

    // DiodeComponent.bake: new PNJunctionWire(5.47e-9, 0.075f, 22, 1.783, 1000, 1e-6, anode, cathode) ("1N4007 variation")
    private static PNJunctionWire diode(IElectricNode anode, IElectricNode cathode) {
        return new PNJunctionWire(5.47e-9, 0.075f, 22, 1.783, 1000, 1e-6, anode, cathode);
    }

    // Diode: forward drop through 1 k from 5 V, reverse leakage at -5 V.
    @Test
    void diodeForwardAndReverse() {
        for(double supply : new double[] { 5, -5 }) {
            var net = game();
            var V = net.V((float) supply);
            var GND = net.V(0);
            var A = net.N();
            net.W(1000, V, A);
            var D = diode(A, tie(net, GND));
            net.network.addWire(D);
            int ticks = settle(net, A);
            double drop = A.getVoltage();
            double current = (supply - drop) / 1000;
            String name = "board diode " + (supply > 0 ? "forward" : "reverse");
            report("%s: V(AK) = %.4f V, I = %.6g A (diode wire %.6g A), settled in %d ticks", name, drop, current, D.current(), ticks);
            finite(name, drop, current);
            Assertions.assertTrue(ticks > 0 && ticks <= SETTLE_LIMIT, name + ": settled in " + ticks + " ticks (limit " + SETTLE_LIMIT + ")");
            if(supply > 0) {
                Assertions.assertTrue(drop > 0.55 && drop < 0.85, name + ": forward drop " + drop);
                Assertions.assertEquals(current, D.current(), 1e-6, name + ": diode current vs resistor current");
            } else {
                Assertions.assertTrue(Math.abs(current) < 1e-5, name + ": reverse current " + current);
            }
        }
    }

    // BJTComponent.bake: new BJTWire(collector, base, emitter, 5.47e-12, gain (default 20), 0.1, pnp). Common emitter
    // from 5 V, Rc = 1 k: Rb = 100 k puts it in the active region (Ic about gain * Ib); Rb = 4.7 k saturates it.
    private static double[] bjtStage(boolean pnp, float rb) {
        var net = game();
        var VCC = net.V(5);
        var GND = net.V(0);
        var C = net.N();
        var B = net.N();
        if(!pnp) {
            net.W(1000, VCC, C);
            net.W(rb, VCC, B);
            net.network.addWire(new BJTWire(C, B, tie(net, GND), 5.47e-12, 20, 0.1, false));
        } else {
            // Emitter on +5 V, base pulled down, collector load to ground
            net.W(1000, C, GND);
            net.W(rb, B, GND);
            net.network.addWire(new BJTWire(C, B, tie(net, VCC), 5.47e-12, 20, 0.1, true));
        }
        int ticks = settle(net, C, B);
        double vc = C.getVoltage(), vb = B.getVoltage();
        double ic = pnp ? vc / 1000 : (5 - vc) / 1000;
        double ib = pnp ? vb / rb : (5 - vb) / rb;
        return new double[] { ticks, vc, vb, ic, ib };
    }

    @Test
    void bjtActiveAndSaturated() {
        for(boolean pnp : new boolean[] { false, true }) {
            String kind = pnp ? "PNP" : "NPN";
            var active = bjtStage(pnp, 100_000);
            var sat = bjtStage(pnp, 4_700);
            double beta = active[3] / active[4];
            double vce = pnp ? 5 - active[1] : active[1];
            double vceSat = pnp ? 5 - sat[1] : sat[1];
            double vbe = pnp ? 5 - active[2] : active[2];
            report("board %s, Rb 100 k: Vbe = %.4f V, Ic = %.4f mA, Ib = %.4f uA, Ic/Ib = %.2f (gain 20), Vce = %.3f V, %d ticks",
                    kind, vbe, active[3] * 1000, active[4] * 1e6, beta, vce, (int) active[0]);
            report("board %s, Rb 4.7 k: Vce(sat) = %.4f V, Ic = %.4f mA, %d ticks", kind, vceSat, sat[3] * 1000, (int) sat[0]);
            finite("board " + kind, active[1], active[2], sat[1], sat[2]);
            Assertions.assertTrue(within(active[0]) && within(sat[0]), "board " + kind + ": settled in " + active[0] + " / " + sat[0] + " ticks");
            Assertions.assertTrue(vbe > 0.4 && vbe < 0.9, "board " + kind + ": Vbe " + vbe);
            Assertions.assertTrue(beta > 15 && beta < 25, "board " + kind + ": Ic/Ib " + beta + " not near the gain 20");
            Assertions.assertTrue(vce > 1, "board " + kind + ": active stage Vce " + vce + " (should not saturate)");
            Assertions.assertTrue(vceSat < 0.4, "board " + kind + ": Vce(sat) " + vceSat);
        }
    }

    // ElectronTubeComponent (triode) defaults: mu 5, kg 13600, kp 600, kvb 300, ex 1.5, saturation current 0.1 A (the
    // thermal callback scales it by clamp(T - 1300, 0, 150) / 100: 1 at 1400 C, 0 cold). Anode from 250 V through 10 k,
    // cathode at 0 V, grid at 0 V and -4 V.
    private static double[] triode(float grid, boolean hot) {
        var net = game();
        var HV = net.V(250);
        var GND = net.V(0);
        var G = net.V(grid);
        var A = net.N();
        net.W(10_000, HV, A);
        var tube = new ElectronTubeWire(5, 13_600, 600, 300, 1.5f, 0.1f, tie(net, GND), A, tie(net, G));
        tube.setSaturationCurrent(hot ? 0.1f : 0f);
        net.network.addWire(tube);
        int ticks = settle(net, A);
        return new double[] { ticks, A.getVoltage(), (250 - A.getVoltage()) / 10_000 };
    }

    @Test
    void triodeGridControl() {
        var g0 = triode(0, true);
        var g4 = triode(-4, true);
        var cold = triode(0, false);
        report("board triode hot: Vg 0 V: Va = %.3f V, Ia = %.4f mA (%d ticks); Vg -4 V: Va = %.3f V, Ia = %.4f mA (%d ticks); cold: Ia = %.3g mA (%d ticks)",
                g0[1], g0[2] * 1000, (int) g0[0], g4[1], g4[2] * 1000, (int) g4[0], cold[2] * 1000, (int) cold[0]);
        finite("board triode", g0[1], g4[1], cold[1]);
        Assertions.assertTrue(within(g0[0]) && within(g4[0]) && within(cold[0]), "board triode: settled in " + g0[0] + " / " + g4[0] + " / " + cold[0] + " ticks");
        Assertions.assertTrue(g0[2] > 1e-4, "board triode: no anode current at Vg = 0 (" + g0[2] + ")");
        Assertions.assertTrue(g4[2] < g0[2], "board triode: negative grid does not reduce the anode current");
        Assertions.assertTrue(g4[2] >= -1e-6, "board triode: reverse anode current " + g4[2]);
        Assertions.assertTrue(Math.abs(cold[2]) < 1e-5, "board triode: cold tube conducts " + cold[2]);
    }

    // PentodeComponent defaults: mu 8, kg1 6000, kg2 4500, kp 48, kvb 12, ex 1.35, saturation current 0.1 A (hot).
    // Screen at 150 V, anode from 250 V through 10 k, grid 0 V and -4 V.
    @Test
    void pentodeGridControl() {
        double[] ia = new double[2];
        int[] ticks = new int[2];
        float[] grids = { 0, -4 };
        for(int i = 0; i < 2; ++i) {
            var net = game();
            var HV = net.V(250);
            var GND = net.V(0);
            var G = net.V(grids[i]);
            var SG = net.V(150);
            var A = net.N();
            net.W(10_000, HV, A);
            var tube = new PentodeWire(8, 6_000, 4_500, 48, 12, 1.35f, 0.1f, tie(net, GND), A, tie(net, G), tie(net, SG));
            net.network.addWire(tube);
            ticks[i] = settle(net, A);
            ia[i] = (250 - A.getVoltage()) / 10_000;
            finite("board pentode", A.getVoltage());
        }
        report("board pentode hot, screen 150 V: Ia = %.4f mA at Vg 0 V (%d ticks), %.4f mA at Vg -4 V (%d ticks)",
                ia[0] * 1000, ticks[0], ia[1] * 1000, ticks[1]);
        Assertions.assertTrue(within(ticks[0]) && within(ticks[1]), "board pentode: settled in " + ticks[0] + " / " + ticks[1] + " ticks");
        Assertions.assertTrue(ia[0] > 1e-4, "board pentode: no anode current at Vg = 0");
        Assertions.assertTrue(ia[1] < ia[0], "board pentode: negative grid does not reduce the anode current");
    }

    // ThyratronComponent defaults: control ratio 20, breakdown 60 V, holding clamp(0.2 * 60, 8, 16) = 12 V, holding
    // current 0.5 / 60 A, discharge conductance 50, grid leak 1 Mohm to the cathode, emission 1 when hot. Anode from 200 V
    // through 2 k: off with the grid at -30 V, fires and holds near 12 V with the grid at 0 V.
    @Test
    void thyratronFiring() {
        double[] va = new double[2];
        int[] ticks = new int[2];
        float[] grids = { -30, 0 };
        for(int i = 0; i < 2; ++i) {
            var net = game();
            var HV = net.V(200);
            var GND = net.V(0);
            var G = net.V(grids[i]);
            var A = net.N();
            var K = tie(net, GND);
            var Gn = tie(net, G);
            net.W(2_000, HV, A);
            var tube = new ThyratronWire(20, 60, 12, 0.5f / 60, 50, K, A, Gn);
            tube.setEmission(1);
            net.network.addWire(tube);
            net.W(1_000_000, Gn, K);
            ticks[i] = settle(net, A);
            va[i] = A.getVoltage();
            finite("board thyratron", va[i]);
        }
        report("board thyratron hot, 200 V through 2 k: Va = %.3f V with the grid at -30 V (%d ticks), %.3f V at 0 V (%d ticks)",
                va[0], ticks[0], va[1], ticks[1]);
        Assertions.assertTrue(within(ticks[0]) && within(ticks[1]), "board thyratron: settled in " + ticks[0] + " / " + ticks[1] + " ticks");
        Assertions.assertTrue(va[0] > 190, "board thyratron: fired with the grid at -30 V (Va " + va[0] + ")");
        Assertions.assertTrue(va[1] < 100, "board thyratron: did not fire with the grid at 0 V (Va " + va[1] + ")");
    }

    // RegulatorTubeComponent defaults: holding 60 V, breakdown 1.4 * 60 = 84 V, holding current 0.2 / 60 A, discharge
    // conductance 0.025. From 150 V and from 200 V through 5 k it strikes and holds near 60 V.
    @Test
    void regulatorTubeHolds() {
        double[] vt = new double[2];
        int[] ticks = new int[2];
        float[] supplies = { 150, 200 };
        for(int i = 0; i < 2; ++i) {
            var net = game();
            var HV = net.V(supplies[i]);
            var GND = net.V(0);
            var A = net.N();
            net.W(5_000, HV, A);
            net.network.addWire(new ColdCathodeRegulatorTubeWire(84, 60, 0.2f / 60, 0.025f, A, tie(net, GND)));
            ticks[i] = settle(net, A);
            vt[i] = A.getVoltage();
            finite("board regulator tube", vt[i]);
        }
        report("board regulator tube through 5 k: %.3f V from 150 V (%d ticks), %.3f V from 200 V (%d ticks)",
                vt[0], ticks[0], vt[1], ticks[1]);
        Assertions.assertTrue(within(ticks[0]) && within(ticks[1]), "board regulator tube: settled in " + ticks[0] + " / " + ticks[1] + " ticks");
        for(double v : vt)
            Assertions.assertTrue(v > 50 && v < 80, "board regulator tube: held at " + v + " V, not near 60 V");
        Assertions.assertTrue(Math.abs(vt[1] - vt[0]) < 5, "board regulator tube: does not regulate (" + vt[0] + " / " + vt[1] + ")");
    }
}
