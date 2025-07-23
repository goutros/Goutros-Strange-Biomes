package net.goutros.goutrosstrangebiomes.mixin;

import net.goutros.goutrosstrangebiomes.worldgen.terrain.TerrainFlowAnalyzer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SIMPLE TERRAIN FLOW MIXIN
 *
 * Clean mixin that delegates all the complex logic to TerrainFlowAnalyzer.
 * No inner classes, no complex logic - just a simple method call.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {

    /**
     * Inject into fillFromNoise to modify terrain after base generation but before carvers
     */
    @Inject(method = "fillFromNoise", at = @At("RETURN"))
    private void createTerrainFlowCanyons(Blender blender, RandomState randomState,
                                          net.minecraft.world.level.StructureManager structureManager,
                                          ChunkAccess chunk, CallbackInfoReturnable<ChunkAccess> cir) {
        try {
            // Delegate all the complex logic to the external TerrainFlowAnalyzer class
            TerrainFlowAnalyzer.processChunkTerrainFlow(chunk);
        } catch (Exception e) {
            // Fail silently to prevent crashes
        }
    }
}