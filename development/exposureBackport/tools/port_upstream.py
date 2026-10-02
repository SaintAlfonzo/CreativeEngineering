#!/usr/bin/env python3
"""Copy decompiled upstream (1.21.1 NeoForge) sources into a source set and apply the mechanical 1.21 -> 1.19.2
rewrites. Only files that don't exist in the destination yet are written, so hand-ported files are never touched.

    tools/port_upstream.py <upstream-src-root> <dest-java-root> [--exclude glob ...] [--dry-run] [--force FILE ...]

<upstream-src-root> is a DecompiledMods/<name>-src folder (the one holding io/ or dev/). Paths with a `neoforge`
package segment are written to `forge` instead (README: Upstream -> port conventions).

What it rewrites (everything else is ported by hand, file by file, against the compiler):
  - 1.20.5+ network codecs -> io.github.mortuusars.exposure.backport.codec (same API)
  - CustomPacketPayload -> backport.network.CustomPacketPayload; RegistryFriendlyByteBuf -> FriendlyByteBuf
  - vanilla X.STREAM_CODEC constants -> ByteBufCodecs.X
  - DataComponentType -> backport.component.DataComponentType; stack.get(TYPE) -> TYPE.get(stack) etc.
  - ResourceLocation factories -> constructors
  - NeoForge packages -> Forge packages (only the 1:1 ones), ModConfigSpec -> ForgeConfigSpec
  - Java 21 List.getFirst() -> get(0)
"""
import argparse
import fnmatch
import os
import re
import sys

BP = 'io.github.mortuusars.exposure.backport'

IMPORT_MAP = {
    'net.minecraft.network.codec.StreamCodec': f'{BP}.codec.StreamCodec',
    'net.minecraft.network.codec.ByteBufCodecs': f'{BP}.codec.ByteBufCodecs',
    'net.minecraft.network.codec.StreamDecoder': f'{BP}.codec.StreamDecoder',
    'net.minecraft.network.codec.StreamEncoder': f'{BP}.codec.StreamEncoder',
    'net.minecraft.network.codec.StreamMemberEncoder': f'{BP}.codec.StreamMemberEncoder',
    'net.minecraft.network.protocol.common.custom.CustomPacketPayload': f'{BP}.network.CustomPacketPayload',
    'net.minecraft.core.component.DataComponentType': f'{BP}.component.DataComponentType',
    'net.minecraft.core.component.DataComponents': f'{BP}.component.DataComponents',
    'net.minecraft.world.item.component.CustomData': f'{BP}.component.CustomData',
    'org.joml.Vector2i': f'{BP}.util.Vector2i',
    'org.joml.Matrix4f': 'com.mojang.math.Matrix4f',
    'org.joml.Matrix3f': 'com.mojang.math.Matrix3f',
    'org.joml.Vector3f': 'com.mojang.math.Vector3f',
    'org.joml.Vector4f': 'com.mojang.math.Vector4f',
    'org.joml.Quaternionf': 'com.mojang.math.Quaternion',
    'com.mojang.math.Axis': 'com.mojang.math.Vector3f',
    'org.apache.commons.lang3.function.TriConsumer': f'{BP}.util.TriConsumer',
    'net.minecraft.util.ByIdMap': f'{BP}.util.ByIdMap',
    'net.minecraft.client.DeltaTracker': f'{BP}.client.DeltaTracker',
    # 1.20 widget API -> backport.gui classes with the same simple names (screens: see EXTENDS_MAP)
    'net.minecraft.client.gui.components.AbstractWidget': f'{BP}.gui.AbstractWidget',
    'net.minecraft.client.gui.components.AbstractButton': f'{BP}.gui.AbstractButton',
    'net.minecraft.client.gui.components.Button': f'{BP}.gui.Button',
    'net.minecraft.client.gui.components.Button.OnPress': f'{BP}.gui.Button.OnPress',
    'net.minecraft.client.gui.components.ImageButton': f'{BP}.gui.ImageButton',
    'net.minecraft.client.gui.components.WidgetSprites': f'{BP}.gui.WidgetSprites',
    'net.minecraft.client.gui.components.Tooltip': f'{BP}.gui.Tooltip',
    'net.minecraft.client.gui.components.ImageWidget': f'{BP}.gui.ImageWidget',
    'net.minecraft.client.gui.components.MultiLineTextWidget': f'{BP}.gui.MultiLineTextWidget',
    'net.minecraft.client.gui.components.Checkbox': f'{BP}.gui.Checkbox',
    'net.minecraft.client.gui.components.Renderable': f'{BP}.gui.Renderable',
    'net.minecraft.client.gui.navigation.CommonInputs': f'{BP}.gui.CommonInputs',
    'net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner': f'{BP}.gui.DefaultTooltipPositioner',
    'net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil': f'{BP}.gui.TooltipRenderUtil',
    'net.minecraft.client.gui.GuiGraphics': f'{BP}.gui.GuiGraphics',
    'net.neoforged.neoforge.common.ModConfigSpec': 'net.minecraftforge.common.ForgeConfigSpec',
    'net.neoforged.neoforge.common.NeoForge': 'net.minecraftforge.common.MinecraftForge',
}

