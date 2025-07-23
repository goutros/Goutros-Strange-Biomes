package net.goutros.goutrosstrangebiomes.worldgen.support;

import net.goutros.goutrosstrangebiomes.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class PillowFeatureSorter {

    /**
     * Sort features with pillow-specific priority
     */
    public static List<PlacedFeature> sortFeaturesForPillow(List<PlacedFeature> features,
                                                            ChunkAccess chunk) {
        if (!isPillowChunk(chunk)) {
            return features;
        }

        // Prioritize pillow terrain features
        return features.stream()
                .sorted((a, b) -> {
                    int aPriority = getPillowFeaturePriority(a);
                    int bPriority = getPillowFeaturePriority(b);
                    return Integer.compare(bPriority, aPriority);
                })
                .toList();
    }

    private static int getPillowFeaturePriority(PlacedFeature feature) {
        String featureName = feature.toString().toLowerCase();

        // Higher priority for pillow-specific features
        if (featureName.contains("pillow")) return 100;
        if (featureName.contains("button")) return 90;
        if (featureName.contains("surface")) return 80;

        // Lower priority for conflicting features
        if (featureName.contains("stone") || featureName.contains("dirt")) return 10;

        return 50; // Default priority
    }

    private static boolean isPillowChunk(ChunkAccess chunk) {
        BlockPos center = chunk.getPos().getMiddleBlockPosition(64);
        return chunk.getNoiseBiome(center.getX() >> 2, center.getY() >> 2, center.getZ() >> 2)
                .is(ModBiomes.PILLOW_PLATEAU);
    }
}
