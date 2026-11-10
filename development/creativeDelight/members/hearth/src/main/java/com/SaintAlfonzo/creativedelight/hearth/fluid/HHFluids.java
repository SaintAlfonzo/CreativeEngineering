package com.SaintAlfonzo.creativedelight.hearth.fluid;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Fluids kept from 1.4.1. As upstream they have no world block: they live in tanks, pipes and buckets. */
public final class HHFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, HearthAndHarvestMod.MODID);
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, HearthAndHarvestMod.MODID);

    public static final Map<String, Entry> ALL = new LinkedHashMap<>();
    public static final Entry GOAT_MILK = fluid("goat_milk", 3000, 1600, 0, HHItems.GOAT_MILK_BUCKET);
    public static final Entry BRINE = fluid("brine", 2000, 1400, 0, HHItems.BRINE_BUCKET);
    public static final Entry SAP = fluid("sap", 2000, 1400, 0, HHItems.SAP_BUCKET);

    static {
        for (String id : new String[]{"blueberry_juice", "raspberry_juice", "cherry_juice", "sweet_berry_juice",
                "blueberry_wine", "raspberry_wine", "sweet_berry_wine", "glow_berry_wine", "melon_wine", "lemon_wine",
                "root_beer", "moonshine"}) {
            fluid(id, 2000, 1400, 0, null);
        }
        fluid("glow_berry_juice", 2000, 1400, 8, null);
    }

    private static Entry fluid(String name, int viscosity, int density, int light, Supplier<Item> bucket) {
        ResourceLocation still = new ResourceLocation(HearthAndHarvestMod.MODID, "block/fluid/" + name + "_still");
        ResourceLocation flowing = new ResourceLocation(HearthAndHarvestMod.MODID, "block/fluid/" + name + "_flow");
        RegistryObject<FluidType> type = TYPES.register(name, () -> new FluidType(FluidType.Properties.create()
                .descriptionId("fluid_type." + HearthAndHarvestMod.MODID + "." + name)
                .viscosity(viscosity).density(density).lightLevel(light)) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return still;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return flowing;
                    }
                });
            }
        });
        Entry entry = new Entry(type);
        entry.source = FLUIDS.register(name, () -> new ForgeFlowingFluid.Source(entry.properties(bucket)));
        entry.flowing = FLUIDS.register("flowing_" + name, () -> new ForgeFlowingFluid.Flowing(entry.properties(bucket)));
        ALL.put(name, entry);
        return entry;
    }

    public static final class Entry {
        public final RegistryObject<FluidType> type;
        public RegistryObject<Fluid> source;
        public RegistryObject<Fluid> flowing;

        Entry(RegistryObject<FluidType> type) {
            this.type = type;
        }

        ForgeFlowingFluid.Properties properties(Supplier<Item> bucket) {
            ForgeFlowingFluid.Properties props = new ForgeFlowingFluid.Properties(type, source, flowing);
            return bucket == null ? props : props.bucket(bucket);
        }
    }

    private HHFluids() {
    }
}
