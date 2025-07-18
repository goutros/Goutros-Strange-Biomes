package net.goutros.goutrosstrangebiomes.worldgen.biome;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.entity.ModEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;

/**
 * Enhanced biome definitions with integrated terrain generation features
 */
public class ModBiomes {

    public static final DeferredRegister<Biome> BIOMES =
            DeferredRegister.create(Registries.BIOME, GoutrosStrangeBiomes.MOD_ID);

    public static final ResourceKey<Biome> PILLOW_PLATEAU = ResourceKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, "pillow_plateau"));

    public static final DeferredHolder<Biome, Biome> PILLOW_PLATEAU_BIOME =
            BIOMES.register("pillow_plateau", () -> ModBiomes.pillowPlateau());

    private static int calculateSkyColor(float temperature) {
        float clampedTemp = temperature / 3.0F;
        clampedTemp = Mth.clamp(clampedTemp, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - clampedTemp * 0.05F, 0.5F + clampedTemp * 0.1F, 1.0F);
    }

    private static Biome biome(boolean hasPrecipitation, float temperature, float downfall,
                               MobSpawnSettings.Builder spawnBuilder, BiomeGenerationSettings.Builder biomeBuilder,
                               @Nullable net.minecraft.sounds.Music music) {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(hasPrecipitation)
                .temperature(temperature)
                .downfall(downfall)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x87ceeb)      // Sky blue water
                        .waterFogColor(0xf0f8ff)   // Alice blue water fog
                        .skyColor(0xffc0cb)        // Pink sky for magical feel
                        .grassColorOverride(0x98fb98)  // Pale green grass
                        .foliageColorOverride(0xdda0dd) // Plum foliage
                        .fogColor(0xf5f5dc)        // Beige fog
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(music)
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    private static void globalOverworldGeneration(BiomeGenerationSettings.Builder builder) {
        BiomeDefaultFeatures.addDefaultCarversAndLakes(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
    }

    public static Biome pillowPlateau() {
        return pillowPlateau(null);
    }

    public static Biome pillowPlateau(BootstrapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        // Add Yarn Cats with higher spawn rate in Pillow Plateau
        spawnBuilder.addSpawn(MobCategory.CREATURE,
                new MobSpawnSettings.SpawnerData(ModEntities.YARN_CAT.get(), 25, 4, 8));

        BiomeGenerationSettings.Builder biomeBuilder;
        if (context != null) {
            biomeBuilder = new BiomeGenerationSettings.Builder(
                    context.lookup(Registries.PLACED_FEATURE),
                    context.lookup(Registries.CONFIGURED_CARVER));

            // Add essential world generation features
            globalOverworldGeneration(biomeBuilder);

            // Note: Custom terrain features will be added via biome modifiers
            // This ensures proper integration with the new terrain generation system

        } else {
            biomeBuilder = new BiomeGenerationSettings.Builder(null, null);
        }

        return biome(true, 0.8f, 0.6f, spawnBuilder, biomeBuilder,
                Musics.createGameMusic(SoundEvents.MUSIC_BIOME_MEADOW));
    }

    public static void bootstrapBiomes(BootstrapContext<Biome> context) {
        context.register(PILLOW_PLATEAU, pillowPlateau(context));
    }
}