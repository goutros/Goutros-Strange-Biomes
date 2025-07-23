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
        // Simple inland placement
        this.addBiome(mapper,
                Climate.Parameter.span(0.0F, 0.4F),      // Temperature
                Climate.Parameter.span(0.3F, 0.8F),      // Humidity
                Climate.Parameter.span(0.8F, 1.0F),      // High continentalness (inland)
                Climate.Parameter.span(-0.2F, 0.3F),     // Erosion
                Climate.Parameter.span(0.4F, 0.8F),      // Depth
                Climate.Parameter.span(0.5F, 1.0F),      // Weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);
    }
}