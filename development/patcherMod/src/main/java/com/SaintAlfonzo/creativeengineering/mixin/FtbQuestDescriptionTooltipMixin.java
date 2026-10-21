package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.FtbTooltipStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/** FTB Quests description item hover events: remember the stack whose tooltip lines go into the hover text, see {@link FtbTooltipStacks}. */
@Mixin(targets = "dev.ftb.mods.ftbquests.gui.quests.ViewQuestPanel$QuestDescriptionField", remap = false)
public class FtbQuestDescriptionTooltipMixin {

    @Redirect(method = "addMouseOverText", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraft/world/item/ItemStack;m_41651_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"))
    private List<Component> creativeengineering$rememberStack(ItemStack stack, Player player, TooltipFlag flag) {
        FtbTooltipStacks.remember(stack);
        return stack.getTooltipLines(player, flag);
    }
}
