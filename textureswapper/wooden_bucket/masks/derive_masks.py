# Derives the paint masks for the filled wooden buckets (plan_wood_bucket_batch_1121 P0, R9 agent-chosen masks).
# Masks are selections only (white, alpha 255 where selected); no texture pixels are made here.
#   <name>_fill.png        pixels where src/<bucket>.png differs from the empty vanilla src/bucket.png (the fill
#                          and its drips; H&H and Create buckets share the vanilla body), minus neutral grays
#                          (r == g == b, value < 0.7): those are the iron rim and body shading next to the fill
#   nut_milk_liquid.png    nut_milk_bottle.png pixels below the cork (y >= 6) that are not the blue glass
#                          (hue 0.5..0.7 and saturation > 0.1 = glass)
# Run from textureswapper/wooden_bucket: python3 masks/derive_masks.py
import colorsys
import numpy as np
from PIL import Image

def load(p):
	return np.array(Image.open(p).convert('RGBA')).astype(int)

def save(sel, p):
	out = np.zeros(sel.shape + (4,), np.uint8)
	out[sel] = (255, 255, 255, 255)
	Image.fromarray(out, 'RGBA').save(p)

empty = load('src/bucket.png')
for name, src in [('water', 'water_bucket'), ('milk', 'milk_bucket'), ('goat_milk', 'goat_milk_bucket'),
				  ('sap', 'sap_bucket'), ('honey', 'create_honey_bucket')]:
	a = load('src/%s.png' % src)
	grey = (a[:, :, 0] == a[:, :, 1]) & (a[:, :, 1] == a[:, :, 2]) & (a[:, :, 0] < 0.7 * 255)
	save((np.abs(a - empty).sum(2) > 0) & ~grey, 'masks/%s_fill.png' % name)

b = load('src/nut_milk_bottle.png')
sel = np.zeros(b.shape[:2], bool)
for y in range(6, 16):
	for x in range(16):
		r, g, bl, a = b[y, x]
		h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, bl / 255)
		sel[y, x] = a > 0 and not (0.5 <= h <= 0.7 and s > 0.1)
save(sel, 'masks/nut_milk_liquid.png')
