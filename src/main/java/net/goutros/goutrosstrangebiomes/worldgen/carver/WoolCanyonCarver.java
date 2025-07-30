package net.goutros.goutrosstrangebiomes.worldgen.carver;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;

import java.util.function.Function;

/**
 * Enhanced Pillow Canyon Carver that creates wool-layered canyon walls with soft, organic shapes
 */
public class WoolCanyonCarver extends BasePillowCarver<CanyonCarverConfiguration> {

    // Wool block layers for canyon walls (innermost to outermost)
    private static final BlockState[] WOOL_LAYERS = {
            Blocks.WHITE_WOOL.defaultBlockState(),      // Core layer
            Blocks.LIGHT_GRAY_WOOL.defaultBlockState(), // Secondary
            Blocks.PINK_WOOL.defaultBlockState(),       // Accent
            Blocks.MAGENTA_WOOL.defaultBlockState(),    // Outer accent
            ModBlocks.BROWN_PILLOW.get().defaultBlockState() // Transition to normal terrain
    };

    private static final BlockState[] RARE_WOOL_ACCENTS = {
            Blocks.YELLOW_WOOL.defaultBlockState(),
            Blocks.LIME_WOOL.defaultBlockState(),
            Blocks.CYAN_WOOL.defaultBlockState(),
            Blocks.PURPLE_WOOL.defaultBlockState()
    };

    public WoolCanyonCarver(com.mojang.serialization.Codec<CanyonCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, CanyonCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeFunction, RandomSource random,
                         Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {

        // Only generate in pillow biomes
        if (!isPillowBiomeChunk(chunk, biomeFunction)) {
            return false;
        }

        return carveWoolCanyon(context, config, chunk, biomeFunction, random, aquifer, chunkPos, carvingMask);
    }

    private boolean carveWoolCanyon(CarvingContext context, CanyonCarverConfiguration config,
                                    ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                    RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                                    CarvingMask carvingMask) {

        // Reduced probability for special canyons
        if (random.nextFloat() > 0.008F) return false;

        double startX = chunkPos.getMiddleBlockX() + random.nextInt(16) - 8;
        double startY = 45 + random.nextInt(30); // Mid-level canyons
        double startZ = chunkPos.getMiddleBlockZ() + random.nextInt(16) - 8;

        // Canyon parameters - wider and more organic
        float width = (random.nextFloat() * 3.0F + 2.0F) * 4.0F; // Much wider
        float depth = random.nextFloat() * 20.0F + 15.0F; // Deeper
        float length = random.nextFloat() * 60.0F + 40.0F; // Longer

        // Canyon direction
        float direction = random.nextFloat() * (float) Math.PI * 2.0F;
        float directionVariation = 0.3F;

        return carveOrganicWoolCanyon(context, config, chunk, biomeFunction, aquifer, carvingMask,
                startX, startY, startZ, width, depth, length, direction, directionVariation, random);
    }

    private boolean carveOrganicWoolCanyon(CarvingContext context, CanyonCarverConfiguration config,
                                           ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                           Aquifer aquifer, CarvingMask carvingMask,
                                           double startX, double startY, double startZ,
                                           float width, float depth, float length,
                                           float direction, float directionVariation, RandomSource random) {

        boolean carved = false;
        int steps = (int) (length / 2.0F);

        for (int step = 0; step < steps; step++) {
            float progress = (float) step / steps;

            // Organic direction changes
            direction += (random.nextFloat() - 0.5F) * directionVariation;

            // Current position along canyon path
            double currentX = startX + Math.cos(direction) * step * 2.0;
            double currentZ = startZ + Math.sin(direction) * step * 2.0;
            double currentY = startY - Math.sin(progress * Math.PI) * depth * 0.3; // Gentle depth variation

            // Width variation - wider in middle
            float currentWidth = width * (0.5F + 0.5F * (float) Math.sin(progress * Math.PI));

            // Carve the canyon segment with wool layering
            carved |= carveWoolCanyonSegment(context, config, chunk, biomeFunction, aquifer, carvingMask,
                    currentX, currentY, currentZ, currentWidth, depth, random);
        }

        return carved;
    }

    private boolean carveWoolCanyonSegment(CarvingContext context, CanyonCarverConfiguration config,
                                           ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                           Aquifer aquifer, CarvingMask carvingMask,
                                           double centerX, double centerY, double centerZ,
                                           float width, float depth, RandomSource random) {

        boolean carved = false;
        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();

        // Define canyon bounds
        int minX = Math.max(chunkMinX, (int) (centerX - width));
        int maxX = Math.min(chunkMinX + 15, (int) (centerX + width));
        int minY = Math.max(context.getMinGenY(), (int) (centerY - depth));
        int maxY = Math.min(context.getMinGenY() + context.getGenDepth(), (int) (centerY + depth * 0.5));
        int minZ = Math.max(chunkMinZ, (int) (centerZ - width));
        int maxZ = Math.min(chunkMinZ + 15, (int) (centerZ + width));

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double distanceFromCenter = Math.sqrt(
                        (x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ)
                );

                if (distanceFromCenter <= width) {
                    carved |= carveWoolColumn(context, chunk, x, z, minY, maxY,
                            centerY, distanceFromCenter, width, depth, random);
                }
            }
        }

