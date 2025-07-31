package net.goutros.goutrosstrangebiomes.mixin;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
public class BlockColorsMixin {

    @Inject(method = "getColor(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;I)I",
            at = @At("HEAD"), cancellable = true)
    private void forceWaterColor(BlockState blockState, BlockAndTintGetter level, BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> cir) {
        if (blockState.is(Blocks.WATER) && level instanceof ClientLevel clientLevel && pos != null) {
            Biome biome = clientLevel.getBiome(pos).value();
            ResourceLocation biomeName = clientLevel.registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                    .getKey(biome);

            if (biomeName != null && biomeName.getPath().equals("pillow_plateau")) {
                double hue = ((pos.getX() + pos.getZ()) * 10.0) % 360.0;
                int color = hslToRgbInt((float) hue, 0.7f, 0.6f);

                GoutrosStrangeBiomes.LOGGER.info("MIXIN: Forcing BLOCK water color at {} to 0x{}", pos, Integer.toHexString(color));
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