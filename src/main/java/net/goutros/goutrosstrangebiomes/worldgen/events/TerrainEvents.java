package net.goutros.goutrosstrangebiomes.worldgen.events;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.data.PillowChunkData;
import net.goutros.goutrosstrangebiomes.worldgen.data.PillowGenerationContext;
import net.goutros.goutrosstrangebiomes.worldgen.integration.DataStructureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkDataEvent;

//@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID)
public class TerrainEvents {

// @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onChunkLoad(ChunkEvent.Load event) {
        ChunkAccess chunk = event.getChunk();

        if (event.isNewChunk()) {
            // Initialize pillow data for new chunks
            initializePillowChunk(chunk);
        } else {
            // Validate existing pillow data
            validatePillowData(chunk);
        }
    }

   // @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        ChunkAccess chunk = event.getChunk();

        // Log performance stats before unload
        logChunkStats(chunk);
    }

//    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load event) {
        ChunkAccess chunk = event.getChunk();
        CompoundTag data = event.getData();

        // Load pillow-specific data from NBT
        loadPillowDataFromNBT(chunk, data);
    }

 //   @SubscribeEvent
    public static void onChunkDataSave(ChunkDataEvent.Save event) {
        ChunkAccess chunk = event.getChunk();
        CompoundTag data = event.getData();

        // Save pillow-specific data to NBT
        savePillowDataToNBT(chunk, data);
    }

    private static void initializePillowChunk(ChunkAccess chunk) {
        try {
            // Create basic biome getter
            java.util.function.Function<BlockPos, Holder<Biome>> biomeGetter = pos ->
                    chunk.getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2);

            // Create generation context
            PillowGenerationContext context = DataStructureManager.createContext(
                    null, chunk, biomeGetter);

            // Initialize chunk data structures
            DataStructureManager.initializeChunk(chunk, context);

            GoutrosStrangeBiomes.LOGGER.debug("Initialized pillow chunk at {}", chunk.getPos());

        } catch (Exception e) {
            GoutrosStrangeBiomes.LOGGER.error("Failed to initialize pillow chunk {}: {}",
                    chunk.getPos(), e.getMessage());
        }
    }

    private static void validatePillowData(ChunkAccess chunk) {
        PillowChunkData data = PillowChunkData.get(chunk);

        if (data.hasCustomTerrain()) {
            GoutrosStrangeBiomes.LOGGER.debug("Validated pillow chunk {} with custom terrain",
                    chunk.getPos());
        }
    }

    private static void logChunkStats(ChunkAccess chunk) {
        PillowChunkData data = PillowChunkData.get(chunk);

        if (data.hasCustomTerrain()) {
            GoutrosStrangeBiomes.LOGGER.debug("Unloading pillow chunk {} - softness: {:.2f}",
                    chunk.getPos(), data.getSoftnessFactor());
        }
    }

    private static void loadPillowDataFromNBT(ChunkAccess chunk, CompoundTag nbt) {
        if (!nbt.contains("PillowData")) return;

        CompoundTag pillowTag = nbt.getCompound("PillowData");
        PillowChunkData data = PillowChunkData.get(chunk);

        data.setSoftnessFactor(pillowTag.getDouble("softness"));

        if (pillowTag.contains("heights")) {
            int[] heights = pillowTag.getIntArray("heights");
            for (int i = 0; i < Math.min(heights.length, 256); i++) {
                int x = i % 16;
                int z = i / 16;
                data.setPillowHeight(x, z, heights[i] / 100.0f);
            }
        }
    }

    private static void savePillowDataToNBT(ChunkAccess chunk, CompoundTag nbt) {
        PillowChunkData data = PillowChunkData.get(chunk);

        if (!data.hasCustomTerrain()) return;

        CompoundTag pillowTag = new CompoundTag();
        pillowTag.putDouble("softness", data.getSoftnessFactor());

        int[] heights = new int[256];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                heights[z * 16 + x] = (int)(data.getPillowHeight(x, z) * 100);
            }
        }
        pillowTag.putIntArray("heights", heights);

        nbt.put("PillowData", pillowTag);
    }
}