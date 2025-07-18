package net.goutros.goutrosstrangebiomes.worldgen.events;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.config.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event handlers for terrain generation
 */
@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID)
public class TerrainGenerationEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger("TerrainGenerationEvents");

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("Server started - Blended terrain generation system ready");
        LOGGER.info("Config: Blended terrain enabled: {}, Global blend radius: {}",
                Config.enableBlendedTerrain, Config.globalBlendRadius);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        // Optional: Log when chunks with custom terrain are loaded
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ChunkAccess chunk = event.getChunk();
            // Could add metrics or debugging here
        }
    }
}
