#!/usr/bin/env python3
"""Writes the r1106cbp batch files (scratch_queue.md "SQ-CBP" and "SQ-CDFL", plan_cb_polish.md 1 to 4) next to this
script. Built on r1106cbo's generator (../r1106cbo/gen.py): its batches are reused with the echo-leak tolerance removed
(plan_cb_polish.md 1 moved the echo rows into Equipment, so any leak fails now) and the row corrections from the
r1106cbo run (transition items 89). Boot order: BOOTS at the bottom (`gen.py plan`)."""
import importlib.util, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location("cbo", os.path.join(HERE, "..", "r1106cbo", "gen.py"))
cbo = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(cbo)

TOL = re.compile(r"# the 4 unguarded echo tool rows.*\nexpect effects leaks \[04\]\$\nreject .*\n")
STRICT = "expect effects leaks 0$\nreject \\[cbo\\] leak \nreject \\[cbo\\] leak .*echo_\n"


def strict(text):
    t = TOL.sub(STRICT, text)
    t = t.replace("expect effects leaks [04]$", "expect effects leaks 0$")
    t = t.replace("r1106cbo", "r1106cbp (rerun of r1106cbo)", 1)
    return t


# [absent mods] now prints at the end of the recipe event (absent_mods.js) and the loot-table line comes from
# LootTablesOptionalModsMixin into latest.log
ABSENT_ALL = r'''shell grep -h "absent mods" logs/kubejs/server.log logs/latest.log | sed -E 's/^\[[^]]*\] \[[^]]*\]: //'
'''
LOOTERR = r'''shell n=$(grep -cE "Couldn't parse loot table|Can't find block" logs/latest.log); echo "loot errors=$n"; test "$n" = 0
shell grep -E "Couldn't parse loot table" logs/latest.log | grep -oE "loot table [a-z_:/.0-9]+" | sort | uniq | head -40
'''

B = {}

B["p1_chain"] = strict(cbo.B["c1_chain"]).replace(r"transition items \(80\)", r"transition items \(89\)") + f"""## SQ-CBP-01 | chain: absent-mods lines print, loot tables of absent mods parse
{ABSENT_ALL}bootexpect \\[absent mods\\] loot tables: .* left out by forge:conditions
kjsexpect \\[absent mods\\] (skipped [0-9]+ .* rows for absent|none skipped)
{LOOTERR}look the loot-table line names fruitsdelight, moredelight, supplementaries, upgrade_aquatic, endersdelight among others
## SQ-CBP-03 | chain: horror lasagna filling step with an absent fluid
shell ! grep -h "horror_lasagna" logs/kubejs/server.log logs/latest.log | grep -iE "error|fail"
shell grep -h "horror_lasagna" logs/kubejs/server.log logs/latest.log | head -5
> zq bytype create:filling
sleep 3
reject minecraft:empty
shell grep -h "absent mods.*\\(Item.of\\|Fluid.of\\)" logs/kubejs/server.log | head -3
## SQ-CBP-09 | chain without Equipment: no echo or dragon rows
> cbo effects
waitlog \\[cbo\\] done 30
expect effects leaks 0$
reject effects items by namespace.*creativeengineering
reject \\[cbo\\] leak
"""

B["p2_cooking"] = cbo.B["c3_cooking"].replace("r1106cbo", "r1106cbp (rerun of r1106cbo)", 1) + r"""## SQ-CBP-02 | cooking: no Empty array?, IE empty-output prune line, failures under 40
shell n=$(cat logs/kubejs/*.log logs/latest.log | grep -c "Empty array"); echo "empty_array=$n"; test "$n" = 0
shell grep -qE '\[NCH\] removed [0-9]+ Immersive Engineering recipes whose output tag is empty' logs/kubejs/startup.log  # startup.log (r1106spf: kjsexpect reads server.log)
shell grep -h "\[NCH\] removed" logs/kubejs/*.log | cut -c1-600
shell n=$(cat logs/kubejs/startup.log logs/kubejs/server.log | grep -c "failed and was skipped"); echo "failures=$n"; test "$n" -lt 40
"""

B["p3_optional"] = strict(cbo.B["c5_optional"])
B["p3b_optional_members_off"] = strict(cbo.B["c5b_optional_members_off"])

