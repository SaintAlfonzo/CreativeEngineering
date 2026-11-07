package com.SaintAlfonzo.creativecompat.compat;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * A block entity that holds one potted plant, read by {@link WthitPotContents}. Implemented by Creative Compat's
 * BotanistPotBlockEntity (the Beautify botanist workbench as a flower pot), so this jar never loads a Compat class.
 */
public interface PottedPlantHolder {

    /** Id of the plant block in the pot, or null when empty. */
    @Nullable
    ResourceLocation plant();
}
