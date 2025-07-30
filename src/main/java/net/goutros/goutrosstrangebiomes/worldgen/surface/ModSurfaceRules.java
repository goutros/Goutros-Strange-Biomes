package net.goutros.goutrosstrangebiomes.worldgen.surface;

import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
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
                // Surface layer - grass on top
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        createSurfaceGrassLayers()
                ),

                // Create mesa-style layers using height + noise for variation
                createMesaLayers(),

                // Deep underground foundation
                SurfaceRules.ifTrue(
                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(10), 0),
                        SurfaceRules.state(Blocks.STONE.defaultBlockState())
                )
        );
    }

    private static SurfaceRules.RuleSource createSurfaceGrassLayers() {
        return SurfaceRules.sequence(
                // Highest peaks (150+) - Special yellow grass
                SurfaceRules.ifTrue(
                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(150), 0),
                        SurfaceRules.state(ModBlocks.YELLOW_PILLOW_GRASS_BLOCK.get().defaultBlockState())
                ),

                // High areas (110+) - Regular pillow grass
                SurfaceRules.ifTrue(
                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(110), 0),
                        SurfaceRules.state(ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState())
                ),

                // Mid areas (70+) - Still pillow grass but different variant possible
                SurfaceRules.ifTrue(
                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(70), 0),
                        SurfaceRules.state(ModBlocks.PILLOW_GRASS_BLOCK.get().defaultBlockState())
                ),

                // Low areas - Brown pillow surface
                SurfaceRules.state(ModBlocks.BROWN_PILLOW.get().defaultBlockState())
        );
    }

    private static SurfaceRules.RuleSource createMesaLayers() {
        return SurfaceRules.sequence(
                // Layer 8: 140-160 - Yellow layer (like mesa top)
                createNoiseVariedLayer(140, 160,
                        Blocks.YELLOW_WOOL.defaultBlockState(),
                        Blocks.ORANGE_WOOL.defaultBlockState(),
                        Noises.SURFACE), // Use surface noise for variation

                // Layer 7: 120-140 - Purple/Magenta layer
                createNoiseVariedLayer(120, 140,
                        Blocks.PURPLE_WOOL.defaultBlockState(),
                        Blocks.MAGENTA_WOOL.defaultBlockState(),
                        Noises.RIDGE), // Use ridge noise for different pattern

                // Layer 6: 100-120 - Blue layer
                createNoiseVariedLayer(100, 120,
                        Blocks.BLUE_WOOL.defaultBlockState(),
                        Blocks.LIGHT_BLUE_WOOL.defaultBlockState(),
                        Noises.SURFACE),

                // Layer 5: 80-100 - Cyan/Green layer
                createNoiseVariedLayer(80, 100,
                        Blocks.CYAN_WOOL.defaultBlockState(),
                        Blocks.LIME_WOOL.defaultBlockState(),
                        Noises.RIDGE),

                // Layer 4: 60-80 - Green layer
                createNoiseVariedLayer(60, 80,
                        Blocks.GREEN_WOOL.defaultBlockState(),
                        ModBlocks.LIME_PILLOW.get().defaultBlockState(),
                        Noises.SURFACE),

                // Layer 3: 40-60 - Pink/Red layer
                createNoiseVariedLayer(40, 60,
                        Blocks.PINK_WOOL.defaultBlockState(),
                        Blocks.RED_WOOL.defaultBlockState(),
                        Noises.RIDGE),

                // Layer 2: 20-40 - Orange/Brown layer
                createNoiseVariedLayer(20, 40,
                        Blocks.ORANGE_WOOL.defaultBlockState(),
                        ModBlocks.BROWN_PILLOW.get().defaultBlockState(),
                        Noises.SURFACE),

                // Base layer: Below 20 - Foundation
                SurfaceRules.state(ModBlocks.BROWN_PILLOW.get().defaultBlockState())
        );
    }

    private static SurfaceRules.RuleSource createNoiseVariedLayer(
            int minHeight, int maxHeight,
            net.minecraft.world.level.block.state.BlockState primaryBlock,
            net.minecraft.world.level.block.state.BlockState secondaryBlock,
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters> noiseType) {

        return SurfaceRules.ifTrue(
                // Only apply in height range
                SurfaceRules.yBlockCheck(VerticalAnchor.absolute(minHeight), maxHeight - minHeight),
                SurfaceRules.sequence(
                        // Use noise to vary between the two block types
                        // This creates organic banding within each layer
                        SurfaceRules.ifTrue(
                                SurfaceRules.noiseCondition(noiseType, -0.2, 0.3), // Noise threshold
                                SurfaceRules.state(primaryBlock)
                        ),
                        // Default to secondary block
                        SurfaceRules.state(secondaryBlock)
                )
        );
    }

    private static SurfaceRules.RuleSource createSteppedLayer(
            int baseHeight,
            net.minecraft.world.level.block.state.BlockState block1,
            net.minecraft.world.level.block.state.BlockState block2,
            net.minecraft.world.level.block.state.BlockState block3) {

        return SurfaceRules.sequence(
                // Step 3 (highest within layer)
                SurfaceRules.ifTrue(
                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(baseHeight + 15), 0),
                        SurfaceRules.ifTrue(
                                SurfaceRules.noiseCondition(Noises.SURFACE, 0.3, 1.0),
                                SurfaceRules.state(block3)
                        )
                ),

                // Step 2 (middle)
                SurfaceRules.ifTrue(
                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(baseHeight + 10), 0),
                        SurfaceRules.ifTrue(
                                SurfaceRules.noiseCondition(Noises.SURFACE, 0.0, 0.6),
                                SurfaceRules.state(block2)
                        )
                ),

                // Step 1 (base of layer)
                SurfaceRules.ifTrue(
                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(baseHeight), 0),
                        SurfaceRules.state(block1)
                )
        );
    }
}