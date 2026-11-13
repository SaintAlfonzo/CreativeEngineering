#!/usr/bin/env bash
# Offline solver checks for the Create: Power Grid port (development/backportedPowerGrid, plan_power_grid.md P1 gate):
#   - upstream's solver tests (development/backportedPowerGrid/upstream/test, run through a JUnit stand-in in
#     tools/pg_solver_test/org/junit; PerformanceTest (needs NativeMNA) left out)
#   - upstream's display expression parser tests (other/EquationParserTests: the custom display and plotter / gauge
#     readouts' Expression; added with phase P8)
#   - PortGateTests: divider, RC charge, transformer 1:10, diode drop, floating grid, 1000-node ladder and 32x32 grid timings
# Runs against development/backportedPowerGrid/build/classes (run ./gradlew :backportedPowerGrid:build --offline first).
# No game needed. Usage: tools/test_power_grid_solver.sh [-v] (-v also prints the full runner output to stdout).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
L="$HOME/.local/share/atlauncher/libraries"
C="$HOME/.gradle/caches/modules-2/files-2.1"
PG="$ROOT/development/backportedPowerGrid"
cd "$ROOT"
source "$ROOT/tools/java_test_cp.sh"
for a in org.ejml/ejml-core org.ejml/ejml-ddense org.ejml/ejml-dsparse org.ejml/ejml-simple org.jetbrains/annotations; do
  CP="$CP:$(find "$C/$a" -name '*.jar' ! -name '*sources*' | sort | tail -1)"
done
# Create 0.5.1.i for foundation.utility.Pair and config.ConfigBase (neither touches Minecraft classes when used here).
CP="$PG/build/classes/java/main:$CP:$PG/libs/create-1.19.2-0.5.1.i.jar"
mkdir -p "$T/stub" "$T/test"
javac -proc:none -nowarn -cp "$CP" -d "$T/stub" tools/structure_groups_test/net/minecraftforge/fml/loading/FMLEnvironment.java
SRC=$(find "$PG/upstream/test/org/patryk3211/electricity" -name '*.java' ! -name PerformanceTest.java; find "$PG/upstream/test/org/patryk3211/other" -name '*.java'; find tools/pg_solver_test -name '*.java')
javac -proc:none -nowarn -encoding UTF-8 -cp "$T/stub:$CP" -d "$T/test" $SRC 2>&1 | grep -v '^Note:' || true
CLASSES=$(cd "$T/test" && find org/patryk3211/electricity org/patryk3211/other -name '*.class' ! -name '*$*' | sed 's/\.class$//; s#/#.#g' \
  | grep -v -e TestHelper -e PgTestRunner | sort)
set +e
java -Dpg.known=tools/pg_solver_test/known_failures.txt -cp "$T/stub:$T/test:$CP" org.patryk3211.electricity.PgTestRunner $CLASSES > "$T/out.txt" 2>&1
status=$?
set -e
if [ "${1:-}" = "-v" ]; then cat "$T/out.txt"; else
  grep -E '^(ok |FAIL |KNOWN |NOW PASSES |    |== |  |ALL |FAILED)' "$T/out.txt" | grep -v '^ok ' ; grep -c '^ok ' "$T/out.txt" | sed 's/$/ tests ok/'
fi
exit $status
