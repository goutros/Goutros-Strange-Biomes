package net.goutros.goutrosstrangebiomes.worldgen.api;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Interface for providing custom terrain shapes that blend with vanilla
 */
public interface BiomeTerrainProvider {

    /**
     * Get the terrain height modifier at a position
     * @param x World X coordinate
     * @param z World Z coordinate
     * @param vanillaHeight The vanilla terrain height at this position
     * @param biomeInfluence How much this biome influences this position (0-1)
     * @param randomState Random state for noise generation
     * @return Modified terrain height
     */
    double getTerrainHeight(int x, int z, double vanillaHeight, double biomeInfluence, RandomState randomState);

    /**
     * Get the terrain shape modifier (density function)
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     * @param vanillaDensity Vanilla density at this position
     * @param biomeInfluence How much this biome influences this position (0-1)
     * @param randomState Random state for noise generation
     * @return Modified density value
     */
    double getTerrainDensity(int x, int y, int z, double vanillaDensity, double biomeInfluence, RandomState randomState);

    /**
     * Get the blend radius for this terrain type
     */
    double getBlendRadius();

    /**
     * Get the priority of this terrain (higher = takes precedence)
     */
    int getPriority();
}
