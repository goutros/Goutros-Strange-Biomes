package net.goutros.goutrosstrangebiomes.worldgen.api.density;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;

public class EnhancedNoiseRouter {

    public static NoiseRouter enhance(NoiseRouter vanilla) {
        // Wrap the final density with our custom function
        DensityFunction enhancedFinalDensity = new CustomTerrainDensity(
                vanilla.finalDensity(),
                2.0 // Amplifier for terrain modification
        );

        // Create new router with enhanced final density
        return new NoiseRouter(
                vanilla.barrierNoise(),
                vanilla.fluidLevelFloodednessNoise(),
                vanilla.fluidLevelSpreadNoise(),
                vanilla.lavaNoise(),
                vanilla.temperature(),
                vanilla.vegetation(),
                vanilla.continents(),
                vanilla.erosion(),
                vanilla.depth(),
                vanilla.ridges(),
                vanilla.initialDensityWithoutJaggedness(),
                enhancedFinalDensity, // Our custom density function
                vanilla.veinToggle(),
                vanilla.veinRidged(),
                vanilla.veinGap()
        );
    }
}