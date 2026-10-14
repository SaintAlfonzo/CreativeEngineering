# 1.19.2 port (2026-10-14), run as and at a Ser Marrow with no steed under it. No /ride before 1.19.4, and a
# summon can only mount a NEW entity: summon the steed with a fresh skeleton on it, copy Ser Marrow onto that
# skeleton (attributes before health, tags last), then send the old one out of the world. The steed is tamed (as
# vanilla's skeleton-trap horses): an untamed horse throws a mob rider off within seconds, and without /ride
# nothing puts him back on.
summon skeleton_horse ~ ~ ~ {Tame:1b,Health:30,Attributes:[{Name:"generic.max_health",Base:30},{Name:"generic.movement_speed",Base:0.13d},{Name:"horse.jump_strength",Base:0.12d}],Tags:["marrow_steed"],ActiveEffects:[{Id:12,Amplifier:254,Duration:2147483647,ShowParticles:0b}],Passengers:[{id:"minecraft:skeleton",PersistenceRequired:1b,Tags:["marrow_new"]}]}
data modify entity @e[type=skeleton,tag=marrow_new,limit=1,sort=nearest] Attributes set from entity @s Attributes
data modify entity @e[type=skeleton,tag=marrow_new,limit=1,sort=nearest] Health set from entity @s Health
data modify entity @e[type=skeleton,tag=marrow_new,limit=1,sort=nearest] HandItems set from entity @s HandItems
data modify entity @e[type=skeleton,tag=marrow_new,limit=1,sort=nearest] ArmorItems set from entity @s ArmorItems
data modify entity @e[type=skeleton,tag=marrow_new,limit=1,sort=nearest] HandDropChances set from entity @s HandDropChances
data modify entity @e[type=skeleton,tag=marrow_new,limit=1,sort=nearest] ArmorDropChances set from entity @s ArmorDropChances
data modify entity @e[type=skeleton,tag=marrow_new,limit=1,sort=nearest] LeftHanded set from entity @s LeftHanded
data modify entity @e[type=skeleton,tag=marrow_new,limit=1,sort=nearest] CustomName set from entity @s CustomName
data modify entity @e[type=skeleton,tag=marrow_new,limit=1,sort=nearest] Tags set from entity @s Tags
tag @s remove ser_marrow
tp @s ~ ~-600 ~
