package net.goutros.goutrosstrangebiomes.worldgen.pillow;

import net.goutros.goutrosstrangebiomes.worldgen.api.BiomeTerrainProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.util.RandomSource;

/**
 * Pillow Plateau terrain provider that creates layered terrain
 */
public class PillowPlateauTerrainProvider implements BiomeTerrainProvider {

    private static final int[] PILLOW_LAYER_HEIGHTS = {45, 55, 65, 75, 85, 95, 105};
    private static final int MIN_HEIGHT = 35;
    private static final int MAX_HEIGHT = 115;
    private static final double BLEND_RADIUS = 48.0;
    private static final int PRIORITY = 100;

    private static final ThreadLocal<SimplexNoise> LAYER_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(45678L))
    );

    private static final ThreadLocal<SimplexNoise> DETAIL_NOISE = ThreadLocal.withInitial(
            () -> new SimplexNoise(RandomSource.create(56789L))
    );

    @Override
    public double getTerrainHeight(int x, int z, double vanillaHeight, double biomeInfluence, RandomState randomState) {
        // Generate pillow height
        double layerNoise = LAYER_NOISE.get().getValue(x * 0.008, z * 0.008);
        layerNoise = (layerNoise + 1.0) * 0.5; // Normalize to 0-1

        // Select layer
        int layerIndex = (int) (layerNoise * PILLOW_LAYER_HEIGHTS.length);
        layerIndex = Mth.clamp(layerIndex, 0, PILLOW_LAYER_HEIGHTS.length - 1);

        int baseHeight = PILLOW_LAYER_HEIGHTS[layerIndex];

        // Add detail variation
        double detailNoise = DETAIL_NOISE.get().getValue(x * 0.025, z * 0.025);
        detailNoise = (detailNoise + 1.0) * 0.5;

        int variation = (int) ((detailNoise - 0.5) * 6);
        int pillowHeight = baseHeight + variation;
        pillowHeight = Mth.clamp(pillowHeight, MIN_HEIGHT, MAX_HEIGHT);

        // Blend with vanilla height based on biome influence
        return Mth.lerp(biomeInfluence, vanillaHeight, pillowHeight);
    }

    @Override
    public double getTerrainDensity(int x, int y, int z, double vanillaDensity, double biomeInfluence, RandomState randomState) {
        // Get pillow height at this position
        double pillowHeight = getTerrainHeight(x, z, y, biomeInfluence, randomState);

        // Calculate pillow density
        double pillowDensity = pillowHeight - y;

        // Blend with vanilla density
        return Mth.lerp(biomeInfluence, vanillaDensity, pillowDensity);
    }

    @Override
    public double getBlendRadius() {
        return BLEND_RADIUS;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }
}