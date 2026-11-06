# Usage: python3 tools/stone_cutting_boards.py <FD cutting_board.png> <dir with vanilla polished_*.png> src/main/resources/assets/farmersdelight/textures/block
# Stone cutting boards (TODO cooking/kitchen #25): the polished stone's inside (its 1 px block edge left out) painted
# onto Farmer's Delight's cutting board texture, keeping the board's own shading (seams, rim, handle hole).
import sys
from PIL import Image
board_p, stone_dir, out_dir = sys.argv[1:4]
board = Image.open(board_p).convert('RGBA')
bp = board.load()
lum = lambda p: 0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2]
opaque = [(x, y) for y in range(16) for x in range(16) if bp[x, y][3] > 0]
mean = sum(lum(bp[x, y]) for x, y in opaque) / len(opaque)
for stone in ['granite', 'andesite', 'diorite']:
    st = Image.open('%s/polished_%s.png' % (stone_dir, stone)).convert('RGBA').load()
    out = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    op = out.load()
    for x, y in opaque:
        sx, sy = min(14, max(1, x)), min(14, max(1, y))   # inside of the polished block, no edge
        f = lum(bp[x, y]) / mean
        f = 1 + (f - 1) * 0.8                               # keep the board's seams, a little softer on stone
        s = st[sx, sy]
        op[x, y] = tuple(max(0, min(255, round(c * f))) for c in s[:3]) + (bp[x, y][3],)
    out.save('%s/%s_cutting_board.png' % (out_dir, stone))
