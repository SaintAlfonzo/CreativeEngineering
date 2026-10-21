package com.SaintAlfonzo.creativeengineering.mixin;

import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** The lines of an FTB {@code TooltipList}, for {@link com.SaintAlfonzo.creativeengineering.compat.FtbTooltipStacks}. */
@Mixin(value = TooltipList.class, remap = false)
public interface FtbTooltipListAccessor {
    @Accessor("lines")
    List<Component> creativeengineering$getLines();
}
