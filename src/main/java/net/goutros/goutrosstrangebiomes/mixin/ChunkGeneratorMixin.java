package net.goutros.goutrosstrangebiomes.mixin;

import net.goutros.goutrosstrangebiomes.worldgen.terrain.TerrainResculptor;
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
 * Mixin that resculpts terrain after vanilla generation is complete
 */
@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ChunkGeneratorMixin");

    /**
     * Inject after vanilla terrain generation but before decoration
     */
    @Inject(method = "applyBiomeDecoration", at = @At("HEAD"))
    private void resculptCustomTerrain(WorldGenLevel level, ChunkAccess chunk,
                                       StructureManager structureManager, CallbackInfo ci) {
        try {
            ChunkPos chunkPos = chunk.getPos();

            // Resculpt terrain based on biome influence
            TerrainResculptor.resculptTerrain(chunk, level.getLevel().getChunkSource().randomState());

        } catch (Exception e) {
            LOGGER.error("Error in terrain resculpting for chunk {}, {}: {}",
                    chunk.getPos().x, chunk.getPos().z, e.getMessage(), e);
        }
    }

    /**
     * Cleanup cache periodically
     */
    @Inject(method = "applyBiomeDecoration", at = @At("TAIL"))
    private void cleanupCache(WorldGenLevel level, ChunkAccess chunk,
                              StructureManager structureManager, CallbackInfo ci) {
        // Clean up cache every 100 chunks to prevent memory leaks
        if ((chunk.getPos().x + chunk.getPos().z) % 100 == 0) {
            TerrainResculptor.cleanupCache();
        }
    }
}