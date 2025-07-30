package net.goutros.goutrosstrangebiomes.worldgen.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

public class PillowPlateauRegion extends Region {

    public PillowPlateauRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // VERY permissive - should generate frequently
        this.addBiome(mapper,
                Climate.Parameter.span(-1.0F, 1.0F),  // Any temperature
                Climate.Parameter.span(-1.0F, 1.0F),  // Any humidity
                Climate.Parameter.span(0.2F, 1.0F),   // Slightly inland
                Climate.Parameter.span(-1.0F, 1.0F),  // Any erosion
                Climate.Parameter.span(-1.0F, 1.0F),  // Any depth
                Climate.Parameter.span(-1.0F, 1.0F),  // Any weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);
    }
}