B["p4_iw"] = strict(cbo.B["s34"]) + r"""## SQ-CBP-04 | chain + Immersive Weathering: compost skips absent compat folders
shell ! grep -h "\[compost\] values for items that do not exist" logs/kubejs/startup.log logs/kubejs/server.log | grep "immersive_weathering:"
shell grep -h "\[compost\]" logs/kubejs/startup.log logs/kubejs/server.log | cut -c1-400 | head -6
kjsexpect \[compost\] skipped [0-9]+ values for items of absent mods
"""
# kjsexpect reads server.log; the compost line is a startup print: check both files instead
B["p4_iw"] = B["p4_iw"].replace(r"kjsexpect \[compost\] skipped [0-9]+ values for items of absent mods",
                                r'shell grep -h "\[compost\] skipped [0-9]* values for items of absent mods" logs/kubejs/startup.log logs/kubejs/server.log')

B["p5_cd_alone"] = r"""# r1106cbp: Creative Delight alone (cooking set without Creative Brewing), group A (SQ-CDFL-01, -02)
## SQ-CDFL-01 | CD alone: creativedelight: fluids, startup folder, swaps, effect swaps
bootreject Missing or unsupported mandatory|Mixin apply failed|InvalidMixinException|Encountered an unexpected exception
shell grep -h "jar-startup\]\|\[swaps\]" logs/latest.log logs/kubejs/startup.log | sed -E 's/^\[[^]]*\] \[[^]]*\]: //' | sort | uniq -c
shell grep -h "jar-startup\] creativedelight" logs/latest.log logs/kubejs/startup.log | head -3
> forge tags minecraft:fluid get forge:fluid/chorus_juice
> forge tags minecraft:fluid get forge:fluid/egg_solution
> forge tags minecraft:fluid get forge:fluid/frosting
> forge tags minecraft:fluid get forge:fluid/turnip_water
sleep 2
expect creativedelight:chorus_juice
expect creativedelight:egg_solution
expect creativedelight:frosting
expect creativedelight:turnip_water
reject creativeengineering:(chorus_juice|egg_solution|frosting|turnip_water)
shell grep -h "Unknown fluid\|Invalid fluid" logs/kubejs/*.log | head; ! grep -qh "Unknown fluid\|Invalid fluid" logs/kubejs/*.log
shell ! grep -h "effect_swaps" logs/kubejs/*.log | grep -iE "error|fail"
shell grep -h "\[food values\].*effect swaps" logs/kubejs/*.log logs/latest.log | head -3
shell grep -h "\[food values\] [0-9]* effect swaps" logs/kubejs/*.log logs/latest.log | head -1 | grep -q .
shell ! grep -nE "ReferenceError|TypeError|EvaluatorException|is not defined" logs/kubejs/startup.log logs/kubejs/server.log
## SQ-CDFL-02 | CD alone: filling, emptying, kettle, mixing name creativedelight: fluids
> zq bytype create:filling
> zq bytype create:emptying
sleep 3
expect creativedelight:
reject creativeengineering:(carrot_juice|egg_solution|frosting|[a-z_]*_coffee|[a-z_]*_tea)\b
> zq bytype farmersrespite:brewing
> zq bytype create:mixing
sleep 3
expect egg_solution
expect frosting
shell grep -h "failed and was skipped" logs/kubejs/startup.log logs/kubejs/server.log | wc -l
"""

B["p6_equipment"] = r"""# r1106cbp: Equipment without Calendar, Creative Delight, Creative Brewing, group A (SQ-CBP-08)
## SQ-CBP-08 | Equipment without CB: no leaks, echo rows, felsic recipes with creativecommon:magic_resistance
bootreject Missing or unsupported mandatory|Mixin apply failed|InvalidMixinException|Encountered an unexpected exception
bootexpect \[creativeequipment\] loaded
shell ! grep -nE "ReferenceError|TypeError|EvaluatorException|is not defined|global\.CB" logs/kubejs/startup.log logs/kubejs/server.log
> cbo effects
waitlog \[cbo\] done 30
expect effects leaks 0$
reject \[cbo\] leak
look record "effects items by namespace"; creativeequipment echo pickaxe/axe/shovel/hoe onHit rows present
shell grep -h "creativecommon:magic_resistance\|felsic" logs/kubejs/server.log | head -5
> zq potion creativecommon:magic_resistance
> zq potion creativecommon:darkness
> zq potion creativecommon:long_resistance
sleep 3
expect potion creativecommon:magic_resistance registered: true
expect potion creativecommon:long_resistance registered: true
"""

