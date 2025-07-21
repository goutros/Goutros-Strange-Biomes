package net.goutros.goutrosstrangebiomes.worldgen.terrain;

import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes.LOGGER;

/**
 * REFINED TERRAIN RESCULPTOR
 *
 * This now works as a refinement pass that adds details and fixes any issues
 * that the main mixin might have missed, rather than doing the primary generation.
 */
public class TerrainResculptor {

    private static final Map<Long, Float> HEIGHT_CACHE = new ConcurrentHashMap<>();
    private static final SimplexNoise DETAIL_NOISE = new SimplexNoise(RandomSource.create(3001123L));
    private static final SimplexNoise FEATURE_NOISE = new SimplexNoise(RandomSource.create(4001123L));

    public static boolean resculptTerrain(ChunkAccess chunk, RandomState randomState, NoiseGeneratorSettings noiseSettings) {
        ChunkPos pos = chunk.getPos();
        boolean didModifyTerrain = false;
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();
        int seaLevel = noiseSettings.seaLevel();

        // Check if this chunk has any Pillow Plateau biome
        boolean hasPillowBiome = false;
        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                int sampleY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                ResourceKey<Biome> biomeKey = chunk.getNoiseBiome(x >> 2, sampleY >> 2, z >> 2).unwrapKey().orElse(null);

                if (biomeKey != null && biomeKey.equals(ModBiomes.PILLOW_PLATEAU)) {
                    hasPillowBiome = true;
                    break;
                }
            }
            if (hasPillowBiome) break;
        }

        if (!hasPillowBiome) {
            return false; // No Pillow Plateau in this chunk
        }

        // Refinement pass - add details and fix issues
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = pos.getMinBlockX() + x;
                int wz = pos.getMinBlockZ() + z;
                int sampleY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                ResourceKey<Biome> biomeKey = chunk.getNoiseBiome(x >> 2, sampleY >> 2, z >> 2).unwrapKey().orElse(null);

                if (biomeKey != null && biomeKey.equals(ModBiomes.PILLOW_PLATEAU)) {
                    // Add decorative features and details
                    if (addPillowDetails(chunk, x, z, wx, wz, minY, maxY, seaLevel)) {
                        didModifyTerrain = true;
                    }
                }
            }
        }

        // Update heightmaps if we modified terrain
        if (didModifyTerrain) {
            updateChunkHeightmaps(chunk);
        }

        return didModifyTerrain;
    }

    /**
     * Add decorative details to Pillow Plateau terrain
     */
    private static boolean addPillowDetails(ChunkAccess chunk, int x, int z, int worldX, int worldZ,
                                            int minY, int maxY, int seaLevel) {
        boolean modified = false;

        // Get current surface height
        int surfaceY = findSurfaceHeight(chunk, x, z, maxY, minY);

        // Add small decorative features
        double featureNoise = FEATURE_NOISE.getValue(worldX * 0.1, worldZ * 0.1);

        if (featureNoise > 0.7) {
            // Add small pillow "buttons" on surface
            if (addButtonFeature(chunk, x, z, surfaceY + 1)) {
                modified = true;
            }
        } else if (featureNoise < -0.7) {
            // Add small depressions
            if (addDepressionFeature(chunk, x, z, surfaceY)) {
                modified = true;
            }
        }

        // Add detail variation to subsurface
        if (addSubsurfaceDetails(chunk, x, z, surfaceY, worldX, worldZ)) {
            modified = true;
        }

        return modified;
    }

    /**
     * Add small button-like features on the surface
     */
    private static boolean addButtonFeature(ChunkAccess chunk, int x, int z, int y) {
        if (y >= chunk.getMaxBuildHeight() - 1) return false;

        BlockPos pos = new BlockPos(x, y, z);
        BlockState currentState = chunk.getBlockState(pos);

        if (currentState.isAir()) {
            // Randomly choose a bright wool color for buttons
            BlockState buttonState = switch ((x + z) % 4) {
                case 0 -> Blocks.YELLOW_WOOL.defaultBlockState();
                case 1 -> Blocks.LIME_WOOL.defaultBlockState();
                case 2 -> Blocks.ORANGE_WOOL.defaultBlockState();
                case 3 -> Blocks.RED_WOOL.defaultBlockState();
                default -> Blocks.WHITE_WOOL.defaultBlockState();
            };

            chunk.setBlockState(pos, buttonState, false);
            return true;
        }

        return false;
    }

    /**
     * Add small depression features
     */
    private static boolean addDepressionFeature(ChunkAccess chunk, int x, int z, int surfaceY) {
        if (surfaceY <= chunk.getMinBuildHeight()) return false;

        BlockPos pos = new BlockPos(x, surfaceY, z);
        BlockState currentState = chunk.getBlockState(pos);

        if (!currentState.isAir()) {
            // Replace with a slightly darker version
            BlockState depressedState = getDarkerVariant(currentState);
            chunk.setBlockState(pos, depressedState, false);
            return true;
        }

        return false;
    }

    /**
     * Add details to subsurface layers
     */
    private static boolean addSubsurfaceDetails(ChunkAccess chunk, int x, int z, int surfaceY, int worldX, int worldZ) {
        boolean modified = false;

        // Add some variation to the layers below surface
        for (int depth = 1; depth <= 3; depth++) {
            int y = surfaceY - depth;
            if (y < chunk.getMinBuildHeight()) break;

            BlockPos pos = new BlockPos(x, y, z);
            BlockState currentState = chunk.getBlockState(pos);

            // Add some detail noise to create more interesting subsurface
            double detailNoise = DETAIL_NOISE.getValue((worldX + depth) * 0.2, (worldZ + depth) * 0.2);

            if (detailNoise > 0.5 && isWoolBlock(currentState)) {
                // Occasionally add contrasting blocks
                BlockState contrastState = getContrastingWool(currentState);
                chunk.setBlockState(pos, contrastState, false);
                modified = true;
            }
        }

        return modified;
    }

    /**
     * Find the actual surface height
     */
    private static int findSurfaceHeight(ChunkAccess chunk, int x, int z, int maxY, int minY) {
        for (int y = maxY - 1; y >= minY; y--) {
            BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
            if (!state.isAir()) {
                return y;
            }
        }
        return minY;
    }

    /**
     * Get a darker variant of a wool block
     */
    private static BlockState getDarkerVariant(BlockState state) {
        if (state.is(Blocks.WHITE_WOOL)) return Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
        if (state.is(Blocks.YELLOW_WOOL)) return Blocks.ORANGE_WOOL.defaultBlockState();
        if (state.is(Blocks.LIME_WOOL)) return Blocks.GREEN_WOOL.defaultBlockState();
        if (state.is(Blocks.PINK_WOOL)) return Blocks.MAGENTA_WOOL.defaultBlockState();
        if (state.is(Blocks.LIGHT_BLUE_WOOL)) return Blocks.BLUE_WOOL.defaultBlockState();
        if (state.is(Blocks.CYAN_WOOL)) return Blocks.LIGHT_BLUE_WOOL.defaultBlockState();

        // Default to gray if we don't have a specific darker variant
        return Blocks.GRAY_WOOL.defaultBlockState();
    }

    /**
     * Get a contrasting wool color
     */
    private static BlockState getContrastingWool(BlockState state) {
        if (state.is(Blocks.BLUE_WOOL)) return Blocks.ORANGE_WOOL.defaultBlockState();
        if (state.is(Blocks.ORANGE_WOOL)) return Blocks.BLUE_WOOL.defaultBlockState();
        if (state.is(Blocks.RED_WOOL)) return Blocks.GREEN_WOOL.defaultBlockState();
        if (state.is(Blocks.GREEN_WOOL)) return Blocks.RED_WOOL.defaultBlockState();
        if (state.is(Blocks.PURPLE_WOOL)) return Blocks.YELLOW_WOOL.defaultBlockState();
        if (state.is(Blocks.YELLOW_WOOL)) return Blocks.PURPLE_WOOL.defaultBlockState();

        return Blocks.WHITE_WOOL.defaultBlockState();
    }

    /**
     * Check if a block state is a wool block
     */
    private static boolean isWoolBlock(BlockState state) {
        return state.getBlock().toString().contains("wool");
    }

    /**
     * Update chunk heightmaps after modifications
     */
    private static void updateChunkHeightmaps(ChunkAccess chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = findSurfaceHeight(chunk, x, z, chunk.getMaxBuildHeight(), chunk.getMinBuildHeight());
                BlockPos pos = new BlockPos(x, height, z);
                BlockState state = chunk.getBlockState(pos);

                chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG).update(x, height + 1, z, state);
                chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING).update(x, height + 1, z, state);
            }
        }
    }

    public static void cleanupCache() {
        if (HEIGHT_CACHE.size() > 10000) {
            HEIGHT_CACHE.clear();
        }
    }
}