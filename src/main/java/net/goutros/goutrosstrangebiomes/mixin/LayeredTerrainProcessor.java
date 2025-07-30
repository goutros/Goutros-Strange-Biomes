package net.goutros.goutrosstrangebiomes.worldgen;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class LayeredTerrainProcessor {

    public static void processChunk(ChunkAccess chunk) {
        // Check if this chunk contains pillow biome
        if (!isPillowChunk(chunk)) return;

        // Process entire chunk at once - much faster than per-block
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                processColumn(chunk, x, z);
            }
        }

        // Update heightmaps after modification
        chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING);
        chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
    }

    private static void processColumn(ChunkAccess chunk, int localX, int localZ) {
        int worldX = chunk.getPos().getMinBlockX() + localX;
        int worldZ = chunk.getPos().getMinBlockZ() + localZ;

        // Calculate target layer for this position
        double noise = Math.sin(worldX * 0.003) * Math.cos(worldZ * 0.003);
        int targetLayer = (int) Math.floor(noise * 5.0 + 5.0); // 0-10 layers
        int targetHeight = targetLayer * 25 + 40; // 25 blocks per layer

        // Clear everything above target height
        for (int y = targetHeight + 15; y < chunk.getMaxBuildHeight(); y++) {
            chunk.setBlockState(new BlockPos(localX, y, localZ), Blocks.AIR.defaultBlockState(), false);
        }

        // Create solid platform at target height
        for (int y = targetHeight - 5; y <= targetHeight + 15; y++) {
            if (y >= chunk.getMinBuildHeight() && y < chunk.getMaxBuildHeight()) {
                BlockState state = getBlockForHeight(y, targetHeight);
                chunk.setBlockState(new BlockPos(localX, y, localZ), state, false);
            }
        }
    }

    private static BlockState getBlockForHeight(int y, int targetHeight) {
        if (y == targetHeight + 15) {
            return ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState(); // Surface
        } else if (y > targetHeight + 10) {
            return ModBlocks.BROWN_PILLOW.get().defaultBlockState(); // Near surface
        } else {
            return Blocks.STONE.defaultBlockState(); // Deep layer
        }
    }

    private static boolean isPillowChunk(ChunkAccess chunk) {
        BlockPos center = chunk.getPos().getMiddleBlockPosition(64);
        return chunk.getNoiseBiome(center.getX() >> 2, center.getY() >> 2, center.getZ() >> 2)
                .is(ModBiomes.PILLOW_PLATEAU);
    }
}