PACKAGE_MAP = [
    ('net.neoforged.api.distmarker', 'net.minecraftforge.api.distmarker'),
    ('net.neoforged.bus.api', 'net.minecraftforge.eventbus.api'),
    ('net.neoforged.fml', 'net.minecraftforge.fml'),
]

STREAM_CODEC_CONSTANTS = {
    'ResourceLocation.STREAM_CODEC': 'RESOURCE_LOCATION',
    'UUIDUtil.STREAM_CODEC': 'UUID',
    'ItemStack.OPTIONAL_STREAM_CODEC': 'ITEM_STACK',
    'ItemStack.STREAM_CODEC': 'ITEM_STACK',
    'BlockPos.STREAM_CODEC': 'BLOCK_POS',
    'ComponentSerialization.STREAM_CODEC': 'COMPONENT',
    'SoundEvent.DIRECT_STREAM_CODEC': 'SOUND_EVENT',
    'SoundEvent.STREAM_CODEC': 'SOUND_EVENT',
    'ComponentSerialization.TRUSTED_STREAM_CODEC': 'COMPONENT',
    'Tag.STREAM_CODEC': 'TAG',
}

# vanilla codecs 1.19.2 doesn't have -> BackportCodecs
CODEC_CONSTANTS = {
    'ItemPredicate.CODEC': 'ITEM_PREDICATE',
    'LocationPredicate.CODEC': 'LOCATION_PREDICATE',
    'MinMaxBounds.Ints.CODEC': 'INTS',
    'Ints.CODEC': 'INTS',
    'MinMaxBounds.Doubles.CODEC': 'DOUBLES',
    'ComponentSerialization.CODEC': 'COMPONENT',
    'ComponentSerialization.FLAT_CODEC': 'COMPONENT',
    'Tag.CODEC': 'TAG',
    'ExtraCodecs.JSON': 'JSON',
}

# vanilla registries whose 1.19.2 key is Registry.<NAME>_REGISTRY (Registries.<NAME> in 1.19.3+)
REGISTRY_KEYS = ['ITEM', 'BLOCK', 'ENTITY_TYPE', 'SOUND_EVENT', 'RECIPE_TYPE', 'RECIPE_SERIALIZER', 'PARTICLE_TYPE',
                 'MENU', 'FEATURE', 'CUSTOM_STAT', 'COMMAND_ARGUMENT_TYPE', 'BLOCK_ENTITY_TYPE', 'STRUCTURE', 'BIOME',
                 'DIMENSION', 'MOB_EFFECT', 'POTION', 'ENCHANTMENT', 'FLUID']
BUILTIN_REGISTRIES = ['ITEM', 'BLOCK', 'ENTITY_TYPE', 'SOUND_EVENT', 'RECIPE_TYPE', 'RECIPE_SERIALIZER', 'PARTICLE_TYPE',
                      'MENU', 'FEATURE', 'CUSTOM_STAT', 'BLOCK_ENTITY_TYPE', 'MOB_EFFECT', 'POTION', 'ENCHANTMENT', 'FLUID']

# receiver of a component accessor: a dotted chain of identifiers / no-arg or simple-arg calls
RECEIVER = r'([A-Za-z_$][\w$]*(?:\([\w$.]*\))?(?:\.[A-Za-z_$][\w$]*(?:\([\w$.]*\))?)*)'
COMPONENT_TYPE = r'((?:[A-Z]\w*\.)?DataComponents\.[A-Z][A-Z0-9_]*|this\.component)'
ACCESSORS = 'get|set|has|remove|getOrDefault|update'


def add_import(src, fqcn):
    if re.search(rf'^import {re.escape(fqcn)};', src, re.M):
        return src
    m = list(re.finditer(r'^import [^;]+;\n', src, re.M))
    line = f'import {fqcn};\n'
    if m:
        return src[:m[-1].end()] + line + src[m[-1].end():]
    pkg = re.search(r'^package [^;]+;\n', src, re.M)
    return src[:pkg.end()] + '\n' + line + src[pkg.end():]


