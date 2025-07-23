package net.goutros.goutrosstrangebiomes.worldgen.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;

/**
 * Enhanced chunk data container with pillow terrain-specific data
 */
public class PillowChunkData {

    // Attachment types for storing custom data on chunks
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.ATTACHMENT_TYPES, GoutrosStrangeBiomes.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PillowChunkData>> PILLOW_DATA =
            ATTACHMENT_TYPES.register("pillow_data", () -> AttachmentType.builder(() -> new PillowChunkData()).build());

    private double softnessFactor = 1.0;
    private float[] pillowHeights = new float[256]; // 16x16 heightmap
    private boolean hasCustomTerrain = false;
    private int[] surfaceNoiseCache = new int[256];

    public static PillowChunkData get(ChunkAccess chunk) {
        return chunk.getData(PILLOW_DATA.get());
    }

    // Pillow-specific height sampling
    public float getPillowHeight(int x, int z) {
        return pillowHeights[getIndex(x, z)];
    }

    public void setPillowHeight(int x, int z, float height) {
        pillowHeights[getIndex(x, z)] = height;
        hasCustomTerrain = true;
    }

    public double getSoftnessFactor() {
        return softnessFactor;
    }

    public void setSoftnessFactor(double factor) {
        this.softnessFactor = Math.max(0.0, Math.min(2.0, factor));
    }

    public boolean hasCustomTerrain() {
        return hasCustomTerrain;
    }

    public int getSurfaceNoise(int x, int z) {
        return surfaceNoiseCache[getIndex(x, z)];
    }

    public void setSurfaceNoise(int x, int z, int noise) {
        surfaceNoiseCache[getIndex(x, z)] = noise;
    }

    private static int getIndex(int x, int z) {
        return (z & 15) * 16 + (x & 15);
    }
}