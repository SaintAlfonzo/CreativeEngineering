#血条
execute at @a run execute as @e[type=piglin,tag=pig_ranger] store result score @s pig_ranger_hp run data get entity @s Health

bossbar set pig_ranger players @a

execute at @a run execute as @e[tag=pig_ranger,distance=..32] run execute store result bossbar pig_ranger value run data get entity @s Health
execute at @a if entity @e[tag=pig_ranger,distance=..32] run bossbar set pig_ranger visible true
execute at @a if entity @e[tag=pig_ranger,type=piglin,distance=32..] run bossbar set pig_ranger visible false



#制导
execute as @e[type=firework_rocket] at @s run tp @s @e[type=!firework_rocket,team=!ranger,distance=..2.5,limit=1]