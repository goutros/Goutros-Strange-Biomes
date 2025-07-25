package net.goutros.goutrosstrangebiomes.worldgen.surface;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class ModSurfaceRules {

    /**
     * AGGRESSIVE surface rules that will definitely replace terrain in pillow biomes
     */
    public static SurfaceRules.RuleSource pillowPlateauSurface() {
        return SurfaceRules.sequence(
                // PILLOW BIOME RULES - Very aggressive replacement
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(ModBiomes.PILLOW_PLATEAU),
                        SurfaceRules.sequence(
                                // Surface layer - Always pillow grass on top
                                SurfaceRules.ifTrue(
                                        SurfaceRules.ON_FLOOR,
                                        SurfaceRules.sequence(
                                                // Golden pillows at high elevations
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(100), 0),
                                                        SurfaceRules.state(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState())
                                                ),
                                                // Regular pillow grass everywhere else
                                                SurfaceRules.state(ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState())
                                        )
                                ),

                                // Subsurface - LOTS of pillow dirt (up to 20 blocks deep!)
                                SurfaceRules.ifTrue(
                                        SurfaceRules.stoneDepthCheck(0, true, 20, CaveSurface.FLOOR),
                                        SurfaceRules.state(ModBlocks.PILLOW_DIRT.get().defaultBlockState())
                                ),

                                // Cave ceiling - also pillow dirt
                                SurfaceRules.ifTrue(
                                        SurfaceRules.ON_CEILING,
                                        SurfaceRules.state(ModBlocks.PILLOW_DIRT.get().defaultBlockState())
                                ),

                                // Underwater surface - still pillow blocks
                                SurfaceRules.ifTrue(
                                        SurfaceRules.waterBlockCheck(-1, 0),
                                        SurfaceRules.state(ModBlocks.PILLOW_DIRT.get().defaultBlockState())
                                ),

                                // Fallback - if nothing else matches, use pillow dirt
                                SurfaceRules.state(ModBlocks.PILLOW_DIRT.get().defaultBlockState())
                        )
                )
        );
    }
}