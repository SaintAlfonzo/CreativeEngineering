package com.SaintAlfonzo.creativeengineering.cooking;

/**
 * A third-party station block entity given a {@link RemainderBuffer} by mixin (CookingPotRemainderMixin,
 * KettleRemainderMixin). {@link RemainderBufferStorage} saves it; the menu mixins add its slots.
 */
public interface RemainderBufferHolder {

    RemainderBuffer ce$getRemainderBuffer();
}
