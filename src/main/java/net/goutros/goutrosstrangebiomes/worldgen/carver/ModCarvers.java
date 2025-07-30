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

    // Wool Canyon Carver - Colorful wool-layered canyons
    public static final DeferredHolder<WorldCarver<?>, WoolCanyonCarver> WOOL_CANYON =
            CARVERS.register("wool_canyon", () -> new WoolCanyonCarver(CanyonCarverConfiguration.CODEC));

    // Bubble Chamber Carver - Unique to pillow biomes
    public static final DeferredHolder<WorldCarver<?>, BubbleChamberCarver> BUBBLE_CHAMBER =
            CARVERS.register("bubble_chamber", () -> new BubbleChamberCarver(CaveCarverConfiguration.CODEC));

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

/**
 * Enhanced wool cave carver that creates cozy wool-lined caves
 */
class WoolCaveCarver extends BasePillowCarver<CaveCarverConfiguration> {

    private static final BlockState[] WOOL_CAVE_BLOCKS = {
            Blocks.WHITE_WOOL.defaultBlockState(),
            Blocks.LIGHT_GRAY_WOOL.defaultBlockState(),
            Blocks.PINK_WOOL.defaultBlockState(),
            ModBlocks.BROWN_PILLOW.get().defaultBlockState()
    };

    public WoolCaveCarver(com.mojang.serialization.Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, CaveCarverConfiguration config, ChunkAccess chunk,
                         Function<BlockPos, Holder<Biome>> biomeFunction, RandomSource random,
                         Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask) {

        if (!isPillowBiomeChunk(chunk, biomeFunction)) {
            return false;
        }

        return carveWoolCaves(context, config, chunk, biomeFunction, random, aquifer, chunkPos, carvingMask);
    }

    private boolean carveWoolCaves(CarvingContext context, CaveCarverConfiguration config,
                                   ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                   RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                                   CarvingMask carvingMask) {

        int attempts = random.nextInt(3) + 1;
        boolean carved = false;

        for (int i = 0; i < attempts; i++) {
            double centerX = chunkPos.getMiddleBlockX() + random.nextInt(16);
            double centerY = config.y.sample(random, context);
            double centerZ = chunkPos.getMiddleBlockZ() + random.nextInt(16);

            // Smaller, cozier caves
            double radiusH = config.horizontalRadiusMultiplier.sample(random) * 0.6;
            double radiusV = config.verticalRadiusMultiplier.sample(random) * 0.5;

            carved |= carveWoolChamber(context, config, chunk, biomeFunction, aquifer,
                    centerX, centerY, centerZ, radiusH, radiusV, carvingMask, random);
        }

        return carved;
    }

    private boolean carveWoolChamber(CarvingContext context, CaveCarverConfiguration config,
                                     ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                     Aquifer aquifer, double centerX, double centerY, double centerZ,
                                     double radiusH, double radiusV, CarvingMask carvingMask, RandomSource random) {

        boolean carved = false;
        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();

        int minX = Math.max(chunkMinX, (int) (centerX - radiusH));
        int maxX = Math.min(chunkMinX + 15, (int) (centerX + radiusH));
        int minY = Math.max(context.getMinGenY(), (int) (centerY - radiusV));
        int maxY = Math.min(context.getMinGenY() + context.getGenDepth(), (int) (centerY + radiusV));
        int minZ = Math.max(chunkMinZ, (int) (centerZ - radiusV));
        int maxZ = Math.min(chunkMinZ + 15, (int) (centerZ + radiusV));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double distanceH = Math.sqrt((x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ));
                    double distanceV = Math.abs(y - centerY);

                    if (distanceH / radiusH + distanceV / radiusV <= 1.0) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState current = chunk.getBlockState(pos);

                        if (canReplaceBlock(config, current)) {
                            // Distance from wall determines block type
                            double wallDistance = 1.0 - (distanceH / radiusH + distanceV / radiusV);

                            if (wallDistance > 0.8) {
                                // Core - air
                                chunk.setBlockState(pos, Blocks.CAVE_AIR.defaultBlockState(), false);
                            } else if (wallDistance > 0.6) {
                                // Near wall - wool
                                int woolIndex = random.nextInt(WOOL_CAVE_BLOCKS.length - 1);
                                chunk.setBlockState(pos, WOOL_CAVE_BLOCKS[woolIndex], false);
                            }
                            // Else keep existing block for natural transition

                            carved = true;
                        }
                    }
                }
            }
        }

        return carved;
    }

    private boolean isPillowBiomeChunk(ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction) {
        BlockPos center = chunk.getPos().getMiddleBlockPosition(64);
        Holder<Biome> biome = biomeFunction.apply(center);
        return biome.is(ModBiomes.PILLOW_PLATEAU);
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= config.probability * 0.5F;
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

        // Create mesa-style layered terrain
        return carveMesaLayers(context, config, chunk, biomeFunction, random, aquifer, chunkPos, carvingMask);
    }

    private boolean carveMesaLayers(CarvingContext context, CanyonCarverConfiguration config,
                                    ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeFunction,
                                    RandomSource random, Aquifer aquifer, ChunkPos chunkPos,
                                    CarvingMask carvingMask) {

        if (random.nextFloat() > 0.65F) return false; // Only 15% chance

        boolean carved = false;
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();

        // Process every column but simpler logic
        for (int x = chunkMinX; x < chunkMinX + 16; x++) {
            for (int z = chunkMinZ; z < chunkMinZ + 16; z++) {
                carved |= carveTerrainColumn(context, config, chunk, x, z, random);
            }
        }

        return carved;
    }

    private boolean carveTerrainColumn(CarvingContext context, CanyonCarverConfiguration config,
                                       ChunkAccess chunk, int x, int z, RandomSource random) {

        BlockPos checkPos = new BlockPos(x, 64, z);
        Holder<Biome> biome = chunk.getNoiseBiome(x >> 2, 64 >> 2, z >> 2);
        if (!biome.is(ModBiomes.PILLOW_PLATEAU)) {
            return false; // Don't carve outside pillow biome
        }

        boolean carved = false;

        // Simple distance-based stepped terrain
        double distFromChunkCenter = Math.sqrt(
                Math.pow(x % 16 - 8, 2) + Math.pow(z % 16 - 8, 2)
        );

        // Create concentric "rings" of different heights
        int heightStep;
        if (distFromChunkCenter < 3) heightStep = 150;      // Center highest
        else if (distFromChunkCenter < 6) heightStep = 120;  // Ring 2
        else if (distFromChunkCenter < 9) heightStep = 90;   // Ring 3
        else if (distFromChunkCenter < 12) heightStep = 60;  // Ring 4
        else heightStep = 30;                               // Outer lowest

        // Add some noise variation
        double noise = Math.sin(x * 0.1) * Math.cos(z * 0.1) * 10.0;
        int finalHeight = heightStep + (int)noise;

        // Remove everything above this height
        for (int y = finalHeight + 1; y <= 180; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState existing = chunk.getBlockState(pos);

            if (canReplaceBlock(config, existing)) {
                chunk.setBlockState(pos, Blocks.CAVE_AIR.defaultBlockState(), false);
                carved = true;
            }
        }

        return carved;
    }

    @Override
    public boolean isStartChunk(CanyonCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= 0.8F; // High probability for mesa generation
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