package net.goutros.goutrosstrangebiomes.worldgen.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main API for custom terrain generation that blends with vanilla
 */
public class TerrainGenerationAPI {

    private static final ConcurrentHashMap<ResourceKey<Biome>, BiomeTerrainProvider> TERRAIN_PROVIDERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ResourceKey<Biome>, BiomeBlockProvider> BLOCK_PROVIDERS = new ConcurrentHashMap<>();

    /**
     * Register a terrain provider for a biome
     */
    public static void registerTerrainProvider(ResourceKey<Biome> biome, BiomeTerrainProvider provider) {
        TERRAIN_PROVIDERS.put(biome, provider);
    }

    /**
     * Register a block provider for a biome
     */
    public static void registerBlockProvider(ResourceKey<Biome> biome, BiomeBlockProvider provider) {
        BLOCK_PROVIDERS.put(biome, provider);
    }

    /**
     * Get terrain provider for biome
     */
    public static BiomeTerrainProvider getTerrainProvider(ResourceKey<Biome> biome) {
        return TERRAIN_PROVIDERS.get(biome);
    }

    /**
     * Get block provider for biome
     */
    public static BiomeBlockProvider getBlockProvider(ResourceKey<Biome> biome) {
        return BLOCK_PROVIDERS.get(biome);
    }

    /**
     * Check if biome has custom terrain
     */
    public static boolean hasCustomTerrain(ResourceKey<Biome> biome) {
        return TERRAIN_PROVIDERS.containsKey(biome);
    }

    /**
     * Get all registered biomes with custom terrain
     */
    public static List<ResourceKey<Biome>> getCustomTerrainBiomes() {
        return TERRAIN_PROVIDERS.keySet().stream().toList();
    }
}