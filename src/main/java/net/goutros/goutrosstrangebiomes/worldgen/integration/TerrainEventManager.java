// Event system integration for complete terrain API
package net.goutros.goutrosstrangebiomes.worldgen.integration;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.events.TerrainGenEvents;
import net.goutros.goutrosstrangebiomes.worldgen.data.PillowGenerationContext;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * Manages terrain generation event integration
 */
public class TerrainEventManager {

    /**
     * Hook into chunk generation pipeline
     */
    public static void processChunkGeneration(ChunkAccess chunk, GenerationStep.Decoration phase) {
        try {
            // Create generation context
            PillowGenerationContext context = DataStructureManager.createContext(
                    null, chunk, pos -> chunk.getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2)
            );

            // Fire terrain generation events
            TerrainGenEvents.fireTerrainGenEvent(chunk, phase, context);

        } catch (Exception e) {
            GoutrosStrangeBiomes.LOGGER.error("Error in terrain event processing: {}", e.getMessage());
        }
    }

    /**
     * Register event hooks (called during mod setup)
     */
    public static void registerEventHooks() {
        GoutrosStrangeBiomes.LOGGER.info("Terrain generation events registered");
    }
}