package net.goutros.goutrosstrangebiomes.worldgen.surface;

import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;

/**
 * FIXED Vanilla-compatible surface rules for Pillow Plateau
 * This uses the correct Minecraft 1.21.1 SurfaceRules API
 */
public class VanillaIntegrationSurfaceRule {

    // Wool blocks for different layers
    private static final BlockState CYAN_WOOL = Blocks.CYAN_WOOL.defaultBlockState();
    private static final BlockState LIGHT_BLUE_WOOL = Blocks.LIGHT_BLUE_WOOL.defaultBlockState();
    private static final BlockState BLUE_WOOL = Blocks.BLUE_WOOL.defaultBlockState();
    private static final BlockState PURPLE_WOOL = Blocks.PURPLE_WOOL.defaultBlockState();
    private static final BlockState MAGENTA_WOOL = Blocks.MAGENTA_WOOL.defaultBlockState();
    private static final BlockState PINK_WOOL = Blocks.PINK_WOOL.defaultBlockState();
    private static final BlockState WHITE_WOOL = Blocks.WHITE_WOOL.defaultBlockState();

    // Noise parameters for surface variation - using proper ResourceKeys
    private static final ResourceKey<NormalNoise.NoiseParameters> PILLOW_COLOR_NOISE =
            ResourceKey.create(net.minecraft.core.registries.Registries.NOISE,
                    ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, "pillow_color"));

    /**
     * Create surface rules for Pillow Plateau biome using correct 1.21.1 API
     */
    public static SurfaceRules.RuleSource createPillowSurfaceRules() {
        return SurfaceRules.sequence(
                // Only apply in Pillow Plateau biome
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(ModBiomes.PILLOW_PLATEAU),
                        SurfaceRules.sequence(
                                // Surface layer - colorful wool based on noise
                                SurfaceRules.ifTrue(
                                        SurfaceRules.ON_FLOOR,
                                        createNoiseBasedSurfaceRule()
                                ),
                                // Subsurface layers using correct stone depth checks
                                SurfaceRules.ifTrue(
                                        SurfaceRules.waterBlockCheck(0, 0),
                                        SurfaceRules.sequence(
                                                // First subsurface layer (1-2 blocks deep)
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(1, false, CaveSurface.FLOOR),
                                                        SurfaceRules.state(PURPLE_WOOL)
                                                ),
                                                // Deeper subsurface (3-8 blocks deep)
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(3, false, 5, CaveSurface.FLOOR),
                                                        SurfaceRules.state(BLUE_WOOL)
                                                )
                                        )
                                )
                        )
                )
        );
    }

    /**
     * Create noise-based surface rules using correct noise condition API
     */
    private static SurfaceRules.RuleSource createNoiseBasedSurfaceRule() {
        return SurfaceRules.sequence(
                // Use noise conditions to create different colored regions
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(PILLOW_COLOR_NOISE, -1.0, -0.6),
                        SurfaceRules.state(CYAN_WOOL)
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(PILLOW_COLOR_NOISE, -0.6, -0.2),
                        SurfaceRules.state(LIGHT_BLUE_WOOL)
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(PILLOW_COLOR_NOISE, -0.2, 0.2),
                        SurfaceRules.state(BLUE_WOOL)
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(PILLOW_COLOR_NOISE, 0.2, 0.6),
                        SurfaceRules.state(PURPLE_WOOL)
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(PILLOW_COLOR_NOISE, 0.6, 0.8),
                        SurfaceRules.state(MAGENTA_WOOL)
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(PILLOW_COLOR_NOISE, 0.8, 1.0),
                        SurfaceRules.state(PINK_WOOL)
                ),
                // Default fallback
                SurfaceRules.state(WHITE_WOOL)
        );
    }

    /**
     * Create simplified surface rules that don't rely on complex noise
     * This is more reliable and easier to work with
     */
    public static SurfaceRules.RuleSource createSimplePillowSurfaceRules() {
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(ModBiomes.PILLOW_PLATEAU),
                        SurfaceRules.sequence(
                                // Surface - always cyan wool for simplicity
                                SurfaceRules.ifTrue(
                                        SurfaceRules.ON_FLOOR,
                                        SurfaceRules.state(CYAN_WOOL)
                                ),
                                // Just below surface
                                SurfaceRules.ifTrue(
                                        SurfaceRules.stoneDepthCheck(0, true, CaveSurface.FLOOR),
                                        SurfaceRules.state(LIGHT_BLUE_WOOL)
                                ),
                                // Deeper
                                SurfaceRules.ifTrue(
                                        SurfaceRules.stoneDepthCheck(2, true, CaveSurface.FLOOR),
                                        SurfaceRules.state(BLUE_WOOL)
                                )
                        )
                )
        );
    }

    /**
     * Create Y-level based surface rules (height-dependent colors)
     * This doesn't require noise and is more predictable
     */
    public static SurfaceRules.RuleSource createHeightBasedPillowSurfaceRules() {
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(ModBiomes.PILLOW_PLATEAU),
                        SurfaceRules.sequence(
                                // High altitude - white/pink
                                SurfaceRules.ifTrue(
                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(90), 0),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.ON_FLOOR,
                                                SurfaceRules.state(WHITE_WOOL)
                                        )
                                ),
                                // Medium-high altitude - magenta/purple
                                SurfaceRules.ifTrue(
                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(75), 0),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.ON_FLOOR,
                                                SurfaceRules.state(MAGENTA_WOOL)
                                        )
                                ),
                                // Medium altitude - purple/blue
                                SurfaceRules.ifTrue(
                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(60), 0),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.ON_FLOOR,
                                                SurfaceRules.state(PURPLE_WOOL)
                                        )
                                ),
                                // Lower altitude - blue/cyan
                                SurfaceRules.ifTrue(
                                        SurfaceRules.ON_FLOOR,
                                        SurfaceRules.state(CYAN_WOOL)
                                ),
                                // Subsurface everywhere
                                SurfaceRules.ifTrue(
                                        SurfaceRules.stoneDepthCheck(0, true, CaveSurface.FLOOR),
                                        SurfaceRules.state(BLUE_WOOL)
                                )
                        )
                )
        );
    }

    /**
     * Integration point for adding to existing vanilla surface rules
     * Use this method to integrate with existing surface rule systems
     */
    public static SurfaceRules.RuleSource addToVanillaSurfaceRules(SurfaceRules.RuleSource vanillaRules) {
        return SurfaceRules.sequence(
                // Try the height-based approach first (most reliable)
                createHeightBasedPillowSurfaceRules(),
                // Fall back to vanilla rules
                vanillaRules
        );
    }

    /**
     * Simple integration that just changes the surface block
     * This is the most compatible approach
     */
    public static SurfaceRules.RuleSource createMinimalPillowSurfaceRules() {
        return SurfaceRules.ifTrue(
                SurfaceRules.isBiome(ModBiomes.PILLOW_PLATEAU),
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.state(CYAN_WOOL)
                )
        );
    }
}