def receiver_start(src, end):
    """Index where the receiver expression ending at `end` (exclusive) starts: walks back over identifiers, dots,
    call argument lists and [...] indexes. A (...) group not preceded by an identifier is a cast (stop before it)
    unless it is the receiver itself, as in ((Pair)x).y()."""
    i = end
    while i > 0:
        c = src[i - 1]
        if c in ')]':
            close, open_ = c, '(' if c == ')' else '['
            depth = 0
            j = i
            while j > 0:
                j -= 1
                if src[j] == close:
                    depth += 1
                elif src[j] == open_:
                    depth -= 1
                    if depth == 0:
                        break
            if c == ']' or (j > 0 and (src[j - 1].isalnum() or src[j - 1] in '_$>')):
                i = j
                continue
            if i == end or src[i] in '.[':
                i = j
            break
        if c.isalnum() or c in '_$.':
            i -= 1
            continue
        if c in ' \t\n' and i < len(src) and src[i] == '.':
            # whitespace inside a call chain split over lines: `Codec.DOUBLE\n      .validate(...)`
            while i > 0 and src[i - 1] in ' \t\n':
                i -= 1
            continue
        break
    return i


def wrap_receivers_with_args(src, method, wrapper):
    """recv.method(args) -> wrapper(recv, args)"""
    needle = '.' + method + '('
    pos = 0
    while True:
        k = src.find(needle, pos)
        if k < 0:
            return src
        start = receiver_start(src, k)
        recv = src[start:k].rstrip()
        if not recv or recv == wrapper.rsplit('.', 1)[0]:
            pos = k + len(needle)
            continue
        src = src[:start] + wrapper + '(' + recv + ', ' + src[k + len(needle):]
        pos = start + len(wrapper) + 1


def wrap_receivers(src, method, wrapper):
    """recv.method() -> wrapper(recv)"""
    needle = '.' + method + '()'
    while True:
        k = src.find(needle)
        if k < 0:
            return src
        start = receiver_start(src, k)
        src = src[:start] + wrapper + '(' + src[start:k] + ')' + src[k + len(needle):]


