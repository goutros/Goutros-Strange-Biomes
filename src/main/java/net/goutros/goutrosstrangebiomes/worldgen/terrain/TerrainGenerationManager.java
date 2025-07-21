package net.goutros.goutrosstrangebiomes.worldgen.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerrainGenerationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("TerrainGenManager");

    public static boolean generateCustomTerrain(ChunkAccess chunk, RandomState randomState, NoiseGeneratorSettings noiseSettings) {
        try {
            boolean didAnything = TerrainResculptor.resculptTerrain(chunk, randomState, noiseSettings);
            if (didAnything) {
                LOGGER.info("[GSB] Pillow Plateau resculpted at chunk ({}, {})", chunk.getPos().x, chunk.getPos().z);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Error in terrain generation for chunk {}, {}: {}",
                    chunk.getPos().x, chunk.getPos().z, e.getMessage(), e);
            return false;
        }
    }
}

