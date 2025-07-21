package net.goutros.goutrosstrangebiomes.worldgen.biome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.stream.Stream;

/**
 * Custom biome source for Pillow Plateau generation
 * This provides a more natural way to place biomes than TerraBlender
 */
public class PillowBiomeSource extends BiomeSource {

    public static final MapCodec<PillowBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Biome.CODEC.fieldOf("pillow_biome").forGetter(source -> source.pillowBiome),
                    Biome.CODEC.fieldOf("default_biome").forGetter(source -> source.defaultBiome)
            ).apply(instance, PillowBiomeSource::new)
    );

    private final Holder<Biome> pillowBiome;
    private final Holder<Biome> defaultBiome;
    private final SimplexNoise biomeNoise;

    public PillowBiomeSource(Holder<Biome> pillowBiome, Holder<Biome> defaultBiome) {
        this.pillowBiome = pillowBiome;
        this.defaultBiome = defaultBiome;
        this.biomeNoise = new SimplexNoise(RandomSource.create(1234567L));
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        // Convert biome coordinates to world coordinates
        double worldX = x * 4.0;
        double worldZ = z * 4.0;

        // Generate biome placement noise
        double biomeValue = biomeNoise.getValue(worldX * 0.005, worldZ * 0.005);

        // Pillow Plateau appears in specific noise ranges
        if (biomeValue > 0.3 && biomeValue < 0.8) {
            // Add some secondary noise for more natural edges
            double edgeNoise = biomeNoise.getValue(worldX * 0.01, worldZ * 0.01);
            if (edgeNoise > -0.2) {
                return pillowBiome;
            }
        }

        return defaultBiome;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(pillowBiome, defaultBiome);
    }
}