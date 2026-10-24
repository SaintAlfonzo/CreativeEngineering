package com.sammy.minersdelight.registry;

import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MDMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MinersDelightMod.MODID);

    public static final RegistryObject<MenuType<CopperPotMenu>> COPPER_POT = MENU_TYPES.register("copper_pot", () -> IForgeMenuType.create(CopperPotMenu::new));

    private MDMenuTypes() {
    }
}
