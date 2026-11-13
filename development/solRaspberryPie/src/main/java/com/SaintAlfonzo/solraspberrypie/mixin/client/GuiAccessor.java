package com.SaintAlfonzo.solraspberrypie.mixin.client;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The action-bar line's timer: our queued lines wait while another message is showing (10). */
@Mixin(Gui.class)
public interface GuiAccessor {
    @Accessor("overlayMessageTime")
    int solrp$overlayMessageTime();
}
