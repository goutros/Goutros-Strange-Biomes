package net.goutros.goutrosstrangebiomes.worldgen.api;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for easy biome terrain registration for future biomes
 */
public class BiomeTerrainRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("BiomeTerrainRegistry");
    private static final Map<ResourceKey<Biome>, BiomeTerrainConfig> BIOME_CONFIGS = new HashMap<>();

    /**
     * Register a complete terrain configuration for a biome
     */
    public static void registerBiomeTerrain(ResourceKey<Biome> biome, BiomeTerrainConfig config) {
        BIOME_CONFIGS.put(biome, config);

        TerrainGenerationAPI.registerTerrainProvider(biome, config.terrainProvider());
        TerrainGenerationAPI.registerBlockProvider(biome, config.blockProvider());

        LOGGER.info("Registered complete terrain configuration for biome: {}", biome.location());
    }

    /**
     * Get terrain configuration for a biome
     */
    public static BiomeTerrainConfig getTerrainConfig(ResourceKey<Biome> biome) {
        return BIOME_CONFIGS.get(biome);
    }

    /**
     * Get all registered biomes
     */
    public static Set<ResourceKey<Biome>> getRegisteredBiomes() {
        return BIOME_CONFIGS.keySet();
    }

    /**
     * Configuration record for biome terrain
     */
    public record BiomeTerrainConfig(
            BiomeTerrainProvider terrainProvider,
            BiomeBlockProvider blockProvider,
            double blendRadius,
            int priority
    ) {}
}