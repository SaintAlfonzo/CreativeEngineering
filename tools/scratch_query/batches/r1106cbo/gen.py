#!/usr/bin/env python3
"""Writes the r1106cbo batch files (scratch_queue.md "SQ-CBC" and "SQ-CBO") next to this script.
Run order and boot settings: BOOTS at the bottom (also printed as cbo_run.sh lines by `gen.py plan`)."""
import os, sys

HERE = os.path.dirname(os.path.abspath(__file__))

# "checks S" of the SQ-CBO section, used by every CB boot
CHECKS_S = r"""bootexpect \[creativebrewing\] members: \[vinery, brewinandchewin\]$
bootreject Missing or unsupported mandatory|Mixin apply failed|InvalidMixinException|creativebrewing.*(NoClassDefFoundError|ClassNotFoundException)|Encountered an unexpected exception
shell ! grep -nE "ReferenceError|TypeError|EvaluatorException|is not defined" logs/kubejs/startup.log logs/kubejs/server.log
> cbo all
waitlog \[cbo\] done 30
# the 4 unguarded echo tool rows (tool_effects.js 134-137, found r1106cbo boot 1) are tolerated; any other leak fails
expect effects leaks [04]$
reject \[cbo\] leak (?!tool onHit item creativeengineering:echo_(pickaxe|axe|shovel|hoe)$)
"""
SUMGREP = r'''shell grep -h "\[CE potions\]\|\[food values\].*effect swaps\|\[alcohol\] food values\|tipped arrow tipping done\|potion bottling done\|jeed_providers.js:\|\[fluid_jei\]\|dragon-breath\|Lingerification" logs/kubejs/startup.log logs/kubejs/server.log logs/latest.log | sed -E 's/^\[[^]]*\] \[[^]]*\]: //' | sort | uniq -c'''
ABSENT = r'''shell grep -h "absent mods" logs/kubejs/server.log
shell grep -c "failed and was skipped" logs/kubejs/server.log logs/kubejs/startup.log'''
WT = r'''shell grep -h "\[wandering trades\]" logs/kubejs/server.log logs/kubejs/startup.log'''


def golem(effects, expect=(), reject=()):
    """effect check: one NoAI golem in a forced chunk, every effect in `effects`, then its ActiveEffects."""
    s = "> forceload add 0 0\nsleep 1\n> kill @e[tag=cbo]\n> summon minecraft:iron_golem 0 -60 5 {Tags:[\"cbo\"],NoAI:1b}\nsleep 1\n"
    for e in effects:
        s += f"> effect give @e[tag=cbo] {e} 60 0\n"
    s += "sleep 1\n> data get entity @e[tag=cbo,limit=1] ActiveEffects\nsleep 2\n> kill @e[tag=cbo]\n"
    for e in expect:
        s += f'expect "forge:id": "{e}"\n'
    for e in reject:
        s += f'reject "forge:id": "{e}"\n'
    return s


def zq(*cmds):
    return "".join(f"> zq {c}\n" for c in cmds) + "sleep 3\n"


MR_PAIR = r"""> forceload add 0 0
sleep 2
> summon minecraft:iron_golem 0 -60 5 {Tags:["cbaA"],NoAI:1b}
> summon minecraft:iron_golem 3 -60 5 {Tags:["cbaB"],NoAI:1b}
> effect give @e[tag=cbaA] creativecommon:magic_resistance 120 0
> effect give @e[tag=cbaA] minecraft:instant_damage 1 0
> effect give @e[tag=cbaB] minecraft:instant_damage 1 0
sleep 2
> data get entity @e[tag=cbaA,limit=1] Health
> data get entity @e[tag=cbaB,limit=1] Health
> effect give @e[tag=cbaA] minecraft:poison 10 0
sleep 5
> data get entity @e[tag=cbaA,limit=1] Health
> kill @e[tag=cbaA]
> kill @e[tag=cbaB]
"""
DISPENSER = r"""bootexpect potion bucket dispenser behaviors registered
bootexpect potion bucket dispense handler installed from KubeJS
> setblock 10 -60 10 minecraft:dispenser[facing=up]{Items:[{Slot:0b,id:"creativebrewing:lingering_potion_bucket",Count:1b,tag:{Potion:"minecraft:regeneration"}}]}
> setblock 11 -60 10 minecraft:redstone_block
sleep 2
> execute if entity @e[type=minecraft:area_effect_cloud,x=10,y=-59,z=10,distance=..8]
expect Test passed
reject dispensed with no KubeJS handler installed
"""
RELOAD = r"""shell wc -l < logs/latest.log > .preload_n
> reload
waitlog Added [0-9]+ recipes, removed 120
sleep 3
# "server scripts ready" prints on ServerEvents.loaded only (boot), not on /reload
bootexpect server scripts ready, members on: vinery, brewinandchewin
shell n=$(cat .preload_n); tail -n +$n logs/latest.log | grep -c "Error parsing recipe\|Couldn't parse loot table"; ! tail -n +$n logs/latest.log | grep -n "Exception" | grep -v "Can't find block\|Error parsing recipe"
shell ! grep -nE "ReferenceError|TypeError|EvaluatorException|is not defined" logs/kubejs/server.log
> cbo effects
waitlog \[cbo\] done 30
expect effects leaks 0
"""

