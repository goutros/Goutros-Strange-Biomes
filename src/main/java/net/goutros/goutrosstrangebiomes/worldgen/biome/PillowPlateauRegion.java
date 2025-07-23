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

/**
 * STRICT INLAND-ONLY Pillow Plateau Region
 *
 * Uses MAXIMUM continentalness values and excludes ALL oceanic climate parameters
 * to ensure biome NEVER generates in or near oceans.
 */
public class PillowPlateauRegion extends Region {

    public PillowPlateauRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        // STRATEGY 1: MAXIMUM CONTINENTALNESS ONLY
        // Only place in the most inland areas possible
        this.addBiome(mapper,
                Climate.Parameter.span(-0.1F, 0.3F),     // Wide temperature range for variety
                Climate.Parameter.span(0.2F, 0.9F),      // Wide humidity range
                Climate.Parameter.span(0.85F, 1.0F),     // MAXIMUM continentalness - deepest inland
                Climate.Parameter.span(-0.5F, 0.2F),     // Wide erosion range
                Climate.Parameter.span(0.4F, 1.0F),      // ELEVATED terrain only
                Climate.Parameter.span(0.5F, 1.0F),      // High weirdness for interesting terrain
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // STRATEGY 2: CONTINENTAL HIGHLANDS
        // Target mountain foothills and plateau regions
        this.addBiome(mapper,
                Climate.Parameter.span(-0.3F, 0.2F),     // Cool to mild temperature
                Climate.Parameter.span(0.3F, 0.8F),      // Moderate humidity
                Climate.Parameter.span(0.9F, 1.0F),      // ABSOLUTE MAXIMUM continentalness
                Climate.Parameter.span(-0.3F, 0.0F),     // Low erosion for stable terrain
                Climate.Parameter.span(0.6F, 1.0F),      // HIGH elevation - well above sea level
                Climate.Parameter.span(0.7F, 1.0F),      // Very high weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // STRATEGY 3: SPECIFIC INLAND SPOTS
        // Target very specific climate combinations that are guaranteed inland
        this.addBiome(mapper,
                Climate.Parameter.point(0.0F),           // Specific temperature
                Climate.Parameter.span(0.4F, 0.7F),      // Specific humidity range
                Climate.Parameter.span(0.95F, 1.0F),     // NEAR-MAXIMUM continentalness
                Climate.Parameter.point(0.0F),           // Specific erosion
                Climate.Parameter.span(0.5F, 0.8F),      // ELEVATED terrain
                Climate.Parameter.span(0.75F, 0.9F),     // Specific weirdness range
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // STRATEGY 4: DESERT TRANSITION AREAS
        // Target areas that transition from deserts (guaranteed inland)
        this.addBiome(mapper,
                Climate.Parameter.span(0.2F, 0.5F),      // Warm temperature
                Climate.Parameter.span(-0.2F, 0.3F),     // Low to medium humidity
                Climate.Parameter.span(0.8F, 1.0F),      // VERY HIGH continentalness
                Climate.Parameter.span(-0.1F, 0.3F),     // Medium erosion
                Climate.Parameter.span(0.3F, 0.7F),      // ABOVE sea level
                Climate.Parameter.span(0.6F, 1.0F),      // High weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // Note: Completely removed any placements with continentalness below 0.8
        // This should eliminate ALL ocean placement possibilities
    }
}