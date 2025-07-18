package net.goutros.goutrosstrangebiomes.worldgen.terrain;

import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
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
 * Advanced terrain resculptor that generates vanilla terrain first,
 * then resculpts it into dramatic layered plateaus with seamless blending
 */
public class TerrainResculptor {
    private static final Logger LOGGER = LoggerFactory.getLogger("TerrainResculptor");

    // Noise generators for consistent terrain features
    private static final ThreadLocal<SimplexNoise> PLATEAU_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(12345L)));
    private static final ThreadLocal<SimplexNoise> LAYER_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(23456L)));
    private static final ThreadLocal<SimplexNoise> EROSION_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(34567L)));
    private static final ThreadLocal<SimplexNoise> DETAIL_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(45678L)));

    // Terrain parameters for dramatic plateaus
    private static final int SEA_LEVEL = 63;
    private static final int[] PLATEAU_LAYERS = {45, 60, 75, 90, 105, 120, 135}; // Dramatic height differences
    private static final double PLATEAU_FREQUENCY = 0.008; // Large plateau formations
    private static final double LAYER_FREQUENCY = 0.015;   // Layer definition
    private static final double EROSION_FREQUENCY = 0.025; // Erosion patterns
    private static final double DETAIL_FREQUENCY = 0.04;   // Fine details

    // Blending parameters
    private static final int BLEND_RADIUS = 32;            // Blocks to blend over
    private static final double INFLUENCE_FALLOFF = 2.0;   // How quickly influence drops

    // Cache for biome influence calculations
    private static final Map<Long, BiomeInfluenceCache> INFLUENCE_CACHE = new ConcurrentHashMap<>();

    /**
     * Main entry point: resculpt terrain after vanilla generation
     */
    public static void resculptTerrain(ChunkAccess chunk, RandomState randomState) {
        ChunkPos chunkPos = chunk.getPos();

        // Analyze biome composition for this chunk
        BiomeAnalysis analysis = analyzeBiomeInfluence(chunk, chunkPos);

        if (analysis.hasCustomInfluence()) {
            LOGGER.debug("Resculpting terrain for chunk {}, {} (influence: {:.1f}%)",
                    chunkPos.x, chunkPos.z, analysis.maxInfluence * 100);

            resculptChunkTerrain(chunk, analysis);
            updateChunkHeightmaps(chunk);
        }
    }

    /**
     * Analyze biome influence across the chunk and surrounding area
     */
    private static BiomeAnalysis analyzeBiomeInfluence(ChunkAccess chunk, ChunkPos chunkPos) {
        double maxInfluence = 0.0;
        BiomeInfluenceData[][] influenceGrid = new BiomeInfluenceData[16][16];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                BiomeInfluenceData influence = calculateBiomeInfluence(chunk, worldX, worldZ);
                influenceGrid[x][z] = influence;
                maxInfluence = Math.max(maxInfluence, influence.influence);
            }
        }

        return new BiomeAnalysis(maxInfluence, influenceGrid);
    }

    /**
     * Calculate biome influence at a specific position with distance-based falloff
     */
    private static BiomeInfluenceData calculateBiomeInfluence(ChunkAccess chunk, int worldX, int worldZ) {
        long cacheKey = ((long) worldX << 32) | (worldZ & 0xFFFFFFFFL);
        BiomeInfluenceCache cached = INFLUENCE_CACHE.get(cacheKey);

        if (cached != null && cached.chunkPos.equals(chunk.getPos())) {
            return cached.influence;
        }

        double totalInfluence = 0.0;
        double totalWeight = 0.0;
        boolean hasCustomBiome = false;

        // Sample in a radius around the position
        for (int dx = -BLEND_RADIUS; dx <= BLEND_RADIUS; dx += 4) {
            for (int dz = -BLEND_RADIUS; dz <= BLEND_RADIUS; dz += 4) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > BLEND_RADIUS) continue;

                // Distance-based weight with falloff
                double weight = Math.pow(1.0 - (distance / BLEND_RADIUS), INFLUENCE_FALLOFF);

                int sampleX = worldX + dx;
                int sampleZ = worldZ + dz;

                if (isPillowBiomeAt(chunk, sampleX, sampleZ)) {
                    totalInfluence += weight;
                    hasCustomBiome = true;
                }
                totalWeight += weight;
            }
        }

        double influence = totalWeight > 0 ? totalInfluence / totalWeight : 0.0;
        BiomeInfluenceData result = new BiomeInfluenceData(influence, hasCustomBiome);

        // Cache the result
        INFLUENCE_CACHE.put(cacheKey, new BiomeInfluenceCache(chunk.getPos(), result));

        return result;
    }

    /**
     * Check if a world position is in a Pillow Plateau biome
     */
    private static boolean isPillowBiomeAt(ChunkAccess chunk, int worldX, int worldZ) {
        try {
            ChunkPos chunkPos = chunk.getPos();
            if (worldX < chunkPos.getMinBlockX() || worldX > chunkPos.getMaxBlockX() ||
                    worldZ < chunkPos.getMinBlockZ() || worldZ > chunkPos.getMaxBlockZ()) {
                return false; // Outside chunk bounds
            }

            int chunkX = worldX - chunkPos.getMinBlockX();
            int chunkZ = worldZ - chunkPos.getMinBlockZ();
            int biomeX = chunkX >> 2;
            int biomeZ = chunkZ >> 2;

            ResourceKey<Biome> biome = chunk.getNoiseBiome(biomeX, 16, biomeZ)
                    .unwrapKey().orElse(null);

            return biome != null && biome.equals(ModBiomes.PILLOW_PLATEAU);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Resculpt the entire chunk based on biome influence
     */
    private static void resculptChunkTerrain(ChunkAccess chunk, BiomeAnalysis analysis) {
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BiomeInfluenceData influence = analysis.influenceGrid[x][z];

                if (influence.influence > 0.01) { // Only process if there's meaningful influence
                    int worldX = chunk.getPos().getMinBlockX() + x;
                    int worldZ = chunk.getPos().getMinBlockZ() + z;

                    resculptColumn(chunk, x, z, worldX, worldZ, influence, minY, maxY);
                }
            }
        }
    }

    /**
     * Resculpt a single column of terrain
     */
    private static void resculptColumn(ChunkAccess chunk, int x, int z, int worldX, int worldZ,
                                       BiomeInfluenceData influence, int minY, int maxY) {

        // Get current vanilla terrain height
        int vanillaHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        // Generate custom plateau terrain
        PlateauData plateau = generatePlateauTerrain(worldX, worldZ, vanillaHeight);

        // Blend between vanilla and custom based on influence
        int targetHeight = blendHeights(vanillaHeight, plateau.height, influence.influence);

        // Handle island generation (raise from ocean floor)
        if (plateau.isIsland && targetHeight > SEA_LEVEL) {
            targetHeight = Math.max(targetHeight, SEA_LEVEL + 20); // Ensure islands are above water
        }

        // Resculpt the column
        resculptColumnBlocks(chunk, x, z, vanillaHeight, targetHeight, plateau, influence, minY, maxY);
    }

    /**
     * Generate plateau terrain data for a position
     */
    private static PlateauData generatePlateauTerrain(int worldX, int worldZ, int vanillaHeight) {
        // Main plateau shape
        double plateauNoise = PLATEAU_NOISE.get().getValue(worldX * PLATEAU_FREQUENCY, worldZ * PLATEAU_FREQUENCY);
        plateauNoise = (plateauNoise + 1.0) * 0.5; // Normalize 0-1

        // Layer selection
        double layerNoise = LAYER_NOISE.get().getValue(worldX * LAYER_FREQUENCY, worldZ * LAYER_FREQUENCY);
        layerNoise = (layerNoise + 1.0) * 0.5;

        // Erosion patterns
        double erosionNoise = EROSION_NOISE.get().getValue(worldX * EROSION_FREQUENCY, worldZ * EROSION_FREQUENCY);
        erosionNoise = (erosionNoise + 1.0) * 0.5;

        // Fine detail
        double detailNoise = DETAIL_NOISE.get().getValue(worldX * DETAIL_FREQUENCY, worldZ * DETAIL_FREQUENCY);
        detailNoise = (detailNoise + 1.0) * 0.5;

        // Select plateau layer
        int layerIndex = (int) (layerNoise * PLATEAU_LAYERS.length);
        layerIndex = Mth.clamp(layerIndex, 0, PLATEAU_LAYERS.length - 1);
        int baseHeight = PLATEAU_LAYERS[layerIndex];

        // Apply erosion (creates cliff faces and valleys)
        double erosionFactor = Math.pow(erosionNoise, 2.0); // Square for sharper transitions
        if (erosionFactor < 0.3) {
            baseHeight -= (int) ((0.3 - erosionFactor) * 40); // Erode down to create valleys
        }

        // Add fine detail variation
        int detailVariation = (int) ((detailNoise - 0.5) * 8);
        int finalHeight = baseHeight + detailVariation;

        // Determine if this should be an island (raised from ocean)
        boolean isIsland = vanillaHeight < SEA_LEVEL && plateauNoise > 0.4;

        // Select material layer for this height
        int materialLayer = determineMaterialLayer(finalHeight, layerIndex);

        return new PlateauData(finalHeight, materialLayer, isIsland, erosionFactor);
    }

    /**
     * Determine material layer based on height and position
     */
    private static int determineMaterialLayer(int height, int baseLayerIndex) {
        if (height < 50) return 0;      // Deep ocean - dark materials
        if (height < 70) return 1;      // Shallow - medium materials
        if (height < 90) return 2;      // Mid level
        if (height < 110) return 3;     // High level
        if (height < 130) return 4;     // Very high
        return Math.min(baseLayerIndex, 5); // Cap at highest layer
    }

    /**
     * Blend vanilla and custom heights based on influence
     */
    private static int blendHeights(int vanillaHeight, int customHeight, double influence) {
        return (int) Mth.lerp(influence, vanillaHeight, customHeight);
    }

    /**
     * Resculpt the actual blocks in a column
     */
    private static void resculptColumnBlocks(ChunkAccess chunk, int x, int z, int vanillaHeight,
                                             int targetHeight, PlateauData plateau, BiomeInfluenceData influence,
                                             int minY, int maxY) {

        // Determine the range to modify
        int modifyStart = Math.min(vanillaHeight, targetHeight) - 10;
        int modifyEnd = Math.max(vanillaHeight, targetHeight) + 10;
        modifyStart = Math.max(modifyStart, minY);
        modifyEnd = Math.min(modifyEnd, maxY);

        for (int y = modifyStart; y < modifyEnd; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState newState;

            if (y <= targetHeight) {
                // Solid terrain
                newState = getTerrainBlock(y, targetHeight, plateau, influence);
            } else if (y <= SEA_LEVEL && targetHeight < SEA_LEVEL) {
                // Water above terrain that's below sea level
                newState = Blocks.WATER.defaultBlockState();
            } else {
                // Air
                newState = Blocks.AIR.defaultBlockState();
            }

            chunk.setBlockState(pos, newState, false);
        }
    }

    /**
     * Get the appropriate terrain block for a position
     */
    private static BlockState getTerrainBlock(int y, int surfaceHeight, PlateauData plateau, BiomeInfluenceData influence) {
        int depthFromSurface = surfaceHeight - y;

        // Use vanilla blocks but in the style of the layered plateaus
        if (influence.influence > 0.8) {
            // High influence - use the dramatic colored terrain
            return getPlateauBlock(depthFromSurface, plateau.materialLayer);
        } else if (influence.influence > 0.4) {
            // Medium influence - blend with terracotta
            return getBlendedBlock(depthFromSurface, plateau.materialLayer);
        } else {
            // Low influence - subtle modifications to vanilla
            if (depthFromSurface == 0) {
                return Blocks.COARSE_DIRT.defaultBlockState();
            } else if (depthFromSurface <= 3) {
                return Blocks.DIRT.defaultBlockState();
            }
        }

        return Blocks.STONE.defaultBlockState();
    }

    /**
     * Get plateau block for high-influence areas
     */
    private static BlockState getPlateauBlock(int depthFromSurface, int materialLayer) {
        if (depthFromSurface == 0) {
            // Surface layer - dramatic colors
            return switch (materialLayer) {
                case 0 -> Blocks.CYAN_CONCRETE.defaultBlockState();
                case 1 -> Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState();
                case 2 -> Blocks.BLUE_CONCRETE.defaultBlockState();
                case 3 -> Blocks.PURPLE_CONCRETE.defaultBlockState();
                case 4 -> Blocks.MAGENTA_CONCRETE.defaultBlockState();
                case 5 -> Blocks.PINK_CONCRETE.defaultBlockState();
                default -> Blocks.WHITE_CONCRETE.defaultBlockState();
            };
        } else if (depthFromSurface <= 2) {
            // Subsurface
            return switch (materialLayer) {
                case 0, 1 -> Blocks.CYAN_TERRACOTTA.defaultBlockState();
                case 2, 3 -> Blocks.BLUE_TERRACOTTA.defaultBlockState();
                case 4, 5 -> Blocks.PURPLE_TERRACOTTA.defaultBlockState();
                default -> Blocks.GRAY_TERRACOTTA.defaultBlockState();
            };
        } else if (depthFromSurface <= 8) {
            // Deep subsurface
            return Blocks.STONE.defaultBlockState();
        }

        return Blocks.DEEPSLATE.defaultBlockState();
    }

    /**
     * Get blended block for medium-influence areas
     */
    private static BlockState getBlendedBlock(int depthFromSurface, int materialLayer) {
        if (depthFromSurface == 0) {
            return switch (materialLayer % 3) {
                case 0 -> Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
                case 1 -> Blocks.BLUE_TERRACOTTA.defaultBlockState();
                default -> Blocks.PURPLE_TERRACOTTA.defaultBlockState();
            };
        } else if (depthFromSurface <= 3) {
            return Blocks.CLAY.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
    }

    /**
     * Update all heightmaps for the chunk
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
     * Find the surface height at a position
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
     * Clear old cache entries to prevent memory leaks
     */
    public static void cleanupCache() {
        if (INFLUENCE_CACHE.size() > 10000) {
            INFLUENCE_CACHE.clear();
        }
    }

    // Data classes
    private record BiomeAnalysis(double maxInfluence, BiomeInfluenceData[][] influenceGrid) {
        public boolean hasCustomInfluence() {
            return maxInfluence > 0.01;
        }
    }

    private record BiomeInfluenceData(double influence, boolean hasCustomBiome) {}

    private record BiomeInfluenceCache(ChunkPos chunkPos, BiomeInfluenceData influence) {}

    private record PlateauData(int height, int materialLayer, boolean isIsland, double erosionFactor) {}
}