B = {}

B["c1_chain"] = f"""# r1106cbo boot 1: chain, group A, fresh config (SQ-CBC-01, -03, -07; SQ-CBO-01 to -04)
## SQ-CBC-01 | chain: CD transition items, no incomplete-item errors
shell grep -h "transition items" logs/kubejs/startup.log
shell grep -qE '\\[creativedelight\\] transition items \\(80\\)' logs/kubejs/startup.log  # startup.log (r1106spf: kjsexpect reads server.log)
shell grep -c "not registered - add a row\\|no incomplete item registered" logs/kubejs/startup.log logs/kubejs/server.log
shell test "$(cat logs/kubejs/startup.log logs/kubejs/server.log | grep -c "not registered - add a row\\|no incomplete item registered")" = 0
## SQ-CBC-03 | chain: failed-recipe count under 50
shell grep -h "Added [0-9]* recipes" logs/kubejs/server.log
shell n=$(grep -oE "[0-9]+ failed" logs/kubejs/server.log | tail -1 | grep -oE "[0-9]+"); echo "failed=${{n:-0}}"; test "${{n:-0}}" -lt 50
## SQ-CBC-07 | chain: no dependency or ordering error for creativebrewing
bootreject Missing or unsupported mandatory
bootreject \\b[Cc]ycle\\b|[Oo]rdering (cycle|error|constraint)
shell grep -n "creativebrewing" logs/latest.log | grep -iE "depend|missing|unsupported|cycle" | head
## SQ-CBO-01 | chain: optional mods absent, no effect leaks, reference namespaces
{CHECKS_S}expect mods loaded 0 of 24: -
look record the "effects items by namespace" line (reference for the single-mod boots)
> zq potion creativecommon:long_reach
> zq potion buzzier_bees:long_luck
> zq potion alexsmobs:speed_iii
> zq potion quark:resilience
sleep 3
expect potion creativecommon:long_reach registered: false
expect potion buzzier_bees:long_luck registered: false
expect potion alexsmobs:speed_iii registered: false
expect potion quark:resilience registered: false
## SQ-CBO-02 | chain: potion, swap, alcohol, bottling, JEED counts (reference)
{SUMGREP}
bootreject Lingerification:
bootreject registered [0-9]+ dragon-breath
shell grep -h "better_beacons not present" logs/kubejs/startup.log logs/kubejs/server.log logs/latest.log | head -4
shell grep -c "better_beacons not present" logs/kubejs/startup.log logs/kubejs/server.log logs/latest.log
shell ! grep -hE "\\[CE potions\\] mix skipped|effect .* not registered" logs/kubejs/startup.log logs/kubejs/server.log logs/latest.log
look counts recorded; better_beacons skip lines for long_reach and phantom_bane; re-added 0 dragon-breath
## SQ-CBO-03 | chain: absent-mods summary, NCH failure count
{ABSENT}
look the [absent mods] lines name only mods missing from the chain; failure count recorded
## SQ-CBO-04 | chain: Poison applies, reload with absent-mod config ids
{golem(["minecraft:poison"], expect=["minecraft:poison"])}{RELOAD}"""

