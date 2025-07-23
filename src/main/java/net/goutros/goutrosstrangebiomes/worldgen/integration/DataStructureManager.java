package net.goutros.goutrosstrangebiomes.worldgen.integration;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.data.*;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.data.noise.PillowNoiseChunk;
import net.goutros.goutrosstrangebiomes.worldgen.data.noise.PillowNoiseConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Central manager for all terrain data structures
 */
@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataStructureManager {

    private static PillowNoiseConfig defaultNoiseConfig;

    /**
     * Register all data structure systems
     */
    public static void register(IEventBus eventBus) {
        PillowChunkData.ATTACHMENT_TYPES.register(eventBus);
        GoutrosStrangeBiomes.LOGGER.info("Data structure systems registered");
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            initializeDataStructures();
        });
    }

    private static void initializeDataStructures() {
        defaultNoiseConfig = PillowNoiseConfig.defaultConfig();
        GoutrosStrangeBiomes.LOGGER.info("Data structures initialized");
    }

    /**
     * Create pillow generation context for chunk
     */
    public static PillowGenerationContext createContext(ChunkGenerator generator,
                                                        ChunkAccess chunk,
                                                        java.util.function.Function<BlockPos, Holder<Biome>> biomeGetter) {
        boolean isPillowBiome = isPillowChunk(chunk, biomeGetter);
        double blendRadius = isPillowBiome ? 32.0 : 0.0; // 2-chunk radius

        return new PillowGenerationContext(
                generator,
                chunk,
                defaultNoiseConfig,
                isPillowBiome,
                blendRadius
        );
    }

    /**
     * Check if chunk contains pillow biome
     */
    private static boolean isPillowChunk(ChunkAccess chunk, java.util.function.Function<BlockPos, Holder<Biome>> biomeGetter) {
        ChunkPos pos = chunk.getPos();
        BlockPos center = pos.getMiddleBlockPosition(64);
        Holder<Biome> biome = biomeGetter.apply(center);
        return biome.is(ModBiomes.PILLOW_PLATEAU);
    }

    /**
     * Initialize chunk data structures
     */
    public static void initializeChunk(ChunkAccess chunk, PillowGenerationContext context) {
        PillowChunkData data = PillowChunkData.get(chunk);

        if (context.isPillowBiome()) {
            // Calculate softness factor based on position
            ChunkPos pos = chunk.getPos();
            double softness = calculateChunkSoftness(pos.x, pos.z);
            data.setSoftnessFactor(softness);

            // Pre-calculate surface noise for performance
            precalculateSurfaceNoise(chunk, data, context);
        }
    }

    private static double calculateChunkSoftness(int chunkX, int chunkZ) {
        double noise = Math.sin(chunkX * 0.1) * Math.cos(chunkZ * 0.1);
        return 0.5 + (noise + 1.0) * 0.25; // Range: 0.5 to 1.0
    }

    private static void precalculateSurfaceNoise(ChunkAccess chunk, PillowChunkData data, PillowGenerationContext context) {
        ChunkPos pos = chunk.getPos();
        int startX = pos.getMinBlockX();
        int startZ = pos.getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double worldX = startX + x;
                double worldZ = startZ + z;

                int surfaceNoise = (int) (context.calculateTerrainHeight(worldX, 64, worldZ) * 100);
                data.setSurfaceNoise(x, z, surfaceNoise);
            }
        }
    }

    /**
     * Create enhanced noise chunk
     */
    public static PillowNoiseChunk createNoiseChunk(NoiseChunk delegate, ChunkAccess chunk, PillowGenerationContext context) {
        return new PillowNoiseChunk(delegate, chunk, context);
    }

    /**
     * Sample terrain height at position
     */
    public static double sampleTerrainHeight(ChunkAccess chunk, double x, double y, double z) {
        PillowChunkData data = PillowChunkData.get(chunk);

        if (!data.hasCustomTerrain()) {
            return 0.0;
        }

        // Convert world coordinates to chunk-local
        ChunkPos pos = chunk.getPos();
        int localX = (int) (x - pos.getMinBlockX());
        int localZ = (int) (z - pos.getMinBlockZ());

        if (localX >= 0 && localX < 16 && localZ >= 0 && localZ < 16) {
            return data.getPillowHeight(localX, localZ);
        }

        return 0.0;
    }

    /**
     * Update heightmaps for chunk
     */
    public static void updateHeightmaps(ChunkAccess chunk, PillowGenerationContext context) {
        if (!context.isPillowBiome()) {
            return;
        }

        PillowHeightmapUtils.PillowHeightmap pillowMap = PillowHeightmapUtils.createFrom(chunk, context);

        // Update vanilla heightmaps with pillow modifications
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                float pillowHeight = pillowMap.getHeight(x, z);
                int vanillaHeight = chunk.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, x, z);

                // Only update if pillow height is higher
                if (pillowHeight > vanillaHeight) {
                    // This would require custom heightmap modification
                    // For now, store in chunk data
                    PillowChunkData.get(chunk).setPillowHeight(x, z, pillowHeight);
                }
            }
        }
    }

    /**
     * Get default noise configuration
     */
    public static PillowNoiseConfig getDefaultNoiseConfig() {
        if (defaultNoiseConfig == null) {
            defaultNoiseConfig = PillowNoiseConfig.defaultConfig();
        }
        return defaultNoiseConfig;
    }

    /**
     * Utility for biome-aware sampling
     */
    public static class BiomeAwareSampler {
        private final java.util.function.Function<BlockPos, Holder<Biome>> biomeGetter;
        private final PillowNoiseConfig noiseConfig;

        public BiomeAwareSampler(java.util.function.Function<BlockPos, Holder<Biome>> biomeGetter, PillowNoiseConfig noiseConfig) {
            this.biomeGetter = biomeGetter;
            this.noiseConfig = noiseConfig;
        }

        public double sampleAt(double x, double y, double z) {
            BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
            Holder<Biome> biome = biomeGetter.apply(pos);

            if (biome.is(ModBiomes.PILLOW_PLATEAU)) {
                return noiseConfig.calculatePillowNoise(x, y, z);
            }

            return 0.0;
        }

        public double sampleWithBlending(double x, double y, double z, double blendRadius) {
            double pillowValue = sampleAt(x, y, z);

            if (pillowValue == 0.0 || blendRadius <= 0.0) {
                return pillowValue;
            }

            // Sample neighbors for blending
            double totalWeight = 1.0;
            double totalValue = pillowValue;

            int samples = 4;
            for (int i = 0; i < samples; i++) {
                double angle = (i * Math.PI * 2.0) / samples;
                double nx = x + Math.cos(angle) * blendRadius;
                double nz = z + Math.sin(angle) * blendRadius;

                double neighborValue = sampleAt(nx, y, nz);
                double weight = 0.25; // Equal weight for all neighbors

                totalValue += neighborValue * weight;
                totalWeight += weight;
            }

            return totalValue / totalWeight;
        }
    }

    /**
     * Performance monitoring utilities
     */
    public static class PerformanceMonitor {
        private static long totalSamples = 0;
        private static long totalTime = 0;

        public static void recordSample(long timeNanos) {
            totalSamples++;
            totalTime += timeNanos;
        }

        public static double getAverageTimeNanos() {
            return totalSamples > 0 ? (double) totalTime / totalSamples : 0.0;
        }

        public static void reset() {
            totalSamples = 0;
            totalTime = 0;
        }

        public static String getStats() {
            return String.format("Samples: %d, Avg: %.2f ns", totalSamples, getAverageTimeNanos());
        }
    }
}