def transform(src):
    for old, new in IMPORT_MAP.items():
        src = re.sub(rf'\b{re.escape(old)}\b', new, src)
    for old, new in PACKAGE_MAP:
        src = src.replace(old + '.', new + '.')
    src = re.sub(r'\bModConfigSpec\b', 'ForgeConfigSpec', src)
    src = re.sub(r'\bNeoForge\.EVENT_BUS\b', 'MinecraftForge.EVENT_BUS', src)

    # RegistryFriendlyByteBuf -> FriendlyByteBuf
    if 'RegistryFriendlyByteBuf' in src:
        src = re.sub(r'^import net\.minecraft\.network\.RegistryFriendlyByteBuf;\n', '', src, flags=re.M)
        src = re.sub(r'\bRegistryFriendlyByteBuf\b', 'FriendlyByteBuf', src)
        src = add_import(src, 'net.minecraft.network.FriendlyByteBuf')

    # vanilla STREAM_CODEC constants
    used = False
    for old, new in STREAM_CODEC_CONSTANTS.items():
        if old in src:
            src = re.sub(rf'(?<![\w.]){re.escape(old)}\b', f'ByteBufCodecs.{new}', src)
            used = True
    if used:
        src = add_import(src, f'{BP}.codec.ByteBufCodecs')
    # classes that only existed for those constants
    src = re.sub(r'^import net\.minecraft\.network\.chat\.ComponentSerialization;\n', '', src, flags=re.M)

    # ResourceLocation factories
    src = re.sub(r'ResourceLocation\.fromNamespaceAndPath\(', 'new ResourceLocation(', src)
    src = re.sub(r'ResourceLocation\.withDefaultNamespace\(', 'new ResourceLocation(', src)
    src = re.sub(r'ResourceLocation\.parse\(', 'new ResourceLocation(', src)
    src = re.sub(r'ResourceLocation::parse\b', 'ResourceLocation::new', src)

    # data component accessors: recv.get(X.DataComponents.Y ...) -> X.DataComponents.Y.get(recv ...)
    def comp(m):
        recv, op, typ, rest = m.group(1), m.group(2), m.group(3), m.group(4)
        return f'{typ}.{op}({recv}{rest}'
    src = re.sub(rf'{RECEIVER}\.({ACCESSORS})\({COMPONENT_TYPE}(\s*[,)])', comp, src)

    # registry keys / registries: 1.19.3+ Registries.X and BuiltInRegistries.X -> 1.19.2 Registry.X_REGISTRY / Registry.X
    for name in REGISTRY_KEYS:
        src = re.sub(rf'\b(?:net\.minecraft\.core\.registries\.)?Registries\.{name}\b', f'Registry.{name}_REGISTRY', src)
    for name in BUILTIN_REGISTRIES:
        src = re.sub(rf'\b(?:net\.minecraft\.core\.registries\.)?BuiltInRegistries\.{name}\b', f'Registry.{name}', src)
    if re.search(r'\bRegistry\.[A-Z_]+', src):
        src = re.sub(r'^import net\.minecraft\.core\.registries\.(?:Registries|BuiltInRegistries);\n', '', src, flags=re.M)
        src = add_import(src, 'net.minecraft.core.Registry')

    # DFU 5 / pre-1.20 codecs
    src = src.replace('DataResult.error(() -> ', 'DataResult.error(')
    src = re.sub(r'\bExtraCodecs\.intRange\(', 'Codec.intRange(', src)
    bc = False
    for old, new in CODEC_CONSTANTS.items():
        if re.search(rf'(?<![\w.]){re.escape(old)}\b', src):
            src = re.sub(rf'(?<![\w.]){re.escape(old)}\b', f'BackportCodecs.{new}', src)
            bc = True
    src, n1 = re.subn(r'([\w.]+)\.listOf\((\d+), (\d+)\)', r'BackportCodecs.listOf(\1, \2, \3)', src)
    src, n2 = re.subn(r'(?<![\w.])(?!BackportCodecs\.)([\w.]+)\.sizeLimitedListOf\(', r'BackportCodecs.sizeLimitedListOf(\1, ', src)
    src, n3 = re.subn(r'\bCodec\.string\(', 'BackportCodecs.string(', src)
    if bc or n1 or n2 or n3:
        src = add_import(src, f'{BP}.codec.BackportCodecs')

    # Entity.level() (1.20+) -> the public 1.19.2 field; upstream's own Minecrft.level() helper stays
    src = re.sub(r'(?<!Minecrft)\.level\(\)', '.level', src)
    # decompiler artifact: completedFuture((Object)null) etc.
    src = src.replace('(Object)null', 'null')

    # DFU: ((Pair)CODEC.decode(ops, x).getOrThrow()).getFirst() -> parse; DFU 7 getOrThrow() -> BackportCodecs
    src = re.sub(r'\(\(Pair\)([\w.]+)\.decode\(([^;]*?)\)\.getOrThrow\(\)\)\.getFirst\(\)', r'BackportCodecs.getOrThrow(\1.parse(\2))', src)
    if '.getOrThrow()' in src:
        src = wrap_receivers(src, 'getOrThrow', 'BackportCodecs.getOrThrow')
        src = add_import(src, f'{BP}.codec.BackportCodecs')
    src = re.sub(r'DataResult\.error\((\w+)::(\w+)\)', r'DataResult.error(\1.\2())', src)
    src = re.sub(r'\bEither::unwrap\b', 'BackportCodecs::unwrap', src)
    src = re.sub(r'\bStringUtil\.isBlank\(', 'org.apache.commons.lang3.StringUtils.isBlank(', src)
    src = re.sub(r'\bResourceKey\.streamCodec\(', 'ByteBufCodecs.resourceKey(', src)
    src = re.sub(r'\bUUIDUtil\.LENIENT_CODEC\b', 'BackportCodecs.UUID_LENIENT', src)
    src = re.sub(r'\bUUIDUtil\.STRING_CODEC\b', 'BackportCodecs.UUID_STRING', src)
    src = re.sub(r'(\.serverLevel)\(\)', '.getLevel()', src)
    if re.search(r'\bBackportCodecs\b', src):
        src = add_import(src, f'{BP}.codec.BackportCodecs')
    if re.search(r'\bByteBufCodecs\.', src):
        src = add_import(src, f'{BP}.codec.ByteBufCodecs')

    # decompiler artifact: ((RawGeneric)expr).x(...) loses the generic type -> (expr).x(...)
    src = re.sub(r'\(\((?:Set|List|Map|Collection|Optional|Supplier|Holder|Iterable|Stream|Map\.Entry)\)(?=[\w(])', '(', src)

    # Item.TooltipContext (1.20.5+) -> the 1.19.2 appendHoverText level parameter
    src = re.sub(r'^import net\.minecraft\.world\.item\.Item\.TooltipContext;\n', '', src, flags=re.M)
    if 'Item.TooltipContext' in src or re.search(r'\bTooltipContext \w+[,)]', src):
        src = src.replace('Item.TooltipContext', '@Nullable Level')
        src = re.sub(r'(?<![\w.@])TooltipContext (\w+[,)])', r'@Nullable Level \1', src)
        src = add_import(src, 'net.minecraft.world.level.Level')
        if not re.search(r'^import (org\.jetbrains\.annotations|javax\.annotation)\.Nullable;', src, re.M):
            src = add_import(src, 'org.jetbrains.annotations.Nullable')

    # DFU 7 codec.validate(fn) -> BackportCodecs.validate(codec, fn)
    if re.search(r'\.validate\(', src) and 'Codec' in src:
        src = wrap_receivers_with_args(src, 'validate', 'BackportCodecs.validate')
        src = add_import(src, f'{BP}.codec.BackportCodecs')
    # 1.20.2+ ClipContext(from, to, block, fluid, CollisionContext.of(entity)) -> 1.19.2 takes the entity
    src = re.sub(r'CollisionContext\.of\((\w+)\)', r'\1', src) if 'new ClipContext(' in src else src
    # ItemPredicate.test (1.20.5+) -> matches, for the predicate() accessors of Lens/Filter and Optional<ItemPredicate>
    src = re.sub(r'\.predicate\(\)\.test\(', '.predicate().matches(', src)

    # 1.19.3+ ItemStack/vanilla API -> backport.util.Backport statics (same behaviour)
    for name in ('isSameItemSameComponents', 'hashItemAndComponents', 'listMatches', 'hashStackList'):
        src = re.sub(rf'\bItemStack\.{name}\(', f'Backport.{name}(', src)
    src = re.sub(r'\bItemStack\.isSameItem\(', 'ItemStack.isSame(', src)
    src = re.sub(r'\bItemStack\.(?:OPTIONAL_CODEC|STRICT_CODEC|SINGLE_ITEM_CODEC)\b', 'ItemStack.CODEC', src)
    for name in ('copyWithCount', 'transmuteCopy', 'getPrecipitationAt'):
        if '.' + name + '(' in src:
            src = wrap_receivers_with_args(src, name, f'Backport.{name}')
    if '.getTextureDiffuseColor()' in src:
        src = wrap_receivers(src, 'getTextureDiffuseColor', 'Backport.getTextureDiffuseColor')
    if re.search(r'\bBackport\.', src):
        src = add_import(src, f'{BP}.util.Backport')
    src = src.replace('.addFirst(', '.add(0, ').replace('.removeFirst()', '.remove(0)')
    src = src.replace('.isSingleplayer()', '.hasSingleplayerServer()')
    src = src.replace('.getTimer().getGameTimeDeltaTicks()', '.getDeltaFrameTime()')
    src = re.sub(r'\.saveWithFullMetadata\((?:[^()]|\([^()]*\))+\)', '.saveWithFullMetadata()', src)
    # 1.20.5 ItemStack NBT with registries -> 1.19.2
    REG = r'(?:this\.registryAccess\(\)|registries|registryAccess|provider|levelRegistry|[\w.]+\.registryAccess\(\))'
    src = re.sub(rf'\bItemStack\.parse\({REG}, ', 'Backport.parseItemStack(', src)
    src = re.sub(rf'\bItemStack\.parseOptional\({REG}, ', 'Backport.parseItemStackOptional(', src)
    src = re.sub(rf'\.save\({REG}\)', '.save(new CompoundTag())', src)
    src = re.sub(rf'\.save\({REG}, ', '.save(', src)
    src = re.sub(rf'(ContainerHelper\.(?:load|save)AllItems\(\w+, [\w.]+), {REG}\)', r'\1)', src)
    if re.search(r'\bBackport\.', src):
        src = add_import(src, f'{BP}.util.Backport')
    if 'new CompoundTag()' in src:
        src = add_import(src, 'net.minecraft.nbt.CompoundTag')
    src = re.sub(r'new FriendlyByteBuf\(([^,()]+(?:\([^()]*\))?), [\w.()]+\)', r'new FriendlyByteBuf(\1)', src)

    # interface injection (Loom/NeoForge) doesn't exist on ForgeGradle 5: PlayerMixin makes Player implement
    # CameraOperator/CameraHolder at runtime, so upstream's player.getActiveExposureCamera() becomes a cast
    ops = 'getActiveExposureCamera|getActiveExposureCameraOptional|setActiveExposureCamera|removeActiveExposureCamera|getExposureCameraActionAnim'
    holder_ops = 'getPlayerExecutingExposure|getServerPlayerExecutingExposure|getPlayerAwardedForExposure|getServerPlayerAwardedForExposure|getExposureAuthorEntity|getExposureCameraOperator|asHolderEntity'
    src, n_op = re.subn(rf'(?<![\w.)])(?!super\b)((?:this\.)?\w+(?:\(\))?|Minecrft\.player\(\))\.({ops})\(', r'((CameraOperator)\1).\2(', src)
    src, n_holder = re.subn(rf'(?<![\w.)])((?:this\.)?\w*(?:[pP]layer|Minecrft\.player)\w*(?:\(\))?)\.({holder_ops})\(', r'((CameraHolder)\1).\2(', src)
    if n_op:
        src = add_import(src, 'io.github.mortuusars.exposure.world.entity.CameraOperator')
    if n_holder:
        src = add_import(src, 'io.github.mortuusars.exposure.world.entity.CameraHolder')

    # JOML (1.19.3+) -> com.mojang.math: Quaternionf -> Quaternion, Axis.XP -> Vector3f.XP
    src = re.sub(r'\bQuaternionf\b', 'Quaternion', src)
    src = re.sub(r'\bAxis\.([XYZ][PN])\b', r'Vector3f.\1', src)

    # 1.20 CommandSourceStack.sendSuccess(Supplier<Component>, bool) -> 1.19.2 sendSuccess(Component, bool)
    src = re.sub(r'\.sendSuccess\(\s*\(\) ->\s*', '.sendSuccess(', src)
    src = re.sub(r'\.sendSuccess\((msg|message), ', r'.sendSuccess(\1.get(), ', src)
    # NeoForge config BooleanValue.isTrue()/isFalse()
    src = re.sub(r'(Config\.\w+\.[A-Z_]+)\.isTrue\(\)', r'\1.get()', src)
    src = re.sub(r'(Config\.\w+\.[A-Z_]+)\.isFalse\(\)', r'!\1.get()', src)
    # 1.20 Slot.setByPlayer -> set; StringUtil.filterText -> SharedConstants.filterText
    src = src.replace('.setByPlayer(', '.set(')
    src = re.sub(r'\bStringUtil\.filterText\(', 'net.minecraft.SharedConstants.filterText(', src)
    # Block: 1.20.5 split use() into useWithoutItem/useItemOn and made the overrides protected; 1.19.2 has public use()
    src = re.sub(r'protected InteractionResult useWithoutItem\(BlockState (\w+), Level (\w+), BlockPos (\w+), Player (\w+), BlockHitResult (\w+)\)',
                 r'public InteractionResult use(BlockState \1, Level \2, BlockPos \3, Player \4, InteractionHand hand, BlockHitResult \5)', src)
    src = re.sub(r'super\.useWithoutItem\((\w+), (\w+), (\w+), (\w+), (\w+)\)', r'super.use(\1, \2, \3, \4, hand, \5)', src)
    if 'InteractionHand hand' in src:
        src = add_import(src, 'net.minecraft.world.InteractionHand')
    src = re.sub(r'\bprotected (\w+(?:<[^>]*>)?) (\w+)\(((?:@NotNull |@Nullable )?BlockState )', r'public \1 \2(\3', src)

    # Entity.registryAccess() (1.20.5) -> through the level
    src = re.sub(r'\b(\w*(?:[pP]layer|[eE]ntity)|asHolderEntity\(\))\.registryAccess\(\)', r'\1.level.registryAccess()', src)
    # Entity.onGround() (1.20) -> field
    src = re.sub(r'\.onGround\(\)', '.onGround', src)
    # BlockState.isSolid() (1.20, Material removed) -> material
    src = re.sub(r'(\b\w*[sS]tate)\.isSolid\(\)', r'\1.getMaterial().isSolid()', src)
    # DamageTypeTags (1.19.4) -> DamageSource flags
    for tag, method in (('IS_EXPLOSION', 'isExplosion'), ('IS_FIRE', 'isFire'), ('IS_PROJECTILE', 'isProjectile'),
                        ('BYPASSES_ARMOR', 'isBypassArmor'), ('IS_FALL', 'isFall'), ('BYPASSES_INVULNERABILITY', 'isBypassInvul')):
        src = re.sub(rf'(\w+)\.is\(DamageTypeTags\.{tag}\)', rf'\1.{method}()', src)
    src = re.sub(r'^import net\.minecraft\.tags\.DamageTypeTags;\n', '', src, flags=re.M)
    # BlockEntity load/save (1.20.5 registries parameter)
    src = re.sub(r'protected void loadAdditional\(CompoundTag (\w+), (?:HolderLookup\.)?Provider \w+\)', r'public void load(CompoundTag \1)', src)
    src = re.sub(r'super\.loadAdditional\((\w+), \w+\)', r'super.load(\1)', src)
    src = re.sub(r'protected void saveAdditional\(CompoundTag (\w+), (?:HolderLookup\.)?Provider \w+\)', r'protected void saveAdditional(CompoundTag \1)', src)
    src = re.sub(r'super\.saveAdditional\((\w+), \w+\)', r'super.saveAdditional(\1)', src)
    src = re.sub(r'public CompoundTag getUpdateTag\((?:HolderLookup\.)?Provider \w+\)', 'public CompoundTag getUpdateTag()', src)
    src = re.sub(r'\.saveWithoutMetadata\([\w.()]+\)', '.saveWithoutMetadata()', src)
    src = re.sub(r'^import net\.minecraft\.core\.HolderLookup\.Provider;\n', '', src, flags=re.M)
    # Entity synched data (1.20.5 builder) -> 1.19.2 define()
    src = re.sub(r'protected void defineSynchedData\((?:SynchedEntityData\.)?Builder (\w+)\) \{', r'protected void defineSynchedData() {', src)
    src = re.sub(r'\bbuilder\.define\(', 'this.entityData.define(', src)
    src = re.sub(r'super\.defineSynchedData\(builder\)', 'super.defineSynchedData()', src)
    src = re.sub(r'^import net\.minecraft\.network\.syncher\.SynchedEntityData\.Builder;\n', '', src, flags=re.M)
    # entityData.set(key, value, true) (forced re-sync, 1.20.2+) -> Backport.setEntityData (SynchedEntityDataAccessor)
    src, n = re.subn(r'([\w.]*getEntityData\(\))\.set\(([^;]*?), true\);', r'Backport.setEntityData(\1, \2, true);', src)
    if n:
        src = add_import(src, f'{BP}.util.Backport')
    # 1.21 BufferBuilder: Tesselator.begin(mode, format) / buildOrThrow -> 1.19.2 getBuilder + begin / end
    src = re.sub(r'BufferBuilder (\w+) = Tesselator\.getInstance\(\)\.begin\(([^;]*)\);',
                 r'BufferBuilder \1 = Tesselator.getInstance().getBuilder();\n      \1.begin(\2);', src)
    src = src.replace('.buildOrThrow()', '.end()')
    # 1.21 vertex chains -> 1.19.2 (statement ends with endVertex)
    def vertex_statement(m):
        stmt = m.group(0)[:-1]
        for old, new in (('.addVertex(', '.vertex('), ('.setColor(', '.color('), ('.setUv(', '.uv('), ('.setLight(', '.uv2('),
                         ('.setOverlay(', '.overlayCoords('), ('.setNormal(', '.normal('), ('.setUv1(', '.overlayCoords('),
                         ('.setUv2(', '.uv2(')):
            stmt = stmt.replace(old, new)
        return stmt + '.endVertex();'
    src = re.sub(r'\w+\.addVertex\([^;]*;', vertex_statement, src)

    # screens: 1.20.2+ render contract via bridge classes
    if re.search(r'\bextends Screen\b', src):
        src = re.sub(r'\bextends Screen\b', 'extends BackportScreen', src)
        src = add_import(src, f'{BP}.gui.BackportScreen')
    if re.search(r'\bextends AbstractContainerScreen<', src):
        src = re.sub(r'\bextends AbstractContainerScreen<', 'extends BackportContainerScreen<', src)
        src = add_import(src, f'{BP}.gui.BackportContainerScreen')

    # 1.20 GUI render types -> backport.gui.GuiRenderTypes (state around immediate fills)
    if re.search(r'\bRenderType\.(gui|guiOverlay|guiTextHighlight|guiGhostRecipeOverlay)\(\)', src):
        src = re.sub(r'\bRenderType\.(gui|guiOverlay|guiTextHighlight|guiGhostRecipeOverlay)\(\)', r'GuiRenderTypes.\1()', src)
        src = add_import(src, f'{BP}.gui.GuiRenderTypes')

    # ItemDisplayContext (1.19.4) -> ItemTransforms.TransformType; renderStatic lost its Level argument
    if 'ItemDisplayContext' in src:
        src = re.sub(r'^import net\.minecraft\.world\.item\.ItemDisplayContext;\n', '', src, flags=re.M)
        # in mixin target descriptors first
        src = src.replace('Lnet/minecraft/world/item/ItemDisplayContext;', 'Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;')
        src = re.sub(r'\bItemDisplayContext\b', 'ItemTransforms.TransformType', src)
        src = add_import(src, 'net.minecraft.client.renderer.block.model.ItemTransforms')
    src = re.sub(r'\.renderStatic\(([^;]*?), ([\w.]*\blevel|null), (\w+)\)', r'.renderStatic(\1, \3)', src)
    # Font.DisplayMode (1.19.4) -> the 1.19.2 seeThrough flag
    src = src.replace('DisplayMode.SEE_THROUGH', 'true').replace('DisplayMode.NORMAL', 'false').replace('DisplayMode.POLYGON_OFFSET', 'false')
    src = re.sub(r'^import net\.minecraft\.client\.gui\.Font\.DisplayMode;\n', '', src, flags=re.M)

    # 1.21 standalone models are ModelResourceLocation(id, "standalone"); 1.19.2 loads/looks them up by plain id
    if 'ModelResourceLocation' in src and 'new ModelResourceLocation(' not in src:
        src = re.sub(r'^import net\.minecraft\.client\.resources\.model\.ModelResourceLocation;\n', '', src, flags=re.M)
        src = re.sub(r'\bModelResourceLocation\b', 'ResourceLocation', src)
        src = add_import(src, 'net.minecraft.resources.ResourceLocation')

    # more 1.19.3+ vanilla API
    src = re.sub(r'\bMath\.clamp\(', 'Mth.clamp(', src)
    if 'Mth.clamp(' in src:
        src = add_import(src, 'net.minecraft.util.Mth')
    src = re.sub(r'\.getTimer\(\)\.getGameTimeDeltaPartialTick\(\w+\)', '.getFrameTime()', src)
    src = re.sub(r'\bSoundEvents\.(\w+)\.value\(\)', r'SoundEvents.\1', src)
    src = src.replace('.isHighlightable()', '.isActive()')
    src = re.sub(r'\bgetTooltipFromContainerItem\(', 'getTooltipFromItem(', src)
    if re.search(r'\bScreen\.getTooltipFromItem\(', src):
        src = re.sub(r'\bScreen\.getTooltipFromItem\(', 'ClientBackport.getTooltipFromItem(', src)
        src = add_import(src, f'{BP}.client.ClientBackport')
    src = re.sub(r'\bMth\.lerpInt\(', 'Backport.lerpInt(', src)
    if '.getLast()' in src:
        src = wrap_receivers(src, 'getLast', 'Backport.getLast')
    src = re.sub(r'\b(entity|holderEntity|cameraHolder)\.getRandom\(\)', r'\1.level.getRandom()', src)
    if re.search(r'\bBackport\.', src):
        src = add_import(src, f'{BP}.util.Backport')

    # Java 21: `case null, default ->` (pattern switch) -> default
    src = src.replace('case null, default ->', 'default ->')

    # Java 21: switch pattern exhaustiveness error
    src = re.sub(r'new MatchException\((?:\(String\))?null, (?:\(Throwable\))?null\)', 'new IllegalStateException()', src)

    # decompiler artifact: raw functional-interface casts on lambdas, (Function)((x) -> ...) -> ((x) -> ...)
    src = re.sub(r'\((?:Function|BiFunction|Consumer|BiConsumer|Supplier|Predicate|BiPredicate|UnaryOperator)\)(?=\(\(?[\w, ]*\)? ->|\(\w+ ->)', '', src)
    # decompiler artifact: erasure cast on Class.cast, (Item)this.itemType.cast(x) -> this.itemType.cast(x)
    src = re.sub(r'\(\w+\)(this\.\w+\.cast\()', r'\1', src)

    # Java 21 SequencedCollection
    src = re.sub(r'\.getFirst\(\)', '.get(0)', src)
    return src


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('upstream')
    ap.add_argument('dest')
    ap.add_argument('--exclude', nargs='*', default=[])
    ap.add_argument('--force', nargs='*', default=[], help='relative paths to overwrite anyway')
    ap.add_argument('--refresh', action='store_true', help='overwrite every existing file not in tools/hand_ported.txt')
    ap.add_argument('--dry-run', action='store_true')
    a = ap.parse_args()

    hand_ported_file = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'hand_ported.txt')
    hand_ported = set()
    if os.path.exists(hand_ported_file):
        hand_ported = {l.strip() for l in open(hand_ported_file) if l.strip() and not l.startswith('#')}

    excludes_file = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'port_excludes.txt')
    if os.path.exists(excludes_file):
        a.exclude += [l.strip() for l in open(excludes_file) if l.strip() and not l.startswith('#')]

    written = skipped = 0
    for root, _, files in os.walk(a.upstream):
        for f in files:
            if not f.endswith('.java'):
                continue
            rel = os.path.relpath(os.path.join(root, f), a.upstream)
            if rel.startswith('architectury_inject') or any(fnmatch.fnmatch(rel, g) for g in a.exclude):
                continue
            out_rel = re.sub(r'(^|/)neoforge(/|$)', r'\1forge\2', rel)
            out = os.path.join(a.dest, out_rel)
            if out_rel in hand_ported or (os.path.exists(out) and not a.refresh and out_rel not in a.force):
                skipped += 1
                continue
            src = open(os.path.join(root, f), encoding='utf-8').read()
            src = transform(src)
            # our own packages: upstream <mod>.neoforge.* lives in <mod>.forge.*
            src = re.sub(r'\b((?:io\.github\.mortuusars|dev\.titanite\.sparkwave)\.[\w.]*?)\.neoforge\b', r'\1.forge', src)
            if not a.dry_run:
                os.makedirs(os.path.dirname(out), exist_ok=True)
                open(out, 'w', encoding='utf-8').write(src)
            written += 1
    print(f'written {written}, skipped (already ported) {skipped}', file=sys.stderr)


if __name__ == '__main__':
    main()
