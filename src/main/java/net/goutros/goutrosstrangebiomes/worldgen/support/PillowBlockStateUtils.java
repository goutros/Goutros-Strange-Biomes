package net.goutros.goutrosstrangebiomes.worldgen.support;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class PillowBlockStateUtils {

    /**
     * Smart block placement that considers pillow terrain
     */
    public static boolean placeBlockSmart(ChunkAccess chunk, BlockPos pos, BlockState state,
                                          RandomSource random) {
        BlockState existing = chunk.getBlockState(pos);

        if (isPillowBiome(chunk, pos)) {
            return placePillowBlock(chunk, pos, state, existing, random);
        }

        return placeVanillaBlock(chunk, pos, state, existing);
    }

    private static boolean placePillowBlock(ChunkAccess chunk, BlockPos pos, BlockState newState,
                                            BlockState existing, RandomSource random) {
        // Convert vanilla blocks to pillow equivalents
        newState = convertToPillowBlock(newState);

        // Check if placement is appropriate
        if (canReplace(existing, newState)) {
            chunk.setBlockState(pos, newState, false);

            // Add some variation
            if (random.nextFloat() < 0.1f && isGrassLike(newState)) {
                addPillowVariation(chunk, pos, random);
            }

            return true;
        }

        return false;
    }

    private static boolean placeVanillaBlock(ChunkAccess chunk, BlockPos pos, BlockState newState,
                                             BlockState existing) {
        if (canReplace(existing, newState)) {
            chunk.setBlockState(pos, newState, false);
            return true;
        }
        return false;
    }

    private static BlockState convertToPillowBlock(BlockState original) {
        Block block = original.getBlock();

        if (block == Blocks.GRASS_BLOCK) {
            return ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState();
        } else if (block == Blocks.DIRT) {
            return ModBlocks.PILLOW_DIRT.get().defaultBlockState();
        }

        return original;
    }

    private static boolean canReplace(BlockState existing, BlockState newState) {
        return existing.isAir() ||
                existing.is(Blocks.WATER) ||
                existing.is(net.minecraft.tags.BlockTags.REPLACEABLE) ||
                existing.getBlock() == newState.getBlock();
    }

    private static boolean isGrassLike(BlockState state) {
        return state.is(ModBlocks.PILLOW_GRASS_BLOCK.get()) ||
                state.is(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get());
    }

    private static void addPillowVariation(ChunkAccess chunk, BlockPos pos, RandomSource random) {
        // Occasionally place golden pillow grass
        if (random.nextFloat() < 0.05f) {
            chunk.setBlockState(pos, ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState(), false);
        }

        // Add buttons on adjacent blocks
        if (random.nextFloat() < 0.02f) {
            for (int i = 0; i < 4; i++) {
                BlockPos adjacent = pos.offset(
                        random.nextInt(3) - 1, 1, random.nextInt(3) - 1);

                if (chunk.getBlockState(adjacent).isAir()) {
                    chunk.setBlockState(adjacent, ModBlocks.BUTTONS.get().defaultBlockState(), false);
                    break;
                }
            }
        }
    }

    private static boolean isPillowBiome(ChunkAccess chunk, BlockPos pos) {
        return chunk.getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2)
                .is(ModBiomes.PILLOW_PLATEAU);
    }

    /**
     * Bulk placement utility for terrain generation
     */
    public static class BulkPlacer {
        private final ChunkAccess chunk;
        private final RandomSource random;
        private int placedBlocks = 0;

        public BulkPlacer(ChunkAccess chunk, RandomSource random) {
            this.chunk = chunk;
            this.random = random;
        }

        public BulkPlacer place(BlockPos pos, BlockState state) {
            if (PillowBlockStateUtils.placeBlockSmart(chunk, pos, state, random)) {
                placedBlocks++;
            }
            return this;
        }

        public BulkPlacer placeColumn(int x, int z, int startY, int endY, BlockState state) {
            for (int y = startY; y <= endY; y++) {
                place(new BlockPos(x, y, z), state);
            }
            return this;
        }

        public BulkPlacer fillArea(BlockPos start, BlockPos end, BlockState state) {
            int minX = Math.min(start.getX(), end.getX());
            int maxX = Math.max(start.getX(), end.getX());
            int minY = Math.min(start.getY(), end.getY());
            int maxY = Math.max(start.getY(), end.getY());
            int minZ = Math.min(start.getZ(), end.getZ());
            int maxZ = Math.max(start.getZ(), end.getZ());

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        place(new BlockPos(x, y, z), state);
                    }
                }
            }
            return this;
        }

        public int getPlacedCount() {
            return placedBlocks;
        }
    }
}
