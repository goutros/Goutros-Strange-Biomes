// Enhanced Blender for smooth terrain transitions
package net.goutros.goutrosstrangebiomes.worldgen.blending;

import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.blending.Blender;

public class PillowBlender {

    /**
     * Creates enhanced blender for smooth pillow terrain transitions
     */
    public static Blender.BlendingOutput enhanceBlending(Blender.BlendingOutput original,
                                                         int blockX, int blockZ,
                                                         BiomeResolver biomeResolver) {

        // Check if we're near pillow plateau biome
        Holder<Biome> biome = biomeResolver.getNoiseBiome(blockX >> 2, 64 >> 2, blockZ >> 2, null);

        if (biome.is(ModBiomes.PILLOW_PLATEAU)) {
            return enhancePillowBlending(original, blockX, blockZ);
        }

        return original;
    }

    private static Blender.BlendingOutput enhancePillowBlending(Blender.BlendingOutput original,
                                                                int blockX, int blockZ) {
        // Soften terrain transitions for pillow areas
        double softening = calculateSofteningFactor(blockX, blockZ);

        double newAlpha = original.alpha() * softening;
        double newOffset = original.blendingOffset() * 0.7; // Reduce harsh transitions

        return new Blender.BlendingOutput(newAlpha, newOffset);
    }

    private static double calculateSofteningFactor(int blockX, int blockZ) {
        // Use noise to create organic softening patterns
        double noise = Math.sin(blockX * 0.01) * Math.cos(blockZ * 0.01);
        return 0.6 + (noise + 1.0) * 0.2; // Range: 0.6 to 1.0
    }

    /**
     * Custom density blending for smoother pillow terrain
     */
    public static double blendPillowDensity(DensityFunction.FunctionContext context,
                                            double originalDensity,
                                            BiomeResolver biomeResolver) {

        int x = context.blockX() >> 2;
        int y = context.blockY() >> 2;
        int z = context.blockZ() >> 2;

        Holder<Biome> biome = biomeResolver.getNoiseBiome(x, y, z, null);

        if (biome.is(ModBiomes.PILLOW_PLATEAU)) {
            return applyPillowDensityModification(originalDensity, context);
        }

        return originalDensity;
    }

    private static double applyPillowDensityModification(double density,
                                                         DensityFunction.FunctionContext context) {
        int y = context.blockY();

        // Make terrain softer and more rounded
        if (y > 60 && y < 120) {
            double pillowEffect = Math.sin((y - 60) * Math.PI / 60.0) * 0.3;
            density = density * (1.0 - pillowEffect);
        }

        // Reduce sharp edges
        return density * 0.85;
    }
}