B["c1b_chain_mr"] = f"""# r1106cbo boot 1 restart: copper_poisoning off, magic_resistance reductionPerLevel 0.5 (SQ-CBO-05)
## SQ-CBO-05 | config read: Magic Resistance 0.5 per level
bootreject Incorrect key|correcting
{MR_PAIR}count entity data: 97\\.0 == 2
count entity data: 94\\.0 == 1
"""

B["c3_cooking"] = f"""# r1106cbo boot 3: cooking set, group A (SQ-CBC-02)
## SQ-CBC-02 | cooking: NCH failures and absent-mods summary
shell grep -h "failed and was skipped" logs/kubejs/startup.log logs/kubejs/server.log | sed -E 's/[0-9]+//g' | sort | uniq -c | sort -rn | head -40
shell grep -h "failed and was skipped" logs/kubejs/startup.log logs/kubejs/server.log | wc -l
shell ! grep -h "failed and was skipped" logs/kubejs/startup.log logs/kubejs/server.log | grep -E "Empty array\\?|grown_|twig_nest|rope_to__air|canned_mulberry_pie_filling_pie"
shell test "$(cat logs/kubejs/startup.log logs/kubejs/server.log | grep -c "failed and was skipped")" -lt 40
shell grep -h "absent mods" logs/kubejs/server.log
kjsexpect absent mods
look record what is left; [absent mods] names quark, upgrade_aquatic, bushierflowers among others
"""

B["c5_optional"] = f"""# r1106cbo boot 5: chain + all 24 optional dependencies, group A (SQ-CBO-10 to -17)
## SQ-CBO-10 | optional: 24 of 24, namespaces, potions registered
{CHECKS_S}expect mods loaded 24 of 24
expect effects items by namespace: .*alexsmobs.*botania.*create_deep_dark.*dragonloot.*everythingcopper.*felsic_gear.*immersive_armors.*immersiveengineering.*supplementaries
> cbo potions
waitlog \\[cbo\\] done 30
look record the potion ... registered lines (all true except quark:resilience and upgrade_aquatic:restfulness expected)
## SQ-CBO-11 | optional: counts above the chain
{SUMGREP}
kjsexpect potion bottling done \\([1-9]
kjsexpect jeed_providers.js: [1-9][0-9]* effect provider recipes
bootexpect registered [1-9][0-9]* dragon-breath fluid-brewing recipes
bootexpect Lingerification: registered refinery list generator
shell grep -h "\\[alcohol\\] food values" logs/kubejs/startup.log logs/kubejs/server.log | grep "44 of 44"
shell ! grep -hE "better_beacons not present|\\[CE potions\\] mix skipped|effect .* not registered" logs/kubejs/startup.log logs/kubejs/server.log logs/latest.log
look mix and effect swap counts above SQ-CBO-02's; re-added [1-9]
## SQ-CBO-12 | optional: guarded brews
{zq("potion creativecommon:long_reach minecraft:redstone", "potion minecraft:thick geode:wrappist_shard,geode:celestite_shard", "potion minecraft:awkward buzzier_bees:four_leaf_clover,buzzier_bees:silverfish_bottle", "potion minecraft:strong_swiftness alexsmobs:gazelle_horn", "potion alexsmobs:lava_vision minecraft:redstone")}expect long_reach \\+ minecraft:redstone -> .*long_long_reach
expect wrappist_shard -> .*long_reach
expect celestite_shard -> .*phantom_bane
expect four_leaf_clover -> .*luck
expect silverfish_bottle -> .*unluck
expect gazelle_horn -> .*speed_iii
expect lava_vision \\+ minecraft:redstone -> .*long_lava_vision
## SQ-CBO-13 | optional: effect remaps and removals
{golem(["neapolitan:agility", "fruitsdelight:chorus", "quark:resilience", "upgrade_aquatic:restfulness", "upgrade_aquatic:insomnia", "alexsmobs:power_down"], expect=["vinery:climbing_effect", "vinery:teleport", "alexsmobs:knockback_resistance"], reject=["neapolitan:agility", "fruitsdelight:chorus", "quark:resilience", "upgrade_aquatic:restfulness", "upgrade_aquatic:insomnia", "alexsmobs:power_down"])}## SQ-CBO-14 | optional: no CB server script skipped, winemaker, fermenting, effect providers
kjsexpect Loaded script creativebrewing:kubejs/alexsmobs/lava_vision.js
kjsreject Skipped creativebrewing:kubejs/
kjsexpect \\[wandering trades\\] winemaker [1-9]
{WT}
{zq("bytype brewinandchewin:fermenting", "bytype jeed:effect_provider")}expect bytype brewinandchewin:fermenting: (2[1-9]|[3-9]\\d)$
expect bytype jeed:effect_provider: [1-9]
expect bytype jeed:effect_provider (creativebrewing|vinery):
## SQ-CBO-15 | optional: Magic Resistance golems and potion bucket dispenser
{MR_PAIR}expect Applied effect Magic Resistance
count entity data: 95\\.2 == 2
count entity data: 94\\.0 == 1
{DISPENSER}## SQ-CBO-16 | optional: absent-mods summary names no optional id
{ABSENT}
shell ! grep -h "absent mods" logs/kubejs/server.log | grep -wE "alexsmobs|appleskin|better_beacons|botania|buzzier_bees|create_central_kitchen|create_deep_dark|dragonloot|everythingcopper|farmersrespite|felsic_gear|fruitsdelight|geode|immersive_armors|immersive_weathering|immersiveengineering|jeed|jei|neapolitan|potionbundles|quark|supplementaries|tmted|upgrade_aquatic"
look failure count not above SQ-CBO-03's
## SQ-CBO-17 | optional: reload
{RELOAD}"""

