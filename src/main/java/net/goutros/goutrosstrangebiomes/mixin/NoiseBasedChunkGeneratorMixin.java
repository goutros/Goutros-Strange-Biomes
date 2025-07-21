package net.goutros.goutrosstrangebiomes.mixin;

import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BiomeTags;
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
 * ENHANCED LAYERED CANYON TERRAIN SYSTEM
 *
 * Creates beautiful terraced layers with curved transitions around vanilla terrain heights,
 * using custom pillow blocks and golden pillow patches for variety.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {

    private static final SimplexNoise COLOR_NOISE = new SimplexNoise(RandomSource.create(12345L));
    private static final SimplexNoise LAYER_NOISE = new SimplexNoise(RandomSource.create(67890L));
    private static final SimplexNoise EDGE_NOISE = new SimplexNoise(RandomSource.create(54321L));
    private static final SimplexNoise GOLDEN_PATCH_NOISE = new SimplexNoise(RandomSource.create(98765L));
    private static final SimplexNoise CURVE_NOISE = new SimplexNoise(RandomSource.create(13579L));
    private static final SimplexNoise GRAY_PATCH_NOISE = new SimplexNoise(RandomSource.create(24680L)); // For gray wool patches

    // Layer configuration for cartoon canyon effect
    private static final int LAYER_HEIGHT = 8; // Height of each terrace step
    private static final int LAYER_THICKNESS = 3; // Thickness of each colored layer
    private static final int SEA_LEVEL = 63;
    private static final double CURVE_STRENGTH = 2.0; // How much curvature to apply

    // Probability gradient for wool-stone transition
    private static final double[] WOOL_PROBABILITIES = {1.0, 0.75, 0.55, 0.30, 0.12, 0.06, 0.02};

    @Inject(method = "buildSurface", at = @At("TAIL"))
    private void createPillowCanyonLayers(WorldGenRegion region, StructureManager structureManager,
                                          RandomState randomState, ChunkAccess chunk, CallbackInfo ci) {

        if (shouldProcessChunk(chunk)) {
            createCanyonLayers(chunk, region);
            // Add post-processing to fix subsurface blocks
            postProcessSubsurface(chunk);
            // Final pass to fill any air gaps
            fillAirGaps(chunk);
        }
    }

    private void fillAirGaps(ChunkAccess chunk) {
    }

    /**
     * Check if this chunk should be processed for Pillow Plateau
     */
    private boolean shouldProcessChunk(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();

        // Sample multiple points to determine biome influence
        int pillowCount = 0;
        int oceanCount = 0;
        int totalSamples = 0;

        for (int x = 2; x < 16; x += 6) {
            for (int z = 2; z < 16; z += 6) {
                totalSamples++;

                int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                Holder<Biome> biome = chunk.getNoiseBiome(x >> 2, surfaceY >> 2, z >> 2);
                ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);

                if (biomeKey != null && biomeKey.equals(ModBiomes.PILLOW_PLATEAU)) {
                    pillowCount++;
                }

                // Check for ocean biomes
                if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN)) {
                    oceanCount++;
                }
            }
        }

        // Only process if we have significant Pillow Plateau presence and minimal ocean
        return pillowCount >= (totalSamples * 0.4) && oceanCount < (totalSamples * 0.3);
    }

    /**
     * Create layered canyon terrain using vanilla heights as base
     */
    private void createCanyonLayers(ChunkAccess chunk, WorldGenRegion region) {
        ChunkPos chunkPos = chunk.getPos();
        int pillowGrassCount = 0;
        int goldenGrassCount = 0;
        int totalProcessed = 0;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                if (processPosition(chunk, x, z, worldX, worldZ, region)) {
                    totalProcessed++;

                    // Count what surface blocks were placed for debugging
                    int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                    BlockState surfaceState = chunk.getBlockState(new BlockPos(x, surfaceY - 1, z));

                    if (surfaceState.is(ModBlocks.PILLOW_GRASS_BLOCK.get())) {
                        pillowGrassCount++;
                    } else if (surfaceState.is(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get())) {
                        goldenGrassCount++;
                    }
                }
            }
        }

        // Log statistics for debugging
        if (totalProcessed > 0) {
            System.out.println(String.format("Chunk (%d,%d): Processed %d positions, %d pillow grass, %d golden grass",
                    chunkPos.x, chunkPos.z, totalProcessed, pillowGrassCount, goldenGrassCount));
        }
    }

    /**
     * Process a single position to create canyon layers
     */
    private boolean processPosition(ChunkAccess chunk, int x, int z, int worldX, int worldZ, WorldGenRegion region) {
        // Get vanilla terrain height
        int vanillaSurfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        // Skip if underwater
        if (vanillaSurfaceY <= SEA_LEVEL + 2) {
            return false;
        }

        // Check biome at this position
        Holder<Biome> biome = chunk.getNoiseBiome(x >> 2, vanillaSurfaceY >> 2, z >> 2);
        ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);

        if (biomeKey == null || !biomeKey.equals(ModBiomes.PILLOW_PLATEAU)) {
            return false;
        }

        // Calculate biome edge influence for smooth transitions
        double edgeInfluence = calculateBiomeEdgeInfluence(chunk, x, z, worldX, worldZ);

        if (edgeInfluence < 0.1) {
            return false; // Too close to biome edge
        }

        // Create canyon layers based on vanilla height
        createLayersAtPosition(chunk, x, z, worldX, worldZ, vanillaSurfaceY, edgeInfluence);
        return true;
    }

    /**
     * Calculate how far we are from biome edges for smooth transitions - OPTIMIZED
     */
    private double calculateBiomeEdgeInfluence(ChunkAccess chunk, int x, int z, int worldX, int worldZ) {
        double influence = 1.0;

        // Cache current biome check
        int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        Holder<Biome> currentBiome = chunk.getNoiseBiome(x >> 2, surfaceY >> 2, z >> 2);
        ResourceKey<Biome> currentBiomeKey = currentBiome.unwrapKey().orElse(null);

        if (currentBiomeKey == null || !currentBiomeKey.equals(ModBiomes.PILLOW_PLATEAU)) {
            return 0.0; // Not in pillow plateau
        }

        // Optimized edge detection - check only key positions instead of all surrounding
        int[] checkOffsets = {-3, -1, 1, 3}; // Strategic sampling points

        for (int dx : checkOffsets) {
            for (int dz : checkOffsets) {
                int checkX = Mth.clamp(x + dx, 0, 15);
                int checkZ = Mth.clamp(z + dz, 0, 15);

                int checkY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, checkX, checkZ);
                Holder<Biome> checkBiome = chunk.getNoiseBiome(checkX >> 2, checkY >> 2, checkZ >> 2);
                ResourceKey<Biome> checkBiomeKey = checkBiome.unwrapKey().orElse(null);

                if (checkBiomeKey == null || !checkBiomeKey.equals(ModBiomes.PILLOW_PLATEAU)) {
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    influence = Math.min(influence, distance / 4.0); // Gentler falloff
                }

                // Extra penalty for ocean biomes
                if (checkBiome.is(BiomeTags.IS_OCEAN) || checkBiome.is(BiomeTags.IS_DEEP_OCEAN)) {
                    influence *= 0.2; // Strong penalty for ocean proximity
                }
            }
        }

        // Add noise-based variation for natural edges with smoother noise
        double edgeNoise = EDGE_NOISE.getValue(worldX * 0.01, worldZ * 0.01); // Smoother edge noise
        influence = Math.max(0.0, Math.min(1.0, influence + edgeNoise * 0.05)); // Reduced noise impact

        // Add distance-based smoothing from chunk borders
        double chunkEdgeDistance = Math.min(
                Math.min(x, 15 - x),
                Math.min(z, 15 - z)
        );

        if (chunkEdgeDistance < 3) {
            influence *= (chunkEdgeDistance / 3.0); // Smooth chunk border transitions
        }

        return influence;
    }

    /**
     * Create beautiful layered canyon effect at a position with curved transitions and smoothing
     */
    private void createLayersAtPosition(ChunkAccess chunk, int x, int z, int worldX, int worldZ,
                                        int vanillaSurfaceY, double edgeInfluence) {

        // Calculate which layer this height falls into
        int baseLayer = (vanillaSurfaceY - SEA_LEVEL) / LAYER_HEIGHT;

        // Add some noise variation to layer boundaries
        double layerNoise = LAYER_NOISE.getValue(worldX * 0.01, worldZ * 0.01);
        int layerOffset = (int)(layerNoise * 2); // ±2 block variation

        int targetLayer = Math.max(0, baseLayer + layerOffset);
        int baseTargetHeight = SEA_LEVEL + (targetLayer * LAYER_HEIGHT);

        // Calculate curved transition based on proximity to layer boundaries
        double curveAdjustment = calculateCurveAdjustment(worldX, worldZ, vanillaSurfaceY, baseTargetHeight);
        int targetHeight = (int)(baseTargetHeight + curveAdjustment);

        // Apply terrain smoothing by sampling neighbor heights
        targetHeight = applySmoothingFilter(chunk, x, z, targetHeight, edgeInfluence);

        // Reduce layer height near biome edges for smooth transitions
        targetHeight = (int)Mth.lerp(edgeInfluence, vanillaSurfaceY, targetHeight);

        // Ensure we don't go too far from vanilla height
        targetHeight = Mth.clamp(targetHeight,
                Math.max(vanillaSurfaceY - 12, SEA_LEVEL + 5),
                vanillaSurfaceY + 8);

        // Create the layered terrain
        createLayeredColumn(chunk, x, z, worldX, worldZ, vanillaSurfaceY, targetHeight, targetLayer, edgeInfluence);
    }

    /**
     * Apply smoothing filter to reduce sharp height changes
     */
    private int applySmoothingFilter(ChunkAccess chunk, int x, int z, int targetHeight, double edgeInfluence) {
        if (edgeInfluence < 0.3) {
            return targetHeight; // Don't smooth near edges
        }

        int totalHeight = targetHeight;
        int sampleCount = 1;

        // Sample neighboring positions for smoothing
        int[] offsets = {-1, 0, 1};
        for (int dx : offsets) {
            for (int dz : offsets) {
                if (dx == 0 && dz == 0) continue;

                int neighborX = x + dx;
                int neighborZ = z + dz;

                if (neighborX >= 0 && neighborX < 16 && neighborZ >= 0 && neighborZ < 16) {
                    int neighborHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, neighborX, neighborZ);
                    totalHeight += neighborHeight;
                    sampleCount++;
                }
            }
        }

        // Blend original target with smoothed average
        int smoothedHeight = totalHeight / sampleCount;
        double smoothingStrength = edgeInfluence * 0.3; // Stronger smoothing in biome center

        return (int)Mth.lerp(smoothingStrength, targetHeight, smoothedHeight);
    }

    /**
     * Calculate curved transitions between layers for pillow-like effect with smoothing
     */
    private double calculateCurveAdjustment(int worldX, int worldZ, int vanillaSurfaceY, int baseTargetHeight) {
        // Distance from the vanilla height to the target layer
        double layerDistance = vanillaSurfaceY - baseTargetHeight;
        double normalizedDistance = Math.abs(layerDistance) / LAYER_HEIGHT;

        // Add curve noise for natural variation with smoother frequency
        double curveNoise = CURVE_NOISE.getValue(worldX * 0.02, worldZ * 0.02); // Smoother noise

        // Add smoothing noise for gentler transitions
        double smoothingNoise = LAYER_NOISE.getValue(worldX * 0.015, worldZ * 0.015);

        // Calculate curve direction and strength with smoothing
        double curveStrength = CURVE_STRENGTH * (1.0 + curveNoise * 0.2 + smoothingNoise * 0.1);

        if (Math.abs(layerDistance) < LAYER_HEIGHT * 0.8) { // Wider transition zone
            // Near layer boundary - apply curvature
            if (layerDistance > 0) {
                // Above target layer - concave up (approaching upper layer)
                return curveStrength * normalizedDistance * normalizedDistance * 0.7; // Gentler curves
            } else {
                // Below target layer - concave down (approaching lower layer)
                return -curveStrength * normalizedDistance * normalizedDistance * 0.7;
            }
        }

        return 0.0; // No curve adjustment if far from layer boundaries
    }

    /**
     * Create a column with beautiful layered canyon effect - FIXED surface preservation
     */
    private void createLayeredColumn(ChunkAccess chunk, int x, int z, int worldX, int worldZ,
                                     int vanillaSurfaceY, int targetHeight, int layer, double edgeInfluence) {

        int minY = Math.min(vanillaSurfaceY, targetHeight);
        int maxY = Math.max(vanillaSurfaceY, targetHeight);

        // Pre-determine surface block to avoid conflicts
        boolean isGoldenPatch = isInGoldenPatch(worldX, worldZ);
        BlockState correctSurfaceBlock = getSurfaceColorBlock(0, edgeInfluence, isGoldenPatch);

        // First pass: handle terrain modification
        for (int y = minY; y <= maxY && y < chunk.getMaxBuildHeight() - 5; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState currentState = chunk.getBlockState(pos);

            if (y < targetHeight) {
                // Fill below target height
                if (shouldReplaceBlock(currentState, y <= vanillaSurfaceY)) {
                    BlockState newState = getLayeredBlock(worldX, worldZ, y, layer, edgeInfluence);
                    chunk.setBlockState(pos, newState, false);
                }
            } else if (y == targetHeight) {
                // SURFACE LEVEL - always place the correct surface block
                if (shouldReplaceBlock(currentState, y <= vanillaSurfaceY)) {
                    chunk.setBlockState(pos, correctSurfaceBlock, false);
                }
            } else if (y <= vanillaSurfaceY) {
                // Carve down from vanilla height (above target surface)
                if (isSafeToCarve(currentState)) {
                    chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                }
            }
        }

        // Second pass: ensure proper subsurface blocks below surface (but don't override surface!)
        for (int depth = 1; depth <= 3; depth++) {
            int y = targetHeight - depth;
            if (y < chunk.getMinBuildHeight()) break;

            BlockPos subPos = new BlockPos(x, y, z);
            BlockState currentSub = chunk.getBlockState(subPos);

            if (currentSub.isAir() || shouldReplaceBlock(currentSub, true)) {
                // Place appropriate subsurface block
                chunk.setBlockState(subPos, ModBlocks.PILLOW_DIRT.get().defaultBlockState(), false);
            }
        }

        // Update the heightmap for this position
        updateHeightmapAt(chunk, x, z, targetHeight);
    }

    /**
     * Get the appropriate block for a layer and height using custom pillow blocks - FIXED
     */
    private BlockState getLayeredBlock(int worldX, int worldZ, int y, int baseLayer, double edgeInfluence) {
        // Calculate color based on position noise
        double colorNoise = COLOR_NOISE.getValue(worldX * 0.005, worldZ * 0.005);
        int colorVariant = (int)((colorNoise + 1.0) * 3.5); // 0-6 range

        // Check for golden pillow patches
        boolean isGoldenPatch = isInGoldenPatch(worldX, worldZ);

        // Determine layer within the current level
        int localLayer = (y - SEA_LEVEL) % LAYER_HEIGHT;

        // Always return a solid block - never air

        // Surface layer gets the main color
        if (localLayer >= LAYER_HEIGHT - 1) {
            return getSurfaceColorBlock(colorVariant, edgeInfluence, isGoldenPatch);
        }
        // Mid layers get accent colors
        else if (localLayer >= LAYER_HEIGHT - LAYER_THICKNESS) {
            return getAccentColorBlock(colorVariant, edgeInfluence, isGoldenPatch);
        }
        // Base layers get neutral colors
        else {
            return getBaseColorBlock(colorVariant, edgeInfluence, isGoldenPatch);
        }
    }

    /**
     * Check if position is in a golden pillow patch (much more generous)
     */
    private boolean isInGoldenPatch(int worldX, int worldZ) {
        double patchNoise = GOLDEN_PATCH_NOISE.getValue(worldX * 0.01, worldZ * 0.01);
        double patchDensity = GOLDEN_PATCH_NOISE.getValue(worldX * 0.004, worldZ * 0.004);

        // Much more generous conditions - about 30% of the biome should be golden
        return patchNoise > -0.2 && patchDensity > -0.4;
    }

    /**
     * Get surface color block using custom pillow blocks with much more aggressive coverage
     */
    private BlockState getSurfaceColorBlock(int variant, double edgeInfluence, boolean isGoldenPatch) {
        // Only use vanilla grass at the very edges of the biome
        if (edgeInfluence < 0.15) {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }

        // Use golden pillow grass in golden patches (very low threshold)
        if (isGoldenPatch) {
            return ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState();
        }

        // Use regular pillow grass for almost all areas
        return ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState();
    }

    /**
     * Get accent color block - prioritize pillow dirt heavily
     */
    private BlockState getAccentColorBlock(int variant, double edgeInfluence, boolean isGoldenPatch) {
        // Only use vanilla dirt at the very edges
        if (edgeInfluence < 0.1) {
            return Blocks.DIRT.defaultBlockState();
        }

        // Use pillow dirt for almost all subsurface areas
        return ModBlocks.PILLOW_DIRT.get().defaultBlockState();
    }

    /**
     * Get base color block - use pillow dirt and light gray wool system
     */
    private BlockState getBaseColorBlock(int variant, double edgeInfluence, boolean isGoldenPatch) {
        // Only use stone at the very edges
        if (edgeInfluence < 0.05) {
            return Blocks.STONE.defaultBlockState();
        }

        // Use pillow dirt for most base layers
        if (edgeInfluence > 0.1) {
            return ModBlocks.PILLOW_DIRT.get().defaultBlockState();
        }

        // Use light gray wool for stone replacement in pillow areas
        return Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
    }

    /**
     * Check if we should replace a block - SAFER version to prevent air gaps
     */
    private boolean shouldReplaceBlock(BlockState state, boolean isBelowOriginalSurface) {
        // Always replace air
        if (state.isAir()) {
            return true;
        }

        if (isBelowOriginalSurface) {
            // Below original surface - replace natural blocks and existing wool
            return state.is(Blocks.STONE) ||
                    state.is(Blocks.DEEPSLATE) ||
                    state.is(Blocks.DIRT) ||
                    state.is(Blocks.GRASS_BLOCK) ||
                    state.is(Blocks.COARSE_DIRT) ||
                    state.getBlock().toString().contains("wool") ||
                    state.is(ModBlocks.PILLOW_GRASS_BLOCK.get()) ||
                    state.is(ModBlocks.PILLOW_DIRT.get()) ||
                    state.is(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get());
        } else {
            // Above original surface - only place in air or replace natural surface blocks
            return state.isAir() ||
                    state.is(Blocks.GRASS_BLOCK) ||
                    state.is(Blocks.DIRT);
        }
    }

    /**
     * Check if block is safe to carve - MORE CONSERVATIVE to prevent air gaps
     */
    private boolean isSafeToCarve(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.DIRT) ||
                state.is(Blocks.COARSE_DIRT) ||
                // Don't carve stone unless we're sure we'll replace it
                (state.is(Blocks.STONE) && Math.random() > 0.5) ||
                state.getBlock().toString().contains("wool") ||
                state.is(ModBlocks.PILLOW_GRASS_BLOCK.get()) ||
                state.is(ModBlocks.PILLOW_DIRT.get()) ||
                state.is(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get());
    }

    /**
     * Post-process subsurface blocks throughout the Pillow Plateau biome
     */
    private void postProcessSubsurface(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

                // Check if this position is in Pillow Plateau biome
                Holder<Biome> biome = chunk.getNoiseBiome(x >> 2, surfaceY >> 2, z >> 2);
                ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);

                if (biomeKey != null && biomeKey.equals(ModBiomes.PILLOW_PLATEAU)) {
                    // Calculate biome influence for this position
                    double edgeInfluence = calculateBiomeEdgeInfluence(chunk, x, z, worldX, worldZ);

                    if (edgeInfluence > 0.1) { // Only process areas with decent pillow influence
                        processSubsurfaceColumn(chunk, x, z, surfaceY, edgeInfluence);
                    }
                }
            }
        }
    }

    /**
     * Process subsurface column in Pillow Plateau areas
     */
    private void processSubsurfaceColumn(ChunkAccess chunk, int x, int z, int surfaceY, double edgeInfluence) {
        ChunkPos chunkPos = chunk.getPos();
        int worldX = chunkPos.getMinBlockX() + x;
        int worldZ = chunkPos.getMinBlockZ() + z;

        // Process blocks going down from surface
        for (int depth = 1; depth <= 20; depth++) {
            int y = surfaceY - depth;
            if (y < chunk.getMinBuildHeight()) break;

            BlockPos pos = new BlockPos(x, y, z);
            BlockState currentState = chunk.getBlockState(pos);

            BlockState newState = getSubsurfaceReplacement(currentState, depth, edgeInfluence, worldX, worldZ);
            if (newState != null && !newState.equals(currentState)) {
                chunk.setBlockState(pos, newState, false);
            }
        }
    }

    /**
     * Get replacement block for subsurface based on depth, current block, and biome influence
     * Now uses probability-based wool-stone gradient
     */
    private BlockState getSubsurfaceReplacement(BlockState currentState, int depth, double edgeInfluence, int worldX, int worldZ) {
        // Only replace in areas with good biome influence
        if (edgeInfluence < 0.15) {
            return null;
        }

        // Dirt replacement - always use pillow dirt in pillow areas
        if (currentState.is(Blocks.DIRT) || currentState.is(Blocks.COARSE_DIRT)) {
            if (depth <= 8 && edgeInfluence > 0.2) {
                return ModBlocks.PILLOW_DIRT.get().defaultBlockState();
            }
            return null;
        }

        // Stone replacement with probability-based gradient
        if (currentState.is(Blocks.STONE) || currentState.is(Blocks.DEEPSLATE)) {
            if (depth <= 3 && edgeInfluence > 0.3) {
                // Very close to surface - always use pillow dirt
                return ModBlocks.PILLOW_DIRT.get().defaultBlockState();
            } else if (depth <= 15 && edgeInfluence > 0.25) {
                // Use probability-based wool replacement
                return getProbabilisticWoolReplacement(depth, edgeInfluence, worldX, worldZ);
            }
            // Very deep - leave as stone
            return null;
        }

        // Don't replace other blocks
        return null;
    }

    /**
     * Get probabilistic wool replacement based on depth
     */
    private BlockState getProbabilisticWoolReplacement(int depth, double edgeInfluence, int worldX, int worldZ) {
        // Calculate probability index (depth 4-15 maps to indices 0-6)
        int probabilityIndex = Math.min((depth - 4) / 2, WOOL_PROBABILITIES.length - 1);
        double woolChance = WOOL_PROBABILITIES[probabilityIndex] * edgeInfluence;

        // Random chance for wool vs stone
        if (Math.random() < woolChance) {
            // Check for gray wool patches
            if (isInGrayPatch(worldX, worldZ)) {
                return Blocks.GRAY_WOOL.defaultBlockState();
            } else {
                return Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
            }
        } else {
            return Blocks.STONE.defaultBlockState();
        }
    }

    /**
     * Check if position is in a gray wool patch
     */
    private boolean isInGrayPatch(int worldX, int worldZ) {
        double patchNoise = GRAY_PATCH_NOISE.getValue(worldX * 0.02, worldZ * 0.02);

        // About 15% of areas should be gray patches
        return patchNoise > 0.4;
    }
    private void updateHeightmapAt(ChunkAccess chunk, int x, int z, int newHeight) {
        try {
            // Find the actual surface
            int actualSurface = newHeight;
            for (int y = newHeight + 5; y >= newHeight - 5; y--) {
                if (y < chunk.getMinBuildHeight() || y >= chunk.getMaxBuildHeight()) continue;

                BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
                if (!state.isAir()) {
                    actualSurface = y + 1;
                    break;
                }
            }

            BlockState surfaceState = chunk.getBlockState(new BlockPos(x, actualSurface - 1, z));
            chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG)
                    .update(x, actualSurface, z, surfaceState);

        } catch (Exception e) {
            // Ignore heightmap update errors
        }
    }
}