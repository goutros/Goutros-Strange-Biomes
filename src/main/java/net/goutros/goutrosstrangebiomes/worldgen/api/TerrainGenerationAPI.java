package net.goutros.goutrosstrangebiomes.worldgen.api;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.api.density.DensityFunctionRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TerrainGenerationAPI {

    public static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.chunk.ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(BuiltInRegistries.CHUNK_GENERATOR, GoutrosStrangeBiomes.MOD_ID);

    public static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.biome.BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(BuiltInRegistries.BIOME_SOURCE, GoutrosStrangeBiomes.MOD_ID);

    static {
        CHUNK_GENERATORS.register("enhanced", () -> EnhancedChunkGenerator.CODEC);
        BIOME_SOURCES.register("enhanced", () -> EnhancedBiomeSource.CODEC);
    }

    public static void register(IEventBus modEventBus) {
        CHUNK_GENERATORS.register(modEventBus);
        BIOME_SOURCES.register(modEventBus);
        DensityFunctionRegistry.register(modEventBus);
        GoutrosStrangeBiomes.LOGGER.info("Terrain Generation API registered");
    }
}