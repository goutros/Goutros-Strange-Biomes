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

    // Bubble Chamber Carver - Unique to pillow biomes
    public static final DeferredHolder<WorldCarver<?>, BubbleChamberCarver> BUBBLE_CHAMBER =
            CARVERS.register("bubble_chamber", () -> new BubbleChamberCarver(CaveCarverConfiguration.CODEC));

    // Wool Canyon Carver - Colorful wool-layered canyons
    public static final DeferredHolder<WorldCarver<?>, WoolCanyonCarver> WOOL_CANYON =
            CARVERS.register("wool_canyon", () -> new WoolCanyonCarver(CanyonCarverConfiguration.CODEC));

    // Wool Cave Carver - Wool-lined cave systems
    public static final DeferredHolder<WorldCarver<?>, WoolCaveCarver> WOOL_CAVE =
            CARVERS.register("wool_cave", () -> new WoolCaveCarver(CaveCarverConfiguration.CODEC));
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
                chunk.setBlockState(pos, ModBlocks.BROWN_PILLOW.get().defaultBlockState(), false);
            }
        }
    }
}

class PillowCaveCarver extends BasePillowCarver<CaveCarverConfiguration> {
    public PillowCaveCarver(com.mojang.serialization.Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeFunction, RandomSource random,
                         Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {

        if (!isPillowBiome(chunk, biomeFunction)) return false;
        if (random.nextFloat() > 0.3F) return false; // 30% chance

        // Create smaller, cozier caves manually
        return createPillowCaves(context, config, chunk, biomeFunction, random, aquifer, chunkPos, carvingMask);
    }

    private boolean createPillowCaves(CarvingContext context, CaveCarverConfiguration config,
                                      ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                      RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                                      CarvingMask carvingMask) {

        boolean carved = false;
        int attempts = random.nextInt(3) + 1;

        for (int i = 0; i < attempts; i++) {
            double centerX = chunkPos.getMiddleBlockX() + random.nextInt(16);
            double centerY = config.y.sample(random, context);
            double centerZ = chunkPos.getMiddleBlockZ() + random.nextInt(16);

            // Smaller cave radius
            double radiusH = config.horizontalRadiusMultiplier.sample(random) * 0.6;
            double radiusV = config.verticalRadiusMultiplier.sample(random) * 0.5;

            carved |= this.carveEllipsoid(
                    context, config, chunk, biomeFunction, aquifer,
                    centerX, centerY, centerZ, radiusH, radiusV,
                    carvingMask, (ctx, x, y, z, yPos) -> false
            );
        }

        return carved;
    }

    private boolean isPillowBiome(ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction) {
        BlockPos center = chunk.getPos().getMiddleBlockPosition(64);
        return biomeFunction.apply(center).is(ModBiomes.PILLOW_PLATEAU);
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= 0.3F;
    }
}

class BubbleChamberCarver extends BasePillowCarver<CaveCarverConfiguration> {
    public BubbleChamberCarver(com.mojang.serialization.Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeFunction, RandomSource random,
                         Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {

        if (!isPillowBiome(chunk, biomeFunction)) return false;
        if (random.nextFloat() > 0.1F) return false; // Rare 10% chance

        return createBubbleChambers(context, config, chunk, biomeFunction, random, aquifer, chunkPos, carvingMask);
    }

    private boolean createBubbleChambers(CarvingContext context, CaveCarverConfiguration config,
                                         ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                         RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                                         CarvingMask carvingMask) {

        boolean carved = false;
        int chambers = random.nextInt(2) + 1;

        for (int i = 0; i < chambers; i++) {
            double centerX = chunkPos.getMiddleBlockX() + random.nextInt(16);
            double centerY = random.nextInt(40) + 40; // Mid-level chambers
            double centerZ = chunkPos.getMiddleBlockZ() + random.nextInt(16);

            // Perfect spheres
            double radius = random.nextDouble() * 3.0 + 2.0;

            carved |= this.carveEllipsoid(
                    context, config, chunk, biomeFunction, aquifer,
                    centerX, centerY, centerZ,
                    radius, radius, // Perfect sphere
                    carvingMask, (ctx, x, y, z, yPos) -> false
            );
        }

        return carved;
    }

    private boolean isPillowBiome(ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction) {
        BlockPos center = chunk.getPos().getMiddleBlockPosition(64);
        return biomeFunction.apply(center).is(ModBiomes.PILLOW_PLATEAU);
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= 0.1F;
    }
}

class WoolCanyonCarver extends BasePillowCarver<CanyonCarverConfiguration> {
    public WoolCanyonCarver(com.mojang.serialization.Codec<CanyonCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, CanyonCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeFunction, RandomSource random,
                         Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {

        if (!isPillowBiome(chunk, biomeFunction)) return false;
        if (random.nextFloat() > 0.05F) return false; // Very rare

        return createWoolCanyons(context, config, chunk, biomeFunction, random, aquifer, chunkPos, carvingMask);
    }

    private boolean createWoolCanyons(CarvingContext context, CanyonCarverConfiguration config,
                                      ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                      RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                                      CarvingMask carvingMask) {

        // Simple canyon carving - just create a valley
        boolean carved = false;
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();

        double centerX = chunkPos.getMiddleBlockX();
        double centerZ = chunkPos.getMiddleBlockZ();
        double width = 8.0 + random.nextDouble() * 8.0;

        for (int x = chunkMinX; x < chunkMinX + 16; x++) {
            for (int z = chunkMinZ; z < chunkMinZ + 16; z++) {
                double distance = Math.sqrt((x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ));

                if (distance < width) {
                    int depth = (int)(20 * (1.0 - distance / width));

                    for (int y = 60; y < 60 + depth; y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState current = chunk.getBlockState(pos);

                        if (this.canReplaceBlock(config, current)) {
                            chunk.setBlockState(pos, Blocks.CAVE_AIR.defaultBlockState(), false);
                            carved = true;
                        }
                    }
                }
            }
        }

        return carved;
    }

    private boolean isPillowBiome(ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction) {
        BlockPos center = chunk.getPos().getMiddleBlockPosition(64);
        return biomeFunction.apply(center).is(ModBiomes.PILLOW_PLATEAU);
    }

    @Override
    public boolean isStartChunk(CanyonCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= 0.05F;
    }
}

class WoolCaveCarver extends BasePillowCarver<CaveCarverConfiguration> {
    public WoolCaveCarver(com.mojang.serialization.Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeFunction, RandomSource random,
                         Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {

        if (!isPillowBiome(chunk, biomeFunction)) return false;
        if (random.nextFloat() > 0.2F) return false; // 20% chance

        return createWoolCaves(context, config, chunk, biomeFunction, random, aquifer, chunkPos, carvingMask);
    }

    private boolean createWoolCaves(CarvingContext context, CaveCarverConfiguration config,
                                    ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                    RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                                    CarvingMask carvingMask) {

        boolean carved = false;
        int attempts = random.nextInt(2) + 1;

        for (int i = 0; i < attempts; i++) {
            double centerX = chunkPos.getMiddleBlockX() + random.nextInt(16);
            double centerY = config.y.sample(random, context);
            double centerZ = chunkPos.getMiddleBlockZ() + random.nextInt(16);

            // Small wool-lined caves
            double radiusH = config.horizontalRadiusMultiplier.sample(random) * 0.4;
            double radiusV = config.verticalRadiusMultiplier.sample(random) * 0.3;

            carved |= this.carveEllipsoid(
                    context, config, chunk, biomeFunction, aquifer,
                    centerX, centerY, centerZ, radiusH, radiusV,
                    carvingMask, (ctx, x, y, z, yPos) -> false
            );
        }

        return carved;
    }

    private boolean isPillowBiome(ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction) {
        BlockPos center = chunk.getPos().getMiddleBlockPosition(64);
        return biomeFunction.apply(center).is(ModBiomes.PILLOW_PLATEAU);
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= 0.2F;
    }
}