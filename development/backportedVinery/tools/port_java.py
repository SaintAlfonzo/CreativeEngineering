#!/usr/bin/env python3
"""First-pass copy of upstream 1.21.1 Java sources into the 1.19.2 port, with the mechanical API rewrites.

    tools/port_java.py <upstream root> <relative path>...   (run from the backportedVinery folder)

<upstream root> is a source tree such as ../../DecompiledMods/vinery-1.5.5-src. Each <relative path> (for example
net/satisfy/vinery/core/block/TableBlock.java) is copied to src/main/java/<relative path> ONLY IF the port doesn't have
that file yet, so hand edits are never overwritten. The result is a starting point, not a finished port: every file is
then fixed by hand against the compiler (1.19.2 APIs that differ in shape, not just in name).
"""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(os.path.dirname(HERE), 'src', 'main', 'java')

# (pattern, replacement) applied in order
RULES = [
    (r'ResourceLocation\.fromNamespaceAndPath\(', 'new ResourceLocation('),
    (r'ResourceLocation\.withDefaultNamespace\(', 'new ResourceLocation('),
    (r'ResourceLocation\.parse\(', 'new ResourceLocation('),
    (r'Properties\.ofFullCopy\(', 'Properties.copy('),
    (r'\bAxis\.(XP|XN|YP|YN|ZP|ZN)\b', r'Vector3f.\1'),
    (r'import com\.mojang\.math\.Axis;', 'import com.mojang.math.Vector3f;'),
    (r'FastColor\.ARGB32\.opaque\(([^()]*(?:\([^()]*\)[^()]*)*)\)', r'(0xFF000000 | (\1))'),
    (r'isSameItemSameComponents', 'isSameItemSameTags'),
    # block entity NBT
    (r'(public|protected) void loadAdditional\(CompoundTag (\w+),\s*HolderLookup\.Provider \w+\)', r'public void load(CompoundTag \2)'),
    (r'super\.loadAdditional\((\w+),\s*\w+\)', r'super.load(\1)'),
    (r'(public|protected) void saveAdditional\(CompoundTag (\w+),\s*HolderLookup\.Provider \w+\)', r'protected void saveAdditional(CompoundTag \2)'),
    (r'super\.saveAdditional\((\w+),\s*\w+\)', r'super.saveAdditional(\1)'),
    (r'ContainerHelper\.(loadAllItems|saveAllItems)\((\w+),\s*([\w.]+),\s*\w+\)', r'ContainerHelper.\1(\2, \3)'),
    (r'getUpdateTag\(HolderLookup\.Provider \w+\)', 'getUpdateTag()'),
    (r'saveWithoutMetadata\(\w+\)', 'saveWithoutMetadata()'),
    # entities / players: level() accessor is a field in 1.19.2
    (r'\.level\(\)', '.level'),
    (r'\bthis\.level\(\)', 'this.level'),
    # block methods are public in 1.19.2
    (r'protected (@NotNull )?(\w[\w<>?, ]*) (useItemOn|useWithoutItem|onRemove|tick|randomTick|isRandomlyTicking|getShape|getCollisionShape|getRenderShape|updateShape|canSurvive|getFluidState|rotate|mirror|onProjectileHit|isPathfindable|getBlockSupportShape|hasAnalogOutputSignal|getAnalogOutputSignal|getMenuProvider|propagatesSkylightDown|entityInside|onPlace|neighborChanged|getLightBlock|playerWillDestroy|attack|getDrops|skipRendering|getVisualShape)\(',
     r'public \1\2 \3('),
    (r'import net\.minecraft\.world\.ItemInteractionResult;', 'import net.satisfy.foundation.util.ItemInteractionResult;'),
    (r'import net\.minecraft\.core\.registries\.Registries;', 'import net.minecraft.core.Registry;'),
    (r'\bRegistries\.(ITEM|BLOCK|ENTITY_TYPE|BIOME|MOB_EFFECT|RECIPE_TYPE)\b', r'Registry.\1_REGISTRY'),
    (r'BuiltInRegistries\.ITEM', 'Registry.ITEM'),
    (r'BuiltInRegistries\.BLOCK', 'Registry.BLOCK'),
    (r'BuiltInRegistries\.MOB_EFFECT', 'Registry.MOB_EFFECT'),
    (r'BuiltInRegistries\.ENTITY_TYPE', 'Registry.ENTITY_TYPE'),
    (r'import net\.minecraft\.core\.registries\.BuiltInRegistries;', 'import net.minecraft.core.Registry;'),
]

# whole methods that only exist for 1.20.5+ codecs
DROP_METHODS = [
    re.compile(r'\n\s*@Override\s*\n\s*protected (@NotNull )?MapCodec<[^>]*(?:<[^>]*>)?[^>]*> codec\(\) \{[^}]*\}\n'),
]


def convert(text):
    for pattern, repl in RULES:
        text = re.sub(pattern, repl, text)
    for drop in DROP_METHODS:
        text = drop.sub('\n', text)
    return text


def main():
    root = sys.argv[1]
    for rel in sys.argv[2:]:
        dst = os.path.join(OUT, rel)
        if os.path.exists(dst):
            print(f'skip (exists): {rel}')
            continue
        src = os.path.join(root, rel)
        with open(src, encoding='utf-8') as f:
            text = convert(f.read())
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        with open(dst, 'w', encoding='utf-8') as f:
            f.write(text)
        print(f'ported: {rel}')


if __name__ == '__main__':
    main()
