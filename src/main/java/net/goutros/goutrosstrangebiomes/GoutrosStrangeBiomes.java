package net.goutros.goutrosstrangebiomes;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.density.ModDensityFunctions;
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

        // Register the Deferred Register to the mod event bus to ensure entities and items are properly initialized
        ModEntities.ENTITY_TYPES.register(modEventBus);  // This registers entity types
        ModItems.ITEMS.register(modEventBus);  // Register other items
        ModBlocks.BLOCKS.register(modEventBus);  // Register blocks
        ModBlocks.ITEMS.register(modEventBus);  // Register block items
        ModBiomes.BIOMES.register(modEventBus);  // Register biomes
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);  // Register creative tabs
        ModDensityFunctions.DENSITY_FUNCTIONS.register(modEventBus);  // Register density functions
    }

    // This method is called after common setup
    private void commonSetup(final FMLCommonSetupEvent event) {
        // Set up systems such as TerraBlender region registration and block flammability
        event.enqueueWork(() -> {
            LOGGER.info("Setting up Pillow Plateau density function system...");

            // Register TerraBlender region for biome placement
            ResourceLocation regionId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "pillow_plateau");
            Regions.register(new PillowPlateauRegion(regionId, 1));

            // Set up block flammability
            ModBlocks.makeFlammable(ModBlocks.PILLOW_GRASS_BLOCK.get(), 30, 60);
            ModBlocks.makeFlammable(ModBlocks.PILLOW_DIRT.get(), 20, 30);
            ModBlocks.makeFlammable(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get(), 30, 60);

            LOGGER.info("Pillow Plateau density function system setup complete!");
        });
    }
}
