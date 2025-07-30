package net.goutros.goutrosstrangebiomes;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.tab.ModCreativeTabs;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.biome.PillowPlateauRegion;
import net.goutros.goutrosstrangebiomes.worldgen.surface.ModSurfaceRules;
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
import terrablender.api.SurfaceRuleManager;

@Mod(GoutrosStrangeBiomes.MOD_ID)
public class GoutrosStrangeBiomes {

    public static final String MOD_ID = "goutrosstrangebiomes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GoutrosStrangeBiomes(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);

        ModBiomes.BIOMES.register(modEventBus);
        ModFeatures.CONFIGURED_FEATURES.register(modEventBus);
        ModFeatures.PLACED_FEATURES.register(modEventBus);
        ModFeatures.CONFIGURED_CARVERS.register(modEventBus);
        ModCarvers.CARVERS.register(modEventBus);

        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        LOGGER.info("Goutros Strange Biomes initialized - Mesa carver terrain enabled!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ResourceLocation regionId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "pillow_plateau");
            Regions.register(new PillowPlateauRegion(regionId, 8));

            SurfaceRuleManager.addSurfaceRules(
                    SurfaceRuleManager.RuleCategory.OVERWORLD,
                    MOD_ID,
                    ModSurfaceRules.pillowPlateauSurface()
            );

            LOGGER.info("Mesa-style layered terrain ready! No more arithmetic patterns!");
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Client setup complete - ready for beautiful terrain!");
        });
    }
}