#ser marrow
#血条
execute at @a run execute as @e[type=skeleton,tag=ser_marrow] store result score @s ser_marrow_hp run data get entity @s Health

bossbar set ser_marrow players @a
execute at @a run execute as @e[tag=ser_marrow,type=skeleton,distance=..32] run execute store result bossbar ser_marrow value run data get entity @s Health
execute at @a if entity @e[tag=ser_marrow,type=skeleton,distance=..32] run bossbar set ser_marrow visible true
execute at @a if entity @e[tag=ser_marrow,type=skeleton,distance=32..] run bossbar set ser_marrow visible false

#换武器
execute at @a run execute as @e[tag=ser_marrow,tag=!sword_fight,type=skeleton,distance=..5,scores={ser_marrow_hp=49..150}] run item replace entity @s weapon.mainhand with minecraft:iron_sword{Enchantments:[{id:"minecraft:knockback",lvl:2}],HandDropChances:[0.6F,0F]}
execute at @a run execute as @e[tag=ser_marrow,tag=!sword_fight,type=skeleton,distance=..5,scores={ser_marrow_hp=49..150}] run tag @s add sword_fight
execute at @a run execute as @e[tag=ser_marrow,tag=bow_fight,type=skeleton,distance=..5,scores={ser_marrow_hp=49..150}] run tag @s remove bow_fight

execute at @a run execute as @e[tag=ser_marrow,type=skeleton,distance=5..64,tag=!bow_fight] run item replace entity @s weapon.mainhand with minecraft:bow{Enchantments:[{id:"power",lvl:5},{id:"minecraft:punch",lvl:2}],HandDropChances:[0.2F,0F]}
execute at @a run execute as @e[tag=ser_marrow,tag=!bow_fight,type=skeleton,distance=5..64] run tag @s add bow_fight
execute at @a run execute as @e[tag=ser_marrow,tag=sword_fight,type=skeleton,distance=5..64] run tag @s remove sword_fight

#第一阶段
# 1.19.2 port (2026-10-14): mounting moved to summon_skeleton -> mount_ser_marrow (no /ride before 1.19.4)
# 1.19.2 port: no /damage before 1.19.4; standing at the steed withers instead (about 1 HP a second, like the 1 damage a tick through hurt frames)
execute at @e[tag=marrow_steed] run effect give @a[distance=..1.5] minecraft:wither 2 1 true

execute at @a run execute as @e[tag=marrow_steed,distance=..12] at @s run particle flame ^0.2 ^1.9 ^1
execute at @a run execute as @e[tag=marrow_steed,distance=..12] at @s run particle flame ^-0.2 ^1.9 ^1

#第二阶段
execute at @a run execute as @e[tag=ser_marrow,type=skeleton,distance=..64,scores={ser_marrow_hp=..49}] at @s if predicate eugene:chance_1 run particle flame ~0.7 ~2.2 ~0.9
execute at @a run execute as @e[tag=ser_marrow,type=skeleton,distance=..64,scores={ser_marrow_hp=..49}] at @s if predicate eugene:chance_1 run particle flame ~0.6 ~2.4 ~-0.7

execute at @a run execute as @e[tag=ser_marrow,type=skeleton,distance=..64,scores={ser_marrow_hp=..49}] at @s if predicate eugene:chance_1 run particle smoke ~0.8 ~2.5 ~0.6
execute at @a run execute as @e[tag=ser_marrow,type=skeleton,distance=..64,scores={ser_marrow_hp=..49}] at @s if predicate eugene:chance_1 run particle smoke ~0.5 ~2.4 ~0.4