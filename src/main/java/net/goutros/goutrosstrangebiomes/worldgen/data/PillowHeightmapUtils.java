package net.goutros.goutrosstrangebiomes.worldgen.data;

import net.goutros.goutrosstrangebiomes.worldgen.data.PillowChunkData;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class PillowHeightmapUtils {

    /**
     * Custom heightmap type for pillow terrain
     */
    public static class PillowHeightmap {
        private final float[] heights = new float[256];
        private final ChunkAccess chunk;

        public PillowHeightmap(ChunkAccess chunk) {
            this.chunk = chunk;
        }

        public void calculatePillowHeights(PillowGenerationContext context) {
            net.minecraft.world.level.ChunkPos pos = chunk.getPos();
            int startX = pos.getMinBlockX();
            int startZ = pos.getMinBlockZ();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    double worldX = startX + x;
                    double worldZ = startZ + z;

                    // Sample at multiple Y levels and find surface
                    float surfaceHeight = findPillowSurface(context, worldX, worldZ);
                    heights[getIndex(x, z)] = surfaceHeight;
                }
            }
        }

        private float findPillowSurface(PillowGenerationContext context, double x, double z) {
            float baseHeight = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int)x & 15, (int)z & 15);

            if (!context.isPillowBiome()) {
                return baseHeight;
            }

            // Apply pillow terrain modification
            double pillowModification = context.calculateTerrainHeight(x, baseHeight, z);
            return (float)(baseHeight + pillowModification);
        }

        public float getHeight(int x, int z) {
            return heights[getIndex(x, z)];
        }

        public void setHeight(int x, int z, float height) {
            heights[getIndex(x, z)] = height;
        }

        private static int getIndex(int x, int z) {
            return (z & 15) * 16 + (x & 15);
        }

        public float[] getHeights() {
            return heights.clone();
        }
    }

    /**
     * Samples height with interpolation for smooth terrain
     */
    public static double sampleHeightSmooth(PillowHeightmap heightmap, double x, double z) {
        int x0 = (int)Math.floor(x);
        int z0 = (int)Math.floor(z);
        int x1 = x0 + 1;
        int z1 = z0 + 1;

        double fx = x - x0;
        double fz = z - z0;

        float h00 = heightmap.getHeight(x0 & 15, z0 & 15);
        float h10 = heightmap.getHeight(x1 & 15, z0 & 15);
        float h01 = heightmap.getHeight(x0 & 15, z1 & 15);
        float h11 = heightmap.getHeight(x1 & 15, z1 & 15);

        // Bilinear interpolation
        double h0 = h00 * (1 - fx) + h10 * fx;
        double h1 = h01 * (1 - fx) + h11 * fx;

        return h0 * (1 - fz) + h1 * fz;
    }

    /**
     * Creates pillow heightmap from chunk data
     */
    public static PillowHeightmap createFrom(ChunkAccess chunk, PillowGenerationContext context) {
        PillowHeightmap heightmap = new PillowHeightmap(chunk);
        heightmap.calculatePillowHeights(context);

        // Store in chunk data
        PillowChunkData data = PillowChunkData.get(chunk);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                data.setPillowHeight(x, z, heightmap.getHeight(x, z));
            }
        }

        return heightmap;
    }
}
