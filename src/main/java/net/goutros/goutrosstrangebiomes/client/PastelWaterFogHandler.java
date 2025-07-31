package net.goutros.goutrosstrangebiomes.client;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public class PastelWaterFogHandler {

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        GoutrosStrangeBiomes.LOGGER.info("Fog color event triggered!");

        if (event.getCamera().getEntity() != null &&
                event.getCamera().getEntity().level() instanceof ClientLevel level) {

            BlockPos pos = event.getCamera().getBlockPosition();
            Biome biome = level.getBiome(pos).value();
            ResourceLocation biomeName = level.registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                    .getKey(biome);

            GoutrosStrangeBiomes.LOGGER.info("Fog check - Biome: {}, Pos: {}", biomeName, pos);

            if (biomeName != null && biomeName.getPath().equals("pillow_plateau")) {
                GoutrosStrangeBiomes.LOGGER.info("Pillow plateau detected for fog! Applying custom colors");

                // Simple test colors
                float[] color = generateTestPastelColor(pos.getX(), pos.getZ(), level.getGameTime());

                GoutrosStrangeBiomes.LOGGER.info("Setting fog color to R:{}, G:{}, B:{}", color[0], color[1], color[2]);

                event.setRed(color[0]);
                event.setGreen(color[1]);
                event.setBlue(color[2]);
            } else {
                GoutrosStrangeBiomes.LOGGER.info("Not pillow plateau, keeping default fog");
            }
        } else {
            GoutrosStrangeBiomes.LOGGER.info("Camera entity or level is null");
        }
    }

    private static float[] generateTestPastelColor(int x, int z, long gameTime) {
        // Very simple rainbow test
        double angle = (x + z + gameTime * 0.01) * 0.02;
        float hue = (float) ((angle * 57.2958) % 360.0); // Convert to degrees
        if (hue < 0) hue += 360.0f;

        GoutrosStrangeBiomes.LOGGER.info("Test fog hue: {}", hue);

        return hslToRgb(hue, 0.6f, 0.85f);
    }

    private static float[] hslToRgb(float hue, float saturation, float lightness) {
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

        return new float[]{r, g, b};
    }
}