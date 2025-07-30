package net.goutros.goutrosstrangebiomes.worldgen.density;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom density functions for Pillow Plateau terrain generation.
 * This creates the layered mesa-style terrain properly using vanilla noise.
 */
public class ModDensityFunctions {

    public static final DeferredRegister<DensityFunction> DENSITY_FUNCTIONS =
            DeferredRegister.create(Registries.DENSITY_FUNCTION, GoutrosStrangeBiomes.MOD_ID);

    // Resource Keys for our density functions
    public static final ResourceKey<DensityFunction> PILLOW_PLATEAU_TERRAIN_KEY =
            createKey("pillow_plateau_terrain");
    public static final ResourceKey<DensityFunction> PILLOW_PLATEAU_LAYERING_KEY =
            createKey("pillow_plateau_layering");
    public static final ResourceKey<DensityFunction> PILLOW_PLATEAU_TERRACING_KEY =
            createKey("pillow_plateau_terracing");

    private static ResourceKey<DensityFunction> createKey(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION,
                ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, name));
    }

    /**
     * Bootstrap method for data generation
     */
    public static void bootstrapDensityFunctions(BootstrapContext<DensityFunction> context) {
        HolderGetter<NormalNoise.NoiseParameters> noises = context.lookup(Registries.NOISE);

        // Register our custom density functions
        context.register(PILLOW_PLATEAU_TERRAIN_KEY, createPillowPlateauTerrain(noises));
        context.register(PILLOW_PLATEAU_LAYERING_KEY, createPillowPlateauLayering(noises));
        context.register(PILLOW_PLATEAU_TERRACING_KEY, createPillowPlateauTerracing(noises));
    }

    /**
     * Creates the main terrain density function for pillow plateau
     * This works WITH vanilla noise instead of against it
     */
    private static DensityFunction createPillowPlateauTerrain(HolderGetter<NormalNoise.NoiseParameters> noises) {
        // Start with vanilla continents for smooth biome blending
        DensityFunction continents = DensityFunctions.noise(noises.getOrThrow(Noises.CONTINENTALNESS), 1.0, 1.0);

        // Create base mesa-style terracing
        DensityFunction terracing = createTerracing(noises);

        // Blend them together for natural transitions
        DensityFunction blended = DensityFunctions.add(
                DensityFunctions.mul(continents, DensityFunctions.constant(0.3)), // 30% vanilla influence
                DensityFunctions.mul(terracing, DensityFunctions.constant(0.7))    // 70% mesa influence
        );

        // Apply height scaling (similar to vanilla)
        return DensityFunctions.add(
                blended,
                DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5) // Standard height gradient
        );
    }

    /**
     * Creates terracing effect for mesa-style terrain
     */
    private static DensityFunction createTerracing(HolderGetter<NormalNoise.NoiseParameters> noises) {
        // Base noise for overall shape
        DensityFunction baseNoise = DensityFunctions.noise(noises.getOrThrow(Noises.RIDGE), 0.01, 1.0);

        // Create stepped terraces using a different approach since Mapped.Type is protected
        // We'll use multiple range choices to create step effects
        DensityFunction scaledNoise = DensityFunctions.mul(
                DensityFunctions.add(baseNoise, DensityFunctions.constant(1.0)),
                DensityFunctions.constant(4.0) // 4 discrete levels
        );

        // Create stepped effect using range choices
        DensityFunction stepped = DensityFunctions.rangeChoice(
                scaledNoise,
                0.0, 1.0,
                DensityFunctions.constant(0.0),
                DensityFunctions.rangeChoice(
                        scaledNoise,
                        1.0, 2.0,
                        DensityFunctions.constant(0.25),
                        DensityFunctions.rangeChoice(
                                scaledNoise,
                                2.0, 3.0,
                                DensityFunctions.constant(0.5),
                                DensityFunctions.constant(0.75)
                        )
                )
        );

        // Add detail noise for organic variation
        DensityFunction detail = DensityFunctions.noise(noises.getOrThrow(Noises.RIDGE), 0.05, 0.1);

        return DensityFunctions.add(stepped, detail);
    }

    /**
     * Creates layering effect for different wool colors at different heights
     */
    private static DensityFunction createPillowPlateauLayering(HolderGetter<NormalNoise.NoiseParameters> noises) {
        // Horizontal variation for layer colors
        DensityFunction horizontal = DensityFunctions.noise(noises.getOrThrow(Noises.NOODLE), 0.02, 1.0);

        // Vertical gradient for height-based layers
        DensityFunction vertical = DensityFunctions.yClampedGradient(60, 160, 0.0, 8.0);

        // Combine for layered effect
        return DensityFunctions.add(horizontal, vertical);
    }

    /**
     * Creates micro-terracing for detailed step effects
     */
    private static DensityFunction createPillowPlateauTerracing(HolderGetter<NormalNoise.NoiseParameters> noises) {
        // Fine detail for micro-steps
        DensityFunction microNoise = DensityFunctions.noise(noises.getOrThrow(Noises.SURFACE), 0.1, 0.3);

        // Create stepped effect using range choices instead of protected Mapped.Type
        DensityFunction scaledNoise = DensityFunctions.mul(microNoise, DensityFunctions.constant(2.0));

        return DensityFunctions.rangeChoice(
                scaledNoise,
                -1.0, 0.0,
                DensityFunctions.constant(-0.25),
                DensityFunctions.rangeChoice(
                        scaledNoise,
                        0.0, 1.0,
                        DensityFunctions.constant(0.0),
                        DensityFunctions.constant(0.25)
                )
        );
    }
}