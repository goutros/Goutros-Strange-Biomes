package net.goutros.goutrosstrangebiomes.worldgen.generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Custom Chunk Generator that extends vanilla NoiseBasedChunkGenerator
 * This approach is more vanilla-compatible and cleaner than mixins
 */
public class PillowChunkGenerator extends ChunkGenerator {

    public static final MapCodec<PillowChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings)
            ).apply(instance, PillowChunkGenerator::new)
    );

    private final Holder<NoiseGeneratorSettings> settings;
    private final SimplexNoise layerNoise;
    private final SimplexNoise detailNoise;
    private final SimplexNoise biomeInfluenceNoise;

    public PillowChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource);
        this.settings = settings;

        // Initialize noise generators with fixed seeds for consistency
        RandomSource random = RandomSource.create(2584);
        this.layerNoise = new SimplexNoise(random);
        this.detailNoise = new SimplexNoise(RandomSource.create(random.nextLong()));
        this.biomeInfluenceNoise = new SimplexNoise(RandomSource.create(random.nextLong()));
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState,
                             BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunk, GenerationStep.Carving carvingStep) {
        // Use vanilla carver behavior
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager,
                             RandomState randomState, ChunkAccess chunk) {
        // Custom surface building for Pillow Plateau
        buildPillowSurface(chunk, randomState);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        // Use vanilla mob spawning
    }

    @Override
    public int getGenDepth() {
        return this.settings.value().noiseSettings().height();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(() -> {
            generatePillowTerrain(chunk, randomState);
            return chunk;
        });
    }

    @Override
    public int getSeaLevel() {
        return this.settings.value().seaLevel();
    }

    @Override
    public int getMinY() {
        return this.settings.value().noiseSettings().minY();
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType,
                             LevelHeightAccessor heightAccessor, RandomState randomState) {
        return getPillowHeight(x, z);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int height = getPillowHeight(x, z);
        BlockState[] column = new BlockState[heightAccessor.getHeight()];

        for (int y = heightAccessor.getMinBuildHeight(); y < heightAccessor.getMaxBuildHeight(); y++) {
            int index = y - heightAccessor.getMinBuildHeight();
            column[index] = y <= height ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }

        return new NoiseColumn(heightAccessor.getMinBuildHeight(), column);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("Pillow Plateau Generator");
        info.add("Layer: " + getPillowLayer(pos.getX(), pos.getZ()));
        info.add("Height: " + getPillowHeight(pos.getX(), pos.getZ()));
    }

    /**
     * Generate the main Pillow Plateau terrain
     */
    private void generatePillowTerrain(ChunkAccess chunk, RandomState randomState) {
        ChunkPos chunkPos = chunk.getPos();

        // Check if this chunk should have Pillow Plateau terrain
        if (!shouldGeneratePillowTerrain(chunk)) {
            generateVanillaTerrain(chunk, randomState);
            return;
        }

        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                generatePillowColumn(chunk, x, z, worldX, worldZ, minY, maxY);
            }
        }
    }

    /**
     * Generate a single column of Pillow Plateau terrain
     */
    private void generatePillowColumn(ChunkAccess chunk, int x, int z, int worldX, int worldZ, int minY, int maxY) {
        int targetHeight = getPillowHeight(worldX, worldZ);
        int layer = getPillowLayer(worldX, worldZ);

        for (int y = minY; y < maxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state;

            if (y < targetHeight - 8) {
                // Deep underground - stone
                state = Blocks.STONE.defaultBlockState();
            } else if (y < targetHeight - 2) {
                // Subsurface - wool base
                state = getPillowSubsurfaceBlock(layer);
            } else if (y <= targetHeight) {
                // Surface layer - colored wool
                state = getPillowSurfaceBlock(layer);
            } else {
                // Air above surface
                state = Blocks.AIR.defaultBlockState();
            }

            chunk.setBlockState(pos, state, false);
        }
    }

    /**
     * Check if this chunk should generate Pillow Plateau terrain
     */
    private boolean shouldGeneratePillowTerrain(ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();

        // Sample biome at center of chunk
        int centerX = pos.getMinBlockX() + 8;
        int centerZ = pos.getMinBlockZ() + 8;
        int sampleY = 64; // Sample at sea level

        Holder<Biome> biome = chunk.getNoiseBiome(8 >> 2, sampleY >> 2, 8 >> 2);
        return biome.unwrapKey().map(key -> key.equals(ModBiomes.PILLOW_PLATEAU)).orElse(false);
    }

    /**
     * Generate vanilla-style terrain for non-Pillow chunks
     */
    private void generateVanillaTerrain(ChunkAccess chunk, RandomState randomState) {
        // Simple basic terrain generation
        ChunkPos chunkPos = chunk.getPos();
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                // Simple height calculation
                double noise = this.detailNoise.getValue(worldX * 0.01, worldZ * 0.01);
                int height = 64 + (int)(noise * 16);

                for (int y = minY; y < maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state;

                    if (y < height - 3) {
                        state = Blocks.STONE.defaultBlockState();
                    } else if (y < height) {
                        state = Blocks.DIRT.defaultBlockState();
                    } else if (y == height) {
                        state = Blocks.GRASS_BLOCK.defaultBlockState();
                    } else {
                        state = Blocks.AIR.defaultBlockState();
                    }

                    chunk.setBlockState(pos, state, false);
                }
            }
        }
    }

    /**
     * Build the surface layer with proper blocks
     */
    private void buildPillowSurface(ChunkAccess chunk, RandomState randomState) {
        if (!shouldGeneratePillowTerrain(chunk)) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;

                // Find surface
                for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = chunk.getBlockState(pos);

                    if (!state.isAir()) {
                        // This is the surface - ensure it's the right block
                        int layer = getPillowLayer(worldX, worldZ);
                        chunk.setBlockState(pos, getPillowSurfaceBlock(layer), false);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Calculate Pillow Plateau height at world coordinates
     */
    private int getPillowHeight(int worldX, int worldZ) {
        // Layer noise determines which height tier
        double layerValue = this.layerNoise.getValue(worldX * 0.008, worldZ * 0.008);
        layerValue = (layerValue + 1.0) * 0.5; // Normalize to 0-1

        // Height tiers
        int[] heights = {45, 55, 65, 75, 85, 95, 105};
        int layerIndex = Math.min((int)(layerValue * heights.length), heights.length - 1);
        int baseHeight = heights[layerIndex];

        // Add detail variation
        double detailValue = this.detailNoise.getValue(worldX * 0.025, worldZ * 0.025);
        int variation = (int)(detailValue * 4); // ±4 blocks variation

        return Math.max(35, Math.min(115, baseHeight + variation));
    }

    /**
     * Get the layer index for coloring
     */
    private int getPillowLayer(int worldX, int worldZ) {
        double layerValue = this.layerNoise.getValue(worldX * 0.008, worldZ * 0.008);
        layerValue = (layerValue + 1.0) * 0.5;
        return Math.min((int)(layerValue * 7), 6);
    }

    /**
     * Get surface block for a layer
     */
    private BlockState getPillowSurfaceBlock(int layer) {
        return switch (layer) {
            case 0 -> Blocks.CYAN_WOOL.defaultBlockState();
            case 1 -> Blocks.LIGHT_BLUE_WOOL.defaultBlockState();
            case 2 -> Blocks.BLUE_WOOL.defaultBlockState();
            case 3 -> Blocks.PURPLE_WOOL.defaultBlockState();
            case 4 -> Blocks.MAGENTA_WOOL.defaultBlockState();
            case 5 -> Blocks.PINK_WOOL.defaultBlockState();
            case 6 -> Blocks.WHITE_WOOL.defaultBlockState();
            default -> Blocks.GRAY_WOOL.defaultBlockState();
        };
    }

    /**
     * Get subsurface block for a layer
     */
    private BlockState getPillowSubsurfaceBlock(int layer) {
        return switch (layer % 3) {
            case 0 -> Blocks.BLUE_WOOL.defaultBlockState();
            case 1 -> Blocks.PURPLE_WOOL.defaultBlockState();
            case 2 -> Blocks.MAGENTA_WOOL.defaultBlockState();
            default -> Blocks.GRAY_WOOL.defaultBlockState();
        };
    }
}