# Old save: full pack with the INSTALLED jars (NOSWAP=1), items with the old creativebrewing: / creativeengineering: ids
# saved; then the new jars swapped in (oldsave_swap.sh) and the same world booted (p8_full_new)
B["p7_full_old"] = r"""# r1106cbp: full pack, installed jars, old ids saved (setup for SQ-CBP-06, SQ-CDFL-03)
## SETUP-OLD | old-id items, fluids and effects saved
> forceload add 0 0
sleep 2
> fill -2 -61 -2 12 -58 12 minecraft:air
> setblock 0 -61 0 create:fluid_tank{TankContent:{FluidName:"creativeengineering:carrot_juice",Amount:4000}}
> setblock 0 -61 3 create:fluid_tank{TankContent:{FluidName:"creativeengineering:egg_solution",Amount:500}}
> setblock 0 -61 6 create:fluid_tank{TankContent:{FluidName:"create:potion",Amount:1000,Tag:{Potion:"creativebrewing:resistance",Bottle:"REGULAR"}}}
> setblock 4 -61 0 minecraft:chest{Items:[{Slot:0b,id:"minecraft:potion",Count:1b,tag:{Potion:"creativebrewing:resistance"}},{Slot:1b,id:"minecraft:potion",Count:1b,tag:{Potion:"creativebrewing:magic_resistance"}},{Slot:2b,id:"creativebrewing:potion_bucket",Count:1b,tag:{Potion:"creativebrewing:magic_resistance"}},{Slot:3b,id:"minecraft:potion",Count:1b,tag:{Potion:"kubejs:long_wither"}}]}
> setblock 8 -61 0 minecraft:dispenser[facing=south]{Items:[{Slot:0b,id:"minecraft:splash_potion",Count:1b,tag:{Potion:"creativebrewing:resistance"}}]}
> setblock 10 -61 0 minecraft:dispenser[facing=south]{Items:[{Slot:0b,id:"minecraft:splash_potion",Count:1b,tag:{Potion:"creativebrewing:magic_resistance"}}]}
> summon minecraft:iron_golem 4 -61 8 {Tags:["mrold"],NoAI:1b,PersistenceRequired:1b}
> effect give @e[tag=mrold] creativebrewing:magic_resistance 100000 0
sleep 1
> data get block 0 -61 0 TankContent
> data get block 0 -61 3 TankContent
> data get block 0 -61 6 TankContent
> data get block 4 -61 0 Items
> data get entity @e[tag=mrold,limit=1] ActiveEffects
expect creativeengineering:carrot_juice
expect creativeengineering:egg_solution
expect creativebrewing:magic_resistance
> save-all flush
sleep 3
"""

