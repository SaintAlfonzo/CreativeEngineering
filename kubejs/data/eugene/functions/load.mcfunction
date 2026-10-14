#骨髓爵士
bossbar add ser_marrow [{"keybind":"bossbar.ser.marrow"}]
bossbar set ser_marrow max 150
bossbar set ser_marrow style progress
bossbar set ser_marrow color white
scoreboard objectives add ser_marrow_hp dummy

function eugene:summon_skeleton


#猪灵游侠
team add ranger

bossbar add pig_ranger [{"keybind":"bossbar.pig.ranger"}]
bossbar set pig_ranger max 150
bossbar set pig_ranger style progress
bossbar set pig_ranger color red
scoreboard objectives add pig_ranger_hp dummy

function eugene:loop_120s

function eugene:load_firework