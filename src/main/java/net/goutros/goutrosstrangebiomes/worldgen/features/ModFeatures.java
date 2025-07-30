// Enhanced Feature Generation for Pillow Plateau
package net.goutros.goutrosstrangebiomes.worldgen.features;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.carver.ModCarvers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModFeatures {

    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES =
            DeferredRegister.create(Registries.CONFIGURED_FEATURE, GoutrosStrangeBiomes.MOD_ID);

    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES =
            DeferredRegister.create(Registries.PLACED_FEATURE, GoutrosStrangeBiomes.MOD_ID);

    public static final DeferredRegister<ConfiguredWorldCarver<?>> CONFIGURED_CARVERS =
            DeferredRegister.create(Registries.CONFIGURED_CARVER, GoutrosStrangeBiomes.MOD_ID);

    // Resource Keys for data generation
    public static final ResourceKey<ConfiguredFeature<?, ?>> PILLOW_TERRAIN_KEY =
            createConfiguredFeatureKey("pillow_terrain");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PILLOW_SURFACE_KEY =
            createConfiguredFeatureKey("pillow_surface");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BUTTON_PATCHES_KEY =
            createConfiguredFeatureKey("button_patches");

    public static final ResourceKey<PlacedFeature> PILLOW_TERRAIN_PLACED_KEY =
            createPlacedFeatureKey("pillow_terrain");
    public static final ResourceKey<PlacedFeature> PILLOW_SURFACE_PLACED_KEY =
            createPlacedFeatureKey("pillow_surface");
    public static final ResourceKey<PlacedFeature> BUTTON_PATCHES_PLACED_KEY =
            createPlacedFeatureKey("button_patches");

    // Carver Resource Keys
    public static final ResourceKey<ConfiguredWorldCarver<?>> PILLOW_CAVE_KEY =
            createCarverKey("pillow_cave");
    public static final ResourceKey<ConfiguredWorldCarver<?>> PILLOW_CANYON_KEY =
            createCarverKey("pillow_canyon");
    public static final ResourceKey<ConfiguredWorldCarver<?>> BUBBLE_CHAMBER_KEY =
            createCarverKey("bubble_chamber");

    // Deferred Registry entries
    public static final DeferredHolder<ConfiguredFeature<?, ?>, ConfiguredFeature<?, ?>> PILLOW_TERRAIN =
            CONFIGURED_FEATURES.register("pillow_terrain", () ->
                    new ConfiguredFeature<>(Feature.RANDOM_PATCH, createPillowTerrainConfig()));

    public static final DeferredHolder<ConfiguredFeature<?, ?>, ConfiguredFeature<?, ?>> PILLOW_SURFACE =
            CONFIGURED_FEATURES.register("pillow_surface", () ->
                    new ConfiguredFeature<>(Feature.RANDOM_PATCH, createPillowSurfaceConfig()));

    public static final DeferredHolder<ConfiguredFeature<?, ?>, ConfiguredFeature<?, ?>> BUTTON_PATCHES =
            CONFIGURED_FEATURES.register("button_patches", () ->
                    new ConfiguredFeature<>(Feature.SIMPLE_BLOCK,
                            new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.BUTTONS.get()))));

    public static final DeferredHolder<PlacedFeature, PlacedFeature> PILLOW_TERRAIN_PLACED =
            PLACED_FEATURES.register("pillow_terrain", () ->
                    new PlacedFeature(PILLOW_TERRAIN.getDelegate(),
                            java.util.List.of(
                                    CountPlacement.of(10), // More attempts
                                    InSquarePlacement.spread(),
                                    HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                    BiomeFilter.biome()
                            )));

    public static final DeferredHolder<PlacedFeature, PlacedFeature> PILLOW_SURFACE_PLACED =
            PLACED_FEATURES.register("pillow_surface", () ->
                    new PlacedFeature(PILLOW_SURFACE.getDelegate(),
                            java.util.List.of(
                                    CountPlacement.of(8), // More attempts
                                    InSquarePlacement.spread(),
                                    HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                    RarityFilter.onAverageOnceEvery(2), // More frequent
                                    BiomeFilter.biome()
                            )));

    public static final DeferredHolder<PlacedFeature, PlacedFeature> BUTTON_PATCHES_PLACED =
            PLACED_FEATURES.register("button_patches", () ->
                    new PlacedFeature(BUTTON_PATCHES.getDelegate(),
                            java.util.List.of(
                                    CountPlacement.of(5), // More buttons
                                    InSquarePlacement.spread(),
                                    HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                    RarityFilter.onAverageOnceEvery(4), // More frequent
                                    BiomeFilter.biome()
                            )));

    // Configured Carvers
    public static final DeferredHolder<ConfiguredWorldCarver<?>, ConfiguredWorldCarver<?>> PILLOW_CAVE_CONFIGURED =
            CONFIGURED_CARVERS.register("pillow_cave", () ->
                    new ConfiguredWorldCarver<>(ModCarvers.PILLOW_CAVE.get(), createPillowCaveConfig()));

    public static final DeferredHolder<ConfiguredWorldCarver<?>, ConfiguredWorldCarver<?>> PILLOW_CANYON_CONFIGURED =
            CONFIGURED_CARVERS.register("pillow_canyon", () ->
                    new ConfiguredWorldCarver<>(ModCarvers.PILLOW_CANYON.get(), createPillowCanyonConfig()));

    public static final DeferredHolder<ConfiguredWorldCarver<?>, ConfiguredWorldCarver<?>> BUBBLE_CHAMBER_CONFIGURED =
            CONFIGURED_CARVERS.register("bubble_chamber", () ->
                    new ConfiguredWorldCarver<>(ModCarvers.BUBBLE_CHAMBER.get(), createBubbleChamberConfig()));

    // Helper methods for Resource Key creation
    private static ResourceKey<ConfiguredFeature<?, ?>> createConfiguredFeatureKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, name));
    }

    private static ResourceKey<PlacedFeature> createPlacedFeatureKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, name));
    }

    private static ResourceKey<ConfiguredWorldCarver<?>> createCarverKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_CARVER,
                ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, name));
    }

    private static RandomPatchConfiguration createPillowTerrainConfig() {
        return new RandomPatchConfiguration(
                32, // tries
                7,  // xz_spread
                3,  // y_spread
                PlacementUtils.filtered(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.BROWN_PILLOW.get())),
                        BlockPredicate.allOf(
                                BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                BlockPredicate.wouldSurvive(ModBlocks.BROWN_PILLOW.get().defaultBlockState(), BlockPos.ZERO)
                        )
                )
        );
    }

    private static RandomPatchConfiguration createPillowSurfaceConfig() {
        return new RandomPatchConfiguration(
                16, // tries
                7,  // xz_spread
                3,  // y_spread
                PlacementUtils.filtered(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.PILLOW_GRASS_BLOCK.get())),
                        BlockPredicate.allOf(
                                BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                BlockPredicate.anyOf(
                                        BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), Blocks.DIRT),
                                        BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), ModBlocks.BROWN_PILLOW.get())
                                )
                        )
                )
        );
    }

    private static CanyonCarverConfiguration createPillowCanyonConfig() {
        return new CanyonCarverConfiguration(
                1.0F, // 100% probability - carve EVERY chunk!
                UniformHeight.of(VerticalAnchor.absolute(30), VerticalAnchor.absolute(140)),
                UniformFloat.of(0.1F, 0.9F),
                VerticalAnchor.aboveBottom(8),
                CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()),
                HolderSet.direct(
                        Blocks.STONE.builtInRegistryHolder(),
                        Blocks.DEEPSLATE.builtInRegistryHolder(),
                        Blocks.DIRT.builtInRegistryHolder(),
                        Blocks.GRASS_BLOCK.builtInRegistryHolder(),
                        Blocks.ANDESITE.builtInRegistryHolder(),
                        Blocks.GRANITE.builtInRegistryHolder(),
                        Blocks.DIORITE.builtInRegistryHolder(),
                        // ALL the wool blocks too
                        Blocks.WHITE_WOOL.builtInRegistryHolder(),
                        Blocks.PINK_WOOL.builtInRegistryHolder(),
                        Blocks.MAGENTA_WOOL.builtInRegistryHolder(),
                        Blocks.PURPLE_WOOL.builtInRegistryHolder(),
                        Blocks.BLUE_WOOL.builtInRegistryHolder(),
                        Blocks.CYAN_WOOL.builtInRegistryHolder(),
                        Blocks.LIME_WOOL.builtInRegistryHolder(),
                        Blocks.YELLOW_WOOL.builtInRegistryHolder(),
                        Blocks.ORANGE_WOOL.builtInRegistryHolder(),
                        ModBlocks.BROWN_PILLOW.get().builtInRegistryHolder(),
                        ModBlocks.PILLOW_GRASS_BLOCK.get().builtInRegistryHolder(),
                        ModBlocks.YELLOW_PILLOW_GRASS_BLOCK.get().builtInRegistryHolder()
                ),
                UniformFloat.of(20.0F, 40.0F), // MASSIVE horizontal reach
                new CanyonCarverConfiguration.CanyonShapeConfiguration(
                        UniformFloat.of(30.0F, 60.0F), // Enormous distance factor
                        UniformFloat.of(8.0F, 15.0F),  // Very thick walls
                        8, // Maximum smoothness
                        UniformFloat.of(40.0F, 80.0F), // HUGE horizontal radius
                        8.0F, // Maximum vertical radius
                        15.0F  // Maximum center factor
                )
        );
    }

    private static CaveCarverConfiguration createPillowCaveConfig() {
        return new CaveCarverConfiguration(
                0.6F, // Much higher probability (60%)
                UniformHeight.of(VerticalAnchor.absolute(10), VerticalAnchor.absolute(180)),
                UniformFloat.of(0.1F, 0.9F),
                VerticalAnchor.aboveBottom(8),
                CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()),
                // FIX: Proper replaceable blocks
                HolderSet.direct(
                        Blocks.STONE.builtInRegistryHolder(),
                        Blocks.DEEPSLATE.builtInRegistryHolder(),
                        Blocks.DIRT.builtInRegistryHolder(),
                        Blocks.GRASS_BLOCK.builtInRegistryHolder(),
                        ModBlocks.BROWN_PILLOW.get().builtInRegistryHolder(),
                        ModBlocks.PILLOW_GRASS_BLOCK.get().builtInRegistryHolder(),
                        ModBlocks.YELLOW_PILLOW_GRASS_BLOCK.get().builtInRegistryHolder()
                ),
                UniformFloat.of(2.0F, 8.0F), // Larger caves
                UniformFloat.of(1.5F, 6.0F), // Taller caves
                UniformFloat.of(-1.0F, -0.4F)
        );
    }

    private static CaveCarverConfiguration createBubbleChamberConfig() {
        return new CaveCarverConfiguration(
                0.3F, // Higher probability (30%)
                UniformHeight.of(VerticalAnchor.absolute(30), VerticalAnchor.absolute(120)),
                UniformFloat.of(0.3F, 0.7F),
                VerticalAnchor.aboveBottom(8),
                CarverDebugSettings.of(false, Blocks.MAGENTA_GLAZED_TERRACOTTA.defaultBlockState()),
                // FIX: Proper replaceable blocks
                HolderSet.direct(
                        Blocks.STONE.builtInRegistryHolder(),
                        Blocks.DEEPSLATE.builtInRegistryHolder(),
                        ModBlocks.BROWN_PILLOW.get().builtInRegistryHolder(),
                        ModBlocks.PILLOW_GRASS_BLOCK.get().builtInRegistryHolder(),
                        ModBlocks.YELLOW_PILLOW_GRASS_BLOCK.get().builtInRegistryHolder()
                ),
                UniformFloat.of(8.0F, 20.0F), // HUGE spherical chambers
                UniformFloat.of(8.0F, 20.0F), // Perfectly spherical
                UniformFloat.of(-1.0F, -0.4F)
        );
    }

    /**
     * Bootstrap method for data generation
     */
    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        // Register configured features for data generation
        FeatureUtils.register(context, PILLOW_TERRAIN_KEY, Feature.RANDOM_PATCH, createPillowTerrainConfig());
        FeatureUtils.register(context, PILLOW_SURFACE_KEY, Feature.RANDOM_PATCH, createPillowSurfaceConfig());
        FeatureUtils.register(context, BUTTON_PATCHES_KEY, Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.BUTTONS.get())));
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        PlacementUtils.register(context, PILLOW_TERRAIN_PLACED_KEY,
                configuredFeatures.getOrThrow(PILLOW_TERRAIN_KEY),
                HeightmapPlacement.onHeightmap(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG),
                BiomeFilter.biome());

        PlacementUtils.register(context, PILLOW_SURFACE_PLACED_KEY,
                configuredFeatures.getOrThrow(PILLOW_SURFACE_KEY),
                CountPlacement.of(4),
                InSquarePlacement.spread(),
                HeightmapPlacement.onHeightmap(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG),
                RarityFilter.onAverageOnceEvery(3),
                BiomeFilter.biome());

        PlacementUtils.register(context, BUTTON_PATCHES_PLACED_KEY,
                configuredFeatures.getOrThrow(BUTTON_PATCHES_KEY),
                CountPlacement.of(2),
                InSquarePlacement.spread(),
                HeightmapPlacement.onHeightmap(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG),
                RarityFilter.onAverageOnceEvery(8),
                BiomeFilter.biome());
    }

    public static void bootstrapConfiguredCarvers(BootstrapContext<ConfiguredWorldCarver<?>> context) {
        context.register(PILLOW_CAVE_KEY, new ConfiguredWorldCarver<>(ModCarvers.PILLOW_CAVE.get(), createPillowCaveConfig()));
        context.register(PILLOW_CANYON_KEY, new ConfiguredWorldCarver<>(ModCarvers.PILLOW_CANYON.get(), createPillowCanyonConfig()));
        context.register(BUBBLE_CHAMBER_KEY, new ConfiguredWorldCarver<>(ModCarvers.BUBBLE_CHAMBER.get(), createBubbleChamberConfig()));
    }
}