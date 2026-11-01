package net.satisfy.vinery.core.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.menu.ApplePressMenu;
import net.satisfy.vinery.core.menu.FermentationBarrelMenu;

public class MenuTypeRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Vinery.MOD_ID);

    public static final RegistryObject<MenuType<FermentationBarrelMenu>> FERMENTATION_BARREL_MENU = MENU_TYPES.register("fermentation_barrel_gui_handler", () -> new MenuType<>(FermentationBarrelMenu::new));
    public static final RegistryObject<MenuType<ApplePressMenu>> APPLE_PRESS_MENU = MENU_TYPES.register("apple_press_gui_handler", () -> new MenuType<>(ApplePressMenu::new));
}
