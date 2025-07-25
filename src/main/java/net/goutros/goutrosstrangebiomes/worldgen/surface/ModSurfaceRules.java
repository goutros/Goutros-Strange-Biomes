package net.goutros.goutrosstrangebiomes.worldgen.surface;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class ModSurfaceRules {

    public static SurfaceRules.RuleSource pillowPlateauSurface() {
        return SurfaceRules.sequence(
                // PILLOW BIOME - LAYERED CANYON RULES
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(ModBiomes.PILLOW_PLATEAU),
                        SurfaceRules.sequence(
                                // LAYER 1: High elevations (120+) - Golden layers
                                SurfaceRules.ifTrue(
                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(120), 0),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                                        SurfaceRules.state(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get().defaultBlockState())),
                                                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, true, 5, CaveSurface.FLOOR),
                                                        SurfaceRules.state(Blocks.GOLD_BLOCK.defaultBlockState())),
                                                SurfaceRules.state(Blocks.YELLOW_WOOL.defaultBlockState())
                                        )
                                ),

                                // LAYER 2: High-mid elevations (100-120) - Pink/Magenta
                                SurfaceRules.ifTrue(
                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(100), 0),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                                        SurfaceRules.state(Blocks.PINK_WOOL.defaultBlockState())),
                                                SurfaceRules.state(Blocks.MAGENTA_WOOL.defaultBlockState())
                                        )
                                ),

                                // LAYER 3: Mid elevations (80-100) - Purple/Blue
                                SurfaceRules.ifTrue(
                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(80), 0),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                                        SurfaceRules.state(Blocks.PURPLE_WOOL.defaultBlockState())),
                                                SurfaceRules.state(Blocks.BLUE_WOOL.defaultBlockState())
                                        )
                                ),

                                // LAYER 4: Lower-mid elevations (60-80) - Cyan/Light Blue
                                SurfaceRules.ifTrue(
                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(60), 0),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                                        SurfaceRules.state(Blocks.CYAN_WOOL.defaultBlockState())),
                                                SurfaceRules.state(Blocks.LIGHT_BLUE_WOOL.defaultBlockState())
                                        )
                                ),

                                // LAYER 5: Low elevations (40-60) - Green layers
                                SurfaceRules.ifTrue(
                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(40), 0),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                                        SurfaceRules.state(ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState())),
                                                SurfaceRules.state(Blocks.LIME_WOOL.defaultBlockState())
                                        )
                                ),

                                // LAYER 6: Bottom layer (below 40) - Brown/Orange
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                                SurfaceRules.state(Blocks.ORANGE_WOOL.defaultBlockState())),
                                        SurfaceRules.state(ModBlocks.PILLOW_DIRT.get().defaultBlockState())
                                ),

                                // Cave surfaces - pillow dirt everywhere
                                SurfaceRules.ifTrue(SurfaceRules.ON_CEILING,
                                        SurfaceRules.state(ModBlocks.PILLOW_DIRT.get().defaultBlockState())),

                                // Underwater - blue wool
                                SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0),
                                        SurfaceRules.state(Blocks.BLUE_WOOL.defaultBlockState()))
                        )
                )
        );
    }
}