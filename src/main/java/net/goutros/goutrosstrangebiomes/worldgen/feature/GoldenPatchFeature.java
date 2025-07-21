package net.goutros.goutrosstrangebiomes.worldgen.feature;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * OPTIONAL: Enhanced golden patch features
 *
 * This can add special decorations to golden pillow patches,
 * like small button clusters or special formations.
 */
public class GoldenPatchFeature extends Feature<NoneFeatureConfiguration> {

    public GoldenPatchFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // Only place in golden pillow areas
        BlockState surfaceState = level.getBlockState(pos);
        if (!surfaceState.is(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get())) {
            return false;
        }

        // Chance to place buttons on golden patches
        if (random.nextFloat() < 0.3f) {
            BlockPos buttonPos = pos.above();
            if (level.getBlockState(buttonPos).isAir()) {
                level.setBlock(buttonPos, ModBlocks.BUTTONS.get().defaultBlockState(), 3);
                return true;
            }
        }

        // Chance to create small golden formations
        if (random.nextFloat() < 0.1f) {
            for (int i = 0; i < 3; i++) {
                BlockPos randomPos = pos.offset(
                        random.nextInt(3) - 1,
                        0,
                        random.nextInt(3) - 1
                );

                if (level.getBlockState(randomPos).is(ModBlocks.PILLOW_GRASS_BLOCK.get())) {
                    level.setBlock(randomPos, ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState(), 3);
                }
            }
            return true;
        }

        return false;
    }
}