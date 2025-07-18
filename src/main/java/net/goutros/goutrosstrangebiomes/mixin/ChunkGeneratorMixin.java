package net.goutros.goutrosstrangebiomes.mixin;

import net.goutros.goutrosstrangebiomes.worldgen.terrain.TerrainResculptor;
import net.goutros.goutrosstrangebiomes.worldgen.terrain.SeamlessBiomeBlender;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Enhanced mixin that resculpts terrain after vanilla generation with optimized performance
 */
@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ChunkGeneratorMixin");
    private static int processedChunks = 0;

    /**
     * Inject after vanilla terrain generation but before decoration
     * This ensures we modify terrain shapes, not just surface blocks
     */
    @Inject(method = "applyBiomeDecoration", at = @At("HEAD"))
    private void resculptCustomTerrain(WorldGenLevel level, ChunkAccess chunk,
                                       StructureManager structureManager, CallbackInfo ci) {
        try {
            ChunkPos chunkPos = chunk.getPos();

            // Quick check if this chunk needs any custom terrain processing
            if (!SeamlessBiomeBlender.chunkHasSignificantInfluence(chunk)) {
                return; // Early exit for performance
            }

            // Resculpt terrain with enhanced system
            long startTime = System.nanoTime();
            TerrainResculptor.resculptTerrain(chunk, level.getLevel().getChunkSource().randomState());
            long endTime = System.nanoTime();

            processedChunks++;

            // Log performance occasionally
            if (processedChunks % 100 == 0) {
                double processingTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
                LOGGER.info("Processed {} chunks with custom terrain. Last chunk took {:.2f}ms",
                        processedChunks, processingTime);
                LOGGER.debug("Cache stats: {}", SeamlessBiomeBlender.getCacheStats());
            }

        } catch (Exception e) {
            LOGGER.error("Error in enhanced terrain resculpting for chunk {}, {}: {}",
                    chunk.getPos().x, chunk.getPos().z, e.getMessage(), e);
        }
    }

    /**
     * Cleanup cache periodically to prevent memory leaks
     */
    @Inject(method = "applyBiomeDecoration", at = @At("TAIL"))
    private void cleanupTerrainCache(WorldGenLevel level, ChunkAccess chunk,
                                     StructureManager structureManager, CallbackInfo ci) {
        // Clean up caches every 200 chunks to prevent memory leaks
        if ((chunk.getPos().x + chunk.getPos().z) % 200 == 0) {
            TerrainResculptor.cleanupCache();
            SeamlessBiomeBlender.forceCleanupCache();
        }
    }
}