// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3.
// Phase P1 gate (feature_writeup/plan_power_grid.md, power_grid.md section 7): five known circuits, each run on a plain
// ElectricalNetwork (as upstream's tests) and on the network the game builds (WorldNetworks: GraphedElectricalNetwork,
// G_MIN anchoring on, backend from CSolver.SolverBackend.create), plus the 1000-node ladder timing (input for SQ-PWG-04).
// Run by tools/test_power_grid_solver.sh.
package org.patryk3211.electricity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.patryk3211.powergrid.config.CSolver;
import org.patryk3211.powergrid.electricity.sim.ElectricWire;
import org.patryk3211.powergrid.electricity.sim.ElectricalNetwork;
import org.patryk3211.powergrid.electricity.sim.GraphedElectricalNetwork;
import org.patryk3211.powergrid.electricity.sim.NetworkGraph;
import org.patryk3211.powergrid.electricity.sim.node.FloatingNode;
import org.patryk3211.powergrid.electricity.sim.node.VoltageSourceCoupling;
import org.patryk3211.powergrid.electricity.sim.special.CapacitorWire;
import org.patryk3211.powergrid.electricity.sim.special.PNJunctionWire;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PortGateTests extends TestHelper {
    /** Lines the runner prints after the pass/fail list (results and timings). */
    public static final List<String> REPORT = new ArrayList<>();

    private static void report(String format, Object... args) {
        REPORT.add(String.format(format, args));
    }

    /** Upstream's test network (ElectricalNetwork, no G_MIN). */
    private static Network plain() {
        return new Network();
    }

    /** The network the game builds (WorldNetworks.createNetwork: graphed, G_MIN on, backend factory from the config enum). */
    private static Network game() {
        var net = new Network();
        net.network = new GraphedElectricalNetwork(new NetworkGraph(), true, CSolver.SolverBackend.JAVA::create);
        // Upstream's tests disable the post-change warm-up freeze (warmUp(-1)) so transients start at once; same here.
        net.network.warmUp(-1);
        return net;
    }

    private static void both(String name, java.util.function.BiConsumer<String, Supplier<Network>> body) {
        // Both variants always run; failures are combined.
        var failures = new ArrayList<String>();
        String[] labels = { " [plain]", " [game]" };
        List<Supplier<Network>> makers = List.of(PortGateTests::plain, PortGateTests::game);
        for(int i = 0; i < 2; ++i) {
            try {
                body.accept(name + labels[i], makers.get(i));
            } catch(AssertionError | RuntimeException e) {
                failures.add(String.valueOf(e.getMessage()));
            }
        }
        if(!failures.isEmpty())
            throw new AssertionError(String.join(" | ", failures));
    }

    private static <T> T quiet(Supplier<T> body) {
        var out = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        try {
            return body.get();
        } finally {
            System.setOut(out);
        }
    }

    // 1. Voltage divider: 10 V, 1 k / 1 k -> 5 V, 5 mA.
    @Test
    void gate1VoltageDivider() {
        both("divider", (name, make) -> {
            var Net = make.get();
            var V = Net.V(10);
            var N = Net.N();
            Net.W(1000, V, N);
            Net.W(1000, N, null);
            Net.calculate();
            report("%s: V(mid) = %.9f V (5), I = %.9f mA (5)", name, N.getVoltage(), V.getCurrent() * 1000);
            Assertions.assertEquals(5.0, N.getVoltage(), 1e-6, name + ": divider voltage");
            Assertions.assertEquals(0.005, V.getCurrent(), 1e-9, name + ": divider current");
            Assertions.assertTrue(Net.network.isConverged(), name + ": not converged");
        });
    }

    // 2. RC charge: 1 V through 1 ohm into 1 F (RC = 1 s = 20 ticks): 63.2 % after one time constant, within 2 %.
    @Test
    void gate2RcCharge() {
        both("RC", (name, make) -> {
            var Net = make.get();
            var V = Net.V(1);
            var N = Net.N();
            Net.W(1, V, N);
            var C = new CapacitorWire(1, N, null);
            Net.network.addWire(C);
            double dt = Net.network.getDeltaTime();
            int ticks = (int) Math.round(1.0 / dt);
            for(int i = 0; i < ticks; ++i)
                Net.calculate();
            double vc = C.potentialDifference();
            double expected = 1 - Math.exp(-1);
            report("%s: dt = %.3f s, after %d ticks V(C) = %.5f V (%.5f), error %.2f %%", name, dt, ticks, vc, expected,
                    100 * Math.abs(vc - expected) / expected);
            Assertions.assertEquals(expected, vc, 0.02 * expected, name + ": capacitor voltage after one RC");
        });
    }

    // 3. Ideal transformer 1:10 under load: 10 V primary, 100 ohm on the secondary -> 100 V, 1 A secondary, 10 A primary.
    @Test
    void gate3Transformer() {
        both("transformer 1:10", (name, make) -> {
            var Net = make.get();
            var V = Net.V(10);
            var S = Net.N();
            Net.TR(10, V, S);
            var load = Net.W(100, S, null);
            Net.calculate();
            report("%s: V(sec) = %.6f V (100), I(load) = %.6f A (1), I(primary) = %.6f A (10)", name, S.getVoltage(),
                    load.current(), V.getCurrent());
            Assertions.assertEquals(100, S.getVoltage(), 1e-4, name + ": secondary voltage");
            Assertions.assertEquals(1, load.current(), 1e-6, name + ": load current");
            Assertions.assertEquals(10, V.getCurrent(), 1e-5, name + ": primary current");
        });
    }

    // 4. Diode forward drop: 5 V, 1 k, diode with the circuit-board diode's parameters (DiodeComponent) to ground.
    @Test
    void gate4DiodeDrop() {
        both("diode", (name, make) -> {
            var Net = make.get();
            var V = Net.V(5);
            var GND = Net.V(0);
            var A = Net.N();
            var K = Net.N();
            Net.W(1000, V, A);
            Net.W(0.001f, K, GND);
            // Cathode on a node (0 V source), as every diode in the game sits between two nodes: PNJunctionWire's
            // residual does not handle a null (ground) node.
            var D = new PNJunctionWire(5.47e-9, 0.075f, 22, 1.783, A, K);
            Net.network.addWire(D);
            int ticks = 0;
            for(; ticks < 40; ++ticks) {
                quiet(() -> { Net.calculate(); return null; });
                if(ticks >= 2 && Net.network.isConverged())
                    break;
            }
            double drop = A.getVoltage() - K.getVoltage();
            report("%s: forward drop %.4f V at %.3f mA, converged %s after %d ticks", name, drop, D.current() * 1000,
                    Net.network.isConverged(), ticks + 1);
            Assertions.assertTrue(Net.network.isConverged(), name + ": not converged");
            Assertions.assertTrue(drop > 0.55 && drop < 0.8, name + ": forward drop " + drop + " not near 0.7 V");
            Assertions.assertEquals((5 - drop) / 1000, D.current(), 1e-6, name + ": diode current vs resistor current");
        });
    }

    // 5. Floating grid: a two-terminal 5 V source with no ground reference driving 1 k + 1 k, plus an island of
    // resistors with no source at all. The game network anchors it (power_grid.md 3.x item 6); values must stay finite.
    @Test
    void gate5FloatingGrid() {
        var Net = game();
        var N1 = Net.N();
        var N2 = Net.N();
        var N3 = Net.N();
        var src = new VoltageSourceCoupling(N1, N2, 0, 5);
        Net.network.addNode(src);
        Net.W(1000, N1, N3);
        Net.W(1000, N3, N2);
        var I1 = Net.N();
        var I2 = Net.N();
        Net.W(10, I1, I2);
        for(int i = 0; i < 5; ++i)
            quiet(() -> { Net.calculate(); return null; });
        double[] v = { N1.getVoltage(), N2.getVoltage(), N3.getVoltage(), I1.getVoltage(), I2.getVoltage(), src.getCurrent() };
        report("floating [game]: V = %.6f / %.6f / %.6f, island %.3g / %.3g, I(src) = %.6f mA, converged %s", v[0], v[1], v[2],
                v[3], v[4], v[5] * 1000, Net.network.isConverged());
        for(double x : v)
            Assertions.assertTrue(Double.isFinite(x), "floating [game]: non-finite value " + x);
        Assertions.assertEquals(5, N1.getVoltage() - N2.getVoltage(), 1e-6, "floating [game]: source voltage");
        Assertions.assertEquals(2.5, N1.getVoltage() - N3.getVoltage(), 1e-6, "floating [game]: divider");
        Assertions.assertEquals(-0.0025, src.getCurrent(), 1e-8, "floating [game]: source current");
        Assertions.assertTrue(Net.network.isConverged(), "floating [game]: not converged");

        // Upstream's plain test network has no anchoring; record (not assert) what it does with the same circuit.
        var P = plain();
        var P1 = P.N();
        var P2 = P.N();
        var P3 = P.N();
        var psrc = new VoltageSourceCoupling(P1, P2, 0, 5);
        P.network.addNode(psrc);
        P.W(1000, P1, P3);
        P.W(1000, P3, P2);
        String result;
        try {
            quiet(() -> { P.calculate(); return null; });
            result = String.format("V(N1-N2) = %.6f, I = %.6f mA, finite %s", P1.getVoltage() - P2.getVoltage(),
                    psrc.getCurrent() * 1000, Double.isFinite(P1.getVoltage()) && Double.isFinite(psrc.getCurrent()));
        } catch(RuntimeException e) {
            result = "threw " + e;
        }
        report("floating [plain, no G_MIN, info only]: %s", result);
    }

    // 8. Structure and value changes after the first solve, on wires to ground and between nodes: each must reach the
    // same result as a network built fresh with the final values (upstream StructureChangeTests, on both networks).
    @Test
    void gate8ChangesAfterSolve() {
        both("changes", (name, make) -> {
            var Net = make.get();
            var V = Net.V(10);
            var N = Net.N();
            var M = Net.N();
            Net.W(1000, V, N);
            var shunt = Net.W(1000, N, null);
            Net.W(500, N, M);
            Net.W(500, M, null);
            quiet(() -> { Net.calculate(); return null; });
            // fresh: N = 10 * (1000 || 1000) / (1000 + 500) = 3.3333
            Assertions.assertEquals(10.0 / 3, N.getVoltage(), 1e-6, name + ": initial");

            var extra = Net.W(1000, N, null);
            quiet(() -> { Net.calculate(); return null; });
            report("%s: ground wire added: V(N) = %.6f (2.5)", name, N.getVoltage());
            Assertions.assertEquals(2.5, N.getVoltage(), 1e-6, name + ": after adding a wire to ground");

            extra.remove();
            quiet(() -> { Net.calculate(); return null; });
            report("%s: ground wire removed: V(N) = %.6f (3.333333)", name, N.getVoltage());
            Assertions.assertEquals(10.0 / 3, N.getVoltage(), 1e-6, name + ": after removing a wire to ground");

            shunt.setResistance(250);
            quiet(() -> { Net.calculate(); return null; });
            // 250 || 1000 = 200; N = 10 * 200 / 1200
            report("%s: ground wire resistance 1000 -> 250: V(N) = %.6f (1.666667)", name, N.getVoltage());
            Assertions.assertEquals(10.0 * 200 / 1200, N.getVoltage(), 1e-6, name + ": after changing a ground wire's resistance");

            var bridge = Net.W(1000, V, M);
            quiet(() -> { Net.calculate(); return null; });
            Assertions.assertTrue(Net.network.isConverged(), name + ": not converged after a node-to-node wire");
            report("%s: node wire added: V(N) = %.6f (2.258065), V(M) = %.6f (2.903226)", name, N.getVoltage(), M.getVoltage());
            // nodal: 0.007 N - 0.002 M = 0.01, -0.002 N + 0.005 M = 0.01
            Assertions.assertEquals(0.014 / 0.0062, N.getVoltage(), 1e-6, name + ": after adding a node-to-node wire (N)");
            Assertions.assertEquals(2 + 0.4 * 0.014 / 0.0062, M.getVoltage(), 1e-6, name + ": after adding a node-to-node wire (M)");
            bridge.remove();
            quiet(() -> { Net.calculate(); return null; });
            Assertions.assertEquals(10.0 * 200 / 1200, N.getVoltage(), 1e-6, name + ": after removing a node-to-node wire");
        });
    }

    // ---- 1000-node ladder (SQ-PWG-04 input) ----------------------------------------------------------------------

    private static final int LADDER = 1000;
    private static final double R_SOURCE = 1, R_SERIES = 0.1, R_SHUNT = 1000;

    /** Node voltages of the ladder by direct tridiagonal elimination (Thomas), independent of the solver. */
    private static double[] ladderReference(double sourceVoltage, double firstSeries) {
        int n = LADDER;
        double[] a = new double[n], b = new double[n], c = new double[n], d = new double[n];
        for(int i = 0; i < n; ++i) {
            double gLeft = i == 0 ? 1 / R_SOURCE : 1 / (i == 1 ? firstSeries : R_SERIES);
            double gRight = i == n - 1 ? 0 : 1 / (i == 0 ? firstSeries : R_SERIES);
            b[i] = gLeft + gRight + 1 / R_SHUNT;
            a[i] = i == 0 ? 0 : -gLeft;
            c[i] = -gRight;
            d[i] = i == 0 ? sourceVoltage / R_SOURCE : 0;
        }
        for(int i = 1; i < n; ++i) {
            double m = a[i] / b[i - 1];
            b[i] -= m * c[i - 1];
            d[i] -= m * d[i - 1];
        }
        double[] x = new double[n];
        x[n - 1] = d[n - 1] / b[n - 1];
        for(int i = n - 2; i >= 0; --i)
            x[i] = (d[i] - c[i] * x[i + 1]) / b[i];
        return x;
    }

    private record Ladder(Network net, VoltageSourceNodePair source, FloatingNode[] nodes, ElectricWire firstSeries) { }

    private static Ladder buildLadder(Network Net) {
        var V = Net.V(10);
        var nodes = new FloatingNode[LADDER];
        for(int i = 0; i < LADDER; ++i)
            nodes[i] = Net.N();
        Net.W((float) R_SOURCE, V, nodes[0]);
        ElectricWire first = null;
        for(int i = 0; i < LADDER; ++i) {
            Net.W((float) R_SHUNT, nodes[i], null);
            if(i + 1 < LADDER) {
                var w = Net.W((float) R_SERIES, nodes[i], nodes[i + 1]);
                if(first == null)
                    first = w;
            }
        }
        return new Ladder(Net, V, nodes, first);
    }

    private static double maxError(Ladder l, double sourceVoltage, double firstSeries) {
        var ref = ladderReference(sourceVoltage, firstSeries);
        double err = 0;
        for(int i = 0; i < LADDER; ++i)
            err = Math.max(err, Math.abs(l.nodes[i].getVoltage() - ref[i]));
        return err;
    }

    private interface Step { void apply(int i); }

    /** Runs {@code count} solves, each after {@code step}; returns {mean, max, min} microseconds. */
    private static double[] time(Ladder l, int count, Step step) {
        double total = 0, max = 0, min = Double.MAX_VALUE;
        for(int i = 0; i < count; ++i) {
            step.apply(i);
            long t0 = System.nanoTime();
            l.net.calculate();
            double us = (System.nanoTime() - t0) / 1000.0;
            total += us;
            max = Math.max(max, us);
            min = Math.min(min, us);
        }
        return new double[] { total / count, max, min };
    }

    private static void ladderRun(String name, Supplier<Network> make, boolean record) {
        long tb = System.nanoTime();
        var l = buildLadder(make.get());
        double buildUs = (System.nanoTime() - tb) / 1000.0;
        long t0 = System.nanoTime();
        l.net.calculate();
        double firstUs = (System.nanoTime() - t0) / 1000.0;
        double err0 = maxError(l, 10, R_SERIES);
        Assertions.assertTrue(err0 < 1e-6, name + ": first solve max node error " + err0);

        var steady = time(l, 200, i -> { });
        var source = time(l, 200, i -> l.source.setVoltage(i % 2 == 0 ? 12 : 10));
        // Same pattern as upstream's PerformanceTest: alternate one wire's resistance so the matrix is refactorized.
        var refactor = time(l, 200, i -> l.firstSeries.setResistance((float) (i % 2 == 0 ? R_SERIES * 2 : R_SERIES)));
        // Last step set R_SERIES (i = 199 is odd); source last set to 10 V (i = 199 odd).
        double err1 = maxError(l, 10, R_SERIES);
        Assertions.assertTrue(err1 < 1e-6, name + ": max node error after changes " + err1);
        Assertions.assertTrue(l.net.network.isConverged(), name + ": not converged");
        if(record) {
            report("%s: build %.0f us, first solve %.0f us, max node error %.2g V (vs tridiagonal reference)", name, buildUs,
                    firstUs, Math.max(err0, err1));
            report("%s: unchanged    mean %.1f us, min %.1f, max %.1f (200 solves)", name, steady[0], steady[2], steady[1]);
            report("%s: source step  mean %.1f us, min %.1f, max %.1f (200 solves)", name, source[0], source[2], source[1]);
            report("%s: refactorize  mean %.1f us, min %.1f, max %.1f (200 solves, one wire's resistance alternated)", name,
                    refactor[0], refactor[2], refactor[1]);
        }
    }

    @Test
    void gate6Ladder1000() {
        quiet(() -> {
            // JIT warm-up runs, not recorded.
            for(int i = 0; i < 3; ++i) {
                ladderRun("warm-up", PortGateTests::plain, false);
                ladderRun("warm-up", PortGateTests::game, false);
            }
            ladderRun("ladder 1000 [plain]", PortGateTests::plain, true);
            ladderRun("ladder 1000 [game]", PortGateTests::game, true);
            return null;
        });
    }

    // Upstream's PerformanceTest grid (32 x 32 = 1024 nodes, resistor mesh, sources at two corners), Java backend only
    // (the upstream file also runs NativeMNA, which is not ported, so it is not compiled).
    @Test
    void gate7Grid32() {
        quiet(() -> {
            for(int round = 0; round < 2; ++round) {
                var Net = new Network();
                Net.network = new GraphedElectricalNetwork(new NetworkGraph(), false, CSolver.SolverBackend.JAVA::create);
                var wires = new ArrayList<ElectricWire>();
                final int SIZE = 32;
                var nodes = buildGrid(Net, SIZE, 1.0f, wires);
                var V1 = Net.V(5);
                var GND = Net.V(0);
                Net.W(0.1f, V1, nodes[0]);
                Net.W(0.1f, GND, nodes[SIZE * SIZE - 1]);
                double total = 0, max = 0;
                final int count = 100;
                for(int i = 0; i < count; ++i) {
                    V1.setVoltage(-V1.getVoltage());
                    wires.get(0).setResistance(wires.get(0).getResistance() * (i % 2 == 0 ? 2.0f : 0.5f));
                    long t0 = System.nanoTime();
                    Net.calculate();
                    double us = (System.nanoTime() - t0) / 1000.0;
                    total += us;
                    max = Math.max(max, us);
                }
                Assertions.assertTrue(Net.network.isConverged(), "grid 32x32: not converged");
                if(round == 1)
                    report("grid 32x32 (upstream PerformanceTest, Java): mean %.1f us, max %.1f us per solve (100 solves, source flip + refactorize each)",
                            total / count, max);
            }
            return null;
        });
    }
}
