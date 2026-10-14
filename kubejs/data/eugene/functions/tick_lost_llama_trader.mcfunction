#召唤羊驼
execute at @a run execute as @e[tag=lost_llama,tag=!llama_quest,distance=..4] at @s run execute as @e[limit=1,distance=96..128,sort=nearest] at @s unless entity @e[tag=the_lost_llama] if block ~ ~2 ~ air if block ~ ~3 ~ air if block ~ ~4 ~ air if block ~ ~5 ~ air if block ~ ~6 ~ air if block ~ ~7 ~ air if block ~ ~8 ~ air if block ~ ~9 ~ air if block ~ ~10 ~ air if block ~ ~11 ~ air if block ~ ~12 ~ air if block ~ ~13 ~ air if block ~ ~14 ~ air if block ~ ~15 ~ air run summon trader_llama ~ ~ ~ {CustomName:'[{"keybind":"lost.llama","color":"blue","blod":true,"italic":false}]',CustomNameVisible:1,Tags:["the_lost_llama"]}

#发出请求
execute at @a run execute as @e[tag=lost_llama,tag=!llama_quest,distance=..4] at @s if entity @e[tag=the_lost_llama] run teleport @s ^ ^ ^ facing entity @a[limit=1,sort=nearest]
execute at @a if entity @e[tag=lost_llama,tag=!llama_quest,distance=..4] if entity @e[tag=the_lost_llama] run tellraw @s {"keybind":"lost.llama.quest.quest"}
execute at @a run execute as @e[tag=lost_llama,tag=!llama_quest,distance=..4] at @s if entity @e[tag=the_lost_llama] run playsound minecraft:entity.wandering_trader.no ambient @a[distance=..4] ~ ~ ~ 100

execute as @a at @s if entity @e[tag=lost_llama,tag=!llama_quest,distance=..4] if entity @e[tag=the_lost_llama] run title @s times 20 100 20
execute as @a at @s if entity @e[tag=lost_llama,tag=!llama_quest,distance=..4] if entity @e[tag=the_lost_llama] run title @s subtitle {"keybind":"lost.llama.quest.take"}
execute as @a at @s if entity @e[tag=lost_llama,tag=!llama_quest,distance=..4] if entity @e[tag=the_lost_llama] run title @s title {"text":""}

execute at @a if entity @e[tag=the_lost_llama] run tag @e[tag=lost_llama,tag=!llama_quest,distance=..4] add llama_quest
execute at @a if entity @e[tag=the_lost_llama] run tag @e[tag=lost_llama,tag=llama_quest,distance=..4] remove lost_llama

#见到丢失的羊驼
execute at @a run execute as @e[tag=llama_quest,distance=..4] at @s if entity @e[limit=1,distance=..4,tag=the_lost_llama] run tag @s add llama_found
execute at @a run execute as @e[tag=llama_quest,tag=llama_found,distance=..4] at @s if entity @e[limit=1,distance=..4,tag=the_lost_llama] run tag @s remove llama_quest
execute at @a run execute as @e[tag=the_lost_llama,distance=..4] at @s if entity @e[limit=1,distance=..4,tag=llama_found] run tag @s remove the_lost_llama

#给予感谢和奖励并消除所有标签
execute as @a at @s run execute as @e[tag=llama_found,distance=..4] at @s run teleport @s ^ ^ ^ facing entity @a[limit=1,sort=nearest]
execute as @a at @s run execute if entity @e[tag=llama_found,distance=..4] run tellraw @s {"keybind":"lost.llama.quest.thanks"}
execute as @a at @s run execute as @e[tag=llama_found,distance=..4] at @s run playsound minecraft:entity.wandering_trader.yes ambient @a[distance=..4] ~ ~ ~ 100

execute as @a at @s if entity @e[tag=llama_found,distance=..4] run title @s times 20 100 20
execute as @a at @s if entity @e[tag=llama_found,distance=..4] run title @s subtitle {"keybind":"lost.llama.quest.finish"}
execute as @a at @s if entity @e[tag=llama_found,distance=..4] run title @s title {"text":""}

execute as @a at @s run execute if entity @e[tag=llama_found,distance=..4] run xp add @s 1 levels
execute as @a at @s run execute if entity @e[tag=llama_found,distance=..4] run give @s emerald 32
execute as @a at @s run execute as @e[tag=llama_found,distance=..4] run tag @s remove llama_found