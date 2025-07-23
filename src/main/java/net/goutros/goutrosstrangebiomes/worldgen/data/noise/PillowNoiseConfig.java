package net.goutros.goutrosstrangebiomes.worldgen.data.noise;

import net.minecraft.world.level.levelgen.synth.NoiseUtils;

public class PillowNoiseConfig {

    public static final double PILLOW_FREQUENCY = 0.01;
    public static final double PILLOW_AMPLITUDE = 8.0;
    public static final double SOFTNESS_FREQUENCY = 0.005;
    public static final double SURFACE_VARIATION = 3.0;

    private final double frequency;
    private final double amplitude;
    private final double softnessScale;
    private final double verticalScale;

    public PillowNoiseConfig(double frequency, double amplitude, double softnessScale, double verticalScale) {
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.softnessScale = softnessScale;
        this.verticalScale = verticalScale;
    }

    public static PillowNoiseConfig defaultConfig() {
        return new PillowNoiseConfig(PILLOW_FREQUENCY, PILLOW_AMPLITUDE, SOFTNESS_FREQUENCY, SURFACE_VARIATION);
    }

    public double calculatePillowNoise(double x, double y, double z) {
        double baseNoise = Math.sin(x * frequency) * Math.cos(z * frequency) * amplitude;
        double softness = Math.sin(x * softnessScale) * Math.cos(z * softnessScale) * 0.5 + 0.5;

        // Apply bias towards extreme for pillow effect
        return NoiseUtils.biasTowardsExtreme(baseNoise, softness) * verticalScale;
    }

    public double getFrequency() { return frequency; }
    public double getAmplitude() { return amplitude; }
    public double getSoftnessScale() { return softnessScale; }
    public double getVerticalScale() { return verticalScale; }
}
