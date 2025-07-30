package net.goutros.goutrosstrangebiomes.client;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.client.model.YarnCatModel;
import net.goutros.goutrosstrangebiomes.client.renderer.YarnCatRenderer;
import net.goutros.goutrosstrangebiomes.client.water.PillowWaterColorProvider;
import net.goutros.goutrosstrangebiomes.entity.ModEntities;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.YARN_CAT.get(), YarnCatRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(YarnCatModel.LAYER_LOCATION, YarnCatModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        GoutrosStrangeBiomes.LOGGER.info("Registering Pillow Plateau water color handlers...");

        // Register custom water color provider for water blocks
        event.register((state, level, pos, tintIndex) -> {
            if (level == null || pos == null) {
                return 0x3F76E4; // Default water color
            }

            try {
                // Try to get custom water color
                int customColor = PillowWaterColorProvider.getWaterColor(level, pos);
                if (customColor != -1) {
                    return customColor;
                }
            } catch (Exception e) {
                // Log error but don't crash - using proper logger method
                GoutrosStrangeBiomes.LOGGER.warn("Error calculating custom water color: " + e.getMessage());
            }

            // Fall back to vanilla biome-based water color
            return BiomeColors.getAverageWaterColor(level, pos);
        }, Blocks.WATER);

        // Register for water cauldrons
        event.register((state, level, pos, tintIndex) -> {
            if (level == null || pos == null) {
                return 0x3F76E4; // Default water color
            }

            try {
                int customColor = PillowWaterColorProvider.getWaterColor(level, pos);
                if (customColor != -1) {
                    return customColor;
                }
            } catch (Exception e) {
                GoutrosStrangeBiomes.LOGGER.warn("Error calculating custom cauldron water color: " + e.getMessage());
            }

            return BiomeColors.getAverageWaterColor(level, pos);
        }, Blocks.WATER_CAULDRON);

        // Optional: Register for bubble columns if you want them to be colored too
        event.register((state, level, pos, tintIndex) -> {
            if (level == null || pos == null) {
                return 0x3F76E4;
            }

            try {
                int customColor = PillowWaterColorProvider.getWaterColor(level, pos);
                if (customColor != -1) {
                    // Make bubble columns slightly more transparent/lighter
                    int r = (customColor >> 16) & 0xFF;
                    int g = (customColor >> 8) & 0xFF;
                    int b = customColor & 0xFF;

                    r = Math.min(255, r + 20);
                    g = Math.min(255, g + 20);
                    b = Math.min(255, b + 20);

                    return (r << 16) | (g << 8) | b;
                }
            } catch (Exception e) {
                GoutrosStrangeBiomes.LOGGER.warn("Error calculating custom bubble column color: " + e.getMessage());
            }

            return BiomeColors.getAverageWaterColor(level, pos);
        }, Blocks.BUBBLE_COLUMN);

        GoutrosStrangeBiomes.LOGGER.info("Pillow Plateau water color handlers registered successfully!");
    }
}