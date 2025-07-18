package net.goutros.goutrosstrangebiomes.worldgen.terrain;

import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.performance.TerrainPerformanceMonitor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ENHANCED TERRAIN RESCULPTOR
 * Generates vanilla terrain first, then resculpts it into dramatic layered plateaus
 * with proper island generation and seamless blending at biome boundaries.
 */
public class TerrainResculptor {
    private static final Logger LOGGER = LoggerFactory.getLogger("TerrainResculptor");

    // Terrain parameters matching the reference image
    private static final int SEA_LEVEL = 63;
    private static final int[] PLATEAU_LAYERS = {45, 65, 85, 105, 125, 145}; // Dramatic stepped layers
    private static final int MIN_ISLAND_HEIGHT = 75; // Minimum height for ocean islands
    private static final int MAX_TERRAIN_HEIGHT = 180;

    // Noise frequencies for natural terrain features
    private static final double PLATEAU_FREQUENCY = 0.006;   // Large plateau formations
    private static final double LAYER_FREQUENCY = 0.012;     // Layer definition
    private static final double EROSION_FREQUENCY = 0.020;   // Erosion patterns
    private static final double SHORE_FREQUENCY = 0.030;     // Shore blending
    private static final double DETAIL_FREQUENCY = 0.045;    // Fine details

    // Blending parameters for seamless transitions
    private static final int BLEND_RADIUS = 48;              // Blocks to sample for blending
    private static final int SAMPLE_STEP = 6;                // Step size for performance
    private static final double INFLUENCE_FALLOFF = 1.8;     // How quickly influence drops

