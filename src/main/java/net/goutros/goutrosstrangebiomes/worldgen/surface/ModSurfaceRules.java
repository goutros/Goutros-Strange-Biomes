package net.goutros.goutrosstrangebiomes.worldgen.surface;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class ModSurfaceRules {

    /**
     * Creates surface rules for Pillow Plateau biome with layered terrain
     */
    public static SurfaceRules.RuleSource pillowPlateauSurface() {
        return SurfaceRules.sequence(
                // Surface layer - Pillow Grass
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(ModBiomes.PILLOW_PLATEAU),
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.sequence(
                                        // Golden pillows at high elevations
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(120), 0),
                                                SurfaceRules.state(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState())
                                        ),
                                        // Regular pillow grass elsewhere
                                        SurfaceRules.state(ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState())
                                )
                        )
                ),

                // Subsurface layer - Pillow Dirt (3-8 blocks deep)
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(ModBiomes.PILLOW_PLATEAU),
                        SurfaceRules.ifTrue(
                                SurfaceRules.stoneDepthCheck(0, true, 8, CaveSurface.FLOOR),
                                SurfaceRules.state(ModBlocks.PILLOW_DIRT.get().defaultBlockState())
                        )
                ),

                // Cave ceiling treatment
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(ModBiomes.PILLOW_PLATEAU),
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_CEILING,
                                SurfaceRules.state(ModBlocks.PILLOW_DIRT.get().defaultBlockState())
                        )
                )
        );
    }

    /**
     * Integrates pillow surface rules with vanilla overworld rules
     */
    public static SurfaceRules.RuleSource makeOverworldRules() {
        return SurfaceRules.sequence(
                pillowPlateauSurface(),
                // Add vanilla rules after custom ones
                SurfaceRules.ifTrue(
                        SurfaceRules.not(SurfaceRules.isBiome(ModBiomes.PILLOW_PLATEAU)),
                        getVanillaOverworldRules()
                )
        );
    }

    private static SurfaceRules.RuleSource getVanillaOverworldRules() {
        // This would typically reference vanilla surface rules
        // For now, return basic grass/dirt/stone pattern
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.state(Blocks.GRASS_BLOCK.defaultBlockState())
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.stoneDepthCheck(0, true, 3, CaveSurface.FLOOR),
                        SurfaceRules.state(Blocks.DIRT.defaultBlockState())
                ),
                SurfaceRules.state(Blocks.STONE.defaultBlockState())
        );
    }
}