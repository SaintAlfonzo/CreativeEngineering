package com.axperty.storagedelight.registry;

import com.axperty.storagedelight.block.entity.BookshelfDoorBlockEntity;
import com.axperty.storagedelight.block.entity.CabinetCountertopBlockEntity;
import com.axperty.storagedelight.block.entity.CabinetVariantBlockEntity;
import com.axperty.storagedelight.block.entity.DrawerBlockEntity;
import com.axperty.storagedelight.block.entity.DrawerBooksBlockEntity;
import com.axperty.storagedelight.block.entity.DrawerDoorBlockEntity;
import com.axperty.storagedelight.block.entity.GlassCabinetBlockEntity;
import com.axperty.storagedelight.block.entity.SmallDrawersBlockEntity;
import com.axperty.storagedelight.registry.BlockRegistry.Kind;
import java.util.function.Supplier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The block entity types keep their upstream registry names; their valid blocks are every block of the kind. */
public class EntityTypesRegistry {
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "storagedelight");
    public static final RegistryObject<BlockEntityType<DrawerBlockEntity>> DRAWER = type("cabinet", DrawerBlockEntity::new, Kind.DRAWER);
    public static final RegistryObject<BlockEntityType<DrawerDoorBlockEntity>> DRAWER_DOOR = type("drawer_door", DrawerDoorBlockEntity::new, Kind.DRAWER_DOOR);
    public static final RegistryObject<BlockEntityType<DrawerBooksBlockEntity>> DRAWER_BOOKS = type("drawer_books", DrawerBooksBlockEntity::new, Kind.DRAWER_BOOKS);
    public static final RegistryObject<BlockEntityType<GlassCabinetBlockEntity>> GLASS_CABINET = type("glass_cabinet", GlassCabinetBlockEntity::new, Kind.GLASS_CABINET);
    public static final RegistryObject<BlockEntityType<CabinetVariantBlockEntity>> CABINET_VARIANT = type("cabinet_variant", CabinetVariantBlockEntity::new, Kind.CABINET_VARIANT);
    public static final RegistryObject<BlockEntityType<BookshelfDoorBlockEntity>> BOOKSHELF_DOOR = type("bookshelf_door", BookshelfDoorBlockEntity::new, Kind.BOOKSHELF_DOOR);
    public static final RegistryObject<BlockEntityType<SmallDrawersBlockEntity>> SMALL_DRAWERS = type("small_drawers", SmallDrawersBlockEntity::new, Kind.SMALL_DRAWERS);
    public static final RegistryObject<BlockEntityType<CabinetCountertopBlockEntity>> COUNTERTOP = type("countertop", CabinetCountertopBlockEntity::new, Kind.COUNTERTOP);

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> type(String name, BlockEntitySupplier<T> factory, Kind kind) {
        Supplier<BlockEntityType<T>> supplier = () -> Builder.of(factory, BlockRegistry.blocks(kind)).build(null);
        return TILES.register(name, supplier);
    }
}
