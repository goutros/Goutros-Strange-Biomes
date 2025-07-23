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

public class ModBiomes {

    public static final DeferredRegister<Biome> BIOMES =
            DeferredRegister.create(Registries.BIOME, GoutrosStrangeBiomes.MOD_ID);

    public static final ResourceKey<Biome> PILLOW_PLATEAU = ResourceKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, "pillow_plateau"));

    public static final DeferredHolder<Biome, Biome> PILLOW_PLATEAU_BIOME =
            BIOMES.register("pillow_plateau", () -> ModBiomes.pillowPlateau());

    private static Biome pillowPlateau() {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        spawnBuilder.addSpawn(MobCategory.CREATURE,
                new MobSpawnSettings.SpawnerData(ModEntities.YARN_CAT.get(), 20, 3, 5));

        BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(null, null);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.8f)
                .downfall(0.6f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x8900331)
                        .waterFogColor(0x15792383)
                        .skyColor(0xffc0cb)
                        .grassColorOverride(0x98fb98)
                        .foliageColorOverride(0xdda0dd)
                        .fogColor(0xf5f5dc)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_MEADOW))
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    public static void bootstrapBiomes(BootstrapContext<Biome> context) {
        context.register(PILLOW_PLATEAU, pillowPlateau());
    }
}