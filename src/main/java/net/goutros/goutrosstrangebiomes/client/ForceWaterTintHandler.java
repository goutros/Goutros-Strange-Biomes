package net.goutros.goutrosstrangebiomes.client;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ForceWaterTintHandler {

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        GoutrosStrangeBiomes.LOGGER.info("FORCE REGISTERING water color handlers...");

        BlockColors blockColors = event.getBlockColors();

        // AGGRESSIVE: Register for ALL water-related blocks
        blockColors.register((state, world, pos, tintIndex) -> {
                    GoutrosStrangeBiomes.LOGGER.info("FORCE WATER TINT CALLED! State: {}, TintIndex: {}", state, tintIndex);

                    // ALWAYS return rainbow color regardless of biome for testing
                    if (pos != null) {
                        int color = forceRainbowColor(pos);
                        GoutrosStrangeBiomes.LOGGER.info("FORCE RETURNING COLOR: 0x{}", Integer.toHexString(color));
                        return color;
                    }

                    // Fallback bright color to test if handler works at all
                    GoutrosStrangeBiomes.LOGGER.info("FORCE FALLBACK: bright cyan");
                    return 0x00FFFF; // Bright cyan
                },
                // Register for EVERYTHING water-related
                Blocks.WATER,
                Blocks.BUBBLE_COLUMN,
                Blocks.KELP,
                Blocks.KELP_PLANT,
                Blocks.SEAGRASS,
                Blocks.TALL_SEAGRASS,
                Blocks.SEA_PICKLE);

        GoutrosStrangeBiomes.LOGGER.info("FORCE water handlers registered!");
    }

    private static int forceRainbowColor(BlockPos pos) {
        // Simple rainbow calculation
        double hue = ((pos.getX() + pos.getZ()) * 5.0) % 360.0;
        return hslToRgbInt((float) hue, 0.7f, 0.8f);
    }

    private static int hslToRgbInt(float hue, float saturation, float lightness) {
        float c = (1.0f - Math.abs(2.0f * lightness - 1.0f)) * saturation;
        float x = c * (1.0f - Math.abs((hue / 60.0f) % 2.0f - 1.0f));
        float m = lightness - c / 2.0f;

        float r, g, b;
        int hueSection = (int) (hue / 60.0f);
        switch (hueSection) {
            case 0: r = c; g = x; b = 0; break;
            case 1: r = x; g = c; b = 0; break;
            case 2: r = 0; g = c; b = x; break;
            case 3: r = 0; g = x; b = c; break;
            case 4: r = x; g = 0; b = c; break;
            case 5:
            default: r = c; g = 0; b = x; break;
        }

        r = Mth.clamp(r + m, 0.0f, 1.0f);
        g = Mth.clamp(g + m, 0.0f, 1.0f);
        b = Mth.clamp(b + m, 0.0f, 1.0f);

        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }
}