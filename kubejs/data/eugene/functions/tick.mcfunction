execute if entity @e[type=wandering_trader,tag=fake] run function eugene:tick_fake_trader

execute if entity @e[tag=ser_marrow] run function eugene:tick_ser_marrow
execute unless entity @e[tag=ser_marrow] run bossbar set ser_marrow visible false

execute if entity @e[tag=pig_ranger] run function eugene:tick_pig_ranger
execute unless entity @e[tag=pig_ranger] run bossbar set pig_ranger visible false

# #Trader's Lost Llama
execute as @e[type=wandering_trader] run function eugene:tick_lost_llama_trader