package net.goutros.goutrosstrangebiomes.mixin;

import net.goutros.goutrosstrangebiomes.worldgen.LayeredTerrainProcessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

    @Inject(method = "fillFromNoise", at = @At("RETURN"))
    private void onFillFromNoise(Blender blender, RandomState randomState,
                                 StructureManager structureManager, ChunkAccess chunk,
                                 CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {

        // Apply layered terrain AFTER normal generation
        LayeredTerrainProcessor.processChunk(chunk);
    }
}
