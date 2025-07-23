package net.goutros.goutrosstrangebiomes.worldgen.api;

import com.mojang.serialization.MapCodec;
import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.api.density.DensityFunctionRegistry;
import net.goutros.goutrosstrangebiomes.worldgen.carver.ModCarvers;
import net.goutros.goutrosstrangebiomes.worldgen.features.ModFeatures;
import net.goutros.goutrosstrangebiomes.worldgen.integration.DataStructureManager;
import net.goutros.goutrosstrangebiomes.worldgen.integration.EnhancedTerrainManager;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.integration.TerrainEventManager;
import net.goutros.goutrosstrangebiomes.worldgen.support.SupportingSystemsManager;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TerrainGenerationAPI {

    /**
     * Complete registration with all systems including data structures
     */
    public static void register(IEventBus modEventBus) {
        // Core systems
        TerrainGenerationAPI.register(modEventBus);
        ModCarvers.CARVERS.register(modEventBus);
        ModFeatures.CONFIGURED_FEATURES.register(modEventBus);
        ModFeatures.PLACED_FEATURES.register(modEventBus);
        ModFeatures.CONFIGURED_CARVERS.register(modEventBus);
        DataStructureManager.register(modEventBus);

        GoutrosStrangeBiomes.LOGGER.info("Complete Terrain API registered");
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            EnhancedTerrainManager.initializeTerrainSystems();
            TerrainEventManager.registerEventHooks();

            GoutrosStrangeBiomes.LOGGER.info("Complete terrain system initialized");
        });
    }

    public static void initializeWithStructureManager(StructureManager manager) {
        SupportingSystemsManager.initialize(manager);
    }
}