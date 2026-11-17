# generates tools/scratch_query/batches/r1115/p3_a1r.qb: roofed rerun of SQ-MELT-01, -02, -03, -06 part 1, SALT2-06 (overworld)
# p3_a1.qb's distance-4 controls melted too: Serene Seasons melts surface ice and snow outside winter (heightmap top,
# melting.md 6), so here every target gets a glass block right above it (heightmap top = the glass, light passes).
# Plus no-source controls, roofed and open, to show the other melt. Same layout as p3_a1.qb shifted +150 in x.
import sys
G = -61; OFF = 150
L = []
def c(s): L.append("> " + s)
def w(label, x, y, z): c(f"zmelt w {label} overworld {x+OFF} {y} {z}")
def blk(x, y, z, b): c(f"setblock {x+OFF} {y} {z} {b}")
def tgt(x, z, b, label, roof=True):
    blk(x, G, z, b)
    if roof: blk(x, G+1, z, "minecraft:glass")
    w(label, x, G, z)
def src(x, z, b, under=None):
    if under: blk(x, G-1, z, under)
    blk(x, G, z, b)
def ifb(label, x, y, z, b): c(f"execute if block {x+OFF} {y} {z} {b} run say {label}")
def row(i, t): L.append(""); L.append(f"## {i} | {t}")
L.append("""# r1115 phase 3 (runner-3), boot A1 (same server, after p3_a1.qb): roofed rerun (see tools/scratch_query/melt_gen_p3r.py).
# Bot m3r at 2210 -60 2060 keeps x 2140..2260 random-ticking.
pace 0.5""")
row("P3-R0", "setup: forceload the shifted area, bot m3r, build roofed cases, controls; randomTickSpeed 1000 120 s")
c("forceload add 2144 1984 2271 2143"); c("gamerule randomTickSpeed 3")
L.append("sleep 3")
c("pbot spawn m3r 2210 -60 2060 minecraft:overworld survival")
X, Z = 2000, 2000
tgt(X+1, Z, "minecraft:ice", "r01_ice"); tgt(X-1, Z, "minecraft:snow_block", "r01_snowblock")
tgt(X, Z+1, "minecraft:powder_snow", "r01_powder"); tgt(X, Z-1, "minecraft:snow[layers=3]", "r01_layer")
src(X, Z, "salt:salt_block")
X, Z = 2000, 2024
tgt(X+1, Z, "minecraft:ice", "r06_ow_ice"); tgt(X, Z+1, "minecraft:snow", "r06_ow_snow"); src(X, Z, "salt:salt_block")
cases02 = [("fire", "minecraft:fire", "minecraft:netherrack"), ("soulfire", "minecraft:soul_fire", "minecraft:soul_soil"),
           ("campfire", "minecraft:campfire[lit=true]", None), ("soulcampfire", "minecraft:soul_campfire[lit=true]", None),
           ("magma", "minecraft:magma_block", None)]
X = 2030
for k, (n, b, u) in enumerate(cases02):
    Z = 2000 + 12*k
    tgt(X+3, Z, "minecraft:ice", f"r02_{n}_ice3"); tgt(X-4, Z, "minecraft:ice", f"r02_{n}_ice4")
    tgt(X, Z+3, "minecraft:snow", f"r02_{n}_snow3"); tgt(X, Z-4, "minecraft:snow", f"r02_{n}_snow4")
    src(X, Z, b, u)
tgt(2031, 2060, "minecraft:ice", "r02_unlit_ice1"); src(2030, 2060, "minecraft:campfire[lit=false]")
X = 2060
tgt(X+4, 2000, "minecraft:ice", "r03_src_ice4"); tgt(X-5, 2000, "minecraft:ice", "r03_src_ice5"); src(X, 2000, "minecraft:lava")
tgt(X+6, 2012, "minecraft:ice", "r03_flow_ice4"); blk(X+3, G, 2012, "minecraft:glass"); blk(X+1, G, 2012, "minecraft:air"); blk(X+2, G, 2012, "minecraft:air")
src(X, 2012, "minecraft:lava")
tgt(X+4, 2024, "minecraft:ice", "r03_cauldron_ice4"); src(X, 2024, "minecraft:lava_cauldron")
X = 2090
cases06 = [("torch", "minecraft:torch"), ("soultorch", "minecraft:soul_torch"), ("lantern", "minecraft:lantern"),
           ("sconce", "supplementaries:sconce"), ("copperlantern", "everythingcopper:copper_lantern"), ("glowstone", "minecraft:glowstone")]
for k, (n, b) in enumerate(cases06):
    Z = 2000 + 12*k
    tgt(X+3, Z, "minecraft:ice", f"r06_{n}_ice3"); tgt(X-4, Z, "minecraft:ice", f"r06_{n}_ice4")
    tgt(X, Z+3, "minecraft:snow", f"r06_{n}_snow3"); tgt(X, Z-4, "minecraft:snow", f"r06_{n}_snow4")
    src(X, Z, b)
