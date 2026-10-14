# 1.19.2 port (2026-10-14): the steed comes with Ser Marrow on it (mount_ser_marrow; no /ride before 1.19.4)
execute as @e[tag=ser_marrow,scores={ser_marrow_hp=49..150}] at @s unless entity @e[tag=marrow_steed,distance=..1] run function eugene:mount_ser_marrow

#召唤小兵
execute at @e[tag=ser_marrow,scores={ser_marrow_hp=..49}] unless entity @e[type=skeleton,tag=!ser_marrow,distance=..6] run summon skeleton ^ ^ ^2.5

#粒子
execute at @e[tag=ser_marrow,scores={ser_marrow_hp=..49}] run particle flame ~0.8 ~2.3 ~0.7
execute at @e[tag=ser_marrow,scores={ser_marrow_hp=..49}] run particle flame ~0.8 ~2.4 ~0.6

execute at @e[tag=ser_marrow,scores={ser_marrow_hp=..49}] run particle smoke ~0.7 ~2.5 ~0.6
execute at @e[tag=ser_marrow,scores={ser_marrow_hp=..49}] run particle smoke ~0.4 ~2.4 ~0.9

schedule function eugene:summon_skeleton 6s