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
                createLayeredTerrain()
        );
    }

    private static SurfaceRules.RuleSource createLayeredTerrain() {
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        createSurfaceGrassLayers()
                ),

                SurfaceRules.ifTrue(
                        SurfaceRules.waterBlockCheck(0, 0),
                        SurfaceRules.state(ModBlocks.BROWN_PILLOW.get().defaultBlockState())
                ),

                SurfaceRules.ifTrue(
                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(8), 4),
                        SurfaceRules.state(Blocks.STONE.defaultBlockState())
                ),

                createMesaLayers()
        );
    }

    private static SurfaceRules.RuleSource createSurfaceGrassLayers() {
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(150), 0),
                        SurfaceRules.state(ModBlocks.YELLOW_PILLOW_GRASS_BLOCK.get().defaultBlockState())
                ),
                SurfaceRules.state(ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState())
        );
    }

    private static SurfaceRules.RuleSource createMesaLayers() {
        return SurfaceRules.sequence(
                SurfaceRules.state(ModBlocks.BROWN_PILLOW.get().defaultBlockState())
        );
    }
}
