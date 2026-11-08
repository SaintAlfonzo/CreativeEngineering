#!/usr/bin/env bash
# Scratch run for "SQ-CGE part 2" (scratch_queue.md; creative_engineering.md 7.4): low gravity for every falling entity
# type, dimension limit, End oxygen, startup script phase, physics tags. Batches in batches/r1108cge2/, results in
# perf/runs/r1108cge2/<boot>. Targeted run: no perf session (JFR per boot as prep_srv sets it).
#
#   cge2_run.sh server OUT        four boots, one after the other, stops on a boot crash:
#                                 b1 full pack, group A superflat, level r1108cge2 (configs as generated)
#                                 b2 same world, Create Gravity biome_configs End -1 + Nether listed
#                                 b3 same world, Common gravityDimensions + the Nether
#                                 b4 cc_only_srv.sh full (Common + its optional mods, no Compat), level cge2c
#   cge2_run.sh join OUT          b1 srv again with both configs back to their defaults, left UP for the client join
#                                 (CQ-CGE-03: Multiplayer > Direct Connection > localhost:25599)
#   cge2_run.sh op OUT            op the last player that joined the b1 srv (its name from the server log)
#   cge2_run.sh stop OUT          stop the b1 srv and wait until no game JVM is up
#   cge2_run.sh client OUT GAME   throwaway client in GAME (eval_client.py setup), Common + Compat from build/libs copied
#                                 into GAME/mods, the b1 world copied in as CGE2 (cheats on). Then, by hand:
#                                 eval_client.py launch GAME, open CGE2, qrun.py client GAME batches/r1108cge2/c1_sp.qb RES
#
# Jars: Common and Compat are COPIED from build/libs into every srv (a parallel build cannot rewrite them mid-run) and
# must be the 2026-11-08 builds this part targets (Compat with "// phase: helpers" on jei_order.js, Common with the
# LowGravity mixins); the run stops when they are missing. One game JVM at a time: every boot checks game_jvms.sh.
set -euo pipefail
INST="$(cd "$(dirname "$0")/../.." && pwd)"
Q="$INST/tools/scratch_query"
B="$Q/batches/r1108cge2"
RUN="$INST/perf/runs/r1108cge2"
COMMON="$INST/development/creativeCommon/build/libs/creativecommon-1.1.0.jar"
COMPAT="$INST/development/creativeCompat/build/libs/creativecompat-1.0.0.jar"
MODE=${1:?server|join|op|stop|client}; OUT=${2:?OUT}
A="$OUT/a"; C="$OUT/c"; D="$OUT/d"; LEVEL=r1108cge2

die() { echo "cge2_run: $*" >&2; exit 1; }
no_jvm() { [ -z "$(bash "$Q/game_jvms.sh")" ] || die "a game JVM is up: $(bash "$Q/game_jvms.sh")"; }

check_jars() {
	# Captured first: under pipefail, grep -q closing the pipe early kills unzip with SIGPIPE and reads as "not found".
	local listing header
	listing=$(unzip -l "$COMMON")
	header=$(unzip -p "$COMPAT" kubejs_startup/creativecompat/func/jei_order.js)
	grep -q "gravity/LowGravity.class" <<<"$listing" || die "Common in build/libs has no LowGravity (build :creativeCommon first)"
	grep -q "^// phase: helpers" <<<"$header" \
		|| die "Compat in build/libs has no 'phase: helpers' header (build :creativeCompat first)"
}

put_jars() { # SRV [compat]
	local srv=$1
	rm -f "$srv"/mods/creativecommon-*.jar; cp "$COMMON" "$srv/mods/"
	if [ "${2:-}" = compat ]; then rm -f "$srv"/mods/creativecompat-*.jar; cp "$COMPAT" "$srv/mods/"; fi
	cp "$Q/cge_probe.js" "$srv/kubejs/server_scripts/zz_cge_probe.js"
}

boot() { # OUT
	no_jvm
	(bash "$INST/tools/creative_labels_test/scratch_server.sh" boot "$1" >/dev/null 2>&1 &)
	sleep 2
	local r; r=$(bash "$Q/wait_boot.sh" "$1" 900) || { echo "$r"; bash "$Q/stop_wait.sh" "$1" 120 || true; die "boot failed: $r"; }
	echo "$(date +%H:%M) $r ($1)"
}

stop() { # the JVM can outlive stop_wait.sh by a few seconds after the save
	bash "$Q/stop_wait.sh" "$1" 180
	local i; for i in $(seq 30); do [ -z "$(bash "$Q/game_jvms.sh")" ] && break; sleep 1; done
	no_jvm
}

