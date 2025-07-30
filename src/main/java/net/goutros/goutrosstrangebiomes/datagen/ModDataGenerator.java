package net.goutros.goutrosstrangebiomes.datagen;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.block.ModBlocks;
import net.goutros.goutrosstrangebiomes.entity.ModEntities;
import net.goutros.goutrosstrangebiomes.item.ModItems;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.features.ModFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        // Basic data providers
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModBiomeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModBiomeModifierProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(ModEntityLootTables::new, LootContextParamSets.ENTITY)
                ), lookupProvider));
        generator.addProvider(event.includeServer(), new ModBlockTagProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, fileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, fileHelper));
    }
}

class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, GoutrosStrangeBiomes.MOD_ID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(ModBlocks.PILLOW_GRASS_BLOCK.get(),
                models().cubeBottomTop(
                        getName(ModBlocks.PILLOW_GRASS_BLOCK),
                        modLoc("block/pillow_grass_block"),
                        modLoc("block/brown_pillow"),
                        modLoc("block/lime_pillow")
                )
        );

        simpleBlock(ModBlocks.YELLOW_PILLOW_GRASS_BLOCK.get(),
                models().cubeBottomTop(getName(ModBlocks.YELLOW_PILLOW_GRASS_BLOCK),
                        modLoc("block/yellow_pillow_grass_block"),
                        modLoc("block/brown_pillow"),
                        modLoc("block/yellow_pillow")));

        simpleBlock(ModBlocks.WHITE_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.WHITE_PILLOW), modLoc("block/white_pillow")));

        simpleBlock(ModBlocks.LIGHT_GRAY_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.LIGHT_GRAY_PILLOW), modLoc("block/light_gray_pillow")));

        simpleBlock(ModBlocks.GRAY_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.GRAY_PILLOW), modLoc("block/gray_pillow")));

        simpleBlock(ModBlocks.BLACK_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.BLACK_PILLOW), modLoc("block/black_pillow")));

        simpleBlock(ModBlocks.BROWN_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.BROWN_PILLOW), modLoc("block/brown_pillow")));

        simpleBlock(ModBlocks.RED_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.RED_PILLOW), modLoc("block/red_pillow")));

        simpleBlock(ModBlocks.ORANGE_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.ORANGE_PILLOW), modLoc("block/orange_pillow")));

        simpleBlock(ModBlocks.YELLOW_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.YELLOW_PILLOW), modLoc("block/yellow_pillow")));

        simpleBlock(ModBlocks.LIME_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.LIME_PILLOW), modLoc("block/lime_pillow")));

        simpleBlock(ModBlocks.GREEN_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.GREEN_PILLOW), modLoc("block/green_pillow")));

        simpleBlock(ModBlocks.CYAN_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.CYAN_PILLOW), modLoc("block/cyan_pillow")));

        simpleBlock(ModBlocks.LIGHT_BLUE_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.LIGHT_BLUE_PILLOW), modLoc("block/light_blue_pillow")));

        simpleBlock(ModBlocks.BLUE_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.BLUE_PILLOW), modLoc("block/blue_pillow")));

        simpleBlock(ModBlocks.PURPLE_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.PURPLE_PILLOW), modLoc("block/purple_pillow")));

        simpleBlock(ModBlocks.MAGENTA_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.MAGENTA_PILLOW), modLoc("block/magenta_pillow")));

        simpleBlock(ModBlocks.PINK_PILLOW.get(),
                models().cubeAll(getName(ModBlocks.PINK_PILLOW), modLoc("block/pink_pillow")));
    }

    String getName(Supplier<Block> blockSupplier) {
        return BuiltInRegistries.BLOCK.getKey(blockSupplier.get()).getPath();
    }
}

class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, GoutrosStrangeBiomes.MOD_ID, helper);
    }

    @Override
    protected void registerModels() {
        simpleBlockItem(ModBlocks.PILLOW_GRASS_BLOCK.get());
        simpleBlockItem(ModBlocks.YELLOW_PILLOW_GRASS_BLOCK.get());

        simpleBlockItem(ModBlocks.WHITE_PILLOW.get());
        simpleBlockItem(ModBlocks.LIGHT_GRAY_PILLOW.get());
        simpleBlockItem(ModBlocks.GRAY_PILLOW.get());
        simpleBlockItem(ModBlocks.BLACK_PILLOW.get());
        simpleBlockItem(ModBlocks.BROWN_PILLOW.get());
        simpleBlockItem(ModBlocks.RED_PILLOW.get());
        simpleBlockItem(ModBlocks.ORANGE_PILLOW.get());
        simpleBlockItem(ModBlocks.YELLOW_PILLOW.get());
        simpleBlockItem(ModBlocks.LIME_PILLOW.get());
        simpleBlockItem(ModBlocks.GREEN_PILLOW.get());
        simpleBlockItem(ModBlocks.CYAN_PILLOW.get());
        simpleBlockItem(ModBlocks.LIGHT_BLUE_PILLOW.get());
        simpleBlockItem(ModBlocks.BLUE_PILLOW.get());
        simpleBlockItem(ModBlocks.PURPLE_PILLOW.get());
        simpleBlockItem(ModBlocks.MAGENTA_PILLOW.get());
        simpleBlockItem(ModBlocks.PINK_PILLOW.get());
    }
}

