package net.goutros.goutrosstrangebiomes;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.density.ModDensityFunctions;
import net.goutros.goutrosstrangebiomes.worldgen.generator.ModChunkGenerators;
import net.goutros.goutrosstrangebiomes.tab.ModCreativeTabs;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.biome.PillowPlateauRegion;
import net.goutros.goutrosstrangebiomes.entity.ModEntities;
import net.goutros.goutrosstrangebiomes.item.ModItems;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import terrablender.api.Regions;

@Mod(GoutrosStrangeBiomes.MOD_ID)
public class GoutrosStrangeBiomes {

    public static final String MOD_ID = "goutrosstrangebiomes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GoutrosStrangeBiomes(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Register all deferred registers
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBiomes.BIOMES.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModDensityFunctions.DENSITY_FUNCTIONS.register(modEventBus);

        // Register chunk generators - this is the new vanilla-compatible approach
        ModChunkGenerators.CHUNK_GENERATORS.register(modEventBus);

        LOGGER.info("Registered vanilla-compatible chunk generation system and water color system");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Setting up vanilla-compatible Pillow Plateau generation...");

            // Register TerraBlender region for biome placement
            // This now works in conjunction with our custom chunk generator
            ResourceLocation regionId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "pillow_plateau");
            Regions.register(new PillowPlateauRegion(regionId, 1));

            // Set up block flammability
            ModBlocks.makeFlammable(ModBlocks.PILLOW_GRASS_BLOCK.get(), 30, 60);
            ModBlocks.makeFlammable(ModBlocks.PILLOW_DIRT.get(), 20, 30);
            ModBlocks.makeFlammable(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get(), 30, 60);
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Setting up client-side water color system...");

            // Client-specific setup for water colors
            // The actual color handler registration happens through the @EventBusSubscriber

            LOGGER.info("Client-side water color system ready!");
        });
    }
}