neg = [("sealantern", "minecraft:sea_lantern"), ("redstonetorch", "minecraft:redstone_torch"),
       ("jellytorch", "upgrade_aquatic:blue_jelly_torch"), ("brazierunlit", "decorative_blocks:brazier[lit=false]")]
for k, (n, b) in enumerate(neg):
    Z = 2072 + 12*k
    tgt(X+1, Z, "minecraft:ice", f"r06_{n}_ice1"); src(X, Z, b)
# controls, no source within 10: roofed and open ice and snow layer
tgt(2060, 2090, "minecraft:ice", "rc_ice_roof"); tgt(2064, 2090, "minecraft:snow", "rc_snow_roof")
tgt(2060, 2100, "minecraft:ice", "rc_ice_open", False); tgt(2064, 2100, "minecraft:snow", "rc_snow_open", False)
L.append(r"reject (Unknown (item|block|block type|entity)|Incorrect argument for command|Unknown or incomplete command|zmelt\] error)")
c("gamerule randomTickSpeed 1000")
L.append("sleep 120")
c("gamerule randomTickSpeed 3")
c("zmelt show"); L.append(r"waitlog \[zmelt\] show done 30")
L.append("look start states; controls: open ones may melt (Serene Seasons), roofed ones must not")
def judge(rid, checks, lk=""):
    row(rid, "roofed rerun")
    for lab, x, y, z, b in checks: ifb(f"{rid}-{lab}", x, y, z, b)
    for lab, *_ in checks: L.append(f"expect {rid}-{lab}$")
    if lk: L.append("look " + lk)
judge("SQ-MELT-01r", [("ice_water", 2001, G, 2000, "minecraft:water[level=0]"), ("snowblock_air", 1999, G, 2000, "minecraft:air"),
    ("powder_air", 2000, G, 2001, "minecraft:air"), ("layer_air", 2000, G, 1999, "minecraft:air"), ("salt_kept", 2000, G, 2000, "salt:salt_block")])
judge("SQ-SALT2-06r", [("ow_ice_water", 2001, G, 2024, "minecraft:water[level=0]"), ("ow_snow_air", 2000, G, 2025, "minecraft:air")])
ch = []
for k, (n, *_ ) in enumerate(cases02):
    Z = 2000 + 12*k
    ch += [(f"{n}_ice3_water", 2033, G, Z, "minecraft:water"), (f"{n}_ice4_ice", 2026, G, Z, "minecraft:ice"),
           (f"{n}_snow3_air", 2030, G, Z+3, "minecraft:air"), (f"{n}_snow4_snow", 2030, G, Z-4, "minecraft:snow")]
ch.append(("unlit_ice1_ice", 2031, G, 2060, "minecraft:ice"))
judge("SQ-MELT-02r", ch)
judge("SQ-MELT-03r", [("src_ice4_water", 2064, G, 2000, "minecraft:water"), ("src_ice5_ice", 2055, G, 2000, "minecraft:ice"),
    ("flow_ice4_water", 2066, G, 2012, "minecraft:water"), ("cauldron_ice4_water", 2064, G, 2024, "minecraft:water")])
ch = []
for k, (n, b) in enumerate(cases06):
    Z = 2000 + 12*k
    ch += [(f"{n}_ice3_water", 2093, G, Z, "minecraft:water"), (f"{n}_ice4_ice", 2086, G, Z, "minecraft:ice"),
           (f"{n}_snow3_air", 2090, G, Z+3, "minecraft:air"), (f"{n}_snow4_snow", 2090, G, Z-4, "minecraft:snow")]
for k, (n, b) in enumerate(neg): ch.append((f"{n}_ice1_ice", 2091, G, 2072 + 12*k, "minecraft:ice"))
judge("SQ-MELT-06r", ch)
row("P3-RC", "controls: roofed kept; open recorded")
ifb("P3-RC-ice_roof_kept", 2060, G, 2090, "minecraft:ice"); ifb("P3-RC-snow_roof_kept", 2064, G, 2090, "minecraft:snow")
ifb("P3-RC-ice_open_kept", 2060, G, 2100, "minecraft:ice"); ifb("P3-RC-snow_open_kept", 2064, G, 2100, "minecraft:snow")
L.append("expect P3-RC-ice_roof_kept$"); L.append("expect P3-RC-snow_roof_kept$")
L.append("""shell grep -hE "\\[zmelt\\] change r" logs/latest.log | sed -E 's/^.*\\[zmelt\\]/[zmelt]/'""")
L.append("look open controls melted = the non-salt, non-fire path (Serene Seasons)")
open(sys.argv[1], "w").write("\n".join(L) + "\n"); print(len(L))
