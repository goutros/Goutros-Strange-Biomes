package net.goutros.goutrosstrangebiomes.mixin;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidState.class)
public class WaterColorMixin {

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    private void forceRainbowWaterColor(net.minecraft.world.level.BlockAndTintGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        FluidState fluidState = (FluidState)(Object)this;

        if (fluidState.getType() == Fluids.WATER && level instanceof ClientLevel clientLevel) {
            Biome biome = clientLevel.getBiome(pos).value();
            ResourceLocation biomeName = clientLevel.registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                    .getKey(biome);

            if (biomeName != null && biomeName.getPath().equals("pillow_plateau")) {
                // Force rainbow water color
                double hue = ((pos.getX() + pos.getZ()) * 10.0) % 360.0;
                int color = hslToRgbInt((float) hue, 0.7f, 0.6f);

                GoutrosStrangeBiomes.LOGGER.info("MIXIN: Forcing water color at {} to 0x{}", pos, Integer.toHexString(color));
                cir.setReturnValue(color);
            }
        }
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