package net.goutros.goutrosstrangebiomes.config;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Original config options
    private static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    // Enhanced Terrain Generation Configuration
    static {
        BUILDER.comment("Enhanced Terrain Generation Settings").push("terrain_generation");
    }

    public static final ModConfigSpec.BooleanValue ENABLE_BLENDED_TERRAIN = BUILDER
            .comment("Enable blended terrain generation that smoothly transitions with vanilla terrain")
            .define("enableBlendedTerrain", true);

    public static final ModConfigSpec.DoubleValue GLOBAL_BLEND_RADIUS = BUILDER
            .comment("Global blend radius for all custom terrain types")
            .defineInRange("globalBlendRadius", 48.0, 16.0, 128.0);

    public static final ModConfigSpec.DoubleValue BLEND_SMOOTHNESS = BUILDER
            .comment("Smoothness factor for terrain blending (0.0 = sharp, 1.0 = very smooth)")
            .defineInRange("blendSmoothness", 0.8, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue USE_VANILLA_HEIGHT_BASELINE = BUILDER
            .comment("Use vanilla terrain height as baseline for custom terrain")
            .define("useVanillaHeightBaseline", true);

    public static final ModConfigSpec.BooleanValue ENABLE_DENSITY_BLENDING = BUILDER
            .comment("Enable density-based terrain blending for smoother transitions")
            .define("enableDensityBlending", true);

    public static final ModConfigSpec.IntValue BIOME_SAMPLE_RADIUS = BUILDER
            .comment("Radius in blocks for biome influence sampling")
            .defineInRange("biomeSampleRadius", 32, 8, 64);

    public static final ModConfigSpec.IntValue BIOME_SAMPLE_STEP = BUILDER
            .comment("Step size for biome influence sampling (smaller = more accurate but slower)")
            .defineInRange("biomeSampleStep", 8, 2, 16);

    // Pillow Plateau specific settings
    static {
        BUILDER.comment("Pillow Plateau Terrain Settings").push("pillow_plateau");
    }

    public static final ModConfigSpec.BooleanValue ENABLE_PILLOW_RIVERS = BUILDER
            .comment("Enable river generation in Pillow Plateau biomes")
            .define("enablePillowRivers", true);

    public static final ModConfigSpec.BooleanValue ENABLE_PILLOW_LAKES = BUILDER
            .comment("Enable lake generation in Pillow Plateau biomes")
            .define("enablePillowLakes", true);

    public static final ModConfigSpec.IntValue PILLOW_MIN_HEIGHT = BUILDER
            .comment("Minimum height for Pillow Plateau terrain")
            .defineInRange("pillowMinHeight", 35, 0, 100);

    public static final ModConfigSpec.IntValue PILLOW_MAX_HEIGHT = BUILDER
            .comment("Maximum height for Pillow Plateau terrain")
            .defineInRange("pillowMaxHeight", 115, 100, 200);

    public static final ModConfigSpec.DoubleValue PILLOW_BLEND_RADIUS = BUILDER
            .comment("Specific blend radius for Pillow Plateau terrain")
            .defineInRange("pillowBlendRadius", 64.0, 16.0, 128.0);

    public static final ModConfigSpec.BooleanValue PILLOW_USE_LAYERED_TERRAIN = BUILDER
            .comment("Use layered terrain generation for Pillow Plateau")
            .define("pillowUseLayeredTerrain", true);

    static {
        BUILDER.pop(); // End pillow_plateau section
        BUILDER.pop(); // End terrain_generation section
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    // Static configuration values
    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    // Enhanced terrain generation values
    public static boolean enableBlendedTerrain;
    public static double globalBlendRadius;
    public static double blendSmoothness;
    public static boolean useVanillaHeightBaseline;
    public static boolean enableDensityBlending;
    public static int biomeSampleRadius;
    public static int biomeSampleStep;

    // Pillow Plateau specific values
    public static boolean enablePillowRivers;
    public static boolean enablePillowLakes;
    public static int pillowMinHeight;
    public static int pillowMaxHeight;
    public static double pillowBlendRadius;
    public static boolean pillowUseLayeredTerrain;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // Original config values
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        items = ITEM_STRINGS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());

        // Enhanced terrain generation config values
        enableBlendedTerrain = ENABLE_BLENDED_TERRAIN.get();
        globalBlendRadius = GLOBAL_BLEND_RADIUS.get();
        blendSmoothness = BLEND_SMOOTHNESS.get();
        useVanillaHeightBaseline = USE_VANILLA_HEIGHT_BASELINE.get();
        enableDensityBlending = ENABLE_DENSITY_BLENDING.get();
        biomeSampleRadius = BIOME_SAMPLE_RADIUS.get();
        biomeSampleStep = BIOME_SAMPLE_STEP.get();

        // Pillow Plateau specific config values
        enablePillowRivers = ENABLE_PILLOW_RIVERS.get();
        enablePillowLakes = ENABLE_PILLOW_LAKES.get();
        pillowMinHeight = PILLOW_MIN_HEIGHT.get();
        pillowMaxHeight = PILLOW_MAX_HEIGHT.get();
        pillowBlendRadius = PILLOW_BLEND_RADIUS.get();
        pillowUseLayeredTerrain = PILLOW_USE_LAYERED_TERRAIN.get();
    }
}