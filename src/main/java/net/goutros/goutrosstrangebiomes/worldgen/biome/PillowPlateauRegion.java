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
 * OCEAN-AVOIDING Pillow Plateau Region
 *
 * Carefully places the biome away from oceans and in stable inland areas
 * where the canyon layering effect will work best.
 */
public class PillowPlateauRegion extends Region {

    public PillowPlateauRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        // INLAND PLATEAUS - safe from ocean interference
        this.addBiome(mapper,
                Climate.Parameter.span(-0.1F, 0.1F),     // Moderate temperature (avoid extremes)
                Climate.Parameter.span(0.4F, 0.8F),      // Medium to high humidity
                Climate.Parameter.span(0.2F, 0.8F),      // Well inland (avoid coasts completely)
                Climate.Parameter.span(-0.4F, 0.0F),     // Low to medium erosion (stable terrain)
                Climate.Parameter.span(0.3F, 0.8F),      // Medium to high elevation (above sea level)
                Climate.Parameter.span(0.6F, 0.9F),      // High weirdness for interesting terrain
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // HIGHLAND AREAS - elevated regions perfect for canyon effects
        this.addBiome(mapper,
                Climate.Parameter.span(-0.2F, 0.0F),     // Cool to neutral (highland climate)
                Climate.Parameter.span(0.5F, 0.9F),      // Good humidity for vegetation
                Climate.Parameter.span(0.4F, 1.0F),      // Far inland (continental)
                Climate.Parameter.span(-0.3F, -0.1F),    // Low erosion (stable highlands)
                Climate.Parameter.span(0.5F, 1.0F),      // High elevation (perfect for layers)
                Climate.Parameter.span(0.7F, 1.0F),      // Very high weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // MESA-LIKE REGIONS - areas similar to vanilla mesas where layering looks natural
        this.addBiome(mapper,
                Climate.Parameter.span(0.0F, 0.2F),      // Warm but not hot
                Climate.Parameter.span(0.0F, 0.4F),      // Lower humidity (mesa-like)
                Climate.Parameter.span(0.3F, 0.7F),      // Inland but not too continental
                Climate.Parameter.span(-0.2F, 0.1F),     // Medium erosion
                Climate.Parameter.span(0.4F, 0.9F),      // Medium-high elevation
                Climate.Parameter.span(0.8F, 1.0F),      // Highest weirdness for dramatic terrain
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // ACCESSIBLE TESTING AREA - easier to find during development
        this.addBiome(mapper,
                Climate.Parameter.span(-0.05F, 0.05F),   // Very narrow temperature range
                Climate.Parameter.span(0.6F, 0.7F),      // Specific humidity
                Climate.Parameter.span(0.25F, 0.45F),    // Specific continentalness
                Climate.Parameter.span(-0.25F, -0.05F),  // Specific erosion
                Climate.Parameter.span(0.35F, 0.65F),    // Specific elevation
                Climate.Parameter.span(0.75F, 0.85F),    // Specific weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);
    }
}