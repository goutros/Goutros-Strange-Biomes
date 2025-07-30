package net.goutros.goutrosstrangebiomes.worldgen;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.density.ModDensityFunctions;
import net.goutros.goutrosstrangebiomes.worldgen.features.ModFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Integrates our custom density functions with the game's generation pipeline
 */
@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class WorldGenDataProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DENSITY_FUNCTION, ModDensityFunctions::bootstrapDensityFunctions)
            .add(Registries.BIOME, ModBiomes::bootstrapBiomes)
            .add(Registries.CONFIGURED_FEATURE, ModFeatures::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, ModFeatures::bootstrapPlacedFeatures)
            .add(Registries.CONFIGURED_CARVER, ModFeatures::bootstrapConfiguredCarvers);

    public WorldGenDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(GoutrosStrangeBiomes.MOD_ID));
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                event.includeServer(),
                new WorldGenDataProvider(event.getGenerator().getPackOutput(), event.getLookupProvider())
        );
    }
}