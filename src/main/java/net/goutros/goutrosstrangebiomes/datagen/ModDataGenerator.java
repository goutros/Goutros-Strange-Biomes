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
import net.neoforged.neoforge.registries.NeoForgeRegistries;
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
                        modLoc("block/pillow_grass_block_side"),
                        modLoc("block/pillow_dirt"),
                        modLoc("block/pillow_grass_block_top")
                )
        );
        simpleBlock(ModBlocks.PILLOW_DIRT.get(),
                models().cubeAll(getName(ModBlocks.PILLOW_DIRT), modLoc("block/pillow_dirt")));

        simpleBlock(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get(),
                models().cubeBottomTop(getName(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK),
                        modLoc("block/golden_pillow_grass_block_side"),
                        modLoc("block/pillow_dirt"),
                        modLoc("block/golden_pillow_grass_block_top")));
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
        simpleBlockItem(ModBlocks.PILLOW_DIRT.get());
        simpleBlockItem(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get());
    }
}

class ModBlockLootProvider extends BlockLootSubProvider {

    public ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.PILLOW_GRASS_BLOCK.get());
        dropSelf(ModBlocks.PILLOW_DIRT.get());
        dropSelf(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get());
        dropSelf(ModBlocks.BUTTONS.get());
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
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.PILLOW_DIRT.get())
                .add(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get())
                .add(ModBlocks.PILLOW_GRASS_BLOCK.get());

        tag(BlockTags.REPLACEABLE)
                .add(ModBlocks.PILLOW_DIRT.get())
                .add(ModBlocks.GOLDEN_PILLOW_GRASS_BLOCK.get())
                .add(ModBlocks.PILLOW_GRASS_BLOCK.get());
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