package net.goutros.goutrosstrangebiomes.worldgen.data;

import net.goutros.goutrosstrangebiomes.worldgen.data.noise.PillowNoiseConfig;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class PillowGenerationContext extends WorldGenerationContext {

    private final PillowNoiseConfig noiseConfig;
    private final boolean isPillowBiome;
    private final double biomeBlendRadius;

    public PillowGenerationContext(net.minecraft.world.level.chunk.ChunkGenerator generator,
                                   net.minecraft.world.level.LevelHeightAccessor heightAccessor,
                                   PillowNoiseConfig noiseConfig,
                                   boolean isPillowBiome,
                                   double biomeBlendRadius) {
        super(generator, heightAccessor);
        this.noiseConfig = noiseConfig;
        this.isPillowBiome = isPillowBiome;
        this.biomeBlendRadius = biomeBlendRadius;
    }

    public PillowNoiseConfig getNoiseConfig() {
        return noiseConfig;
    }

    public boolean isPillowBiome() {
        return isPillowBiome;
    }

    public double getBiomeBlendRadius() {
        return biomeBlendRadius;
    }

    public double calculateTerrainHeight(double x, double y, double z) {
        if (!isPillowBiome) {
            return 0.0;
        }

        double pillowNoise = noiseConfig.calculatePillowNoise(x, y, z);

        // Apply biome blending if near edge
        if (biomeBlendRadius > 0) {
            double blendFactor = calculateBlendFactor(x, z);
            pillowNoise *= blendFactor;
        }

        return pillowNoise;
    }

    private double calculateBlendFactor(double x, double z) {
        // Simple distance-based blending - can be enhanced with noise
        double centerX = Math.floor(x / 16.0) * 16.0 + 8.0;
        double centerZ = Math.floor(z / 16.0) * 16.0 + 8.0;
        double distance = Math.sqrt((x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ));

        return Math.max(0.0, Math.min(1.0, (biomeBlendRadius - distance) / biomeBlendRadius));
    }
}
