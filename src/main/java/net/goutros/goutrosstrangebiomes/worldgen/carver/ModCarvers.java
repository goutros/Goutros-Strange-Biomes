// Extensible Carver System for Custom Terrain
package net.goutros.goutrosstrangebiomes.worldgen.carver;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.chunk.CarvingMask;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import java.util.function.Function;

public class ModCarvers {

    public static final DeferredRegister<WorldCarver<?>> CARVERS =
            DeferredRegister.create(Registries.CARVER, GoutrosStrangeBiomes.MOD_ID);

    // Pillow Cave Carver - Softer, rounded caves
    public static final DeferredHolder<WorldCarver<?>, PillowCaveCarver> PILLOW_CAVE =
            CARVERS.register("pillow_cave", () -> new PillowCaveCarver(CaveCarverConfiguration.CODEC));

    // Pillow Canyon Carver - Gentle valleys
    public static final DeferredHolder<WorldCarver<?>, PillowCanyonCarver> PILLOW_CANYON =
            CARVERS.register("pillow_canyon", () -> new PillowCanyonCarver(CanyonCarverConfiguration.CODEC));

    // Bubble Chamber Carver - Unique to pillow biomes
    public static final DeferredHolder<WorldCarver<?>, BubbleChamberCarver> BUBBLE_CHAMBER =
            CARVERS.register("bubble_chamber", () -> new BubbleChamberCarver(CaveCarverConfiguration.CODEC));
}

/**
 * Base class for pillow-themed carvers with common functionality
 */
abstract class BasePillowCarver<C extends net.minecraft.world.level.levelgen.carver.CarverConfiguration>
        extends WorldCarver<C> {

    protected BasePillowCarver(com.mojang.serialization.Codec<C> codec) {
        super(codec);
    }

    @Override
    protected boolean carveBlock(CarvingContext context, C config, ChunkAccess chunk,
                                 Function<BlockPos, Holder<Biome>> biomeFunction,
                                 CarvingMask carvingMask, BlockPos.MutableBlockPos pos,
                                 BlockPos.MutableBlockPos pos2, Aquifer aquifer,
                                 org.apache.commons.lang3.mutable.MutableBoolean foundSurface) {

        BlockState blockState = chunk.getBlockState(pos);

        if (!this.canReplaceBlock(config, blockState)) {
            return false;
        }

        // Custom carving behavior for pillow blocks
        BlockState carveState = getCustomCarveState(context, config, pos, aquifer, biomeFunction);
        if (carveState != null) {
            chunk.setBlockState(pos, carveState, false);

            // Handle surface replacement
            if (foundSurface.isTrue()) {
                replacePillowSurface(chunk, pos2, biomeFunction);
            }

            return true;
        }

        return super.carveBlock(context, config, chunk, biomeFunction, carvingMask, pos, pos2, aquifer, foundSurface);
    }

    protected BlockState getCustomCarveState(CarvingContext context, C config, BlockPos pos,
                                             Aquifer aquifer, Function<BlockPos, Holder<Biome>> biomeFunction) {
        Holder<Biome> biome = biomeFunction.apply(pos);

        if (biome.is(ModBiomes.PILLOW_PLATEAU)) {
            // Special carving rules for pillow biome
            if (pos.getY() <= config.lavaLevel.resolveY(context)) {
                return Blocks.LAVA.defaultBlockState();
            }

            BlockState aquiferState = aquifer.computeSubstance(
                    new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(pos.getX(), pos.getY(), pos.getZ()),
                    0.0
            );

            return aquiferState != null ? aquiferState : Blocks.CAVE_AIR.defaultBlockState();
        }

        return null;
    }

    protected void replacePillowSurface(ChunkAccess chunk, BlockPos pos,
                                        Function<BlockPos, Holder<Biome>> biomeFunction) {
        Holder<Biome> biome = biomeFunction.apply(pos);

        if (biome.is(ModBiomes.PILLOW_PLATEAU)) {
            BlockState existing = chunk.getBlockState(pos);
            if (existing.is(Blocks.DIRT)) {
                chunk.setBlockState(pos, ModBlocks.PILLOW_DIRT.get().defaultBlockState(), false);
            }
        }
    }
}

/**
 * Pillow Cave Carver - Creates softer, more organic cave systems
 */
class PillowCaveCarver extends BasePillowCarver<CaveCarverConfiguration> {

