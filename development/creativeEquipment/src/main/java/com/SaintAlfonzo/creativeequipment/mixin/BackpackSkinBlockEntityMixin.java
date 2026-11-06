package com.SaintAlfonzo.creativeequipment.mixin;

import com.SaintAlfonzo.creativeequipment.backpackskin.BackpackSkins;
import com.SaintAlfonzo.creativeequipment.backpackskin.BackpackSkinsClient;

import net.minecraftforge.client.model.data.ModelData;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

/** Gives the placed backpack's skin to the client model (read from the stack the block entity already syncs). */
@Mixin(value = BackpackBlockEntity.class, remap = false)
public abstract class BackpackSkinBlockEntityMixin {

    /** Overrides Forge's default {@code IForgeBlockEntity.getModelData()}. */
    @NotNull
    public ModelData getModelData() {
        return BackpackSkinsClient.modelData(BackpackSkins.getSkin(((BackpackBlockEntity) (Object) this).getBackpackWrapper().getBackpack()));
    }
}
