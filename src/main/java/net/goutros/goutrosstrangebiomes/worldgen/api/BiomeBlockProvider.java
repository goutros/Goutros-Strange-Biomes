package net.goutros.goutrosstrangebiomes.worldgen.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Interface for providing custom blocks that blend with vanilla
 */
public interface BiomeBlockProvider {

    /**
     * Get the block state at a position
     * @param pos Block position
     * @param vanillaBlock The vanilla block that would be placed here
     * @param biomeInfluence How much this biome influences this position (0-1)
     * @param depthFromSurface How far below the surface this block is
     * @param randomState Random state for generation
     * @return Block state to place
     */
    BlockState getBlockState(BlockPos pos, BlockState vanillaBlock, double biomeInfluence,
                             int depthFromSurface, RandomState randomState);

    /**
     * Get the surface block for this biome
     */
    BlockState getSurfaceBlock(BlockPos pos, double biomeInfluence, RandomState randomState);

    /**
     * Get the subsurface block for this biome
     */
    BlockState getSubsurfaceBlock(BlockPos pos, double biomeInfluence, int depth, RandomState randomState);
}