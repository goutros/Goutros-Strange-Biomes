package net.goutros.goutrosstrangebiomes.worldgen.terrain;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * NATURAL TERRAIN BLENDING SYSTEM
 * Creates smooth, geological-style transitions where Pillow layers curve
 * naturally into vanilla terrain instead of harsh cutoffs
 */
public class NaturalBlendingSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger("NaturalBlendingSystem");

    // Blending parameters for natural transitions
    private static final int INFLUENCE_RADIUS = 64; // Large radius for natural curves
    private static final int SAMPLE_STEP = 8; // Balance performance vs smoothness
    private static final double CURVE_STRENGTH = 0.8; // How much layers curve

    // Original Pillow parameters
    private static final int[] PILLOW_LAYER_HEIGHTS = {45, 55, 65, 75, 85, 95, 105};
    private static final int MIN_HEIGHT = 35;
    private static final int MAX_HEIGHT = 115;
    private static final int SEA_LEVEL = 63;

    // Noise generators for natural shaping
    private static final ThreadLocal<SimplexNoise> BIOME_DISTANCE_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(11111L)));
    private static final ThreadLocal<SimplexNoise> LAYER_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(22222L)));
    private static final ThreadLocal<SimplexNoise> DETAIL_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(33333L)));
    private static final ThreadLocal<SimplexNoise> CURVE_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(44444L)));
    private static final ThreadLocal<SimplexNoise> SHORE_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(55555L)));

    // Cache for expensive calculations
    private static final ConcurrentHashMap<Long, BiomeInfluenceResult> INFLUENCE_CACHE = new ConcurrentHashMap<>();

    /**
     * Apply natural terrain blending with geological-style transitions
     */
    public static void applyNaturalTerrainBlending(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();

        // Check if this chunk needs any Pillow influence
        ChunkInfluenceData chunkInfluence = analyzeChunkInfluence(chunk, chunkPos);

        if (chunkInfluence.maxInfluence < 0.01) {
            LOGGER.debug("No Pillow influence in chunk {}, {} - skipping", chunkPos.x, chunkPos.z);
            return;
        }

        LOGGER.info("Applying natural terrain blending to chunk {}, {} (max influence: {:.2f})",
                chunkPos.x, chunkPos.z, chunkInfluence.maxInfluence);

        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        // Process each position with natural blending
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                // Calculate natural influence and terrain data
                NaturalTerrainData terrainData = calculateNaturalTerrainData(chunk, x, z, worldX, worldZ);

                if (terrainData.influence > 0.01) {
                    generateNaturalTerrainColumn(chunk, x, z, worldX, worldZ, terrainData, minY, maxY);
                }
            }
        }

        updateHeightmaps(chunk);

        // Cleanup cache
        if (INFLUENCE_CACHE.size() > 2000) {
            INFLUENCE_CACHE.clear();
        }
    }

    /**
     * Analyze chunk for Pillow influence in surrounding area
     */
    private static ChunkInfluenceData analyzeChunkInfluence(ChunkAccess chunk, ChunkPos chunkPos) {
        double maxInfluence = 0.0;
        int totalChecks = 0;
        int influenceCount = 0;

        // Check this chunk and surrounding area
        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                totalChecks++;
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                double influence = calculateBiomeInfluenceAtPosition(worldX, worldZ);
                if (influence > 0.01) {
                    influenceCount++;
                    maxInfluence = Math.max(maxInfluence, influence);
                }
            }
        }

        return new ChunkInfluenceData(maxInfluence, influenceCount, totalChecks);
    }

    /**
     * Calculate natural terrain data for a position
     */
    private static NaturalTerrainData calculateNaturalTerrainData(ChunkAccess chunk, int chunkX, int chunkZ,
                                                                  int worldX, int worldZ) {

        // Get current vanilla height
        int vanillaHeight = chunk.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, chunkX, chunkZ);

        // Calculate biome influence with natural curves
        double biomeInfluence = calculateBiomeInfluenceAtPosition(worldX, worldZ);

        if (biomeInfluence < 0.01) {
            return new NaturalTerrainData(0.0, vanillaHeight, vanillaHeight, 0, false, false);
        }

        // Generate Pillow terrain data
        PillowTerrainResult pillowTerrain = generateCurvedPillowTerrain(worldX, worldZ, biomeInfluence);

        // Calculate natural blending
        double curveInfluence = calculateCurveInfluence(worldX, worldZ, biomeInfluence);
        double finalInfluence = biomeInfluence * curveInfluence;

        // Blend heights naturally
        int targetHeight = calculateNaturalBlendedHeight(vanillaHeight, pillowTerrain.height, finalInfluence, worldX, worldZ);

        // Check for water features
        boolean isRiver = checkForRiver(worldX, worldZ, pillowTerrain.layerIndex);
        boolean isLake = checkForLake(worldX, worldZ, pillowTerrain.layerIndex);

        return new NaturalTerrainData(finalInfluence, vanillaHeight, targetHeight,
                pillowTerrain.layerIndex, isRiver, isLake);
    }

    /**
     * Calculate biome influence at a world position with smooth falloff
     */
    private static double calculateBiomeInfluenceAtPosition(int worldX, int worldZ) {
        long cacheKey = ((long) worldX << 32) | (worldZ & 0xFFFFFFFFL);

        BiomeInfluenceResult cached = INFLUENCE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached.influence();
        }

        double totalInfluence = 0.0;
        double totalWeight = 0.0;

        // Sample in a large radius for smooth influence
        for (int dx = -INFLUENCE_RADIUS; dx <= INFLUENCE_RADIUS; dx += SAMPLE_STEP) {
            for (int dz = -INFLUENCE_RADIUS; dz <= INFLUENCE_RADIUS; dz += SAMPLE_STEP) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > INFLUENCE_RADIUS) continue;

                // Use smooth falloff curve
                double weight = 1.0 - (distance / INFLUENCE_RADIUS);
                weight = smoothstep(weight); // Smooth S-curve

                // Check if this position is likely to have Pillow biome
                if (isPillowBiomeAtWorldPosition(worldX + dx, worldZ + dz)) {
                    totalInfluence += weight;
                }

                totalWeight += weight;
            }
        }

        double finalInfluence = totalWeight > 0 ? totalInfluence / totalWeight : 0.0;

        // Add noise variation for natural boundaries
        double boundaryNoise = BIOME_DISTANCE_NOISE.get().getValue(worldX * 0.005, worldZ * 0.005);
        boundaryNoise = (boundaryNoise + 1.0) * 0.5; // Normalize to 0-1

        // Modify influence based on noise for natural variation
        finalInfluence *= (0.7 + 0.6 * boundaryNoise); // Vary influence naturally
        finalInfluence = Mth.clamp(finalInfluence, 0.0, 1.0);

        BiomeInfluenceResult result = new BiomeInfluenceResult(finalInfluence);
        INFLUENCE_CACHE.put(cacheKey, result);

        return finalInfluence;
    }

    /**
     * Generate curved Pillow terrain that follows natural contours
     */
    private static PillowTerrainResult generateCurvedPillowTerrain(int worldX, int worldZ, double biomeInfluence) {
        // Layer selection with curvature influence
        double layerNoise = LAYER_NOISE.get().getValue(worldX * 0.008, worldZ * 0.008);
        layerNoise = (layerNoise + 1.0) * 0.5;

        // Modify layer selection based on distance from biome center
        layerNoise *= (0.5 + 0.5 * biomeInfluence); // Layers get lower towards edges

        int layerIndex = (int) (layerNoise * PILLOW_LAYER_HEIGHTS.length);
        layerIndex = Mth.clamp(layerIndex, 0, PILLOW_LAYER_HEIGHTS.length - 1);

        int baseHeight = PILLOW_LAYER_HEIGHTS[layerIndex];

        // Detail variation that respects biome influence
        double detailNoise = DETAIL_NOISE.get().getValue(worldX * 0.02, worldZ * 0.02);
        detailNoise = (detailNoise + 1.0) * 0.5;

        int variation = (int) ((detailNoise - 0.5) * 6 * biomeInfluence); // Less variation at edges
        int finalHeight = baseHeight + variation;

        finalHeight = Mth.clamp(finalHeight, MIN_HEIGHT, MAX_HEIGHT);

        return new PillowTerrainResult(finalHeight, layerIndex);
    }

    /**
     * Calculate curve influence for natural layer transitions
     */
    private static double calculateCurveInfluence(int worldX, int worldZ, double baseInfluence) {
        // Add curvature noise to make layers flow naturally
        double curveNoise = CURVE_NOISE.get().getValue(worldX * 0.015, worldZ * 0.015);
        curveNoise = (curveNoise + 1.0) * 0.5;

        // Create natural curves that follow terrain
        double curveModifier = 0.7 + 0.6 * curveNoise;

        return baseInfluence * curveModifier * CURVE_STRENGTH + baseInfluence * (1.0 - CURVE_STRENGTH);
    }

    /**
     * Calculate naturally blended height with smooth transitions
     */
    private static int calculateNaturalBlendedHeight(int vanillaHeight, int pillowHeight,
                                                     double influence, int worldX, int worldZ) {

        // Use smooth interpolation
        double smoothInfluence = smoothstep(influence);

        // Add shore-like variation for natural transitions
        double shoreNoise = SHORE_NOISE.get().getValue(worldX * 0.03, worldZ * 0.03);
        shoreNoise = (shoreNoise + 1.0) * 0.5;

        // Create natural height variation
        int heightDifference = pillowHeight - vanillaHeight;
        double shoreVariation = heightDifference * 0.2 * shoreNoise * influence;

        double blendedHeight = Mth.lerp(smoothInfluence, vanillaHeight, pillowHeight) + shoreVariation;

        return (int) Math.round(blendedHeight);
    }

    /**
     * Generate natural terrain column with smooth material transitions
     */
    private static void generateNaturalTerrainColumn(ChunkAccess chunk, int x, int z, int worldX, int worldZ,
                                                     NaturalTerrainData terrainData, int minY, int maxY) {

        int startHeight = Math.min(terrainData.vanillaHeight, terrainData.targetHeight) - 10;
        int endHeight = Math.max(terrainData.vanillaHeight, terrainData.targetHeight) + 5;

        startHeight = Math.max(startHeight, minY);
        endHeight = Math.min(endHeight, maxY - 1);

        // Generate natural terrain layers
        for (int y = startHeight; y <= endHeight; y++) {
            BlockPos pos = new BlockPos(x, y, z);

            if (y <= terrainData.targetHeight) {
                BlockState blockState = getNaturalBlockState(y, terrainData, worldX, worldZ);
                chunk.setBlockState(pos, blockState, false);
            } else {
                chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
            }
        }

        // Handle water features naturally
        if (terrainData.isRiver || terrainData.isLake || terrainData.targetHeight < SEA_LEVEL) {
            handleNaturalWaterFeatures(chunk, x, z, terrainData, maxY);
        }
    }

    /**
     * Get natural block state with smooth material transitions
     */
    private static BlockState getNaturalBlockState(int y, NaturalTerrainData terrainData, int worldX, int worldZ) {
        int depthFromSurface = terrainData.targetHeight - y;
        double influence = terrainData.influence;

        // Handle rivers and lakes
        if (terrainData.isRiver && depthFromSurface <= 1) {
            return getPillowRiverBlock(terrainData.layerIndex);
        }

        if (terrainData.isLake && y >= terrainData.targetHeight - 3) {
            return Blocks.WATER.defaultBlockState();
        }

        // Natural material transitions based on influence
        if (influence > 0.8) {
            // High influence - full Pillow materials
            return getFullPillowBlock(depthFromSurface, terrainData.layerIndex);

        } else if (influence > 0.5) {
            // Medium influence - transitional materials
            return getTransitionalBlock(depthFromSurface, terrainData.layerIndex, influence, worldX, worldZ);

        } else if (influence > 0.2) {
            // Low influence - subtle hints
            return getSubtleBlock(depthFromSurface, influence, worldX, worldZ);

        } else {
            // Very low influence - mostly vanilla with hints
            return getVanillaWithHints(depthFromSurface, influence);
        }
    }

    /**
     * Get full Pillow block for high influence areas
     */
    private static BlockState getFullPillowBlock(int depth, int layerIndex) {
        if (depth == 0) {
            return switch (layerIndex) {
                case 0 -> Blocks.CYAN_WOOL.defaultBlockState();
                case 1 -> Blocks.LIGHT_BLUE_WOOL.defaultBlockState();
                case 2 -> Blocks.BLUE_WOOL.defaultBlockState();
                case 3 -> Blocks.PURPLE_WOOL.defaultBlockState();
                case 4 -> Blocks.MAGENTA_WOOL.defaultBlockState();
                case 5 -> Blocks.PINK_WOOL.defaultBlockState();
                case 6 -> Blocks.WHITE_WOOL.defaultBlockState();
                default -> Blocks.GRAY_WOOL.defaultBlockState();
            };
        } else if (depth <= 3) {
            return switch (layerIndex / 2) {
                case 0 -> Blocks.CYAN_WOOL.defaultBlockState();
                case 1 -> Blocks.BLUE_WOOL.defaultBlockState();
                case 2 -> Blocks.PURPLE_WOOL.defaultBlockState();
                case 3 -> Blocks.MAGENTA_WOOL.defaultBlockState();
                default -> Blocks.GRAY_WOOL.defaultBlockState();
            };
        } else if (depth <= 8) {
            return Blocks.PURPLE_WOOL.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
    }

    /**
     * Get transitional block for medium influence areas
     */
    private static BlockState getTransitionalBlock(int depth, int layerIndex, double influence, int worldX, int worldZ) {
        // Add noise for natural variation
        double materialNoise = DETAIL_NOISE.get().getValue(worldX * 0.1, worldZ * 0.1);
        materialNoise = (materialNoise + 1.0) * 0.5;

        if (depth == 0) {
            if (influence > 0.7 && materialNoise > 0.3) {
                return layerIndex % 2 == 0 ?
                        ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState() :
                        ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState();
            }
            return Blocks.COARSE_DIRT.defaultBlockState();
        } else if (depth <= 2) {
            return ModBlocks.PILLOW_DIRT.get().defaultBlockState();
        } else if (depth <= 6) {
            // Natural terracotta transitions
            if (materialNoise > 0.6) {
                return switch (layerIndex % 3) {
                    case 0 -> Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
                    case 1 -> Blocks.PURPLE_TERRACOTTA.defaultBlockState();
                    default -> Blocks.MAGENTA_TERRACOTTA.defaultBlockState();
                };
            }
            return Blocks.DIRT.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
    }

    /**
     * Get subtle block for low influence areas
     */
    private static BlockState getSubtleBlock(int depth, double influence, int worldX, int worldZ) {
        double subtleNoise = SHORE_NOISE.get().getValue(worldX * 0.05, worldZ * 0.05);
        subtleNoise = (subtleNoise + 1.0) * 0.5;

        if (depth == 0) {
            return subtleNoise > 0.5 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.DIRT.defaultBlockState();
        } else if (depth <= 4) {
            return Blocks.DIRT.defaultBlockState();
        } else if (depth <= 8 && subtleNoise > 0.7) {
            return Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
    }

    /**
     * Get vanilla with hints for very low influence
     */
    private static BlockState getVanillaWithHints(int depth, double influence) {
        if (depth == 0 && influence > 0.15) {
            return Blocks.COARSE_DIRT.defaultBlockState();
        } else if (depth <= 3) {
            return Blocks.DIRT.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
    }

    /**
     * Get Pillow river block
     */
    private static BlockState getPillowRiverBlock(int layerIndex) {
        return switch (layerIndex) {
            case 0 -> Blocks.LIGHT_BLUE_WOOL.defaultBlockState();
            case 1 -> Blocks.BLUE_WOOL.defaultBlockState();
            case 2 -> Blocks.PURPLE_WOOL.defaultBlockState();
            case 3 -> Blocks.MAGENTA_WOOL.defaultBlockState();
            case 4 -> Blocks.PINK_WOOL.defaultBlockState();
            case 5 -> Blocks.MAGENTA_WOOL.defaultBlockState();
            case 6 -> Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
            default -> Blocks.GRAY_WOOL.defaultBlockState();
        };
    }

    /**
     * Handle natural water features
     */
    private static void handleNaturalWaterFeatures(ChunkAccess chunk, int x, int z,
                                                   NaturalTerrainData terrainData, int maxY) {

        int waterStart = terrainData.targetHeight + 1;
        int waterEnd = terrainData.isLake ? terrainData.targetHeight + 4 : SEA_LEVEL;

        for (int y = waterStart; y <= waterEnd && y < maxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (chunk.getBlockState(pos).isAir()) {
                chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
            }
        }
    }

    /**
     * Check for rivers
     */
    private static boolean checkForRiver(int worldX, int worldZ, int layerIndex) {
        double riverNoise = CURVE_NOISE.get().getValue(worldX * 0.010, worldZ * 0.010);
        riverNoise = (riverNoise + 1.0) * 0.5;

        boolean isRiverHeight = (layerIndex >= 2 && layerIndex <= 4);
        return isRiverHeight && Math.abs(riverNoise - 0.5) < 0.02;
    }

    /**
     * Check for lakes
     */
    private static boolean checkForLake(int worldX, int worldZ, int layerIndex) {
        if (layerIndex > 2) return false;

        double lakeNoise = SHORE_NOISE.get().getValue(worldX * 0.012, worldZ * 0.012);
        lakeNoise = (lakeNoise + 1.0) * 0.5;

        return lakeNoise > 0.85;
    }

    /**
     * Check if position is likely Pillow biome
     */
    private static boolean isPillowBiomeAtWorldPosition(int worldX, int worldZ) {
        // Approximate biome check using climate-like noise
        double tempNoise = LAYER_NOISE.get().getValue(worldX * 0.0025, worldZ * 0.0025);
        double humidityNoise = DETAIL_NOISE.get().getValue(worldX * 0.003, worldZ * 0.003);
        double weirdnessNoise = CURVE_NOISE.get().getValue(worldX * 0.001, worldZ * 0.001);

        tempNoise = (tempNoise + 1.0) * 0.5;
        humidityNoise = (humidityNoise + 1.0) * 0.5;
        weirdnessNoise = (weirdnessNoise + 1.0) * 0.5;

        boolean tempMatch = tempNoise >= 0.4 && tempNoise <= 0.6;
        boolean humidityMatch = humidityNoise >= 0.6;
        boolean weirdnessMatch = weirdnessNoise >= 0.7;

        return tempMatch && humidityMatch && weirdnessMatch;
    }

    /**
     * Smooth step function for natural curves
     */
    private static double smoothstep(double x) {
        x = Mth.clamp(x, 0.0, 1.0);
        return x * x * (3.0 - 2.0 * x);
    }

    /**
     * Update heightmaps
     */
    private static void updateHeightmaps(ChunkAccess chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = findSurfaceHeight(chunk, x, z);
                updateHeightmapsAt(chunk, x, z, height);
            }
        }
    }

    private static int findSurfaceHeight(ChunkAccess chunk, int x, int z) {
        for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
            if (!chunk.getBlockState(new BlockPos(x, y, z)).isAir()) {
                return y;
            }
        }
        return chunk.getMinBuildHeight();
    }

    private static void updateHeightmapsAt(ChunkAccess chunk, int x, int z, int height) {
        BlockPos pos = new BlockPos(x, height, z);
        BlockState state = chunk.getBlockState(pos);

        chunk.getOrCreateHeightmapUnprimed(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG)
                .update(x, height + 1, z, state);
        chunk.getOrCreateHeightmapUnprimed(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING)
                .update(x, height + 1, z, state);
        chunk.getOrCreateHeightmapUnprimed(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES)
                .update(x, height + 1, z, state);
        chunk.getOrCreateHeightmapUnprimed(net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG)
                .update(x, height + 1, z, state);
    }

    // Data classes
    private record ChunkInfluenceData(double maxInfluence, int influenceCount, int totalChecks) {}
    private record BiomeInfluenceResult(double influence) {}
    private record PillowTerrainResult(int height, int layerIndex) {}
    private record NaturalTerrainData(double influence, int vanillaHeight, int targetHeight,
                                      int layerIndex, boolean isRiver, boolean isLake) {}
}