package net.goutros.goutrosstrangebiomes.worldgen.events;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.integration.DataStructureManager;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID)
public class PerformanceEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChunkLoadPerformance(ChunkEvent.Load event) {
        if (event.isNewChunk()) {
            long startTime = System.nanoTime();

            // Performance monitoring hook
            event.getChunk().getPos(); // Minimal operation for timing

            long endTime = System.nanoTime();
            DataStructureManager.PerformanceMonitor.recordSample(endTime - startTime);
        }
    }

    /**
     * Log performance stats periodically
     */
    public static void logPerformanceStats() {
        String stats = DataStructureManager.PerformanceMonitor.getStats();
        if (!stats.isEmpty()) {
            GoutrosStrangeBiomes.LOGGER.debug("Terrain generation performance: {}", stats);
        }
    }
}
