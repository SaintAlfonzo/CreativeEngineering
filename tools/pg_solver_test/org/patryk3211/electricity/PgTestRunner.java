// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3.
// Runs every @Test method of the given classes (upstream's solver tests plus PortGateTests) without JUnit, with the
// port's server and common configs built from their specs (default values), as GraphedElectricalNetwork reads them.
// Prints "ok <test>" / "FAIL <test>: <reason>" (the test's own stdout only on failure), then PortGateTests.REPORT.
package org.patryk3211.electricity;

import net.minecraftforge.fml.config.ModConfig;
import org.junit.jupiter.api.Test;
import org.patryk3211.powergrid.collections.ModdedConfigs;
import org.patryk3211.powergrid.config.CCommon;
import org.patryk3211.powergrid.config.CServer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Supplier;

public class PgTestRunner {
    private static void setupConfigs() throws Exception {
        // ModdedConfigs.register() also hands the specs to the mod loading context, which does not exist here; build the
        // two configs the solver reads through the same private helper and store them in the static fields.
        Method register = ModdedConfigs.class.getDeclaredMethod("register", Supplier.class, ModConfig.Type.class);
        register.setAccessible(true);
        Object server = register.invoke(null, (Supplier<CServer>) CServer::new, ModConfig.Type.SERVER);
        Object common = register.invoke(null, (Supplier<CCommon>) CCommon::new, ModConfig.Type.COMMON);
        var f = ModdedConfigs.class.getDeclaredField("server");
        f.setAccessible(true);
        f.set(null, server);
        f = ModdedConfigs.class.getDeclaredField("common");
        f.setAccessible(true);
        f.set(null, common);
    }

    /** known_failures.txt: "Class.method reason" per line; '#' comments. Such a failure is listed but does not fail the run. */
    private static Map<String, String> knownFailures() throws Exception {
        var map = new HashMap<String, String>();
        var file = System.getProperty("pg.known");
        if(file == null)
            return map;
        for(String line : Files.readAllLines(Path.of(file))) {
            line = line.strip();
            if(line.isEmpty() || line.startsWith("#"))
                continue;
            int sp = line.indexOf(' ');
            map.put(sp < 0 ? line : line.substring(0, sp), sp < 0 ? "" : line.substring(sp + 1).strip());
        }
        return map;
    }

    public static void main(String[] args) throws Exception {
        setupConfigs();
        var known = knownFailures();
        int passed = 0, failed = 0, knownFailed = 0;
        var realOut = System.out;
        for(String className : args) {
            Class<?> cls = Class.forName(className);
            var methods = Arrays.stream(cls.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Test.class))
                    .sorted(Comparator.comparing(Method::getName)).toList();
            for(Method m : methods) {
                String name = cls.getSimpleName() + "." + m.getName();
                var buffer = new ByteArrayOutputStream();
                System.setOut(new PrintStream(buffer, true));
                Throwable error = null;
                long t0 = System.nanoTime();
                try {
                    var ctor = cls.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    Object instance = ctor.newInstance();
                    m.setAccessible(true);
                    m.invoke(instance);
                } catch(InvocationTargetException e) {
                    error = e.getCause();
                } catch(Throwable e) {
                    error = e;
                } finally {
                    System.setOut(realOut);
                }
                long ms = (System.nanoTime() - t0) / 1_000_000;
                if(error == null) {
                    ++passed;
                    realOut.printf("ok %s (%d ms)%n", name, ms);
                    if(known.containsKey(name))
                        realOut.printf("NOW PASSES %s (listed as a known failure: remove it from known_failures.txt)%n", name);
                } else if(known.containsKey(name)) {
                    ++knownFailed;
                    realOut.printf("KNOWN %s: %s%n    reason: %s%n", name, error, known.get(name));
                } else {
                    ++failed;
                    realOut.printf("FAIL %s: %s%n", name, error);
                    var trace = error.getStackTrace();
                    for(int i = 0; i < Math.min(6, trace.length); ++i)
                        realOut.println("    at " + trace[i]);
                    var lines = buffer.toString().lines().toList();
                    if(!lines.isEmpty()) {
                        realOut.println("    -- test output (last 15 lines) --");
                        lines.subList(Math.max(0, lines.size() - 15), lines.size()).forEach(l -> realOut.println("    " + l));
                    }
                }
            }
        }
        realOut.println("== Gate results and timings");
        PortGateTests.REPORT.forEach(l -> realOut.println("  " + l));
        realOut.printf("%s: %d passed, %d failed, %d known upstream test failures%n", failed == 0 ? "ALL PASSED" : "FAILED",
                passed, failed, knownFailed);
        System.exit(failed == 0 ? 0 : 1);
    }
}
