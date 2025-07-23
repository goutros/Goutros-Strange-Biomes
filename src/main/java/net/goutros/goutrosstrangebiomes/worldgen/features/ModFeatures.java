// Enhanced Feature Generation for Pillow Plateau
package net.goutros.goutrosstrangebiomes.worldgen.features;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.worldgen.carver.ModCarvers;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
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

    // Placed Features
    public static final DeferredHolder<PlacedFeature, PlacedFeature> PILLOW_TERRAIN_PLACED =
            PLACED_FEATURES.register("pillow_terrain", () ->
                    new PlacedFeature(PILLOW_TERRAIN.getDelegate(),
                            java.util.List.of(
                                    HeightmapPlacement.onHeightmap(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG),
                                    BiomeFilter.biome()
                            )));

    public static final DeferredHolder<PlacedFeature, PlacedFeature> PILLOW_SURFACE_PLACED =
            PLACED_FEATURES.register("pillow_surface", () ->
                    new PlacedFeature(PILLOW_SURFACE.getDelegate(),
                            java.util.List.of(
                                    CountPlacement.of(4),
                                    InSquarePlacement.spread(),
                                    HeightmapPlacement.onHeightmap(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG),
                                    RarityFilter.onAverageOnceEvery(3),
                                    BiomeFilter.biome()
                            )));

    public static final DeferredHolder<PlacedFeature, PlacedFeature> BUTTON_PATCHES_PLACED =
            PLACED_FEATURES.register("button_patches", () ->
                    new PlacedFeature(BUTTON_PATCHES.getDelegate(),
                            java.util.List.of(
                                    CountPlacement.of(2),
                                    InSquarePlacement.spread(),
                                    HeightmapPlacement.onHeightmap(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG),
                                    RarityFilter.onAverageOnceEvery(8),
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

    // Configuration creation methods
    private static RandomPatchConfiguration createPillowTerrainConfig() {
        return FeatureUtils.simplePatchConfiguration(
                Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.PILLOW_DIRT.get())),
                java.util.List.of(Blocks.GRASS_BLOCK),
                32
        );
    }

    private static RandomPatchConfiguration createPillowSurfaceConfig() {
        return FeatureUtils.simplePatchConfiguration(
                Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.PILLOW_GRASS_BLOCK.get())),
                java.util.List.of(Blocks.DIRT, ModBlocks.PILLOW_DIRT.get()),
                16
        );
    }

    private static CaveCarverConfiguration createPillowCaveConfig() {
        return new CaveCarverConfiguration(
                0.15F, // probability
                UniformHeight.of(VerticalAnchor.absolute(10), VerticalAnchor.absolute(180)), // HeightProvider for y range
                UniformFloat.of(0.1F, 0.9F), // y scale (FloatProvider)
                VerticalAnchor.aboveBottom(8), // lava level
                CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()), // debug
                HolderSet.direct(Blocks.STONE.builtInRegistryHolder(), Blocks.DEEPSLATE.builtInRegistryHolder()), // replaceable blocks
                UniformFloat.of(0.7F, 1.4F), // horizontal radius
                UniformFloat.of(0.5F, 1.2F), // vertical radius
                UniformFloat.of(-1.0F, -0.4F) // floor level
        );
    }

    private static CanyonCarverConfiguration createPillowCanyonConfig() {
        return new CanyonCarverConfiguration(
                0.01F, // probability (rare)
                UniformHeight.of(VerticalAnchor.absolute(10), VerticalAnchor.absolute(180)), // HeightProvider for y range
                UniformFloat.of(0.1F, 0.9F), // y scale (FloatProvider)
                VerticalAnchor.aboveBottom(8), // lava level
                CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()), // debug
                HolderSet.direct(Blocks.STONE.builtInRegistryHolder(), Blocks.DEEPSLATE.builtInRegistryHolder()), // replaceable blocks
                UniformFloat.of(0.9F, 1.2F), // vertical rotation
                new CanyonCarverConfiguration.CanyonShapeConfiguration(
                        UniformFloat.of(1.0F, 4.0F), // distance factor (FloatProvider)
                        UniformFloat.of(0.9F, 1.2F), // thickness (FloatProvider)
                        1, // width smoothness (int)
                        UniformFloat.of(3.0F, 8.0F), // horizontal radius factor (FloatProvider)
                        0.5F, // vertical radius default factor (float)
                        2.0F // vertical radius center factor (float)
                )
        );
    }

    private static CaveCarverConfiguration createBubbleChamberConfig() {
        return new CaveCarverConfiguration(
                0.05F, // probability
                UniformHeight.of(VerticalAnchor.absolute(30), VerticalAnchor.absolute(70)), // HeightProvider for y range
                UniformFloat.of(0.3F, 0.7F), // y scale (FloatProvider)
                VerticalAnchor.aboveBottom(8), // lava level
                CarverDebugSettings.of(false, Blocks.MAGENTA_GLAZED_TERRACOTTA.defaultBlockState()), // debug
                HolderSet.direct(Blocks.STONE.builtInRegistryHolder(), Blocks.DEEPSLATE.builtInRegistryHolder()), // replaceable blocks
                UniformFloat.of(2.0F, 6.0F), // horizontal radius
                UniformFloat.of(2.0F, 6.0F), // vertical radius (spherical)
                UniformFloat.of(-1.0F, -0.4F) // floor level
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