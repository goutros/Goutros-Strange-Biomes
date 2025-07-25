package net.goutros.goutrosstrangebiomes.worldgen.api;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.integration.DataStructureManager;
import net.goutros.goutrosstrangebiomes.worldgen.integration.EnhancedTerrainManager;
import net.goutros.goutrosstrangebiomes.worldgen.integration.TerrainEventManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TerrainGenerationAPI {

    /**
     * DON'T register DeferredRegisters here - they're already registered in main mod class
     */
    public static void register(IEventBus modEventBus) {
        DataStructureManager.register(modEventBus);
        GoutrosStrangeBiomes.LOGGER.info("Terrain API initialized");
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TerrainEventManager.registerEventHooks();
            GoutrosStrangeBiomes.LOGGER.info("Terrain system hooks registered");
        });
    }
}