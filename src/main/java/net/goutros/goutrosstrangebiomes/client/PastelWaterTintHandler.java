package net.goutros.goutrosstrangebiomes.client;

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
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class PastelWaterTintHandler {

    // Static helper for water surface tint
    private static int computePastelWaterColor(BlockPos pos) {
        double noise = Math.sin((pos.getX() + pos.getZ()) * 0.01);
        int r = (int) Mth.clamp(170 + noise * 30, 160, 220);
        int g = (int) Mth.clamp(210 + noise * 20, 180, 235);
        int b = (int) Mth.clamp(255 - noise * 15, 220, 255);
        return (r << 16) | (g << 8) | b;
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColors blockColors = event.getBlockColors();

        blockColors.register((state, world, pos, tintIndex) -> {
            if (world instanceof ClientLevel level && pos != null) {
                Biome biome = level.getBiome(pos).value();
                ResourceLocation biomeName = level.registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                        .getKey(biome);

                if (biomeName != null) {
                    System.out.println("Biome detected: " + biomeName); // DEBUG
                    if (biomeName.getPath().equals("pillow_plateau")) {
                        return computePastelWaterColor(pos);
                    }
                }
            }
            return 0x3F76E4; // vanilla fallback
        }, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.KELP, Blocks.KELP_PLANT, Blocks.SEAGRASS);
    }
}
