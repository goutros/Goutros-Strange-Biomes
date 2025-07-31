package net.goutros.goutrosstrangebiomes.client;

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
        if (event.getCamera().getEntity() != null &&
                event.getCamera().getEntity().level() instanceof ClientLevel level) {

            BlockPos pos = event.getCamera().getBlockPosition();
            Biome biome = level.getBiome(pos).value();
            ResourceLocation biomeName = level.registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                    .getKey(biome);

            if (biomeName != null && biomeName.getPath().equals("pillow_plateau")) {
                double noise = Math.cos((pos.getX() - pos.getZ()) * 0.008);
                float r = (float) Mth.clamp((170 + noise * 40) / 255f, 0f, 1f);
                float g = (float) Mth.clamp((210 + noise * 30) / 255f, 0f, 1f);
                float b = (float) Mth.clamp((250 - noise * 20) / 255f, 0f, 1f);

                event.setRed(r);
                event.setGreen(g);
                event.setBlue(b);
            }
        }
    }
}
