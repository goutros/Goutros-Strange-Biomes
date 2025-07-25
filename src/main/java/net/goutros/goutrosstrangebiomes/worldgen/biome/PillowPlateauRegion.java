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
        // MUCH MORE PERMISSIVE CLIMATE PARAMETERS - this should actually generate!
        this.addBiome(mapper,
                Climate.Parameter.span(-0.5F, 0.5F),      // Temperature - wider range
                Climate.Parameter.span(0.0F, 1.0F),       // Humidity - wider range
                Climate.Parameter.span(0.3F, 1.0F),       // Continentalness - still inland but easier
                Climate.Parameter.span(-1.0F, 1.0F),      // Erosion - any erosion level
                Climate.Parameter.span(-1.0F, 1.0F),      // Depth - any depth
                Climate.Parameter.span(-1.0F, 1.0F),      // Weirdness - any weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // Add a second, even more permissive set for testing
        this.addBiome(mapper,
                Climate.Parameter.span(-1.0F, 1.0F),      // Temperature - any
                Climate.Parameter.span(-1.0F, 1.0F),      // Humidity - any
                Climate.Parameter.span(-1.0F, 1.0F),      // Continentalness - any
                Climate.Parameter.span(-1.0F, 1.0F),      // Erosion - any
                Climate.Parameter.span(-1.0F, 1.0F),      // Depth - any
                Climate.Parameter.span(-1.0F, 1.0F),      // Weirdness - any
                0.0F,
                ModBiomes.PILLOW_PLATEAU);
    }
}