package net.goutros.goutrosstrangebiomes.worldgen.surface;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class ModSurfaceRules {

    public static SurfaceRules.RuleSource pillowPlateauSurface() {
        return SurfaceRules.ifTrue(
                SurfaceRules.isBiome(ModBiomes.PILLOW_PLATEAU),
                SurfaceRules.sequence(
                        // Layer 6: 140+ - Gold
                        layerRule(140, ModBlocks.YELLOW_PILLOW_GRASS_BLOCK.get().defaultBlockState(),
                                Blocks.YELLOW_WOOL.defaultBlockState()),

                        // Layer 5: 120-140 - Pink/Magenta
                        layerRule(120, Blocks.PINK_WOOL.defaultBlockState(),
                                Blocks.MAGENTA_WOOL.defaultBlockState()),

                        // Layer 4: 100-120 - Purple/Blue
                        layerRule(100, Blocks.PURPLE_WOOL.defaultBlockState(),
                                Blocks.BLUE_WOOL.defaultBlockState()),

                        // Layer 3: 80-100 - Cyan/Light Blue
                        layerRule(80, Blocks.CYAN_WOOL.defaultBlockState(),
                                Blocks.LIGHT_BLUE_WOOL.defaultBlockState()),

                        // Layer 2: 60-80 - Green
                        layerRule(60, ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState(),
                                Blocks.LIME_WOOL.defaultBlockState()),

                        // Layer 1: Below 60 - Orange/Brown
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                        SurfaceRules.state(Blocks.ORANGE_WOOL.defaultBlockState())),
                                SurfaceRules.state(ModBlocks.BROWN_PILLOW.get().defaultBlockState())
                        )
                )
        );
    }

    private static SurfaceRules.RuleSource layerRule(int minHeight,
                                                     net.minecraft.world.level.block.state.BlockState surface,
                                                     net.minecraft.world.level.block.state.BlockState subsurface) {
        return SurfaceRules.ifTrue(
                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(minHeight), 0),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.state(surface)),
                        SurfaceRules.state(subsurface)
                )
        );
    }
}