    // Noise generators for consistent terrain features
    private static final ThreadLocal<SimplexNoise> PLATEAU_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(12345L)));
    private static final ThreadLocal<SimplexNoise> LAYER_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(23456L)));
    private static final ThreadLocal<SimplexNoise> EROSION_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(34567L)));
    private static final ThreadLocal<SimplexNoise> SHORE_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(45678L)));
    private static final ThreadLocal<SimplexNoise> DETAIL_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(56789L)));
    private static final ThreadLocal<SimplexNoise> ISLAND_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(67890L)));

    // Cache for expensive biome influence calculations
    private static final Map<Long, BiomeInfluenceResult> INFLUENCE_CACHE = new ConcurrentHashMap<>();

    /**
     * Main entry point: resculpt terrain after vanilla generation is complete
     */
    public static void resculptTerrain(ChunkAccess chunk, RandomState randomState) {
        ChunkPos chunkPos = chunk.getPos();
        long startTime = System.nanoTime();

        try {
            // Quick check if this chunk needs any custom terrain
            BiomeInfluenceData chunkInfluence = TerrainPerformanceMonitor.timeOperation(
                    "analyze_chunk_influence",
                    () -> analyzeChunkInfluence(chunk, chunkPos)
            );

            if (chunkInfluence.maxInfluence < 0.01) {
                TerrainPerformanceMonitor.recordChunkProcessing(System.nanoTime() - startTime, true);
                return; // No custom biome influence, skip processing
            }

            LOGGER.debug("Resculpting terrain for chunk {}, {} (influence: {:.1f}%)",
                    chunkPos.x, chunkPos.z, chunkInfluence.maxInfluence * 100);

            // Resculpt terrain with proper island generation and blending
            TerrainPerformanceMonitor.timeOperation("resculpt_terrain",
                    () -> resculptChunkTerrain(chunk, chunkInfluence));

            TerrainPerformanceMonitor.timeOperation("update_heightmaps",
                    () -> updateChunkHeightmaps(chunk));

            TerrainPerformanceMonitor.recordChunkProcessing(System.nanoTime() - startTime, false);

        } catch (Exception e) {
            LOGGER.error("Error in terrain resculpting for chunk {}, {}: {}",
                    chunkPos.x, chunkPos.z, e.getMessage(), e);
            TerrainPerformanceMonitor.recordChunkProcessing(System.nanoTime() - startTime, true);
        }
    }

    /**
     * Analyze maximum biome influence across the chunk for early exit optimization
     */
    private static BiomeInfluenceData analyzeChunkInfluence(ChunkAccess chunk, ChunkPos chunkPos) {
        double maxInfluence = 0.0;
        int influencePoints = 0;

        // Sample at reduced resolution for performance
        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                double influence = calculateBiomeInfluenceAtPosition(worldX, worldZ);
                if (influence > 0.01) {
                    maxInfluence = Math.max(maxInfluence, influence);
                    influencePoints++;
                }
            }
        }

        return new BiomeInfluenceData(maxInfluence, influencePoints);
    }

    /**
     * Calculate biome influence at a position with distance-based falloff and caching
     */
    private static double calculateBiomeInfluenceAtPosition(int worldX, int worldZ) {
        long cacheKey = ((long) worldX << 32) | (worldZ & 0xFFFFFFFFL);
        BiomeInfluenceResult cached = INFLUENCE_CACHE.get(cacheKey);

        if (cached != null) {
            return cached.influence;
        }

        double totalInfluence = 0.0;
        double totalWeight = 0.0;

        // Sample in a radius around the position for smooth blending
        for (int dx = -BLEND_RADIUS; dx <= BLEND_RADIUS; dx += SAMPLE_STEP) {
            for (int dz = -BLEND_RADIUS; dz <= BLEND_RADIUS; dz += SAMPLE_STEP) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > BLEND_RADIUS) continue;

                // Distance-based weight with smooth falloff
                double weight = Math.pow(1.0 - (distance / BLEND_RADIUS), INFLUENCE_FALLOFF);

                // Check if this position is likely to have custom biome using climate approximation
                if (isPillowBiomeAtWorldPosition(worldX + dx, worldZ + dz)) {
                    totalInfluence += weight;
                }
                totalWeight += weight;
            }
        }

        double influence = totalWeight > 0 ? totalInfluence / totalWeight : 0.0;

        // Add boundary noise for natural variation
        double boundaryNoise = SHORE_NOISE.get().getValue(worldX * 0.004, worldZ * 0.004);
        boundaryNoise = (boundaryNoise + 1.0) * 0.5;
        influence *= (0.6 + 0.8 * boundaryNoise); // Natural boundary variation
        influence = Mth.clamp(influence, 0.0, 1.0);

        BiomeInfluenceResult result = new BiomeInfluenceResult(influence);
        INFLUENCE_CACHE.put(cacheKey, result);

        return influence;
    }

    /**
     * Check if a world position is likely to have Pillow Plateau biome using climate approximation
     */
    private static boolean isPillowBiomeAtWorldPosition(int worldX, int worldZ) {
        // Approximate biome placement using climate-like noise (matches TerraBlender parameters)
        double tempNoise = LAYER_NOISE.get().getValue(worldX * 0.0025, worldZ * 0.0025);
        double humidityNoise = DETAIL_NOISE.get().getValue(worldX * 0.003, worldZ * 0.003);
        double weirdnessNoise = EROSION_NOISE.get().getValue(worldX * 0.001, worldZ * 0.001);

        // Normalize to 0-1
        tempNoise = (tempNoise + 1.0) * 0.5;
        humidityNoise = (humidityNoise + 1.0) * 0.5;
        weirdnessNoise = (weirdnessNoise + 1.0) * 0.5;

        // Match TerraBlender climate parameters for Pillow Plateau
        boolean tempMatch = tempNoise >= 0.35 && tempNoise <= 0.65;     // Cool to neutral
        boolean humidityMatch = humidityNoise >= 0.65;                  // High humidity
        boolean weirdnessMatch = weirdnessNoise >= 0.7;                 // High weirdness

        return tempMatch && humidityMatch && weirdnessMatch;
    }

    /**
     * Resculpt terrain for the entire chunk
     */
    private static void resculptChunkTerrain(ChunkAccess chunk, BiomeInfluenceData chunkInfluence) {
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunk.getPos().getMinBlockX() + x;
                int worldZ = chunk.getPos().getMinBlockZ() + z;

                // Calculate influence and terrain data for this position
                double biomeInfluence = calculateBiomeInfluenceAtPosition(worldX, worldZ);

                if (biomeInfluence > 0.01) {
                    resculptTerrainColumn(chunk, x, z, worldX, worldZ, biomeInfluence, minY, maxY);
                }
            }
        }
    }

    /**
     * Resculpt a single terrain column with proper island generation and blending
     */
    private static void resculptTerrainColumn(ChunkAccess chunk, int x, int z, int worldX, int worldZ,
                                              double biomeInfluence, int minY, int maxY) {

        // Get current vanilla terrain height
        int vanillaHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        // Generate custom terrain data
        TerrainData terrainData = generateCustomTerrain(worldX, worldZ, vanillaHeight, biomeInfluence);

        // Calculate final blended height
        int targetHeight = calculateBlendedHeight(vanillaHeight, terrainData, biomeInfluence);

        // Handle island generation in ocean areas
        if (vanillaHeight < SEA_LEVEL && biomeInfluence > 0.4) {
            targetHeight = generateOceanIsland(worldX, worldZ, targetHeight, biomeInfluence);
        }

        // Resculpt the actual terrain blocks
        resculptColumnBlocks(chunk, x, z, vanillaHeight, targetHeight, terrainData, biomeInfluence, minY, maxY);
    }

    /**
     * Generate custom terrain data based on the reference image
     */
    private static TerrainData generateCustomTerrain(int worldX, int worldZ, int vanillaHeight, double biomeInfluence) {
        // Main plateau shape noise
        double plateauNoise = PLATEAU_NOISE.get().getValue(worldX * PLATEAU_FREQUENCY, worldZ * PLATEAU_FREQUENCY);
        plateauNoise = (plateauNoise + 1.0) * 0.5;

        // Layer selection noise
        double layerNoise = LAYER_NOISE.get().getValue(worldX * LAYER_FREQUENCY, worldZ * LAYER_FREQUENCY);
        layerNoise = (layerNoise + 1.0) * 0.5;

        // Erosion patterns for natural cliffs and valleys
        double erosionNoise = EROSION_NOISE.get().getValue(worldX * EROSION_FREQUENCY, worldZ * EROSION_FREQUENCY);
        erosionNoise = (erosionNoise + 1.0) * 0.5;

        // Fine detail variation
        double detailNoise = DETAIL_NOISE.get().getValue(worldX * DETAIL_FREQUENCY, worldZ * DETAIL_FREQUENCY);
        detailNoise = (detailNoise + 1.0) * 0.5;

        // Select plateau layer based on noise
        int layerIndex = (int) (layerNoise * PLATEAU_LAYERS.length);
        layerIndex = Mth.clamp(layerIndex, 0, PLATEAU_LAYERS.length - 1);
        int baseHeight = PLATEAU_LAYERS[layerIndex];

        // Apply erosion for dramatic cliff faces and valleys
        double erosionFactor = Math.pow(erosionNoise, 1.5); // Sharper erosion transitions
        if (erosionFactor < 0.35) {
            // Create valleys and erosion channels
            int erosionDepth = (int) ((0.35 - erosionFactor) * 60);
            baseHeight -= erosionDepth;
        } else if (erosionFactor > 0.8) {
            // Create higher peaks and ridges
            int erosionHeight = (int) ((erosionFactor - 0.8) * 25);
            baseHeight += erosionHeight;
        }

        // Add fine detail variation
        int detailVariation = (int) ((detailNoise - 0.5) * 12);
        int finalHeight = baseHeight + detailVariation;

        // Clamp to reasonable bounds
        finalHeight = Mth.clamp(finalHeight, 25, MAX_TERRAIN_HEIGHT);

        // Determine material type based on height and layer
        int materialType = determineMaterialType(finalHeight, layerIndex, erosionFactor);

        return new TerrainData(finalHeight, layerIndex, materialType, erosionFactor);
    }

    /**
     * Calculate blended height between vanilla and custom terrain
     */
    private static int calculateBlendedHeight(int vanillaHeight, TerrainData terrainData, double biomeInfluence) {
        // Use smooth interpolation for seamless blending
        double smoothInfluence = smoothstep(biomeInfluence);
        return (int) Mth.lerp(smoothInfluence, vanillaHeight, terrainData.height);
    }

    /**
     * Generate ocean islands that rise from the sea floor
     */
    private static int generateOceanIsland(int worldX, int worldZ, int targetHeight, double biomeInfluence) {
        // Island generation noise
        double islandNoise = ISLAND_NOISE.get().getValue(worldX * 0.008, worldZ * 0.008);
        islandNoise = (islandNoise + 1.0) * 0.5;

        // Shore shaping noise for natural coastlines
        double shoreNoise = SHORE_NOISE.get().getValue(worldX * 0.015, worldZ * 0.015);
        shoreNoise = (shoreNoise + 1.0) * 0.5;

        // Calculate island base height
        int islandBase = (int) (MIN_ISLAND_HEIGHT + (targetHeight - MIN_ISLAND_HEIGHT) * islandNoise);

        // Add shore variation for natural coastlines
        int shoreVariation = (int) ((shoreNoise - 0.5) * 15);
        islandBase += shoreVariation;

        // Ensure minimum height above sea level
        islandBase = Math.max(islandBase, SEA_LEVEL + 5);

        return islandBase;
    }

    /**
     * Determine material type based on terrain properties
     */
    private static int determineMaterialType(int height, int layerIndex, double erosionFactor) {
        if (height < 40) return 0;          // Deep/underwater
        if (height < 60) return 1;          // Low coastal
        if (height < 80) return 2;          // Mid level
        if (height < 100) return 3;         // High level
        if (height < 130) return 4;         // Very high
        return 5;                           // Peak level
    }

    /**
     * Resculpt the actual blocks in a terrain column
     */
    private static void resculptColumnBlocks(ChunkAccess chunk, int x, int z, int vanillaHeight, int targetHeight,
                                             TerrainData terrainData, double biomeInfluence, int minY, int maxY) {

        // Determine modification range
        int modifyStart = Math.min(vanillaHeight, targetHeight) - 15;
        int modifyEnd = Math.max(vanillaHeight, targetHeight) + 10;
        modifyStart = Math.max(modifyStart, minY);
        modifyEnd = Math.min(modifyEnd, maxY);

        for (int y = modifyStart; y < modifyEnd; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState newState = determineBlockState(y, targetHeight, terrainData, biomeInfluence);
            chunk.setBlockState(pos, newState, false);
        }

        // Handle water placement for areas below sea level
        handleWaterPlacement(chunk, x, z, targetHeight, maxY);
    }

    /**
     * Determine the appropriate block state for a position
     */
    private static BlockState determineBlockState(int y, int surfaceHeight, TerrainData terrainData, double biomeInfluence) {
        if (y > surfaceHeight) {
            return Blocks.AIR.defaultBlockState();
        }

        int depthFromSurface = surfaceHeight - y;

        // Use vanilla blocks for now, arranged in dramatic layers matching the reference
        if (biomeInfluence > 0.8) {
            // High influence - dramatic stepped terrain
            return getPlateauBlock(depthFromSurface, terrainData.materialType, terrainData.erosionFactor);
        } else if (biomeInfluence > 0.5) {
            // Medium influence - transitional terrain
            return getTransitionalBlock(depthFromSurface, terrainData.materialType);
        } else if (biomeInfluence > 0.2) {
            // Low influence - subtle modifications
            return getSubtleBlock(depthFromSurface);
        }

        // Very low influence - mostly vanilla
        return getVanillaBlock(depthFromSurface);
    }

    /**
     * Get plateau block for high-influence areas (dramatic stepped terrain)
     */
    private static BlockState getPlateauBlock(int depth, int materialType, double erosionFactor) {
        if (depth == 0) {
            // Surface layer - use varied blocks for visual interest
            return switch (materialType) {
                case 0 -> Blocks.SAND.defaultBlockState();
                case 1 -> Blocks.COARSE_DIRT.defaultBlockState();
                case 2 -> Blocks.STONE.defaultBlockState();
                case 3 -> Blocks.COBBLESTONE.defaultBlockState();
                case 4 -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                default -> Blocks.STONE_BRICKS.defaultBlockState();
            };
        } else if (depth <= 3) {
            // Near-surface layer
            return switch (materialType) {
                case 0, 1 -> Blocks.SANDSTONE.defaultBlockState();
                case 2, 3 -> Blocks.STONE.defaultBlockState();
                case 4, 5 -> Blocks.COBBLESTONE.defaultBlockState();
                default -> Blocks.STONE.defaultBlockState();
            };
        } else if (depth <= 8) {
            // Subsurface layer
            if (erosionFactor > 0.7) {
                return Blocks.COBBLESTONE.defaultBlockState(); // Erosion-resistant rock
            }
            return Blocks.STONE.defaultBlockState();
        } else if (depth <= 20) {
            // Deep subsurface
            return Blocks.STONE.defaultBlockState();
        }

        // Very deep
        return Blocks.DEEPSLATE.defaultBlockState();
    }

    /**
     * Get transitional block for medium-influence areas
     */
    private static BlockState getTransitionalBlock(int depth, int materialType) {
        if (depth == 0) {
            return switch (materialType % 3) {
                case 0 -> Blocks.COARSE_DIRT.defaultBlockState();
                case 1 -> Blocks.GRAVEL.defaultBlockState();
                default -> Blocks.COBBLESTONE.defaultBlockState();
            };
        } else if (depth <= 4) {
            return Blocks.STONE.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
    }

    /**
     * Get subtle block for low-influence areas
     */
    private static BlockState getSubtleBlock(int depth) {
        if (depth == 0) {
            return Blocks.COARSE_DIRT.defaultBlockState();
        } else if (depth <= 2) {
            return Blocks.DIRT.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
    }

    /**
     * Get vanilla block for very low influence
     */
    private static BlockState getVanillaBlock(int depth) {
        if (depth == 0) {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        } else if (depth <= 3) {
            return Blocks.DIRT.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
    }

    /**
     * Handle water placement for ocean areas and shore blending
     */
    private static void handleWaterPlacement(ChunkAccess chunk, int x, int z, int surfaceHeight, int maxY) {
        if (surfaceHeight < SEA_LEVEL) {
            // Fill with water up to sea level
            for (int y = surfaceHeight + 1; y <= SEA_LEVEL && y < maxY; y++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (chunk.getBlockState(pos).isAir()) {
                    chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
                }
            }
        }
    }

    /**
     * Smooth step function for natural interpolation
     */
    private static double smoothstep(double x) {
        x = Mth.clamp(x, 0.0, 1.0);
        return x * x * (3.0 - 2.0 * x);
    }

    /**
     * Update all heightmaps after terrain modification
     */
    private static void updateChunkHeightmaps(ChunkAccess chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = findSurfaceHeight(chunk, x, z);
                updateHeightmapsAt(chunk, x, z, height);
            }
        }
    }

    /**
     * Find the actual surface height at a position
     */
    private static int findSurfaceHeight(ChunkAccess chunk, int x, int z) {
        for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
            BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
            if (!state.isAir() && !state.is(Blocks.WATER)) {
                return y;
            }
        }
        return chunk.getMinBuildHeight();
    }

    /**
     * Update heightmaps at a specific position
     */
    private static void updateHeightmapsAt(ChunkAccess chunk, int x, int z, int height) {
        BlockPos pos = new BlockPos(x, height, z);
        BlockState state = chunk.getBlockState(pos);

        chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG)
                .update(x, height + 1, z, state);
        chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING)
                .update(x, height + 1, z, state);
        chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES)
                .update(x, height + 1, z, state);
        chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG)
                .update(x, height + 1, z, state);
    }

    /**
     * Clear cache periodically to prevent memory leaks
     */
    public static void cleanupCache() {
        if (INFLUENCE_CACHE.size() > 5000) {
            INFLUENCE_CACHE.clear();
        }
    }

    // Data classes for terrain information
    private record BiomeInfluenceData(double maxInfluence, int influencePoints) {}
    private record BiomeInfluenceResult(double influence) {}
    private record TerrainData(int height, int layerIndex, int materialType, double erosionFactor) {}
}