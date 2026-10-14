#生成猪灵游侠
execute at @r[predicate=eugene:ranger_spawn] unless entity @e[tag=pig_ranger,distance=..128] at @e[type=zombified_piglin,limit=1,sort=random,distance=64..86] run function eugene:summon_pig_ranger

schedule function eugene:loop_120s 120s