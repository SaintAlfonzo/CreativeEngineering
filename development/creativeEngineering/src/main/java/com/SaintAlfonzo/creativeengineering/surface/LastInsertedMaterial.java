package com.SaintAlfonzo.creativeengineering.surface;

/**
 * Duck interface added to Copycats+' {@code MaterialItemStorage} by
 * {@code mixin/CopycatMaterialOrderMixin}: the part (property name) that most recently received a
 * material, or {@code null} if none has been recorded.
 */
public interface LastInsertedMaterial {

    String creativeengineering$lastProperty();
}
