package net.goutros.goutrosstrangebiomes.worldgen.api.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CustomTerrainDensity implements DensityFunction.SimpleFunction {

    public static final MapCodec<CustomTerrainDensity> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(f -> f.input),
                    Codec.DOUBLE.fieldOf("amplifier").forGetter(f -> f.amplifier)
            ).apply(instance, CustomTerrainDensity::new)
    );

    public static final KeyDispatchDataCodec<CustomTerrainDensity> KEY_CODEC =
            KeyDispatchDataCodec.of(CODEC);

    private final DensityFunction input;
    private final double amplifier;

    public CustomTerrainDensity(DensityFunction input, double amplifier) {
        this.input = input;
        this.amplifier = amplifier;
    }

    @Override
    public double compute(FunctionContext context) {
        double baseValue = input.compute(context);

        // Apply custom terrain modifications for Pillow Plateau areas
        if (isPillowPlateauArea(context)) {
            return modifyForPillowTerrain(baseValue, context);
        }

        return baseValue;
    }

    private boolean isPillowPlateauArea(FunctionContext context) {
        // Simple coordinate-based detection for now
        int x = context.blockX();
        int z = context.blockZ();

        // Use simple noise pattern to determine pillow areas
        double noise = Math.sin(x * 0.01) * Math.cos(z * 0.01);
        return noise > 0.3;
    }

    private double modifyForPillowTerrain(double baseValue, FunctionContext context) {
        int y = context.blockY();

        // Create pillow-like terrain - softer, more rounded
        double pillowEffect = Math.sin(y * 0.02) * amplifier;
        return baseValue + pillowEffect * 0.5;
    }

    @Override
    public double minValue() {
        return input.minValue() - amplifier;
    }

    @Override
    public double maxValue() {
        return input.maxValue() + amplifier;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}