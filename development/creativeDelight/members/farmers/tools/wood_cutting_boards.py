# Usage: python3 tools/wood_cutting_boards.py <FD cutting_board.png> <dir with vanilla *_planks.png> src/main/resources/assets/farmersdelight/textures/block [preview.png]
# Wood cutting boards (TODO cooking/kitchen #38): Farmer's Delight's board texture (a spruce ramp of 5 shades) recoloured
# per wood. Each board shade is replaced by the wood's plank palette (sorted by brightness) sampled at the same rank, so
# the board's seams, rim and handle hole stay and the colours are the plank's own.
import sys
from PIL import Image

WOODS = ['oak', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'crimson', 'warped']
# Where each of the board's 5 shades (darkest first) samples the plank palette (0 = darkest plank colour, 1 = brightest).
# The board is drawn a little lighter than spruce planks, so its shades sit in the upper part of the ramp.
POS = [0.2, 0.42, 0.6, 0.8, 1.0]

lum = lambda c: 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def palette(path):
    im = Image.open(path).convert('RGBA')
    return sorted({p[:3] for p in im.getdata() if p[3]}, key=lum)


def sample(pal, t):
    x = t * (len(pal) - 1)
    i = min(int(x), len(pal) - 2)
    f = x - i
    return tuple(round(a + (b - a) * f) for a, b in zip(pal[i], pal[i + 1]))


def main():
    board_p, planks_dir, out_dir = sys.argv[1:4]
    preview = sys.argv[4] if len(sys.argv) > 4 else None
    board = Image.open(board_p).convert('RGBA')
    shades = palette(board_p)
    if len(shades) != len(POS):
        sys.exit('expected %d board shades, found %d' % (len(POS), len(shades)))
    outs = []
    for wood in WOODS:
        pal = palette('%s/%s_planks.png' % (planks_dir, wood))
        swap = {s: sample(pal, t) for s, t in zip(shades, POS)}
        out = Image.new('RGBA', board.size, (0, 0, 0, 0))
        src, dst = board.load(), out.load()
        for y in range(board.size[1]):
            for x in range(board.size[0]):
                p = src[x, y]
                if p[3]:
                    dst[x, y] = swap[p[:3]] + (p[3],)
        out.save('%s/%s_cutting_board.png' % (out_dir, wood))
        outs.append(out)
    if preview:
        sheet = Image.new('RGBA', (16 * (len(outs) + 1), 16), (40, 40, 40, 255))
        sheet.paste(board, (0, 0))
        for n, o in enumerate(outs):
            sheet.paste(o, (16 * (n + 1), 0), o)
        sheet.resize((sheet.width * 8, sheet.height * 8), Image.NEAREST).save(preview)


if __name__ == '__main__':
    main()
