package net.goutros.goutrosstrangebiomes.worldgen.data.noise;

import net.goutros.goutrosstrangebiomes.worldgen.data.PillowGenerationContext;
import net.goutros.goutrosstrangebiomes.worldgen.data.PillowHeightmapUtils;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;

public class PillowNoiseChunk {

    private final NoiseChunk delegate;
    private final PillowGenerationContext context;
    private final PillowHeightmapUtils.PillowHeightmap heightmap;

    public PillowNoiseChunk(NoiseChunk delegate, ChunkAccess chunk, PillowGenerationContext context) {
        this.delegate = delegate;
        this.context = context;
        this.heightmap = PillowHeightmapUtils.createFrom(chunk, context);
    }

    public double sampleNoise(int x, int y, int z) {
        // Get base noise from delegate
        double baseNoise = delegate.preliminarySurfaceLevel(x, z);

        if (!context.isPillowBiome()) {
            return baseNoise;
        }

        // Apply pillow modifications
        double pillowEffect = context.calculateTerrainHeight(x, y, z);
        return baseNoise + pillowEffect;
    }

    public PillowHeightmapUtils.PillowHeightmap getHeightmap() {
        return heightmap;
    }

    public PillowGenerationContext getContext() {
        return context;
    }

    public NoiseChunk getDelegate() {
        return delegate;
    }

    /**
     * Sample with biome-aware blending
     */
    public double sampleWithBlending(double x, double y, double z) {
        double base = sampleNoise((int)x, (int)y, (int)z);

        if (context.getBiomeBlendRadius() > 0) {
            // Apply additional smoothing at biome boundaries
            return applySmoothingFilter(base, x, z);
        }

        return base;
    }

    private double applySmoothingFilter(double value, double x, double z) {
        // Simple 3x3 smoothing kernel
        double sum = value * 4.0; // Center weight
        int samples = 4;

        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                double neighborValue = sampleNoise((int)(x + dx), (int)0, (int)(z + dz));
                sum += neighborValue;
                samples++;
            }
        }

        return sum / samples;
    }
}
