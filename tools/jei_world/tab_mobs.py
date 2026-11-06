"""mob_bottling tab, generated part: every Bucketable mob and the bucket it becomes.

Source: kubejs/exported/world_jei/mob_buckets.json (/dumpworldjei, patcherMod devtools/WorldJeiMobBuckets.java).
Mobs picked up some other way (Buzzier Bees bottles, the duck sack, Quark slime, Upgrade Aquatic squid) are
hand-written in kubejs/assets/creativecommon/jei_world/mob_bottling/manual.json. The mob_transformation tab is
manual only.
"""
from .common import entity, item, mod_of, row

# Bucketable mobs whose own mobInteract takes a different bucket than the result's fluid says
# (verified in the jar: EntityStradpole.mobInteract checks Items.LAVA_BUCKET).
BUCKET_OVERRIDE = {
    'alexsmobs:stradpole': 'minecraft:lava_bucket',
}


def build(ctx):
    dump = ctx.exported('world_jei/mob_buckets.json')
    if dump is None:
        return {}
    rows = []
    for e in sorted(dump.get('bucketable', []), key=lambda d: d['entity']):
        result = e.get('bucket_item')
        if not result or result in ('minecraft:air', 'None'):
            ctx.warn(f"mob_buckets: {e['entity']} has no bucket item")
            continue
        used = BUCKET_OVERRIDE.get(e['entity'])
        if not used:
            used = e.get('empty_bucket')
            if not used or used in ('minecraft:air', 'None'):
                used = 'minecraft:water_bucket'
        rows.append(row([entity(e['entity'])], [item(result)], action=item(used),
                        requires={mod_of(e['entity']), mod_of(result)} - {'minecraft'}))
    for failed in dump.get('failed', []):
        ctx.warn(f'mob_buckets: could not create {failed}')
    return {'mob_bottling': rows}
