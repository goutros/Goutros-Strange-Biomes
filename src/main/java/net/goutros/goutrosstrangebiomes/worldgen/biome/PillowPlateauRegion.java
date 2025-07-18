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
 * IMPROVED VERSION: Better climate parameters for more natural Pillow Plateau generation
 * that creates larger, more coherent biome areas with smoother transitions.
 */
public class PillowPlateauRegion extends Region {

    public PillowPlateauRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        // MAIN PILLOW PLATEAU AREAS - Larger coherent regions
        // These create the main Pillow Plateau "islands" or plateaus
        this.addBiome(mapper,
                Climate.Parameter.span(-0.2F, 0.0F),     // Cool to neutral temperature range
                Climate.Parameter.span(0.6F, 0.9F),      // High humidity range for lush feel
                Climate.Parameter.span(-0.8F, -0.4F),    // Ocean to near-coast continentalness
                Climate.Parameter.span(-0.2F, 0.2F),     // Varied erosion for interesting terrain
                Climate.Parameter.span(0.4F, 0.8F),      // Elevated terrain (above sea level)
                Climate.Parameter.span(0.7F, 1.0F),      // High weirdness for rarity
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // SECONDARY PLATEAU AREAS - Smaller scattered regions
        // These create smaller Pillow areas that blend more with surroundings
        this.addBiome(mapper,
                Climate.Parameter.span(-0.1F, 0.1F),     // Narrow temperature range
                Climate.Parameter.span(0.7F, 1.0F),      // Very high humidity
                Climate.Parameter.span(-0.6F, -0.2F),    // Coastal continentalness
                Climate.Parameter.span(-0.1F, 0.3F),     // Moderate erosion
                Climate.Parameter.span(0.5F, 0.9F),      // Elevated range
                Climate.Parameter.span(0.8F, 1.0F),      // Very high weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // RARE INLAND PLATEAUS - Very uncommon but creates interesting mainland spawns
        this.addBiome(mapper,
                Climate.Parameter.point(-0.05F),         // Specific cool temperature
                Climate.Parameter.span(0.8F, 1.0F),      // Very high humidity
                Climate.Parameter.span(0.1F, 0.4F),      // Inland continentalness
                Climate.Parameter.span(-0.3F, 0.0F),     // Lower erosion (higher terrain)
                Climate.Parameter.span(0.6F, 1.0F),      // High elevation
                Climate.Parameter.point(0.95F),          // Maximum weirdness for extreme rarity
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // TRANSITION ZONES - Very subtle presence for smooth blending
        // These help create gradual transitions from vanilla to Pillow terrain
        this.addBiome(mapper,
                Climate.Parameter.span(-0.3F, 0.2F),     // Wider temperature range
                Climate.Parameter.span(0.5F, 0.8F),      // Moderate to high humidity
                Climate.Parameter.span(-1.0F, 0.0F),     // Ocean to coast range
                Climate.Parameter.span(-0.4F, 0.4F),     // Wide erosion range
                Climate.Parameter.span(0.3F, 0.7F),      // Moderate elevation
                Climate.Parameter.span(0.6F, 0.85F),     // Moderate weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // HILLTOP PLATEAUS - Small elevated Pillow areas
        // These create Pillow terrain on hilltops and elevated areas
        this.addBiome(mapper,
                Climate.Parameter.span(-0.15F, 0.05F),   // Cool temperature range
                Climate.Parameter.span(0.6F, 0.95F),     // High humidity range
                Climate.Parameter.span(-0.2F, 0.6F),     // Coast to inland range
                Climate.Parameter.span(-0.5F, -0.1F),    // Lower erosion (higher terrain)
                Climate.Parameter.span(0.7F, 1.0F),      // Very high elevation
                Climate.Parameter.span(0.75F, 0.95F),    // High weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);
    }
}