B["p8_full_new"] = r"""# r1106cbp: full pack, NEW jars, same world as p7 (SQ-CBP-05, -06, -07, SQ-CDFL-03, -04, SQ-CBC-04 rerun)
## SQ-CBP-05 | full pack: absent-mods once per load, no prune of full-pack recipes, removed 1328
bootreject Missing or unsupported mandatory|Mixin apply failed|InvalidMixinException|Encountered an unexpected exception
shell grep -h "absent mods\|\[NCH\] removed\|Empty array" logs/kubejs/*.log logs/latest.log | cut -c1-400 | sort | uniq -c | head
shell n=$(grep -ch "\[absent mods\] \(skipped\|none\)" logs/kubejs/server.log); echo "absent lines=$n"
shell n=$(cat logs/kubejs/*.log logs/latest.log | grep -c "Empty array"); echo "empty_array=$n"
shell grep -h "Added [0-9]* recipes" logs/kubejs/server.log
kjsexpect removed 1328 recipes
## SQ-CBP-06 | old world: potions, buckets, Create potion fluid, active Magic Resistance
shell ! grep -iE "missing.*creativebrewing:(resistance|magic_resistance)|creativebrewing:magic_resistance.*missing" logs/latest.log
shell grep -h "\[swaps\]" logs/latest.log | sed -E 's/^\[[^]]*\] \[[^]]*\]: //' | head
> forceload add 0 0
sleep 3
> data get entity @e[tag=mrold,limit=1] ActiveEffects
> data get block 0 -61 6 TankContent
> data get block 4 -61 0 Items
sleep 1
expect "forge:id": "creativecommon:magic_resistance"
> summon minecraft:iron_golem 8 -61 2 {Tags:["mrA"],NoAI:1b}
> summon minecraft:iron_golem 10 -61 2 {Tags:["mrB"],NoAI:1b}
> setblock 8 -60 0 minecraft:redstone_block
> setblock 10 -60 0 minecraft:redstone_block
sleep 3
> data get entity @e[tag=mrA,limit=1] ActiveEffects
> data get entity @e[tag=mrB,limit=1] ActiveEffects
sleep 1
expect "forge:id": "minecraft:resistance"
expect "forge:id": "creativecommon:magic_resistance"
> effect give @e[tag=mrA] creativecommon:magic_resistance 30 0
reject Unknown effect
> zq potion creativecommon:resistance
> zq potion kubejs:long_wither
> zq potion creativecommon:long_wither
sleep 3
look Items: potions show creativebrewing:/kubejs: Potion strings or remapped ones; dispensed splash potions applied their effect (above); kubejs:long_wither resolves
## SQ-CDFL-03 | old world: food fluids remapped to creativedelight:
shell ! grep -E "Identifier .* is missing|[Mm]issing.*creativeengineering:(carrot_juice|egg_solution|flowing_)" logs/latest.log
> data get block 0 -61 0 TankContent
> data get block 0 -61 3 TankContent
sleep 1
expect FluidName: "creativedelight:carrot_juice"
expect FluidName: "creativedelight:egg_solution"
expect Amount: 4000
## SQ-CDFL-04 | soft fluid equivalent of the renamed carrot juice (static + log; jar fill is a client step)
shell for f in carrot_juice dandelion_juice turnip_water; do unzip -p mods/creativedelight-*.jar data/creativeengineering/moonlight/soft_fluids/$f.json 2>/dev/null | tr -d ' \n' | grep -o '"equivalent_fluids":\[[^]]*\]' || echo "$f: no json"; done
shell ! grep -E "WARN.*equivalent_fluids" logs/latest.log
## SQ-CBP-07 | config carry-over: effects and magic_resistance into Common, copper_poisoning into Equipment
shell grep -h "\[swaps\] config sections" logs/latest.log | sed -E 's/^\[[^]]*\] \[[^]]*\]: //'
shell for f in creativecommon creativeequipment creativebrewing; do echo "$f: $(grep -o '^\[[a-z_]*\]' config/$f-common.toml | tr '\n' ' ')"; done
shell grep -q "^\[effects\]" config/creativecommon-common.toml && grep -q "^\[magic_resistance\]" config/creativecommon-common.toml && grep -q "^\[copper_poisoning\]" config/creativeequipment-common.toml
shell ! grep -E "^\[(effects|magic_resistance|copper_poisoning)\]" config/creativebrewing-common.toml
look diff of the moved sections against configs_edited/root_config/creativebrewing-common.toml (shell.txt of this row is the section list; the diff is in the run notes)
"""

STRICT_SINGLES = sorted(k for k in cbo.B if re.fullmatch(r"s\d+", k))
for k in STRICT_SINGLES:
    B[k] = strict(cbo.B[k])

INST = "$INST"
BOOTS = [  # (name, mode, batch, env)
    ("p1", "chain", "p1_chain", ""),
    ("p2", "cooking", "p2_cooking", ""),
    ("p3", "optional", "p3_optional", "KEEP=1"),
    ("p3", "optional", "p3b_optional_members_off", "REUSE=1 PRE='python3 $INST/tools/scratch_query/qrun.py toml config/creativebrewing-common.toml members vinery false && python3 $INST/tools/scratch_query/qrun.py toml config/creativebrewing-common.toml members brewinandchewin false'"),
    ("p4", "chain", "p4_iw", 'ADD="immersive_weathering-*.jar,moonlight-*.jar"'),
    ("p5", "cooking", "p5_cd_alone", 'DROP="creativebrewing-*.jar"'),
    ("p6", "chain", "p6_equipment", 'DROP="creativecalendar-*.jar creativedelight-*.jar creativebrewing-*.jar kubejsdelight-*.jar" PRE=\'cp $INST/development/creativeEquipment/build/libs/creativeequipment-1.0.0.jar mods/\''),
    ("p6f", "chain", "p6_equipment", 'ADD="Felsic Gear *.jar" DROP="creativecalendar-*.jar creativedelight-*.jar creativebrewing-*.jar kubejsdelight-*.jar" PRE=\'cp $INST/development/creativeEquipment/build/libs/creativeequipment-1.0.0.jar mods/\''),  # p6 rerun with Felsic Gear: End gear recipes
] + [(n, "chain", n, f'ADD="{a}"') for n, a in ((f"s{x[0]}", x[1]) for x in cbo.SINGLES if x[0] != "34")]  # s34 = p4

if __name__ == "__main__":
    for k, v in B.items():
        with open(os.path.join(HERE, k + ".qb"), "w") as f:
            f.write(v)
    print(len(B), "batches written")
    if len(sys.argv) > 1 and sys.argv[1] == "plan":
        for b in BOOTS:
            print(*b, sep="\t")