        return carved;
    }

    private boolean carveWoolColumn(CarvingContext context, ChunkAccess chunk, int x, int z,
                                    int minY, int maxY, double centerY, double distanceFromCenter,
                                    float width, float depth, RandomSource random) {

        boolean carved = false;

        // Calculate canyon depth at this position (U-shaped profile)
        double normalizedDistance = distanceFromCenter / width;
        double depthMultiplier = 1.0 - (normalizedDistance * normalizedDistance); // Parabolic profile
        double actualDepth = depth * depthMultiplier;

        for (int y = minY; y <= maxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState currentState = chunk.getBlockState(pos);

            // Skip if already air or liquid
            if (currentState.isAir() || currentState.liquid()) {
                continue;
            }

            double distanceFromFloor = centerY - actualDepth - y;
            double distanceFromCeiling = y - (centerY + actualDepth * 0.3);

            if (y < centerY - actualDepth + 2) {
                // Canyon floor - place air
                chunk.setBlockState(pos, Blocks.CAVE_AIR.defaultBlockState(), false);
                carved = true;
            } else if (distanceFromFloor < WOOL_LAYERS.length && distanceFromFloor >= 0) {
                // Wool layering on canyon walls
                int layerIndex = (int) distanceFromFloor;
                BlockState woolState = getWoolStateForLayer(layerIndex, normalizedDistance, random);
                chunk.setBlockState(pos, woolState, false);
                carved = true;
            } else if (normalizedDistance > 0.7 && random.nextFloat() < 0.1) {
                // Occasional accent blocks near canyon rim
                BlockState accentState = getAccentWoolState(random);
                chunk.setBlockState(pos, accentState, false);
                carved = true;
            }
        }

        return carved;
    }

    private BlockState getWoolStateForLayer(int layer, double distanceFromCenter, RandomSource random) {
        // Clamp layer to available wool layers
        layer = Math.min(layer, WOOL_LAYERS.length - 1);

        // Add some variation based on distance and randomness
        if (random.nextFloat() < 0.1 && distanceFromCenter < 0.5) {
            // Core area gets more variation
            return RARE_WOOL_ACCENTS[random.nextInt(RARE_WOOL_ACCENTS.length)];
        }

        // Occasionally shift layer for organic variation
        if (random.nextFloat() < 0.15) {
            int variation = random.nextBoolean() ? 1 : -1;
            layer = Math.max(0, Math.min(WOOL_LAYERS.length - 1, layer + variation));
        }

        return WOOL_LAYERS[layer];
    }

    private BlockState getAccentWoolState(RandomSource random) {
        if (random.nextFloat() < 0.3) {
            return RARE_WOOL_ACCENTS[random.nextInt(RARE_WOOL_ACCENTS.length)];
        }
        return ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState();
    }

    private boolean isPillowBiomeChunk(ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction) {
        BlockPos center = chunk.getPos().getMiddleBlockPosition(64);
        Holder<Biome> biome = biomeFunction.apply(center);
        return biome.is(ModBiomes.PILLOW_PLATEAU);
    }

    @Override
    public boolean isStartChunk(CanyonCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= 0.02F; // 2% chance for wool canyons
    }

    @Override
    protected BlockState getCustomCarveState(CarvingContext context, CanyonCarverConfiguration config,
                                             BlockPos pos, Aquifer aquifer,
                                             Function<BlockPos, Holder<Biome>> biomeFunction) {
        // Override to prevent normal carving behavior - we handle it manually
        return null;
    }
}