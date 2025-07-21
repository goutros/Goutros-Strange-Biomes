package net.goutros.goutrosstrangebiomes.worldgen.integration;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;

/**
 * DISABLED - Using mixin approach instead
 *
 * This class is disabled to prevent conflicts with the new mixin-based approach.
 * The mixin hooks into the surface building process at the correct time.
 */
// @EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID) // DISABLED
public class VanillaTerrainIntegration {

    // All methods disabled - using NoiseBasedChunkGeneratorMixin instead

    static {
        GoutrosStrangeBiomes.LOGGER.info("VanillaTerrainIntegration disabled - using mixin approach");
    }
}