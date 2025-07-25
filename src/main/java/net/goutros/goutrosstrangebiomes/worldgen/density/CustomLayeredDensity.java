package net.goutros.goutrosstrangebiomes.worldgen.density;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public class CustomLayeredDensity implements DensityFunction.SimpleFunction {

    public static final MapCodec<CustomLayeredDensity> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(f -> f.input)
            ).apply(instance, CustomLayeredDensity::new)
    );

    public static final KeyDispatchDataCodec<CustomLayeredDensity> KEY_CODEC =
            KeyDispatchDataCodec.of(CODEC);

    private final DensityFunction input;

    public CustomLayeredDensity(DensityFunction input) {
        this.input = input;
    }

    @Override
    public double compute(FunctionContext context) {
        double baseValue = input.compute(context);

        // Only modify in pillow biome areas
        if (!isPillowArea(context)) {
            return baseValue;
        }

        int y = context.blockY();
        double x = context.blockX();
        double z = context.blockZ();

        // Create MESA-STYLE layered terrain
        double layerNoise = createLayeredTerrain(x, y, z);

        // Combine with base terrain
        return baseValue + layerNoise;
    }

    private double createLayeredTerrain(double x, double y, double z) {
        // Create horizontal "shelves" every 8-12 blocks
        double shelfHeight = 10.0; // Height between layers
        double currentShelf = Math.floor(y / shelfHeight);
        double distanceFromShelf = (y % shelfHeight) / shelfHeight;

        // Make terrain "step" at each shelf level
        double shelfEffect = 0.0;
        if (distanceFromShelf < 0.7) { // 70% of shelf is flat
            shelfEffect = -2.0; // Carve out to create shelf
        } else {
            shelfEffect = 4.0; // Sharp cliff face
        }

        // Add horizontal variation for canyon/mesa shape
        double horizontalNoise = Math.sin(x * 0.01) * Math.cos(z * 0.01);
        double canyonEffect = horizontalNoise * 15.0; // Deep canyons

        // Combine effects
        return shelfEffect + canyonEffect;
    }

    private boolean isPillowArea(FunctionContext context) {
        // Simple coordinate-based detection
        double x = context.blockX();
        double z = context.blockZ();
        double noise = Math.sin(x * 0.008) * Math.cos(z * 0.008);
        return noise > 0.3; // Matches TerraBlender region placement
    }

    @Override
    public double minValue() {
        return input.minValue() - 20.0;
    }

    @Override
    public double maxValue() {
        return input.maxValue() + 20.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
