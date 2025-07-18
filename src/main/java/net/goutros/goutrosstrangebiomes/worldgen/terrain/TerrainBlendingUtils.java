package net.goutros.goutrosstrangebiomes.worldgen.terrain;

import net.minecraft.util.Mth;

/**
 * Utility functions for smooth terrain blending
 */
public class TerrainBlendingUtils {

    /**
     * Smooth step function for better interpolation
     */
    public static double smoothstep(double edge0, double edge1, double x) {
        double t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    /**
     * Smoother step function for even better interpolation
     */
    public static double smootherstep(double edge0, double edge1, double x) {
        double t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    /**
     * Cubic interpolation
     */
    public static double cubicInterpolate(double y0, double y1, double y2, double y3, double mu) {
        double a0 = y3 - y2 - y0 + y1;
        double a1 = y0 - y1 - a0;
        double a2 = y2 - y0;
        double a3 = y1;

        return a0 * mu * mu * mu + a1 * mu * mu + a2 * mu + a3;
    }

    /**
     * Calculate distance-based weight for terrain blending
     */
    public static double calculateBlendWeight(double distance, double maxDistance) {
        if (distance >= maxDistance) return 0.0;
        if (distance <= 0.0) return 1.0;

        double normalizedDistance = distance / maxDistance;
        return smootherstep(1.0, 0.0, normalizedDistance);
    }

    /**
     * Blend multiple terrain influences
     */
    public static double blendTerrainInfluences(double[] influences, double[] weights) {
        if (influences.length != weights.length) {
            throw new IllegalArgumentException("Influences and weights arrays must have same length");
        }

        double totalWeight = 0.0;
        double totalInfluence = 0.0;

        for (int i = 0; i < influences.length; i++) {
            totalWeight += weights[i];
            totalInfluence += influences[i] * weights[i];
        }

        return totalWeight > 0.0 ? totalInfluence / totalWeight : 0.0;
    }
}