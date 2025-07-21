package net.goutros.goutrosstrangebiomes.worldgen.density;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import com.mojang.serialization.MapCodec;

public class ModDensityFunctions {

    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTIONS =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, GoutrosStrangeBiomes.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<PillowDensityFunction>> PILLOW_MODIFIER =
            DENSITY_FUNCTIONS.register("pillow_modifier", () -> PillowDensityFunction.CODEC);
}