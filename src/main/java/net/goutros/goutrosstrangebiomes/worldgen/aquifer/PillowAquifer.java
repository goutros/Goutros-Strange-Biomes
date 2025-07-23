package net.goutros.goutrosstrangebiomes.worldgen.aquifer;

import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public class PillowAquifer implements Aquifer {

    private final Aquifer delegate;
    private final NoiseChunk noiseChunk;

    public PillowAquifer(NoiseChunk noiseChunk, ChunkPos chunkPos, NoiseRouter noiseRouter,
                         PositionalRandomFactory randomFactory, int minY, int height,
                         Aquifer.FluidPicker fluidPicker) {
        this.noiseChunk = noiseChunk;
        this.delegate = Aquifer.create(noiseChunk, chunkPos, noiseRouter, randomFactory, minY, height, fluidPicker);
    }

    @Override
    public BlockState computeSubstance(DensityFunction.FunctionContext context, double density) {
        // Check if we're in Pillow Plateau biome
        if (isPillowPlateauArea(context)) {
            return computePillowSubstance(context, density);
        }

        return delegate.computeSubstance(context, density);
    }

    private boolean isPillowPlateauArea(DensityFunction.FunctionContext context) {
        double x = context.blockX();
        double z = context.blockZ();

        double noise = Math.sin(x * 0.01) * Math.cos(z * 0.01);
        return noise > 0.3;
    }

    private BlockState computePillowSubstance(DensityFunction.FunctionContext context, double density) {
        int y = context.blockY();

        // Custom water level for Pillow Plateau (slightly higher than sea level)
        int customSeaLevel = 65;

        if (density > 0.0) {
            return null; // Air
        }

        // Custom fluid placement
        if (y <= customSeaLevel) {
            // Create small underground springs with special water
            if (isSpringLocation(context)) {
                return Blocks.WATER.defaultBlockState();
            }

            // Less water overall in pillow areas
            if (y <= customSeaLevel - 5) {
                return Blocks.WATER.defaultBlockState();
            }
        }

        return null;
    }

    private boolean isSpringLocation(DensityFunction.FunctionContext context) {
        // Use simple noise to determine spring locations
        int x = context.blockX();
        int z = context.blockZ();
        int y = context.blockY();

        // Springs more likely in specific Y ranges
        if (y < 45 || y > 80) return false;

        // Simple noise pattern for spring placement
        double noise = Math.sin(x * 0.1) * Math.cos(z * 0.1) * Math.sin(y * 0.05);
        return noise > 0.7;
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return delegate.shouldScheduleFluidUpdate();
    }

    /**
     * Factory method to create Pillow Aquifer
     */
    public static Aquifer create(NoiseChunk noiseChunk, ChunkPos chunkPos,
                                 NoiseRouter noiseRouter, PositionalRandomFactory randomFactory,
                                 int minY, int height, Aquifer.FluidPicker fluidPicker) {
        return new PillowAquifer(noiseChunk, chunkPos, noiseRouter, randomFactory, minY, height, fluidPicker);
    }
}