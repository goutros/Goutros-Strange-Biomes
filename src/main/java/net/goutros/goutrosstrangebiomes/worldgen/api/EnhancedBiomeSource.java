package net.goutros.goutrosstrangebiomes.worldgen.api;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

import java.util.stream.Stream;

public class EnhancedBiomeSource extends BiomeSource {
    public static final MapCodec<EnhancedBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    MultiNoiseBiomeSourceParameterList.CODEC.fieldOf("preset").forGetter(EnhancedBiomeSource::getPreset)
            ).apply(instance, EnhancedBiomeSource::new)
    );

    private final Holder<MultiNoiseBiomeSourceParameterList> preset;
    private final MultiNoiseBiomeSource delegate;

    public EnhancedBiomeSource(Holder<MultiNoiseBiomeSourceParameterList> preset) {
        this.preset = preset;
        this.delegate = MultiNoiseBiomeSource.createFromPreset(preset);
    }

    public Holder<MultiNoiseBiomeSourceParameterList> getPreset() {
        return preset;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return delegate.possibleBiomes().stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        if (shouldUseCustomBiome(x, y, z, sampler)) {
            return ModBiomes.PILLOW_PLATEAU_BIOME.getDelegate();
        }

        return delegate.getNoiseBiome(x, y, z, sampler);
    }

    private boolean shouldUseCustomBiome(int x, int y, int z, Climate.Sampler sampler) {
        Climate.TargetPoint climate = sampler.sample(x, y, z);
        float continentalness = Climate.unquantizeCoord(climate.continentalness());
        float weirdness = Climate.unquantizeCoord(climate.weirdness());

        return continentalness > 0.6f && weirdness > 0.4f;
    }
}