package net.goutros.goutrosstrangebiomes.worldgen.terrain;

import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

/**
 * TERRAIN FLOW ANALYZER
 *
 * Analyzes vanilla terrain height patterns to create natural canyon formations
 * that follow water flow and elevation patterns.
 */
public class TerrainFlowAnalyzer {

    // Noise generators for flow variation
    private static SimplexNoise FLOW_NOISE;
    private static SimplexNoise CANYON_DETAIL_NOISE;
    private static SimplexNoise LAYER_NOISE;
    private static SimplexNoise GOLDEN_NOISE;

    static {
        try {
            FLOW_NOISE = new SimplexNoise(RandomSource.create(77777L));
            CANYON_DETAIL_NOISE = new SimplexNoise(RandomSource.create(88888L));
            LAYER_NOISE = new SimplexNoise(RandomSource.create(99999L));
            GOLDEN_NOISE = new SimplexNoise(RandomSource.create(11111L));
        } catch (Exception e) {
            RandomSource source = RandomSource.create(77777L);
            FLOW_NOISE = new SimplexNoise(source);
            CANYON_DETAIL_NOISE = new SimplexNoise(RandomSource.create(source.nextLong()));
            LAYER_NOISE = new SimplexNoise(RandomSource.create(source.nextLong()));
            GOLDEN_NOISE = new SimplexNoise(RandomSource.create(source.nextLong()));
        }
    }

    private static final int SEA_LEVEL = 63;

    /**
     * Container for terrain flow analysis results
     */
    public static class TerrainFlow {
        public final double flowStrength;
        public final double isValley;
        public final double isRidge;
        public final double slope;
        public final int lowestNeighbor;
        public final boolean isWaterPath;

        public TerrainFlow(double flowStrength, double isValley, double isRidge, double slope,
                           int lowestNeighbor, boolean isWaterPath) {
            this.flowStrength = flowStrength;
            this.isValley = isValley;
            this.isRidge = isRidge;
            this.slope = slope;
            this.lowestNeighbor = lowestNeighbor;
            this.isWaterPath = isWaterPath;
        }
    }

