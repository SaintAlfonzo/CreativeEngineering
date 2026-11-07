package com.SaintAlfonzo.creativedelight.cooking;

import com.SaintAlfonzo.creativecompat.compat.RemainderView;

/**
 * A third-party station block entity given a {@link RemainderBuffer} by mixin (CookingPotRemainderMixin,
 * KettleRemainderMixin). {@link RemainderBufferStorage} saves it; the menu mixins add its slots.
 */
public interface RemainderBufferHolder extends RemainderView.Holder {

    RemainderBuffer ce$getRemainderBuffer();

    /** The buffer as Common's WTHIT line reads it. */
    @Override
    default RemainderView ce$remainderView() {
        return ce$getRemainderBuffer();
    }
}