    public PillowCaveCarver(com.mojang.serialization.Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeFunction, RandomSource random,
                         Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {

        // Enhanced cave generation with softer edges
        return carveEnhancedCaves(context, config, chunk, biomeFunction, random, aquifer, chunkPos, carvingMask);
    }

    private boolean carveEnhancedCaves(CarvingContext context, CaveCarverConfiguration config,
                                       ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                       RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                                       CarvingMask carvingMask) {

        int attempts = random.nextInt(random.nextInt(random.nextInt(15) + 1) + 1);
        if (attempts == 0) return false;

        boolean carved = false;

        for (int i = 0; i < attempts; i++) {
            double centerX = chunkPos.getMiddleBlockX() + random.nextInt(16);
            double centerY = config.y.sample(random, context);
            double centerZ = chunkPos.getMiddleBlockZ() + random.nextInt(16);

            // Softer, more organic cave shapes
            double radiusH = config.horizontalRadiusMultiplier.sample(random) * 0.8; // Smaller radius
            double radiusV = config.verticalRadiusMultiplier.sample(random) * 0.6;   // More flattened

            carved |= this.carveEllipsoid(
                    context, config, chunk, biomeFunction, aquifer,
                    centerX, centerY, centerZ, radiusH, radiusV,
                    carvingMask, (ctx, x, y, z, yPos) -> false // No skip checking
            );
        }

        return carved;
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= config.probability;
    }
}

/**
 * Pillow Canyon Carver - Creates gentle valleys and ravines
 */
class PillowCanyonCarver extends BasePillowCarver<CanyonCarverConfiguration> {

    public PillowCanyonCarver(com.mojang.serialization.Codec<CanyonCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, CanyonCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeFunction, RandomSource random,
                         Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {

        // Create gentle valley-like canyons
        return carveGentleValleys(context, config, chunk, biomeFunction, random, aquifer, chunkPos, carvingMask);
    }

    private boolean carveGentleValleys(CarvingContext context, CanyonCarverConfiguration config,
                                       ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                       RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                                       CarvingMask carvingMask) {

        if (random.nextFloat() > 0.02F) return false; // Rare generation

        double startX = chunkPos.getMiddleBlockX() + random.nextInt(16);
        double startY = random.nextInt(random.nextInt(40) + 8) + 20;
        double startZ = chunkPos.getMiddleBlockZ() + random.nextInt(16);

        // Gentler, wider valleys
        float width = (random.nextFloat() * 2.0F + random.nextFloat()) * 3.0F; // Wider
        float yScale = 1.0F + random.nextFloat() * 6.0F;
        float slope = (random.nextFloat() - 0.5F) * 0.5F; // Gentler slope

        return carveValley(context, config, chunk, biomeFunction, aquifer, carvingMask,
                startX, startY, startZ, width, yScale, slope, random);
    }

    private boolean carveValley(CarvingContext context, CanyonCarverConfiguration config,
                                ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                Aquifer aquifer, CarvingMask carvingMask,
                                double startX, double startY, double startZ,
                                float width, float yScale, float slope, RandomSource random) {

        // Simplified valley carving - create wider, shallower depressions
        boolean carved = false;

        for (int step = 0; step < 30; step++) {
            double currentY = startY + slope * step;
            double radius = width * (1.0 - step / 30.0); // Taper at ends

            if (radius < 1.0) break;

            carved |= this.carveEllipsoid(
                    context, config, chunk, biomeFunction, aquifer,
                    startX, currentY, startZ,
                    radius, radius * 0.3, // Wide and shallow
                    carvingMask, (ctx, x, y, z, yPos) -> false
            );
        }

        return carved;
    }

    @Override
    public boolean isStartChunk(CanyonCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= config.probability * 0.5F; // Half normal chance
    }
}

/**
 * Bubble Chamber Carver - Unique spherical chambers connected by tunnels
 */
class BubbleChamberCarver extends BasePillowCarver<CaveCarverConfiguration> {

    public BubbleChamberCarver(com.mojang.serialization.Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeFunction, RandomSource random,
                         Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {

        // Only generate in pillow biomes
        if (!isPillowBiomeArea(chunk, biomeFunction)) {
            return false;
        }

        return carveBubbleChambers(context, config, chunk, biomeFunction, random, aquifer, chunkPos, carvingMask);
    }

    private boolean isPillowBiomeArea(ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction) {
        // Check center of chunk
        BlockPos center = chunk.getPos().getMiddleBlockPosition(64);
        Holder<Biome> biome = biomeFunction.apply(center);
        return biome.is(ModBiomes.PILLOW_PLATEAU);
    }

    private boolean carveBubbleChambers(CarvingContext context, CaveCarverConfiguration config,
                                        ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                        RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                                        CarvingMask carvingMask) {

        if (random.nextFloat() > 0.1F) return false; // 10% chance

        boolean carved = false;
        int chambers = random.nextInt(3) + 1;

        for (int i = 0; i < chambers; i++) {
            double centerX = chunkPos.getMiddleBlockX() + random.nextInt(16);
            double centerY = random.nextInt(40) + 30; // Mid-level chambers
            double centerZ = chunkPos.getMiddleBlockZ() + random.nextInt(16);

            // Perfect spheres
            double radius = random.nextDouble() * 4.0 + 3.0;

            carved |= this.carveEllipsoid(
                    context, config, chunk, biomeFunction, aquifer,
                    centerX, centerY, centerZ,
                    radius, radius, // Perfect sphere
                    carvingMask, (ctx, x, y, z, yPos) -> false
            );
        }

        return carved;
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= 0.05F; // Rare generation
    }
}