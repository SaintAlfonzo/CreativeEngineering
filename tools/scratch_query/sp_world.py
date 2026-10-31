#!/usr/bin/env python3
"""Copies a scratch server world into a throwaway client's saves with cheats on, for the client block of a bulk run.

    sp_world.py SRC_WORLD GAME_DIR NAME     SRC_WORLD = OUT/srv/<level-name>; result GAME_DIR/saves/NAME

A dedicated server writes allowCommands:0b into level.dat, so typed commands would be refused in single player.
The byte tag is patched in place in the gunzipped file (TAG_Byte, name "allowCommands"); session.lock is dropped.
Run only while neither the server nor the client is up.
"""
import gzip
import os
import shutil
import struct
import sys

TAG = b"\x01" + struct.pack(">H", len(b"allowCommands")) + b"allowCommands"


def main():
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    src, game, name = sys.argv[1:]
    if not os.path.isfile(os.path.join(src, "level.dat")):
        sys.exit(f"no level.dat in {src}")
    dst = os.path.join(game, "saves", name)
    if os.path.exists(dst):
        shutil.rmtree(dst)
    shutil.copytree(src, dst, ignore=shutil.ignore_patterns("session.lock"))
    p = os.path.join(dst, "level.dat")
    data = bytearray(gzip.decompress(open(p, "rb").read()))
    i = data.find(TAG)
    if i < 0:
        sys.exit("allowCommands tag not found in level.dat")
    data[i + len(TAG)] = 1
    with open(p, "wb") as f:
        f.write(gzip.compress(bytes(data)))
    print(f"{dst}: allowCommands = 1")


if __name__ == "__main__":
    main()
