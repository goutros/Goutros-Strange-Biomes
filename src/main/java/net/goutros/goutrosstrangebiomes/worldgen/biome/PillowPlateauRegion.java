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
 * INLAND-ONLY Pillow Plateau Region
 *
 * Carefully configured to NEVER generate in oceans, only in stable inland areas
 * where the canyon layering effect will work best without water interference.
 */
public class PillowPlateauRegion extends Region {

    public PillowPlateauRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        // DEEP INLAND AREAS ONLY - Maximum continentalness to avoid any water
        this.addBiome(mapper,
                Climate.Parameter.span(-0.1F, 0.2F),     // Cool to warm temperature
                Climate.Parameter.span(0.3F, 0.8F),      // Medium to high humidity
                Climate.Parameter.span(0.7F, 1.0F),      // VERY HIGH continentalness - DEEP INLAND ONLY
                Climate.Parameter.span(-0.3F, 0.1F),     // Low to medium erosion
                Climate.Parameter.span(0.3F, 0.9F),      // ABOVE SEA LEVEL - elevated terrain
                Climate.Parameter.span(0.5F, 1.0F),      // High weirdness for interesting terrain
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // CONTINENTAL HIGHLANDS - Mountain foothills far from water
        this.addBiome(mapper,
                Climate.Parameter.span(-0.2F, 0.1F),     // Cool highland climate
                Climate.Parameter.span(0.4F, 0.9F),      // Good humidity
                Climate.Parameter.span(0.8F, 1.0F),      // MAXIMUM continentalness - FAR FROM OCEANS
                Climate.Parameter.span(-0.4F, -0.1F),    // Low erosion (stable highlands)
                Climate.Parameter.span(0.5F, 1.0F),      // HIGH elevation - well above sea level
                Climate.Parameter.span(0.6F, 0.9F),      // High weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // INTERIOR PLATEAUS - Mesa-like areas in continental centers
        this.addBiome(mapper,
                Climate.Parameter.span(0.0F, 0.3F),      // Warm climate
                Climate.Parameter.span(0.0F, 0.5F),      // Low to medium humidity
                Climate.Parameter.span(0.75F, 1.0F),     // VERY HIGH continentalness - CONTINENTAL CENTER
                Climate.Parameter.span(-0.2F, 0.2F),     // Medium erosion for interesting terrain
                Climate.Parameter.span(0.4F, 0.8F),      // ELEVATED - well above sea level
                Climate.Parameter.span(0.7F, 1.0F),      // Very high weirdness for dramatic canyons
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // ACCESSIBLE INLAND TESTING AREA - Easy to find but never oceanic
        this.addBiome(mapper,
                Climate.Parameter.span(-0.05F, 0.05F),   // Narrow temperature range
                Climate.Parameter.span(0.5F, 0.7F),      // Specific humidity
                Climate.Parameter.span(0.6F, 0.8F),      // HIGH continentalness - SAFELY INLAND
                Climate.Parameter.span(-0.15F, 0.05F),   // Specific erosion
                Climate.Parameter.span(0.4F, 0.7F),      // ELEVATED terrain - above sea level
                Climate.Parameter.span(0.6F, 0.8F),      // Specific weirdness
                0.0F,
                ModBiomes.PILLOW_PLATEAU);

        // Note: Removed mountain foothills variant that had lower continentalness
        // to ensure ALL placements are safely inland
    }
}