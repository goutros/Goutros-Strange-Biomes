package net.goutros.goutrosstrangebiomes.worldgen.density;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.util.RandomSource;

/**
 * Density function that creates Pillow Plateau terrain with location-based influence
 */
public record PillowDensityFunction(DensityFunction baseFunction, double influence,
                                    double heightMultiplier) implements DensityFunction.SimpleFunction {

    public static final MapCodec<PillowDensityFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("base_function").forGetter(PillowDensityFunction::baseFunction),
                    Codec.DOUBLE.fieldOf("influence").forGetter(PillowDensityFunction::influence),
                    Codec.DOUBLE.fieldOf("height_multiplier").forGetter(PillowDensityFunction::heightMultiplier)
            ).apply(instance, PillowDensityFunction::new)
    );

    private static final SimplexNoise LAYER_NOISE = new SimplexNoise(RandomSource.create(12345L));
    private static final SimplexNoise DETAIL_NOISE = new SimplexNoise(RandomSource.create(67890L));
    private static final SimplexNoise BIOME_MASK_NOISE = new SimplexNoise(RandomSource.create(54321L));

    @Override
    public double compute(FunctionContext context) {
        double baseValue = baseFunction.compute(context);

        // Calculate pillow influence using noise-based biome detection
        double pillowInfluence = calculatePillowInfluence(context);

        if (pillowInfluence <= 0.01) {
            return baseValue; // No significant Pillow influence
        }

        // Generate Pillow plateau modification
        double pillowModification = generatePillowModification(context);

        // Blend modifications
        return baseValue + (pillowModification * pillowInfluence * influence);
    }

    private double calculatePillowInfluence(FunctionContext context) {
        double x = context.blockX() * 0.008; // Biome-scale frequency
        double z = context.blockZ() * 0.008;

        // Primary biome mask - determines where Pillow areas should be
        double biomeMask = BIOME_MASK_NOISE.getValue(x, z);
        biomeMask = (biomeMask + 1.0) * 0.5; // Normalize to 0-1

        // Only in specific regions (matching TerraBlender placement)
        if (biomeMask < 0.7) return 0.0; // No Pillow influence in most areas

        // Smooth falloff at edges
        double edgeFactor = Math.min(1.0, (biomeMask - 0.7) / 0.2); // Smooth 0.7-0.9 range

        // Add some variation to blend edges
        double blendNoise = DETAIL_NOISE.getValue(x * 2, z * 2) * 0.1;

        return Math.max(0.0, Math.min(1.0, edgeFactor + blendNoise));
    }

    private double generatePillowModification(FunctionContext context) {
        double x = context.blockX() * 0.01;
        double z = context.blockZ() * 0.01;
        double y = context.blockY();

        // Layer selection noise
        double layerNoise = LAYER_NOISE.getValue(x, z);
        layerNoise = (layerNoise + 1.0) * 0.5; // Normalize to 0-1

        // Height layers for plateau effect
        double[] plateauHeights = {45, 55, 65, 75, 85, 95, 105};
        int layerIndex = (int) (layerNoise * plateauHeights.length);
        layerIndex = Math.max(0, Math.min(layerIndex, plateauHeights.length - 1));

        double targetHeight = plateauHeights[layerIndex];

        // Add detail variation
        double detailNoise = DETAIL_NOISE.getValue(x * 1.5, z * 1.5);
        targetHeight += detailNoise * 6; // ±6 block variation

        // Create density modification based on distance from target height
        double heightDifference = y - targetHeight;

        // Plateau density: positive below target, negative above
        if (heightDifference < -20) {
            return 0.8; // Solid below plateau
        } else if (heightDifference < 0) {
            return 0.8 * (1.0 + heightDifference / 20.0); // Gradient to surface
        } else if (heightDifference < 5) {
            return -0.1 * (heightDifference / 5.0); // Slight carving above surface
        } else {
            return -0.1; // Air above plateau
        }
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        baseFunction.fillArray(array, contextProvider);

        for (int i = 0; i < array.length; i++) {
            FunctionContext context = contextProvider.forIndex(i);
            double pillowInfluence = calculatePillowInfluence(context);

            if (pillowInfluence > 0.01) {
                double pillowModification = generatePillowModification(context);
                array[i] += pillowModification * pillowInfluence * influence;
            }
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new PillowDensityFunction(
                baseFunction.mapAll(visitor),
                influence,
                heightMultiplier
        ));
    }

    @Override
    public double minValue() {
        return baseFunction.minValue() - 1.0;
    }

    @Override
    public double maxValue() {
        return baseFunction.maxValue() + 1.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return new KeyDispatchDataCodec<>(CODEC);
    }
}