B["c5b_optional_members_off"] = f"""# r1106cbo boot 5 restart: members vinery and brewinandchewin off (SQ-CBO-18)
## SQ-CBO-18 | optional, both members off
kjsexpect server scripts ready, members on: $
shell ! grep -nE "ReferenceError|TypeError|EvaluatorException|is not defined" logs/kubejs/startup.log logs/kubejs/server.log
> cbo effects
waitlog \\[cbo\\] done 30
expect effects leaks 0
"""

# single optional mods: (name, ADD, modid in the "mods loaded" line, extra batch lines)
NS = lambda n: f"expect effects items by namespace: .*\\b{n}=\n"
SINGLES = [
    ("20", "alexsmobs-*.jar,citadel-*.jar", "alexsmobs",
     "kjsexpect Loaded script creativebrewing:kubejs/alexsmobs/lava_vision.js\n"
     + zq("potion minecraft:strong_swiftness alexsmobs:gazelle_horn", "potion alexsmobs:lava_vision minecraft:redstone")
     + "expect gazelle_horn -> .*speed_iii\nexpect lava_vision \\+ minecraft:redstone -> .*long_lava_vision\n"
     + golem(["alexsmobs:power_down"], reject=["alexsmobs:power_down"])
     + "> cbo potions\nwaitlog \\[cbo\\] done 30\n" + SUMGREP + "\n"),
    ("21", "appleskin-*.jar", "appleskin", ""),
    ("22", "CerbonsBetterBeacons-*.jar", "better_beacons",
     "> cbo potions\nwaitlog \\[cbo\\] done 30\nexpect potion creativecommon:long_reach registered: true\nexpect potion creativecommon:phantom_bane registered: true\n"
     + zq("potion creativecommon:long_reach minecraft:redstone,minecraft:glowstone_dust")
     + "expect redstone -> .*long_long_reach\nexpect glowstone_dust -> .*strong_long_reach\n"
     "shell ! grep -hE \"mix skipped.*geode:\" logs/kubejs/startup.log logs/kubejs/server.log logs/latest.log\n"),
    ("23", "Botania-*.jar,curios-forge-*.jar,Patchouli-*.jar", "botania", NS("botania")),
    ("24", "buzzier_bees-*.jar,blueprint-*.jar", "buzzier_bees",
     zq("potion minecraft:luck minecraft:redstone,minecraft:glowstone_dust", "potion minecraft:awkward buzzier_bees:four_leaf_clover")
     + "expect redstone -> .*long_luck\nexpect glowstone_dust -> .*strong_luck\nexpect four_leaf_clover -> .*luck\n"),
    ("25", "create_central_kitchen-*.jar", "create_central_kitchen",
     "shell grep -h \"dragon-breath\" logs/latest.log logs/kubejs/startup.log\n"
     "bootexpect registered [1-9][0-9]* dragon-breath fluid-brewing recipes\nshell grep -h \"re-added [1-9]\" logs/latest.log logs/kubejs/startup.log logs/kubejs/server.log\n"),
    ("26", "create_deep_dark-*.jar", "create_deep_dark", NS("create_deep_dark") + "bootreject EchoArmorEffectProcedureMixin\n"),
    ("27", "dragonloot-*.jar", "dragonloot", NS("dragonloot")),
    ("28", "everythingcopper-*.jar", "everythingcopper", NS("everythingcopper") + "reject leak .*immersiveengineering:conductive\n"),
    ("29", "FarmersRespite-*.jar", "farmersrespite",
     SUMGREP + "\n" + zq("bytype brewinandchewin:fermenting")
     + "shell ! grep -h \"effect swap: .* is not a food\" logs/kubejs/startup.log logs/kubejs/server.log logs/latest.log\nlook effect swaps above the chain's; fermenting count recorded\n"),
    ("30", "Felsic Gear *.jar", "felsic_gear", NS("felsic_gear") + "reject leak .*better_beacons\n"),
    ("31", "fruitsdelight-*.jar", "fruitsdelight",
     "shell grep -h \"\\[alcohol\\] food values\" logs/kubejs/startup.log logs/kubejs/server.log | grep \"43 of 44\"\n" + WT + "\n"
     + golem(["fruitsdelight:chorus"], expect=["vinery:teleport"], reject=["fruitsdelight:chorus"])
     + "look winemaker count above SQ-CBA-10's\n"),
    ("32", "geodeforge-*.jar", "geode",
     zq("potion minecraft:thick geode:wrappist_shard") + "expect wrappist_shard -> nothing\n"
     "shell ! grep -hE \"mix skipped\" logs/kubejs/startup.log logs/kubejs/server.log logs/latest.log\n"),
    ("33", "immersive_armors-*.jar", "immersive_armors", NS("immersive_armors") + "reject leak .*immersiveengineering:flammable\n"),
    ("34", "immersive_weathering-*.jar,moonlight-*.jar", "immersive_weathering",
     "shell ! grep -E \"WARN.*immersive_weathering\" logs/latest.log | grep -iE \"creativebrewing|creativecommon\"\n"),
    ("35", "ImmersiveEngineering-*.jar", "immersiveengineering",
     NS("immersiveengineering") + "bootexpect Lingerification: registered refinery list generator\n"
     "shell grep -h \"potion bottling done\\|Lingerification: generated\" logs/kubejs/server.log logs/latest.log\n"
     "bootexpect Lingerification: generated [1-9]\nkjsexpect potion bottling done \\([1-9]\n"
     + zq("recipes creativebrewing:bottling") + "expect recipes matching creativebrewing:bottling: [1-9]\n"
     "bootreject PotionHelperMixin|PotionRecipeGeneratorsMixin\n"),
    ("36", "jeed-*.jar", "jeed",
     "kjsexpect jeed_providers.js: [1-9]\n" + zq("bytype jeed:effect_provider")
     + "expect bytype jeed:effect_provider: [1-9]\nexpect magic_resistance\nkjsreject no Vinery wine providers\n"),
    ("37", "jei-*.jar", "jei", "bootreject PotionMachinesJEI|PotionPagesJEI|mezz\\.jei.*Exception\n"),
    ("38", "neapolitan-*.jar,blueprint-*.jar", "neapolitan",
     SUMGREP + "\n" + golem(["neapolitan:agility"], expect=["vinery:climbing_effect"], reject=["neapolitan:agility"])
     + "look effect swaps above the chain's\n"),
    ("39", "potionbundles-*.jar", "potionbundles", "bootreject PotionFluidHandlerMixin\n"),
    ("40", "Quark-*.jar,AutoRegLib-*.jar", "quark",
     zq("potion quark:resilience minecraft:redstone,minecraft:glowstone_dust")
     + "expect resilience \\+ minecraft:redstone -> nothing\nexpect resilience \\+ minecraft:glowstone_dust -> nothing\n"
     + golem(["quark:resilience"]) + "reject Exception\nlook record whether Resilience stayed or was dropped (remap target absent)\n"),
    ("41", "supplementaries-*.jar,moonlight-*.jar", "supplementaries", NS("supplementaries")),
    ("42", "Engineers Delight *.jar,ImmersiveEngineering-*.jar", "immersiveengineering, tmted",
     "shell grep -h \"\\[alcohol\\] food values\" logs/kubejs/startup.log logs/kubejs/server.log | grep \"43 of 44\"\n"),
    ("43", "upgrade_aquatic-*.jar,blueprint-*.jar", "upgrade_aquatic",
     golem(["upgrade_aquatic:restfulness", "upgrade_aquatic:insomnia"], reject=["upgrade_aquatic:restfulness", "upgrade_aquatic:insomnia"])
     + zq("potion upgrade_aquatic:restfulness minecraft:glowstone_dust", "potion upgrade_aquatic:insomnia minecraft:glowstone_dust")
     + "reject restfulness \\+ minecraft:glowstone_dust -> minecraft\nreject insomnia \\+ minecraft:glowstone_dust -> minecraft\n"
     "look both brews -> nothing (or the potion unregistered)\n"),
    ("50", "CerbonsBetterBeacons-*.jar,geodeforge-*.jar", "better_beacons, geode",
     zq("potion minecraft:thick geode:wrappist_shard,geode:celestite_shard")
     + "expect wrappist_shard -> .*long_reach\nexpect celestite_shard -> .*phantom_bane\n"),
    ("51", "alexsmobs-*.jar,citadel-*.jar,Quark-*.jar,AutoRegLib-*.jar", "alexsmobs, quark",
     golem(["quark:resilience"], expect=["alexsmobs:knockback_resistance"], reject=["quark:resilience"]) + SUMGREP
     + "\nlook mix count above SQ-CBO-20's\n"),
    ("52", "everythingcopper-*.jar,ImmersiveEngineering-*.jar", "everythingcopper, immersiveengineering",
     "> cbo effects\nwaitlog \\[cbo\\] done 30\nexpect effects leaks [04]$\n"
     "shell grep -c \"immersiveengineering:conductive\" logs/kubejs/startup.log logs/latest.log\nlook copper armor rows carry immersiveengineering:conductive\n"),
    ("53", "immersive_armors-*.jar,ImmersiveEngineering-*.jar", "immersive_armors, immersiveengineering",
     "> cbo effects\nwaitlog \\[cbo\\] done 30\nexpect effects leaks 0\nlook immersiveengineering:flammable rows present\n"),
]
for n, add, mod, extra in SINGLES:
    cnt = len([m for m in mod.split(", ")])
    B[f"s{n}"] = (f"# r1106cbo single boot: chain + ADD=\"{add}\", group A (SQ-CBO-{n})\n"
                  f"## SQ-CBO-{n} | chain + {add}\n{CHECKS_S}expect mods loaded {cnt} of 24: {mod}$\n{extra}")

BOOTS = [  # (name, mode, batch, env)
    ("c1", "chain", "c1_chain", "KEEP=1"),
    ("c1", "chain", "c1b_chain_mr", "REUSE=1 PRE='python3 $INST/tools/scratch_query/qrun.py toml config/creativebrewing-common.toml copper_poisoning enabled false && python3 $INST/tools/scratch_query/qrun.py toml config/creativebrewing-common.toml magic_resistance reductionPerLevel 0.5'"),
    ("cbc05", "chain", "", "NOUPSTREAM=vinery_upstream NOQRUN=1"),
    ("c3", "cooking", "c3_cooking", ""),
    ("c5", "optional", "c5_optional", "KEEP=1"),
    ("c5", "optional", "c5b_optional_members_off", "REUSE=1 PRE='...members off...'"),
] + [(f"s{n}", "chain", f"s{n}", f'ADD="{a}"') for n, a, _, _ in SINGLES]

if __name__ == "__main__":
    for k, v in B.items():
        with open(os.path.join(HERE, k + ".qb"), "w") as f:
            f.write(v)
    print(len(B), "batches written")
    if len(sys.argv) > 1 and sys.argv[1] == "plan":
        for b in BOOTS:
            print(*b, sep="\t")
