package net.goutros.goutrosstrangebiomes.worldgen.api;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.concurrent.CompletableFuture;

public class EnhancedChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<EnhancedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.getBiomeSource()),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(EnhancedChunkGenerator::generatorSettings)
            ).apply(instance, instance.stable(EnhancedChunkGenerator::new))
    );

    public EnhancedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        // Create enhanced random state with modified noise router
        RandomState enhancedState = createEnhancedRandomState(randomState);

        // Use the enhanced state for terrain generation
        return super.fillFromNoise(blender, enhancedState, structureManager, chunk);
    }

    private RandomState createEnhancedRandomState(RandomState original) {
        // This would require reflection or mixin to modify the noise router
        // For now, we'll use the original - this is where deeper integration would go
        return original;
    }
}