package com.SaintAlfonzo.solraspberrypie.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

/** 11.3: P opens the effect screen; Shift+T locks or unlocks the inventory widget (TrashSlot's key on purpose). */
public final class RpKeys {
    public static final String CATEGORY = "key.categories.solraspberrypie";
    public static final KeyMapping OPEN = new KeyMapping("key.solraspberrypie.open", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, CATEGORY);
    public static final KeyMapping LOCK = new KeyMapping("key.solraspberrypie.lock", KeyConflictContext.GUI, KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T, CATEGORY);

    private RpKeys() {}
}
