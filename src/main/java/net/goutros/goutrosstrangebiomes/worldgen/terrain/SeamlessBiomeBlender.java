package net.goutros.goutrosstrangebiomes.worldgen.terrain;

import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SEAMLESS BIOME TERRAIN BLENDER
 *
 * Optimized system that provides seamless per-block blending at biome boundaries
 * while maintaining high performance through intelligent caching and sampling.
 */
public class SeamlessBiomeBlender {
    private static final Logger LOGGER = LoggerFactory.getLogger("SeamlessBiomeBlender");

    // Performance-optimized blending parameters
    private static final int BLEND_RADIUS = 32;          // Radius for smooth blending
    private static final int CACHE_SAMPLE_SIZE = 8;      // Resolution for influence cache
    private static final int QUICK_SAMPLE_STEP = 4;      // Step size for quick sampling
    private static final double MIN_INFLUENCE = 0.02;    // Minimum influence to process

    // Noise for consistent biome boundary detection
    private static final ThreadLocal<SimplexNoise> BOUNDARY_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(98765L)));

    // Cached influence grid for performance
    private static final Map<Long, CachedInfluenceGrid> INFLUENCE_GRID_CACHE = new ConcurrentHashMap<>();

    /**
     * Calculate seamless biome influence for any world position
     * Uses a cached grid system for optimal performance
     */
    public static double calculateSeamlessBiomeInfluence(ChunkAccess chunk, int worldX, int worldZ) {
        // Get cached influence grid for this region
        CachedInfluenceGrid grid = getOrCreateInfluenceGrid(chunk, worldX, worldZ);

        // Interpolate within the grid for smooth per-block blending
        return interpolateInfluenceFromGrid(grid, worldX, worldZ);
    }

    /**
     * Get or create a cached influence grid for a region
     */
    private static CachedInfluenceGrid getOrCreateInfluenceGrid(ChunkAccess chunk, int worldX, int worldZ) {
        // Calculate grid key based on world position (aligned to grid)
        int gridX = (worldX / CACHE_SAMPLE_SIZE) * CACHE_SAMPLE_SIZE;
        int gridZ = (worldZ / CACHE_SAMPLE_SIZE) * CACHE_SAMPLE_SIZE;
        long gridKey = ((long) gridX << 32) | (gridZ & 0xFFFFFFFFL);

        CachedInfluenceGrid cached = INFLUENCE_GRID_CACHE.get(gridKey);
        if (cached != null) {
            return cached;
        }

        // Create new influence grid
        CachedInfluenceGrid newGrid = createInfluenceGrid(chunk, gridX, gridZ);
        INFLUENCE_GRID_CACHE.put(gridKey, newGrid);

        // Clean cache if it gets too large
        if (INFLUENCE_GRID_CACHE.size() > 1000) {
            cleanupInfluenceCache();
        }

        return newGrid;
    }

    /**
     * Create a new influence grid for a region
     */
    private static CachedInfluenceGrid createInfluenceGrid(ChunkAccess chunk, int gridStartX, int gridStartZ) {
        int gridSize = BLEND_RADIUS / CACHE_SAMPLE_SIZE + 1;
        double[][] influenceGrid = new double[gridSize][gridSize];

        for (int gx = 0; gx < gridSize; gx++) {
            for (int gz = 0; gz < gridSize; gz++) {
                int worldX = gridStartX + gx * CACHE_SAMPLE_SIZE;
                int worldZ = gridStartZ + gz * CACHE_SAMPLE_SIZE;

                // Calculate precise influence at this grid point
                influenceGrid[gx][gz] = calculatePreciseInfluence(worldX, worldZ);
            }
        }

        return new CachedInfluenceGrid(gridStartX, gridStartZ, influenceGrid);
    }

    /**
     * Calculate precise biome influence at a specific position
     */
    private static double calculatePreciseInfluence(int worldX, int worldZ) {
        double totalInfluence = 0.0;
        double totalWeight = 0.0;

        // Sample in a radius for smooth influence calculation
        for (int dx = -BLEND_RADIUS; dx <= BLEND_RADIUS; dx += QUICK_SAMPLE_STEP) {
            for (int dz = -BLEND_RADIUS; dz <= BLEND_RADIUS; dz += QUICK_SAMPLE_STEP) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > BLEND_RADIUS) continue;

                // Smooth distance-based weight
                double weight = 1.0 - (distance / BLEND_RADIUS);
                weight = smoothstep(weight); // Apply smoothing

                // Check if this position has custom biome using climate approximation
                if (isPillowBiomeAtWorldPosition(worldX + dx, worldZ + dz)) {
                    totalInfluence += weight;
                }
                totalWeight += weight;
            }
        }

        double influence = totalWeight > 0 ? totalInfluence / totalWeight : 0.0;

        // Add boundary noise for natural variation
        double boundaryNoise = BOUNDARY_NOISE.get().getValue(worldX * 0.003, worldZ * 0.003);
        boundaryNoise = (boundaryNoise + 1.0) * 0.5;

        // Apply natural boundary variation
        influence *= (0.7 + 0.6 * boundaryNoise);
        return Mth.clamp(influence, 0.0, 1.0);
    }

    /**
     * Interpolate influence from cached grid for smooth per-block transitions
     */
    private static double interpolateInfluenceFromGrid(CachedInfluenceGrid grid, int worldX, int worldZ) {
        // Calculate position within grid
        double gridX = (double)(worldX - grid.startX) / CACHE_SAMPLE_SIZE;
        double gridZ = (double)(worldZ - grid.startZ) / CACHE_SAMPLE_SIZE;

        // Get integer grid coordinates
        int x0 = (int) Math.floor(gridX);
        int z0 = (int) Math.floor(gridZ);
        int x1 = x0 + 1;
        int z1 = z0 + 1;

        // Calculate fractional parts for interpolation
        double fx = gridX - x0;
        double fz = gridZ - z0;

        // Bounds check
        int gridSize = grid.influenceData.length;
        if (x0 < 0 || x1 >= gridSize || z0 < 0 || z1 >= gridSize) {
            // Outside grid bounds, calculate directly
            return calculatePreciseInfluence(worldX, worldZ);
        }

        // Bilinear interpolation for smooth transitions
        double v00 = grid.influenceData[x0][z0];
        double v10 = grid.influenceData[x1][z0];
        double v01 = grid.influenceData[x0][z1];
        double v11 = grid.influenceData[x1][z1];

        // Interpolate along X axis
        double v0 = Mth.lerp(fx, v00, v10);
        double v1 = Mth.lerp(fx, v01, v11);

        // Interpolate along Z axis
        return Mth.lerp(fz, v0, v1);
    }

    /**
     * Check if position is likely to have Pillow Plateau biome
     */
    private static boolean isPillowBiomeAtWorldPosition(int worldX, int worldZ) {
        // Climate approximation matching TerraBlender parameters
        double tempNoise = BOUNDARY_NOISE.get().getValue(worldX * 0.0025, worldZ * 0.0025);
        double humidityNoise = BOUNDARY_NOISE.get().getValue(worldX * 0.003 + 1000, worldZ * 0.003 + 1000);
        double weirdnessNoise = BOUNDARY_NOISE.get().getValue(worldX * 0.001 + 2000, worldZ * 0.001 + 2000);

        // Normalize to 0-1
        tempNoise = (tempNoise + 1.0) * 0.5;
        humidityNoise = (humidityNoise + 1.0) * 0.5;
        weirdnessNoise = (weirdnessNoise + 1.0) * 0.5;

        // Match climate parameters for Pillow Plateau
        boolean tempMatch = tempNoise >= 0.35 && tempNoise <= 0.65;
        boolean humidityMatch = humidityNoise >= 0.65;
        boolean weirdnessMatch = weirdnessNoise >= 0.7;

        return tempMatch && humidityMatch && weirdnessMatch;
    }

    /**
     * Get smooth blending weight for terrain modification
     */
    public static double getBlendingWeight(double influence) {
        if (influence < MIN_INFLUENCE) return 0.0;
        if (influence > 0.98) return 1.0;

        // Apply smooth curve for natural blending
        return smoothstep(influence);
    }

    /**
     * Calculate blended height between vanilla and custom terrain
     */
    public static int blendTerrainHeights(int vanillaHeight, int customHeight, double influence) {
        double weight = getBlendingWeight(influence);
        return (int) Mth.lerp(weight, vanillaHeight, customHeight);
    }

    /**
     * Determine if a chunk has any significant biome influence
     */
    public static boolean chunkHasSignificantInfluence(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();

        // Quick sample across chunk
        for (int x = 0; x < 16; x += 8) {
            for (int z = 0; z < 16; z += 8) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                double influence = calculateSeamlessBiomeInfluence(chunk, worldX, worldZ);
                if (influence > MIN_INFLUENCE) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Apply seamless edge blending for natural transitions
     */
    public static double applyEdgeBlending(double baseInfluence, int worldX, int worldZ) {
        // Add edge softening for very natural boundaries
        double edgeNoise = BOUNDARY_NOISE.get().getValue(worldX * 0.008, worldZ * 0.008);
        edgeNoise = (edgeNoise + 1.0) * 0.5;

        // Soften edges naturally
        double edgeFactor = 0.9 + 0.2 * edgeNoise;
        return baseInfluence * edgeFactor;
    }

    /**
     * Smooth step function for natural interpolation
     */
    private static double smoothstep(double x) {
        x = Mth.clamp(x, 0.0, 1.0);
        return x * x * (3.0 - 2.0 * x);
    }

    /**
     * Enhanced smooth step for even smoother transitions
     */
    public static double smootherstep(double x) {
        x = Mth.clamp(x, 0.0, 1.0);
        return x * x * x * (x * (x * 6.0 - 15.0) + 10.0);
    }

    /**
     * Clean up old cache entries to prevent memory leaks
     */
    private static void cleanupInfluenceCache() {
        if (INFLUENCE_GRID_CACHE.size() > 800) {
            // Keep only the most recently used entries
            INFLUENCE_GRID_CACHE.clear();
            LOGGER.debug("Cleaned influence grid cache");
        }
    }

    /**
     * Force cache cleanup (called from mixin periodically)
     */
    public static void forceCleanupCache() {
        INFLUENCE_GRID_CACHE.clear();
    }

    /**
     * Get cache statistics for debugging
     */
    public static String getCacheStats() {
        return String.format("Influence grid cache: %d entries", INFLUENCE_GRID_CACHE.size());
    }

    /**
     * Cached influence grid for a region
     */
    private record CachedInfluenceGrid(int startX, int startZ, double[][] influenceData) {}
}