class ModBlockLootProvider extends BlockLootSubProvider {

    public ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.PILLOW_GRASS_BLOCK.get());
        dropSelf(ModBlocks.YELLOW_PILLOW_GRASS_BLOCK.get());
        dropSelf(ModBlocks.BUTTONS.get());

        dropSelf(ModBlocks.WHITE_PILLOW.get());
        dropSelf(ModBlocks.LIGHT_GRAY_PILLOW.get());
        dropSelf(ModBlocks.GRAY_PILLOW.get());
        dropSelf(ModBlocks.BLACK_PILLOW.get());
        dropSelf(ModBlocks.BROWN_PILLOW.get());
        dropSelf(ModBlocks.RED_PILLOW.get());
        dropSelf(ModBlocks.ORANGE_PILLOW.get());
        dropSelf(ModBlocks.YELLOW_PILLOW.get());
        dropSelf(ModBlocks.LIME_PILLOW.get());
        dropSelf(ModBlocks.GREEN_PILLOW.get());
        dropSelf(ModBlocks.CYAN_PILLOW.get());
        dropSelf(ModBlocks.LIGHT_BLUE_PILLOW.get());
        dropSelf(ModBlocks.BLUE_PILLOW.get());
        dropSelf(ModBlocks.PURPLE_PILLOW.get());
        dropSelf(ModBlocks.MAGENTA_PILLOW.get());
        dropSelf(ModBlocks.PINK_PILLOW.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(Supplier::get)
                .collect(Collectors.toUnmodifiableList());
    }
}

class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, GoutrosStrangeBiomes.MOD_ID, null);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.WOOL)
                .add(ModBlocks.YELLOW_PILLOW_GRASS_BLOCK.get())
                .add(ModBlocks.PILLOW_GRASS_BLOCK.get())
                .add(ModBlocks.WHITE_PILLOW.get())
                .add(ModBlocks.LIGHT_GRAY_PILLOW.get())
                .add(ModBlocks.GRAY_PILLOW.get())
                .add(ModBlocks.BLACK_PILLOW.get())
                .add(ModBlocks.BROWN_PILLOW.get())
                .add(ModBlocks.RED_PILLOW.get())
                .add(ModBlocks.ORANGE_PILLOW.get())
                .add(ModBlocks.YELLOW_PILLOW.get())
                .add(ModBlocks.LIME_PILLOW.get())
                .add(ModBlocks.GREEN_PILLOW.get())
                .add(ModBlocks.CYAN_PILLOW.get())
                .add(ModBlocks.LIGHT_BLUE_PILLOW.get())
                .add(ModBlocks.BLUE_PILLOW.get())
                .add(ModBlocks.PURPLE_PILLOW.get())
                .add(ModBlocks.MAGENTA_PILLOW.get())
                .add(ModBlocks.PINK_PILLOW.get());



        tag(BlockTags.REPLACEABLE)
                .add(ModBlocks.YELLOW_PILLOW_GRASS_BLOCK.get())
                .add(ModBlocks.PILLOW_GRASS_BLOCK.get());

        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, "pillows")))
                .add(ModBlocks.WHITE_PILLOW.get())
                .add(ModBlocks.LIGHT_GRAY_PILLOW.get())
                .add(ModBlocks.GRAY_PILLOW.get())
                .add(ModBlocks.BLACK_PILLOW.get())
                .add(ModBlocks.BROWN_PILLOW.get())
                .add(ModBlocks.RED_PILLOW.get())
                .add(ModBlocks.ORANGE_PILLOW.get())
                .add(ModBlocks.YELLOW_PILLOW.get())
                .add(ModBlocks.LIME_PILLOW.get())
                .add(ModBlocks.GREEN_PILLOW.get())
                .add(ModBlocks.CYAN_PILLOW.get())
                .add(ModBlocks.LIGHT_BLUE_PILLOW.get())
                .add(ModBlocks.BLUE_PILLOW.get())
                .add(ModBlocks.PURPLE_PILLOW.get())
                .add(ModBlocks.MAGENTA_PILLOW.get())
                .add(ModBlocks.PINK_PILLOW.get());
    }
}

