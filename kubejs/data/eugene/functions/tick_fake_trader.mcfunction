#Fake wandering trader
execute at @a run execute as @e[type=wandering_trader,tag=fake,distance=..6] at @s run summon area_effect_cloud ~ ~1 ~ {Duration:5, Radius:1f, Age:0, WaitTime:10, RadiusPerTick:1f, Particle:"splash_spell_emitter"}
execute at @a run execute as @e[type=wandering_trader,tag=fake,distance=..6] at @s run summon minecraft:illusioner
execute at @a run execute as @e[type=wandering_trader,tag=fake,distance=..6] at @s run playsound minecraft:entity.illusioner.prepare_blindness hostile @a[distance=..10]
execute at @a run execute as @e[type=wandering_trader,tag=fake,distance=..6] at @s run playsound minecraft:entity.illusioner.ambient hostile @a[distance=..10]
execute at @a run execute as @e[type=wandering_trader,tag=fake,distance=..6] at @s run effect give @a[distance=..6] blindness 2 0 true
execute at @a run execute as @e[type=wandering_trader,tag=fake,distance=..6] at @s run tp @s ~ ~-500 ~