#!/usr/bin/env python3
"""Second mechanical pass over already-ported files (in place, idempotent): 1.20.5+ calls with a 1.19.2 shape.

    tools/port_fixups.py <file>...
"""
import re
import sys

RULES = [
    (r'import net\.minecraft\.core\.particles\.ColorParticleOption;', 'import net.satisfy.foundation.particle.ColorParticleOption;'),
    (r'(\w+)\.hurtAndBreak\((\d+),\s*(\w+),\s*LivingEntity\.getSlotForHand\((\w+)\)\)', r'PortUtil.hurt(\1, \2, \3, \4)'),
    (r'(\w+)\.hurtAndBreak\((\d+),\s*(\w+),\s*(EquipmentSlot\.\w+)\)', r'PortUtil.hurt(\1, \2, \3, \4)'),
    (r'(\w+)\.consume\((\d+),\s*(\w+)\)', r'PortUtil.consume(\1, \2, \3)'),
    (r'(\w+)\.is\(ItemTags\.SHOVELS\)', r'PortUtil.isShovel(\1)'),
    (r'\.canBeReplaced\(\)', '.getMaterial().isReplaceable()'),
    (r'(\w+)\.isSolid\(\)', r'\1.getMaterial().isSolid()'),
    (r'isValidBonemealTarget\(LevelReader (\w+), BlockPos (\w+), BlockState (\w+)\)',
     r'isValidBonemealTarget(net.minecraft.world.level.BlockGetter \1, BlockPos \2, BlockState \3, boolean isClient)'),
    (r'isPathfindable\(BlockState (\w+), PathComputationType (\w+)\)',
     r'isPathfindable(BlockState \1, net.minecraft.world.level.BlockGetter getter, BlockPos pos, PathComputationType \2)'),
    (r'level\.damageSources\(\)\.campfire\(\)', 'net.minecraft.world.damagesource.DamageSource.IN_FIRE'),
    (r'(\w+)\.damageSources\(\)\.generic\(\)', 'net.minecraft.world.damagesource.DamageSource.GENERIC'),
    (r'(\w+)\.damageSources\(\)\.magic\(\)', 'net.minecraft.world.damagesource.DamageSource.MAGIC'),
    (r'MobEffectRegistry\.getHolder\(', 'MobEffectRegistry.get('),
    # recipes: 1.19.2 has no RecipeHolder (the recipe is its own holder) and no registry-access result
    (r'\.map\((\w+) -> \1\.value\(\)\)', ''),
    (r'\.map\(RecipeHolder::value\)', ''),
    (r'(\w+)\.value\(\)\.(getIngredients|getResultItem|getType|assemble|getCookingTime|getId)\(', r'\1.\2('),
    (r'getResultItem\([\w.]+(\(\))?(\.\w+\(\))*\)', 'getResultItem()'),
    (r'RecipeHolder<(\w+)>', r'\1'),
    (r'RecipeHolder<\?>', 'net.minecraft.world.item.crafting.Recipe<?>'),
    (r'Math\.ceilDiv\(([^,]+), ([^;]+?)\)\);', r'Math.floorDiv(\1 + \2) - 1, \2));'),
    (r'import net\.minecraft\.world\.item\.crafting\.RecipeHolder;\n', ''),
    (r'saveAdditional\((\w+),\s*provider\)', r'saveAdditional(\1)'),
    (r'saveCustomOnly\(\w+\)', 'saveWithoutMetadata()'),
    (r'\.registryAccess\(\)\)\.copy\(\)', ').copy()'),
    (r'import dev\.architectury\.registry\.registries\.RegistrySupplier;', 'import net.minecraftforge.registries.RegistryObject;'),
    (r'\bRegistrySupplier<', 'RegistryObject<'),
    (r'import dev\.architectury\.injectables\.annotations\.ExpectPlatform;\n', ''),
    (r'@Environment\(EnvType\.CLIENT\)\s*\n', ''),
    (r'import net\.fabricmc\.api\.(EnvType|Environment);\n', ''),
    (r'@Environment\(value\s*=\s*EnvType\.CLIENT\)\s*\n', ''),
    (r'import net\.minecraft\.world\.item\.ItemDisplayContext;', 'import net.minecraft.client.renderer.block.model.ItemTransforms;'),
    (r'ItemDisplayContext\.(\w+)', r'ItemTransforms.TransformType.\1'),
    (r'renderStatic\((\w+), (ItemTransforms\.TransformType\.\w+), (\w+), (\w+), (\w+), (\w+), [\w.()]+, ([^;]+)\);', r'renderStatic(\1, \2, \3, \4, \5, \6, \7);'),
]


def main():
    for path in sys.argv[1:]:
        with open(path, encoding='utf-8') as f:
            text = f.read()
        new = text
        for pattern, repl in RULES:
            new = re.sub(pattern, repl, new)
        if 'PortUtil.' in new and 'import net.satisfy.vinery.core.util.PortUtil;' not in new and 'package net.satisfy.vinery.core.util;' not in new:
            new = re.sub(r'(package [\w.]+;\n)', r'\1\nimport net.satisfy.vinery.core.util.PortUtil;', new, count=1)
        if new != text:
            with open(path, 'w', encoding='utf-8') as f:
                f.write(new)
            print('fixed', path)


if __name__ == '__main__':
    main()
