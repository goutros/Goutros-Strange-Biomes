package net.goutros.goutrosstrangebiomes.client;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public class PastelUnderwaterFogHandler {

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getCamera().getEntity() != null &&
                event.getCamera().getEntity().level() instanceof ClientLevel level) {

            // Check if camera is in water
            FluidState fluidState = level.getFluidState(event.getCamera().getBlockPosition());

            if (fluidState.is(FluidTags.WATER)) {
                BlockPos pos = event.getCamera().getBlockPosition();
                Biome biome = level.getBiome(pos).value();
                ResourceLocation biomeName = level.registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                        .getKey(biome);

                GoutrosStrangeBiomes.LOGGER.info("Underwater in biome: {}", biomeName);

                if (biomeName != null && biomeName.getPath().equals("pillow_plateau")) {
                    GoutrosStrangeBiomes.LOGGER.info("Underwater in pillow plateau! Setting rainbow fog");

                    // Shorter fog distance for vibrant underwater effect
                    event.setNearPlaneDistance(0.1f);
                    event.setFarPlaneDistance(8.0f); // Short distance for intense color
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (event.getCamera().getEntity() != null &&
                event.getCamera().getEntity().level() instanceof ClientLevel level) {

            // Check if underwater
            FluidState fluidState = level.getFluidState(event.getCamera().getBlockPosition());

            if (fluidState.is(FluidTags.WATER)) {
                BlockPos pos = event.getCamera().getBlockPosition();
                Biome biome = level.getBiome(pos).value();
                ResourceLocation biomeName = level.registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                        .getKey(biome);

                if (biomeName != null && biomeName.getPath().equals("pillow_plateau")) {
                    float[] color = generateUnderwaterRainbowColor(pos.getX(), pos.getZ(), level.getGameTime());

                    GoutrosStrangeBiomes.LOGGER.info("Setting underwater fog color to R:{}, G:{}, B:{}",
                            color[0], color[1], color[2]);

                    event.setRed(color[0]);
                    event.setGreen(color[1]);
                    event.setBlue(color[2]);
                }
            }
        }
    }

    private static float[] generateUnderwaterRainbowColor(int x, int z, long gameTime) {
        // Faster animation underwater
        double angle = (x * 0.1 + z * 0.1 + gameTime * 0.05) * 0.1;
        float hue = (float) ((angle * 57.2958) % 360.0);
        if (hue < 0) hue += 360.0f;

        // More saturated underwater
        return hslToRgb(hue, 0.8f, 0.6f);
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

        return new float[]{
                Mth.clamp(r + m, 0.0f, 1.0f),
                Mth.clamp(g + m, 0.0f, 1.0f),
                Mth.clamp(b + m, 0.0f, 1.0f)
        };
    }
}