    /**
     * Main entry point - process entire chunk for terrain flow canyons
     */
    public static void processChunkTerrainFlow(ChunkAccess chunk) {
        try {
            if (chunk == null) return;

            // Quick check for pillow biome presence
            if (!hasPillowBiomeInfluence(chunk)) return;

            ChunkPos chunkPos = chunk.getPos();

            // Build height map of chunk and surrounding area for flow analysis
            int[][] heightMap = buildExtendedHeightMap(chunk);

            // Process each position in the chunk
            for (int localX = 0; localX < 16; localX++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    int worldX = chunkPos.getMinBlockX() + localX;
                    int worldZ = chunkPos.getMinBlockZ() + localZ;

                    processTerrainFlowPosition(chunk, heightMap, localX, localZ, worldX, worldZ);
                }
            }

        } catch (Exception e) {
            // Fail silently
        }
    }

    /**
     * Check if chunk has pillow biome influence
     */
    private static boolean hasPillowBiomeInfluence(ChunkAccess chunk) {
        try {
            for (int x = 1; x < 4; x++) {
                for (int z = 1; z < 4; z++) {
                    for (int y = 2; y < 5; y++) {
                        Holder<Biome> biome = chunk.getNoiseBiome(x, y, z);
                        ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);
                        if (biomeKey != null && biomeKey.equals(ModBiomes.PILLOW_PLATEAU)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Build extended height map including neighboring areas for flow calculation
     */
    private static int[][] buildExtendedHeightMap(ChunkAccess chunk) {
        int[][] heightMap = new int[24][24]; // 16 + 4 on each side for context

        try {
            // Fill center area from chunk data
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    heightMap[x + 4][z + 4] = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                }
            }

            // Estimate surrounding areas using noise and interpolation
            ChunkPos chunkPos = chunk.getPos();
            for (int x = 0; x < 24; x++) {
                for (int z = 0; z < 24; z++) {
                    if (x >= 4 && x < 20 && z >= 4 && z < 20) continue; // Skip center area

                    int worldX = chunkPos.getMinBlockX() + x - 4;
                    int worldZ = chunkPos.getMinBlockZ() + z - 4;

                    // Estimate height using nearby chunk data and noise
                    double estimatedHeight = estimateHeightAtPosition(worldX, worldZ, heightMap, x, z);
                    heightMap[x][z] = (int)estimatedHeight;
                }
            }

        } catch (Exception e) {
            // Fallback: use current chunk center height for all positions
            int centerHeight = heightMap[12][12];
            for (int x = 0; x < 24; x++) {
                for (int z = 0; z < 24; z++) {
                    if (heightMap[x][z] == 0) {
                        heightMap[x][z] = centerHeight;
                    }
                }
            }
        }

        return heightMap;
    }

    /**
     * Estimate height at position using interpolation and noise
     */
    private static double estimateHeightAtPosition(int worldX, int worldZ, int[][] heightMap, int mapX, int mapZ) {
        try {
            // Find nearest known heights for interpolation
            double totalWeight = 0.0;
            double weightedHeight = 0.0;

            for (int x = 4; x < 20; x++) {
                for (int z = 4; z < 20; z++) {
                    if (heightMap[x][z] > 0) {
                        double distance = Math.sqrt((x - mapX) * (x - mapX) + (z - mapZ) * (z - mapZ));
                        if (distance > 0) {
                            double weight = 1.0 / (distance * distance);
                            weightedHeight += heightMap[x][z] * weight;
                            totalWeight += weight;
                        }
                    }
                }
            }

            if (totalWeight > 0) {
                double baseHeight = weightedHeight / totalWeight;

                // Add terrain noise variation
                double terrainNoise = FLOW_NOISE.getValue(worldX * 0.01, worldZ * 0.01);
                return baseHeight + terrainNoise * 8.0;
            }

            return SEA_LEVEL + 10; // Fallback

        } catch (Exception e) {
            return SEA_LEVEL + 10;
        }
    }

    /**
     * Process individual position using terrain flow analysis
     */
    private static void processTerrainFlowPosition(ChunkAccess chunk, int[][] heightMap,
                                                   int localX, int localZ, int worldX, int worldZ) {
        try {
            int mapX = localX + 4; // Offset for extended map
            int mapZ = localZ + 4;

            int currentHeight = heightMap[mapX][mapZ];

            // Skip very low areas (likely water)
            if (currentHeight <= SEA_LEVEL + 2) return;

            // Calculate terrain flow characteristics
            TerrainFlow flow = analyzeTerrainFlow(heightMap, mapX, mapZ, worldX, worldZ);

            // Get biome influence
            double biomeInfluence = calculateBiomeInfluence(chunk, localX, localZ, worldX, worldZ);

            if (biomeInfluence < 0.1) return;

            // Apply canyon modifications based on flow
            applyCanyonModifications(chunk, localX, localZ, worldX, worldZ, currentHeight, flow, biomeInfluence);

        } catch (Exception e) {
            // Skip on error
        }
    }

    /**
     * Analyze terrain flow patterns around this position
     */
    public static TerrainFlow analyzeTerrainFlow(int[][] heightMap, int mapX, int mapZ, int worldX, int worldZ) {
        try {
            int centerHeight = heightMap[mapX][mapZ];

            // Check 8 directions around current position
            int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
            int[] dz = {-1, 0, 1, -1, 1, -1, 0, 1};

            int lowerCount = 0;
            int higherCount = 0;
            int totalHeightDiff = 0;
            int lowestNeighbor = centerHeight;
            int maxHeightDiff = 0;

            for (int i = 0; i < 8; i++) {
                int checkX = mapX + dx[i];
                int checkZ = mapZ + dz[i];

                if (checkX >= 0 && checkX < 24 && checkZ >= 0 && checkZ < 24) {
                    int neighborHeight = heightMap[checkX][checkZ];
                    int heightDiff = centerHeight - neighborHeight;

                    if (neighborHeight < centerHeight) {
                        lowerCount++;
                        totalHeightDiff += heightDiff;
                        maxHeightDiff = Math.max(maxHeightDiff, heightDiff);
                    } else if (neighborHeight > centerHeight) {
                        higherCount++;
                    }

                    lowestNeighbor = Math.min(lowestNeighbor, neighborHeight);
                }
            }

            // Calculate flow characteristics
            double flowStrength = totalHeightDiff / 8.0; // Average height difference
            double isValley = (higherCount >= 5) ? 1.0 : 0.0; // Surrounded by higher terrain
            double isRidge = (lowerCount >= 5) ? 1.0 : 0.0; // Surrounded by lower terrain
            double slope = maxHeightDiff / 8.0; // Steepest slope

            // Determine if this is likely a water flow path
            boolean isWaterPath = (lowerCount >= 3 && flowStrength > 2.0) ||
                    (isValley > 0.5 && flowStrength > 1.0);

            // Add noise variation to flow
            double flowNoise = FLOW_NOISE.getValue(worldX * 0.008, worldZ * 0.008);
            flowStrength += flowNoise * 2.0;

            return new TerrainFlow(flowStrength, isValley, isRidge, slope, lowestNeighbor, isWaterPath);

        } catch (Exception e) {
            return new TerrainFlow(0, 0, 0, 0, SEA_LEVEL, false);
        }
    }

    /**
     * Calculate biome influence for this position
     */
    private static double calculateBiomeInfluence(ChunkAccess chunk, int localX, int localZ, int worldX, int worldZ) {
        try {
            int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, localX, localZ);
            Holder<Biome> biome = chunk.getNoiseBiome(localX >> 2, surfaceY >> 2, localZ >> 2);
            ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);

            if (biomeKey != null && biomeKey.equals(ModBiomes.PILLOW_PLATEAU)) {
                return 1.0; // Full influence in pillow biome
            } else {
                // Transition areas - use noise for smooth edges
                double transitionNoise = CANYON_DETAIL_NOISE.getValue(worldX * 0.01, worldZ * 0.01);
                transitionNoise = (transitionNoise + 1.0) * 0.5;
                return transitionNoise > 0.6 ? (transitionNoise - 0.6) * 2.5 : 0.0;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Apply canyon modifications based on terrain flow analysis
     */
    private static void applyCanyonModifications(ChunkAccess chunk, int localX, int localZ,
                                                 int worldX, int worldZ, int currentHeight,
                                                 TerrainFlow flow, double biomeInfluence) {
        try {
            // Determine canyon type and depth based on flow characteristics
            int targetHeight = currentHeight;
            int canyonType = 0; // 0=none, 1=river_canyon, 2=mesa_step, 3=plateau

            if (flow.isWaterPath && flow.flowStrength > 3.0) {
                // River canyon - carve deep along water flow paths
                int carveDepth = (int)(6 + flow.flowStrength * 0.8) * (int)Math.ceil(biomeInfluence * 0.8);
                targetHeight = Math.max(flow.lowestNeighbor, currentHeight - carveDepth);
                canyonType = 1;

            } else if (flow.isValley > 0.5) {
                // Valley floor - moderate carving
                int valleyDepth = (int)(3 + flow.flowStrength * 0.5) * (int)Math.ceil(biomeInfluence * 0.6);
                targetHeight = Math.max(flow.lowestNeighbor + 2, currentHeight - valleyDepth);
                canyonType = 1;

            } else if (flow.isRidge > 0.5) {
                // Ridge/plateau - build up with mesa steps
                double layerNoise = LAYER_NOISE.getValue(worldX * 0.006, worldZ * 0.006);
                int layerHeight = 4 + (int)(layerNoise * 3);
                int currentLayer = (currentHeight - SEA_LEVEL) / layerHeight;
                targetHeight = SEA_LEVEL + (currentLayer + 1) * layerHeight;
                canyonType = 3;

            } else if (flow.slope > 2.0) {
                // Mesa/stepped terrain for medium slopes
                double layerNoise = LAYER_NOISE.getValue(worldX * 0.008, worldZ * 0.008);
                int layerHeight = 3;
                int currentLayer = (currentHeight - SEA_LEVEL) / layerHeight;
                currentLayer += (int)(layerNoise * 2 - 1); // Vary layer
                targetHeight = SEA_LEVEL + currentLayer * layerHeight;
                canyonType = 2;
            }

            // Blend with vanilla terrain based on influence
            targetHeight = (int)Mth.lerp(biomeInfluence, currentHeight, targetHeight);

            // Apply block modifications
            applyBlockModifications(chunk, localX, localZ, worldX, worldZ, currentHeight, targetHeight, canyonType, biomeInfluence);

        } catch (Exception e) {
            // Skip on error
        }
    }

    /**
     * Apply actual block modifications to create canyon terrain
     */
    private static void applyBlockModifications(ChunkAccess chunk, int localX, int localZ, int worldX, int worldZ,
                                                int originalHeight, int targetHeight, int canyonType, double biomeInfluence) {
        try {
            int minY = Math.max(chunk.getMinBuildHeight(), SEA_LEVEL - 10);
            int maxY = Math.min(chunk.getMaxBuildHeight() - 5, Math.max(originalHeight, targetHeight) + 8);

            for (int y = minY; y <= maxY; y++) {
                BlockPos pos = new BlockPos(localX, y, localZ);
                BlockState currentState = chunk.getBlockState(pos);

                BlockState newState = determineBlockState(worldX, worldZ, y, targetHeight, canyonType, biomeInfluence);

                if (newState != null && shouldReplaceBlock(currentState, biomeInfluence)) {
                    chunk.setBlockState(pos, newState, false);
                }
            }
        } catch (Exception e) {
            // Skip on error
        }
    }

    /**
     * Determine what block should be placed at this position
     */
    private static BlockState determineBlockState(int worldX, int worldZ, int y, int targetHeight,
                                                  int canyonType, double biomeInfluence) {
        try {
            if (y > targetHeight) {
                // Above target - air for canyon carving
                return biomeInfluence > 0.3 ? Blocks.AIR.defaultBlockState() : null;
            }

            if (y == targetHeight) {
                // Surface layer
                if (biomeInfluence > 0.6) {
                    double goldenChance = GOLDEN_NOISE.getValue(worldX * 0.025, worldZ * 0.025);
                    if ((canyonType == 1 && goldenChance > -0.2) || goldenChance > 0.1) {
                        return ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState();
                    } else {
                        return ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState();
                    }
                } else if (biomeInfluence > 0.3) {
                    return ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState();
                } else {
                    return Blocks.COARSE_DIRT.defaultBlockState();
                }
            }

            if (y >= targetHeight - 2) {
                // Subsurface
                if (biomeInfluence > 0.5) {
                    return ModBlocks.PILLOW_DIRT.get().defaultBlockState();
                } else if (biomeInfluence > 0.3) {
                    return Blocks.COARSE_DIRT.defaultBlockState();
                } else {
                    return Blocks.DIRT.defaultBlockState();
                }
            }

            if (y >= targetHeight - 10 && biomeInfluence > 0.5) {
                // Colorful canyon layers
                int layerIndex = (targetHeight - y - 2) / 2;

                if (canyonType == 1) {
                    // River canyon - water-inspired colors
                    int colorIndex = layerIndex % 6;
                    return switch (colorIndex) {
                        case 0 -> Blocks.LIGHT_BLUE_WOOL.defaultBlockState();
                        case 1 -> Blocks.CYAN_WOOL.defaultBlockState();
                        case 2 -> Blocks.BLUE_WOOL.defaultBlockState();
                        case 3 -> Blocks.PURPLE_WOOL.defaultBlockState();
                        case 4 -> Blocks.MAGENTA_WOOL.defaultBlockState();
                        case 5 -> Blocks.PINK_WOOL.defaultBlockState();
                        default -> Blocks.WHITE_WOOL.defaultBlockState();
                    };
                } else if (canyonType == 2) {
                    // Mesa - earth tones
                    int colorIndex = layerIndex % 5;
                    return switch (colorIndex) {
                        case 0 -> Blocks.YELLOW_WOOL.defaultBlockState();
                        case 1 -> Blocks.ORANGE_WOOL.defaultBlockState();
                        case 2 -> Blocks.RED_WOOL.defaultBlockState();
                        case 3 -> Blocks.BROWN_WOOL.defaultBlockState();
                        case 4 -> Blocks.LIME_WOOL.defaultBlockState();
                        default -> Blocks.GREEN_WOOL.defaultBlockState();
                    };
                } else {
                    // Plateau - mixed colors
                    int colorIndex = layerIndex % 4;
                    return switch (colorIndex) {
                        case 0 -> Blocks.LIME_WOOL.defaultBlockState();
                        case 1 -> Blocks.GREEN_WOOL.defaultBlockState();
                        case 2 -> Blocks.YELLOW_WOOL.defaultBlockState();
                        case 3 -> Blocks.ORANGE_WOOL.defaultBlockState();
                        default -> Blocks.WHITE_WOOL.defaultBlockState();
                    };
                }
            }

            return null; // Keep vanilla for deep areas

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if block should be replaced
     */
    private static boolean shouldReplaceBlock(BlockState state, double biomeInfluence) {
        if (biomeInfluence > 0.6) {
            return state.is(Blocks.STONE) || state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
                    state.is(Blocks.COARSE_DIRT) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.ANDESITE) ||
                    state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRAVEL) ||
                    state.is(Blocks.SAND) || state.isAir();
        } else if (biomeInfluence > 0.3) {
            return state.is(Blocks.STONE) || state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
                    state.is(Blocks.COARSE_DIRT) || state.isAir();
        } else {
            return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.isAir();
        }
    }
}