class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.YARN_BALL.get(), 1)
                .requires(Items.STRING, 2)
                .requires(Items.IRON_NUGGET, 1)
                .unlockedBy("has_string", has(Items.STRING))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.WHITE_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.WHITE_WOOL)
                .unlockedBy("has_white_wool", has(Items.WHITE_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_GRAY_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.LIGHT_GRAY_WOOL)
                .unlockedBy("has_light_gray_wool", has(Items.LIGHT_GRAY_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GRAY_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.GRAY_WOOL)
                .unlockedBy("has_gray_wool", has(Items.GRAY_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.BLACK_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.BLACK_WOOL)
                .unlockedBy("has_black_wool", has(Items.BLACK_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.BROWN_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.BROWN_WOOL)
                .unlockedBy("has_brown_wool", has(Items.BROWN_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.RED_WOOL)
                .unlockedBy("has_red_wool", has(Items.RED_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.ORANGE_WOOL)
                .unlockedBy("has_orange_wool", has(Items.ORANGE_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.YELLOW_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.YELLOW_WOOL)
                .unlockedBy("has_yellow_wool", has(Items.YELLOW_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIME_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.LIME_WOOL)
                .unlockedBy("has_lime_wool", has(Items.LIME_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GREEN_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.GREEN_WOOL)
                .unlockedBy("has_green_wool", has(Items.GREEN_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CYAN_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.CYAN_WOOL)
                .unlockedBy("has_cyan_wool", has(Items.CYAN_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_BLUE_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.LIGHT_BLUE_WOOL)
                .unlockedBy("has_light_blue_wool", has(Items.LIGHT_BLUE_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.BLUE_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.BLUE_WOOL)
                .unlockedBy("has_blue_wool", has(Items.BLUE_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.PURPLE_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.PURPLE_WOOL)
                .unlockedBy("has_purple_wool", has(Items.PURPLE_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MAGENTA_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.MAGENTA_WOOL)
                .unlockedBy("has_magenta_wool", has(Items.MAGENTA_WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.PINK_PILLOW.get(), 4)
                .pattern("WW")
                .pattern("WW")
                .define('W', Items.PINK_WOOL)
                .unlockedBy("has_pink_wool", has(Items.PINK_WOOL))
                .save(recipeOutput);
    }
}

class ModEntityLootTables extends EntityLootSubProvider {
    public ModEntityLootTables(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        this.add(ModEntities.YARN_CAT.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.STRING)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.YARN_BALL.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))))));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(ModEntities.YARN_CAT.get());
    }
}

class ModBiomeProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER =
            new RegistrySetBuilder()
                    .add(Registries.BIOME, ModBiomes::bootstrapBiomes) // Biomes first
                    .add(Registries.CONFIGURED_FEATURE, ModFeatures::bootstrapConfiguredFeatures)
                    .add(Registries.PLACED_FEATURE, ModFeatures::bootstrapPlacedFeatures)
                    .add(Registries.CONFIGURED_CARVER, ModFeatures::bootstrapConfiguredCarvers);

    public ModBiomeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(GoutrosStrangeBiomes.MOD_ID));
    }
}

class ModBiomeModifierProvider extends JsonCodecProvider<BiomeModifier> {

    public ModBiomeModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, PackOutput.Target.DATA_PACK, "neoforge/biome_modifier",
                net.minecraft.server.packs.PackType.SERVER_DATA,
                BiomeModifier.DIRECT_CODEC, lookupProvider, GoutrosStrangeBiomes.MOD_ID, null);
    }

    @Override
    protected void gather() {
        var biomeRegistry = this.lookupProvider.join().lookupOrThrow(Registries.BIOME);
        var featureRegistry = this.lookupProvider.join().lookupOrThrow(Registries.PLACED_FEATURE);

        // CRITICAL FIX: Ensure biome exists before creating modifiers
        try {
            var pillowBiome = biomeRegistry.getOrThrow(ModBiomes.PILLOW_PLATEAU);

            // Add pillow terrain features (SURFACE_STRUCTURES for blocks like buttons)
            add("add_pillow_terrain", new BiomeModifiers.AddFeaturesBiomeModifier(
                    HolderSet.direct(pillowBiome),
                    HolderSet.direct(featureRegistry.getOrThrow(ModFeatures.PILLOW_TERRAIN_PLACED_KEY)),
                    GenerationStep.Decoration.UNDERGROUND_DECORATION // Changed from RAW_GENERATION
            ));

            // Add pillow surface features
            add("add_pillow_surface", new BiomeModifiers.AddFeaturesBiomeModifier(
                    HolderSet.direct(pillowBiome),
                    HolderSet.direct(featureRegistry.getOrThrow(ModFeatures.PILLOW_SURFACE_PLACED_KEY)),
                    GenerationStep.Decoration.VEGETAL_DECORATION
            ));

            // Add button placement
            add("add_button_placement", new BiomeModifiers.AddFeaturesBiomeModifier(
                    HolderSet.direct(pillowBiome),
                    HolderSet.direct(featureRegistry.getOrThrow(ModFeatures.BUTTON_PATCHES_PLACED_KEY)),
                    GenerationStep.Decoration.SURFACE_STRUCTURES // Better step for blocks
            ));

            GoutrosStrangeBiomes.LOGGER.info("Biome modifiers registered successfully");

        } catch (Exception e) {
            GoutrosStrangeBiomes.LOGGER.error("Failed to register biome modifiers: {}", e.getMessage());
        }
    }

    private void add(String name, BiomeModifier modifier) {
        this.unconditional(ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, name), modifier);
    }
}