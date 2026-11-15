package com.SaintAlfonzo.creativecompat.surface;

import javax.annotation.Nullable;

/**
 * Duck interface that {@code mixin/InsideBlocksMixin} adds to every {@code Entity}: its {@link PassState}. Created on
 * first use; most entities never touch a pass-through block and keep a null field.
 */
public interface PassHolder {

    /** The entity's state; {@code create = false} returns null when it has none yet. */
    @Nullable
    PassState creativecompat$passState(boolean create);
}
