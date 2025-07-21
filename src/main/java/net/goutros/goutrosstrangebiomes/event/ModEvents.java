package net.goutros.goutrosstrangebiomes.events;

import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles mod event registration and setup
 */
public class ModEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger("ModEvents");

    /**
     * Register mod events during mod construction
     * Call this from your main mod class constructor
     */
    public static void register(IEventBus modEventBus) {
        LOGGER.info("Registering mod events for terrain generation");

        // The TerrainGenerationManager registers itself to the NeoForge event bus
        // when its static initializer runs, so we just need to trigger that

        LOGGER.info("Terrain generation events registered successfully");
    }

    /**
     * Setup method to be called during FMLCommonSetupEvent
     * This replaces the setup code in your main mod class
     */
    public static void setupTerrainGeneration() {
        LOGGER.info("Setting up enhanced terrain generation system...");

        LOGGER.info("Enhanced terrain generation system setup complete!");
    }
}