package net.goutros.goutrosstrangebiomes;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.tab.ModCreativeTabs;
import net.goutros.goutrosstrangebiomes.worldgen.api.TerrainGenerationAPI;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.biome.PillowPlateauRegion;
import net.goutros.goutrosstrangebiomes.entity.ModEntities;
import net.goutros.goutrosstrangebiomes.item.ModItems;
import net.goutros.goutrosstrangebiomes.worldgen.carver.ModCarvers;
import net.goutros.goutrosstrangebiomes.worldgen.features.ModFeatures;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import terrablender.api.Regions;

@Mod(GoutrosStrangeBiomes.MOD_ID)
public class GoutrosStrangeBiomes {

    public static final String MOD_ID = "goutrosstrangebiomes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GoutrosStrangeBiomes(IEventBus modEventBus, ModContainer modContainer) {
        // CRITICAL: Registration order matters!

        // 1. Register basic registries first
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);

        // 2. Register worldgen content
        ModBiomes.BIOMES.register(modEventBus);
        ModFeatures.CONFIGURED_FEATURES.register(modEventBus);
        ModFeatures.PLACED_FEATURES.register(modEventBus);
        ModFeatures.CONFIGURED_CARVERS.register(modEventBus);
        ModCarvers.CARVERS.register(modEventBus);

        // 3. Register creative tabs
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        // 4. Register terrain API
        // TerrainGenerationAPI.register(modEventBus);

        // 5. Add setup listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        LOGGER.info("Goutros Strange Biomes initialized");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register biome region AFTER everything else is registered
            ResourceLocation regionId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "pillow_plateau");
            Regions.register(new PillowPlateauRegion(regionId, 5));

            // Set up block flammability
            ModBlocks.makeFlammable(ModBlocks.PILLOW_GRASS_BLOCK.get(), 30, 60);
            ModBlocks.makeFlammable(ModBlocks.PILLOW_DIRT.get(), 20, 30);
            ModBlocks.makeFlammable(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get(), 30, 60);

            // DEBUG: Log registration status
            LOGGER.info("=== PILLOW PLATEAU DEBUG ===");
            LOGGER.info("Biome registered: {}", ModBiomes.PILLOW_PLATEAU_BIOME.isBound());
            LOGGER.info("Features registered: {}", ModFeatures.PILLOW_TERRAIN.isBound());
            LOGGER.info("Region weight: 5 (higher = more common)");
            LOGGER.info("Look for biome with: /locate biome goutrosstrangebiomes:pillow_plateau");
            LOGGER.info("Check current biome with: /data get entity @s");
            LOGGER.info("===========================");

            LOGGER.info("Common setup completed - biome should now generate!");
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
        });
    }
}