package net.goutros.goutrosstrangebiomes.worldgen.generator;

import com.mojang.serialization.MapCodec;
import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Registration for custom chunk generators
 */
public class ModChunkGenerators {

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, GoutrosStrangeBiomes.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<PillowChunkGenerator>> PILLOW_GENERATOR =
            CHUNK_GENERATORS.register("pillow_generator", () -> PillowChunkGenerator.CODEC);
}