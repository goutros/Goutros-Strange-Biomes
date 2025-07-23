package net.goutros.goutrosstrangebiomes.worldgen.api.density;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DensityFunctionRegistry {

    public static final DeferredRegister<com.mojang.serialization.MapCodec<? extends DensityFunction>> DENSITY_FUNCTIONS =
            DeferredRegister.create(BuiltInRegistries.DENSITY_FUNCTION_TYPE, GoutrosStrangeBiomes.MOD_ID);

    static {
        DENSITY_FUNCTIONS.register("custom_terrain", () -> CustomTerrainDensity.CODEC);
    }

    public static void register(IEventBus modEventBus) {
        DENSITY_FUNCTIONS.register(modEventBus);
        GoutrosStrangeBiomes.LOGGER.info("Custom density functions registered");
    }
}
