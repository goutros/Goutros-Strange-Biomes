package net.goutros.goutrosstrangebiomes.worldgen.pillow;

import net.goutros.goutrosstrangebiomes.worldgen.api.BiomeBlockProvider;
import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.util.RandomSource;

/**
 * Pillow Plateau block provider that creates layered wool terrain
 */
public class PillowPlateauBlockProvider implements BiomeBlockProvider {

    private static final ThreadLocal<SimplexNoise> LAYER_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(45678L))
    );

    @Override
    public BlockState getBlockState(BlockPos pos, BlockState vanillaBlock, double biomeInfluence,
                                    int depthFromSurface, RandomState randomState) {

        if (biomeInfluence < 0.3) {
            return vanillaBlock; // Not enough influence
        }

        // Get layer at this position
        int layerIndex = getLayerIndex(pos.getX(), pos.getZ());

        if (biomeInfluence > 0.8) {
            // High influence - full pillow blocks
            return getPillowBlock(depthFromSurface, layerIndex);
        } else if (biomeInfluence > 0.5) {
            // Medium influence - pillow grass/dirt
            if (depthFromSurface == 0) {
                return layerIndex <= 2 ? ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState()
                        : ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState();
            } else if (depthFromSurface <= 3) {
                return ModBlocks.PILLOW_DIRT.get().defaultBlockState();
            }
        } else {
            // Low influence - subtle changes
            if (depthFromSurface == 0) {
                return Blocks.COARSE_DIRT.defaultBlockState();
            }
        }

        return vanillaBlock;
    }

    @Override
    public BlockState getSurfaceBlock(BlockPos pos, double biomeInfluence, RandomState randomState) {
        int layerIndex = getLayerIndex(pos.getX(), pos.getZ());

        if (biomeInfluence > 0.8) {
            return getPillowSurfaceBlock(layerIndex);
        } else if (biomeInfluence > 0.5) {
            return layerIndex <= 2 ? ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState()
                    : ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState();
        }

        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    @Override
    public BlockState getSubsurfaceBlock(BlockPos pos, double biomeInfluence, int depth, RandomState randomState) {
        int layerIndex = getLayerIndex(pos.getX(), pos.getZ());

        if (biomeInfluence > 0.8) {
            return getPillowSubsurfaceBlock(depth, layerIndex);
        } else if (biomeInfluence > 0.5) {
            return ModBlocks.PILLOW_DIRT.get().defaultBlockState();
        }

        return Blocks.DIRT.defaultBlockState();
    }

    private int getLayerIndex(int x, int z) {
        double layerNoise = LAYER_NOISE.get().getValue(x * 0.008, z * 0.008);
        layerNoise = (layerNoise + 1.0) * 0.5;

        int layerIndex = (int) (layerNoise * 7);
        return Math.max(0, Math.min(6, layerIndex));
    }

    private BlockState getPillowBlock(int depthFromSurface, int layerIndex) {
        if (depthFromSurface == 0) {
            return getPillowSurfaceBlock(layerIndex);
        } else if (depthFromSurface <= 2) {
            return getPillowSubsurfaceBlock(depthFromSurface, layerIndex);
        } else if (depthFromSurface <= 8) {
            return Blocks.PURPLE_WOOL.defaultBlockState();
        }

        return Blocks.STONE.defaultBlockState();
    }

    private BlockState getPillowSurfaceBlock(int layerIndex) {
        return switch (layerIndex) {
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

    private BlockState getPillowSubsurfaceBlock(int depth, int layerIndex) {
        return switch (layerIndex) {
            case 0, 1 -> Blocks.CYAN_WOOL.defaultBlockState();
            case 2, 3 -> Blocks.BLUE_WOOL.defaultBlockState();
            case 4, 5 -> Blocks.PURPLE_WOOL.defaultBlockState();
            case 6 -> Blocks.MAGENTA_WOOL.defaultBlockState();
            default -> Blocks.GRAY_WOOL.defaultBlockState();
        };
    }
}