run_b5() { # ItemPhysic without Create Gravity (SQ-CGE-16)
	bash "$Q/cc_only_srv.sh" "$D" cge2d without=gravity difficulty=normal | tail -2
	cp "$INST"/mods/ItemPhysic_FORGE_*.jar "$INST"/mods/CreativeCore_FORGE_*.jar "$D/srv/mods/"
	put_jars "$D/srv"
	boot "$D"; batch "$D" b5_itemphysic_no_gravity.qb b5; stop "$D"
}

batch() { # OUT BATCH RESNAME
	python3 "$Q/qrun.py" run "$1" "$B/$2" "$RUN/$3" | tail -20
}

gravity_cfg() { # value for biome_configs
	local f="$A/srv/$LEVEL/serverconfig/create_gravity-server.toml"
	[ -f "$f" ] || die "no $f (boot b1 first)"
	sed -i "s|^biome_configs = .*|biome_configs = $1|" "$f"; grep -n '^biome_configs' "$f"
}

common_dims() { # value for gravityDimensions
	local f="$A/srv/config/creativecommon-common.toml"
	[ -f "$f" ] || die "no $f"
	sed -i -E "s|^(\s*)gravityDimensions = .*|\1gravityDimensions = $1|" "$f"; grep -n 'gravityDimensions =' "$f"
}

case "$MODE" in
	server)
		check_jars; no_jvm; mkdir -p "$RUN"
		bash "$Q/prep_srv.sh" "$A" "$LEVEL" A | tail -3
		rm -f "$A"/srv/mods/loadingscreentips-*.jar   # unlisted in mod_sides.md (as r1107cc / r1108cge)
		put_jars "$A/srv" compat
		boot "$A"; batch "$A" b1_full.qb b1; stop "$A"
		gravity_cfg '["#minecraft:is_end;;-1", "#minecraft:is_nether;;"]'
		boot "$A"; batch "$A" b2_factor_nether.qb b2; stop "$A"
		common_dims '["minecraft:the_end", "minecraft:the_nether"]'
		boot "$A"; batch "$A" b3_nether_allowed.qb b3; stop "$A"
		bash "$Q/cc_only_srv.sh" "$C" cge2c full difficulty=normal | tail -2
		put_jars "$C/srv"
		boot "$C"; batch "$C" b4_common_only.qb b4; stop "$C"
		run_b5
		echo "server boots done: $RUN/b1..b5/results.md" ;;
	b5)
		check_jars; no_jvm; mkdir -p "$RUN"; run_b5 ;;
	join)
		check_jars
		gravity_cfg '["#minecraft:is_end;;"]'
		common_dims '["minecraft:the_end"]'
		mkdir -p "$A/srv/$LEVEL/datapacks"; cp -r "$Q/datapack/cge2_drop" "$A/srv/$LEVEL/datapacks/"
		boot "$A"
		echo "b1 srv up on localhost:25599 (offline mode). Join, then: cge2_run.sh op $OUT" ;;
	op)
		name=$(grep -oE ': [A-Za-z0-9_]{3,16} joined the game' "$A/srv/logs/latest.log" | tail -1 | awk '{print $2}')
		[ -n "$name" ] || die "nobody joined yet"
		bash "$INST/tools/creative_labels_test/scratch_server.sh" cmd "$A" "op $name"; echo "opped $name" ;;
	stop)
		stop "$A" ;;
	client)
		GAME=${3:?GAME}; check_jars; no_jvm
		[ -d "$A/srv/$LEVEL" ] || die "no b1 world (run server first)"
		gravity_cfg '["#minecraft:is_end;;"]'   # b2 and b3 left End -1 in the world's serverconfig
		mkdir -p "$A/srv/$LEVEL/datapacks"; cp -r "$Q/datapack/cge2_drop" "$A/srv/$LEVEL/datapacks/"   # copied with the world
		python3 "$INST/tools/perf/eval/eval_client.py" setup "$GAME" | tail -3
		rm -f "$GAME"/mods/creativecommon-*.jar "$GAME"/mods/creativecompat-*.jar
		cp "$COMMON" "$COMPAT" "$GAME/mods/"
		python3 "$Q/sp_world.py" "$A/srv/$LEVEL" "$GAME" CGE2
		echo "client ready: eval_client.py launch $GAME, open CGE2, then qrun.py client $GAME $B/c1_sp.qb $RUN/c1" ;;
	*) die "mode: server | join | op | stop | client" ;;
esac
