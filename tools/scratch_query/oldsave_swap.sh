#!/usr/bin/env bash
# Old-save rows (r1106cbp, SQ-CBP-06, SQ-CDFL-03): after a NOSWAP=1 prep_srv.sh boot saved a world with the installed
# jars, replace the Creative jars in OUT/srv/mods with the newest build/libs copies (COPIES, never links into mods/).
#   oldsave_swap.sh OUT
# Refuses while any game JVM is up.
INST="$(cd "$(dirname "$0")/../.." && pwd)"
OUT=${1:?OUT}; cd "$OUT/srv/mods" || exit 1
[ -n "$(bash "$INST/tools/scratch_query/game_jvms.sh" 2>/dev/null)" ] && { echo "a game JVM is up" >&2; exit 1; }
for p in creativeCommon:creativecommon creativeCompat:creativecompat creativeCalendar:creativecalendar \
         creativeDelight:creativedelight creativeBrewing:creativebrewing creativeEquipment:creativeequipment \
         creativeEngineering:creativeengineering creativeMagic:creativemagic creativeWorldgen:creativeworldgen; do
  d=${p%%:*}; n=${p##*:}
  b=$(ls -t "$INST"/development/$d/build/libs/$n-*.jar 2>/dev/null | grep -vE -- '-(own|sources|overlay)\.jar$' | head -1)
  [ -n "$b" ] || { echo "no build for $n"; continue; }
  rm -f ./$n-*.jar; cp "$b" . && echo "swapped $n <- $(basename "$b") $(date -r "$b" +%m-%d_%H:%M)"
done
