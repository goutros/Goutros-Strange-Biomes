package net.goutros.goutrosstrangebiomes.mixin;

import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * SAFE SIMPLIFIED PILLOW CANYON TERRAIN SYSTEM
 *
 * Minimal, crash-safe implementation that focuses on working reliably
 * while still creating nice canyon terrain with smooth biome blending.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {

    // Simple, thread-safe noise generators
    private static SimplexNoise CANYON_NOISE;
    private static SimplexNoise DETAIL_NOISE;
    private static SimplexNoise GOLDEN_NOISE;
    private static SimplexNoise BLEND_NOISE;

    // Initialize noise generators safely
    static {
        try {
            CANYON_NOISE = new SimplexNoise(RandomSource.create(12345L));
            DETAIL_NOISE = new SimplexNoise(RandomSource.create(67890L));
            GOLDEN_NOISE = new SimplexNoise(RandomSource.create(54321L));
            BLEND_NOISE = new SimplexNoise(RandomSource.create(98765L));
        } catch (Exception e) {
            // Fallback initialization
            RandomSource source = RandomSource.create(12345L);
            CANYON_NOISE = new SimplexNoise(source);
            DETAIL_NOISE = new SimplexNoise(RandomSource.create(source.nextLong()));
            GOLDEN_NOISE = new SimplexNoise(RandomSource.create(source.nextLong()));
            BLEND_NOISE = new SimplexNoise(RandomSource.create(source.nextLong()));
        }
    }

    private static final int SEA_LEVEL = 63;

    @Inject(method = "buildSurface", at = @At("TAIL"))
    private void createSafeCanyonTerrain(WorldGenRegion region, StructureManager structureManager,
                                         RandomState randomState, ChunkAccess chunk, CallbackInfo ci) {
        try {
            // Safety check
            if (chunk == null || region == null) {
                return;
            }

            ChunkPos chunkPos = chunk.getPos();

            // Simple biome detection first
            if (!hasAnyPillowBiome(chunk)) {
                return;
            }

            // Process chunk with safe iteration
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    try {
                        int worldX = chunkPos.getMinBlockX() + x;
                        int worldZ = chunkPos.getMinBlockZ() + z;

                        processSafePosition(chunk, region, x, z, worldX, worldZ);
                    } catch (Exception e) {
                        // Skip this position if there's any issue
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            // Fail silently to prevent crashes
        }
    }

    /**
     * Safe biome detection that EXCLUDES oceans
     */
    private boolean hasAnyPillowBiome(ChunkAccess chunk) {
        try {
            // Check a few key positions in the chunk
            int[] checkX = {4, 8, 12};
            int[] checkZ = {4, 8, 12};

            boolean foundPillow = false;
            boolean foundOcean = false;

            for (int x : checkX) {
                for (int z : checkZ) {
                    try {
                        int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                        Holder<Biome> biome = chunk.getNoiseBiome(x >> 2, surfaceY >> 2, z >> 2);
                        ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);

                        if (biomeKey != null) {
                            String biomeName = biomeKey.location().toString();

                            // Check for Pillow Plateau
                            if (biomeKey.equals(ModBiomes.PILLOW_PLATEAU)) {
                                foundPillow = true;
                            }

                            // Check for ANY ocean biome
                            if (biomeName.contains("ocean") || biomeName.contains("deep_ocean") ||
                                    biomeName.contains("warm_ocean") || biomeName.contains("cold_ocean") ||
                                    biomeName.contains("frozen_ocean") || biomeName.contains("lukewarm_ocean")) {
                                foundOcean = true;
                            }
                        }

                        // Also check height - if most of chunk is at/below sea level, likely oceanic
                        if (surfaceY <= SEA_LEVEL + 1) {
                            foundOcean = true;
                        }

                    } catch (Exception e) {
                        continue;
                    }
                }
            }

            // Only allow pillow generation if we found pillow biome AND no ocean
            return foundPillow && !foundOcean;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safe position processing with error handling
     */
    private void processSafePosition(ChunkAccess chunk, WorldGenRegion region,
                                     int localX, int localZ, int worldX, int worldZ) {
        try {
            // Get vanilla height safely
            int vanillaHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, localX, localZ);

            // Skip underwater areas
            if (vanillaHeight <= SEA_LEVEL + 2) {
                return;
            }

            // Calculate biome influence safely
            double biomeInfluence = calculateSafeBiomeInfluence(chunk, region, worldX, worldZ);

            if (biomeInfluence < 0.1) {
                return; // Too little influence
            }

            // Create safe canyon terrain
            createSafeCanyonColumn(chunk, localX, localZ, worldX, worldZ, vanillaHeight, biomeInfluence);

        } catch (Exception e) {
            // Skip this position if anything fails
        }
    }

    /**
     * Safe biome influence calculation - OCEAN EXCLUSION
     */
    private double calculateSafeBiomeInfluence(ChunkAccess chunk, WorldGenRegion region, int worldX, int worldZ) {
        try {
            // Check current position biome
            BlockPos checkPos = new BlockPos(worldX, 70, worldZ);
            Holder<Biome> biome = region.getBiome(checkPos);
            ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);

            if (biomeKey != null) {
                String biomeName = biomeKey.location().toString();

                // HARD BLOCK any ocean biomes
                if (biomeName.contains("ocean")) {
                    return 0.0; // No influence in any ocean
                }

                // Check for rivers (also avoid)
                if (biomeName.contains("river")) {
                    return 0.0; // No influence in rivers
                }

                // Check if we're at sea level or below (likely water)
                int surfaceHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                        worldX - chunk.getPos().getMinBlockX(), worldZ - chunk.getPos().getMinBlockZ());
                if (surfaceHeight <= SEA_LEVEL + 2) {
                    return 0.0; // No influence at/below sea level
                }
            }

            boolean isPillowBiome = biomeKey != null && biomeKey.equals(ModBiomes.PILLOW_PLATEAU);

            if (!isPillowBiome) {
                // Even outside pillow biome, apply some influence for transition zones
                // BUT only if we're clearly on land
                double transitionNoise = BLEND_NOISE.getValue(worldX * 0.006, worldZ * 0.006);
                transitionNoise = (transitionNoise + 1.0) * 0.5;

                // Check if we're near a pillow biome (transition zone) AND on high ground
                if (transitionNoise > 0.7) {
                    int surfaceHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                            worldX - chunk.getPos().getMinBlockX(), worldZ - chunk.getPos().getMinBlockZ());
                    if (surfaceHeight > SEA_LEVEL + 5) { // Only on clearly elevated land
                        return Math.min(0.4, transitionNoise - 0.3); // Light influence for transitions
                    }
                }
                return 0.0;
            }

            // INSIDE pillow biome - but still check elevation
            int surfaceHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                    worldX - chunk.getPos().getMinBlockX(), worldZ - chunk.getPos().getMinBlockZ());
            if (surfaceHeight <= SEA_LEVEL + 3) {
                return 0.0; // No influence even in pillow biome if too low
            }

            double coreInfluence = 0.9; // Start with high base influence

            // Distance-based falloff from biome edges (but much gentler)
            double edgeNoise = BLEND_NOISE.getValue(worldX * 0.012, worldZ * 0.012);
            edgeNoise = (edgeNoise + 1.0) * 0.5; // Normalize to 0-1

            // Softer edge falloff - keeps high influence further into vanilla areas
            double edgeFactor = Math.pow(edgeNoise, 0.3); // Very gentle falloff

            // Add variation for natural boundaries
            double microNoise = DETAIL_NOISE.getValue(worldX * 0.04, worldZ * 0.04) * 0.15;

            double finalInfluence = coreInfluence * edgeFactor + microNoise;

            return Mth.clamp(finalInfluence, 0.0, 1.0);

        } catch (Exception e) {
            // Fallback to noise-based detection with higher baseline
            try {
                // But still check height even in fallback
                int surfaceHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                        worldX - chunk.getPos().getMinBlockX(), worldZ - chunk.getPos().getMinBlockZ());
                if (surfaceHeight <= SEA_LEVEL + 2) {
                    return 0.0; // No influence at sea level
                }

                double fallbackNoise = BLEND_NOISE.getValue(worldX * 0.008, worldZ * 0.008);
                fallbackNoise = (fallbackNoise + 1.0) * 0.5;
                return fallbackNoise > 0.5 ? Math.min(0.9, fallbackNoise + 0.3) : 0.0;
            } catch (Exception e2) {
                return 0.0;
            }
        }
    }

    /**
     * Safe canyon column creation - NO INNER CLASSES!
     */
    private void createSafeCanyonColumn(ChunkAccess chunk, int x, int z, int worldX, int worldZ,
                                        int vanillaHeight, double biomeInfluence) {
        try {
            // Calculate canyon shape directly without objects
            int targetHeight = calculateSafeTargetHeight(worldX, worldZ, vanillaHeight, biomeInfluence);
            boolean isFloor = isFloorPosition(worldX, worldZ, biomeInfluence);

            int minY = Math.max(chunk.getMinBuildHeight(), SEA_LEVEL - 10);
            int maxY = Math.min(chunk.getMaxBuildHeight() - 5, vanillaHeight + 20);

            for (int y = minY; y <= maxY; y++) {
                try {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState currentState = chunk.getBlockState(pos);

                    if (shouldSafelyReplace(currentState, biomeInfluence)) {
                        BlockState newState = getSafeBlockForPosition(worldX, worldZ, y, targetHeight, isFloor, biomeInfluence);

                        if (newState != null) {
                            chunk.setBlockState(pos, newState, false);
                        }
                    }
                } catch (Exception e) {
                    continue; // Skip this Y level
                }
            }
        } catch (Exception e) {
            // Skip this entire column if there's an issue
        }
    }

    /**
     * Calculate target height with CONSISTENT SMOOTH LAYERING
     */
    private int calculateSafeTargetHeight(int worldX, int worldZ, int vanillaHeight, double biomeInfluence) {
        try {
            // CONSISTENT LAYER SYSTEM - Fixed layer heights
            int layerSize = 4; // Smaller, more manageable layers
            int baseLayer = (vanillaHeight - SEA_LEVEL) / layerSize;

            // Smooth layer selection using noise
            double layerSelectNoise = CANYON_NOISE.getValue(worldX * 0.006, worldZ * 0.006); // Larger scale for smoother areas
            layerSelectNoise = (layerSelectNoise + 1.0) * 0.5; // 0-1

            // Add/subtract layers based on noise (smoother than random)
            int layerAdjustment = (int)((layerSelectNoise - 0.5) * 4); // -2 to +2 layers
            int finalLayer = Math.max(0, baseLayer + layerAdjustment);

            int layeredHeight = SEA_LEVEL + (finalLayer * layerSize);

            // SMOOTHING: Use secondary noise for micro-adjustments within layer
            double microNoise = DETAIL_NOISE.getValue(worldX * 0.025, worldZ * 0.025);
            int microAdjustment = (int)(microNoise * 2); // ±2 blocks within layer
            layeredHeight += microAdjustment;

            // Canyon type modifications (smoother transitions)
            double canyonTypeNoise = BLEND_NOISE.getValue(worldX * 0.01, worldZ * 0.01);
            canyonTypeNoise = (canyonTypeNoise + 1.0) * 0.5;

            int targetHeight;
            if (canyonTypeNoise < 0.25) {
                // CANYON FLOOR - consistent depth
                int carveDepth = layerSize * 2; // Always 2 layers deep
                targetHeight = Math.max(SEA_LEVEL + 5, layeredHeight - carveDepth);

            } else if (canyonTypeNoise < 0.75) {
                // TERRACED WALLS - use the layered height
                targetHeight = layeredHeight;

            } else {
                // RAISED PLATEAUS - consistent height boost
                targetHeight = layeredHeight + layerSize; // Always 1 layer higher
            }

            // SMOOTHING BLEND: More gradual transition from vanilla
            double smoothBlendFactor = Math.pow(biomeInfluence, 0.6); // Even gentler curve
            targetHeight = (int)Mth.lerp(smoothBlendFactor, vanillaHeight, targetHeight);

            // Ensure reasonable bounds
            return Mth.clamp(targetHeight, SEA_LEVEL + 3, vanillaHeight + 16);

        } catch (Exception e) {
            return vanillaHeight;
        }
    }

    /**
     * Check if position is canyon floor
     */
    private boolean isFloorPosition(int worldX, int worldZ, double biomeInfluence) {
        try {
            double canyonNoise = CANYON_NOISE.getValue(worldX * 0.008, worldZ * 0.008);
            canyonNoise = (canyonNoise + 1.0) * 0.5;
            double adjustedNoise = Mth.lerp(biomeInfluence, 0.6, canyonNoise);
            return adjustedNoise < 0.35;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safe block selection with CONSISTENT SMOOTH LAYERING
     */
    private BlockState getSafeBlockForPosition(int worldX, int worldZ, int y, int targetHeight, boolean isFloor, double biomeInfluence) {
        try {
            if (y > targetHeight) {
                // Above target - smooth carving
                if (biomeInfluence > 0.3) {
                    return Blocks.AIR.defaultBlockState();
                } else {
                    return null; // Keep for transition
                }

            } else if (y == targetHeight) {
                // SURFACE LAYER - always replace with pillow blocks at decent influence
                if (biomeInfluence > 0.5) {
                    // High influence - full pillow blocks
                    double goldenNoise = GOLDEN_NOISE.getValue(worldX * 0.012, worldZ * 0.012);
                    if (isFloor && goldenNoise > -0.3) { // Lots of golden on floors
                        return ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState();
                    } else if (goldenNoise > 0.1) {
                        return ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState();
                    } else {
                        return ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState();
                    }
                } else if (biomeInfluence > 0.2) {
                    // Medium influence - pillow grass
                    return ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState();
                } else {
                    // Low influence - coarse dirt transition
                    return Blocks.COARSE_DIRT.defaultBlockState();
                }

            } else if (y >= targetHeight - 2) {
                // SUBSURFACE LAYER - immediate below surface
                if (biomeInfluence > 0.4) {
                    return ModBlocks.PILLOW_DIRT.get().defaultBlockState();
                } else if (biomeInfluence > 0.2) {
                    return Blocks.COARSE_DIRT.defaultBlockState();
                } else {
                    return Blocks.DIRT.defaultBlockState();
                }

            } else if (y >= targetHeight - 8) {
                // SHALLOW COLORFUL LAYERS - consistent 2-block thick stripes
                if (biomeInfluence > 0.5) {
                    int layerIndex = (targetHeight - y - 2) / 2; // Start after subsurface, 2-block layers
                    int colorIndex = layerIndex % 6;

                    return switch (colorIndex) {
                        case 0 -> Blocks.YELLOW_WOOL.defaultBlockState();    // Top layer - warm colors
                        case 1 -> Blocks.ORANGE_WOOL.defaultBlockState();
                        case 2 -> Blocks.RED_WOOL.defaultBlockState();
                        case 3 -> Blocks.MAGENTA_WOOL.defaultBlockState();   // Middle - vibrant
                        case 4 -> Blocks.PURPLE_WOOL.defaultBlockState();
                        case 5 -> Blocks.BLUE_WOOL.defaultBlockState();      // Bottom - cool colors
                        default -> Blocks.CYAN_WOOL.defaultBlockState();
                    };
                } else if (biomeInfluence > 0.3) {
                    // Medium influence - neutral colors only
                    return Blocks.BROWN_WOOL.defaultBlockState(); // Matches dirt theme
                } else {
                    return null; // Keep vanilla
                }

            } else if (y >= targetHeight - 16) {
                // DEEPER LAYERS - consistent 3-block thick stripes
                if (biomeInfluence > 0.7) {
                    int deepLayerIndex = (targetHeight - y - 8) / 3; // 3-block thick deep layers
                    int deepColorIndex = deepLayerIndex % 4;

                    return switch (deepColorIndex) {
                        case 0 -> Blocks.LIME_WOOL.defaultBlockState();
                        case 1 -> Blocks.GREEN_WOOL.defaultBlockState();
                        case 2 -> Blocks.CYAN_WOOL.defaultBlockState();
                        case 3 -> Blocks.LIGHT_BLUE_WOOL.defaultBlockState();
                        default -> Blocks.WHITE_WOOL.defaultBlockState();
                    };
                } else if (biomeInfluence > 0.4) {
                    // Medium influence - gray wool for neutral deep layers
                    return Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
                } else {
                    return null; // Keep vanilla deep blocks
                }

            } else {
                // VERY DEEP - only modify at very high influence
                if (biomeInfluence > 0.8) {
                    // Deep foundation layers - 4-block thick
                    int foundationIndex = (targetHeight - y - 16) / 4;
                    int foundationColor = foundationIndex % 3;

                    return switch (foundationColor) {
                        case 0 -> Blocks.GRAY_WOOL.defaultBlockState();
                        case 1 -> Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
                        case 2 -> Blocks.WHITE_WOOL.defaultBlockState();
                        default -> Blocks.GRAY_WOOL.defaultBlockState();
                    };
                } else {
                    return null; // Keep vanilla bedrock area
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Safe block replacement - COMPREHENSIVE REPLACEMENT to eliminate vanilla blocks
     */
    private boolean shouldSafelyReplace(BlockState state, double biomeInfluence) {
        try {
            if (biomeInfluence > 0.6) {
                // High influence - replace ALL natural terrain blocks
                return state.is(Blocks.STONE) || state.is(Blocks.DIRT) ||
                        state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT) ||
                        state.is(Blocks.DEEPSLATE) || state.is(Blocks.ANDESITE) ||
                        state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) ||
                        state.is(Blocks.GRAVEL) || state.is(Blocks.SAND) ||
                        state.isAir();

            } else if (biomeInfluence > 0.3) {
                // Medium influence - replace most common terrain blocks
                return state.is(Blocks.STONE) || state.is(Blocks.DIRT) ||
                        state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT) ||
                        state.is(Blocks.GRAVEL) || state.isAir();

            } else if (biomeInfluence > 0.1) {
                // Low influence - still modify surface and dirt
                return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
                        state.is(Blocks.COARSE_DIRT) || state.isAir();

            } else {
                return state.isAir(); // Minimal influence
            }
        } catch (Exception e) {
            return false;
        }
    }
}