package net.goutros.goutrosstrangebiomes.worldgen.events;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.data.PillowChunkData;
import net.goutros.goutrosstrangebiomes.worldgen.data.PillowGenerationContext;
import net.goutros.goutrosstrangebiomes.worldgen.integration.DataStructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID)
public class TerrainGenEvents {

    /**
     * Custom terrain generation event
     */
    public static class PillowTerrainGenEvent extends net.neoforged.bus.api.Event {
        private final ChunkAccess chunk;
        private final GenerationStep.Decoration phase;
        private final PillowGenerationContext context;

        public PillowTerrainGenEvent(ChunkAccess chunk, GenerationStep.Decoration phase, PillowGenerationContext context) {
            this.chunk = chunk;
            this.phase = phase;
            this.context = context;
        }

        public ChunkAccess getChunk() { return chunk; }
        public GenerationStep.Decoration getPhase() { return phase; }
        public PillowGenerationContext getContext() { return context; }

        /**
         * Pre-generation event
         */
        public static class Pre extends PillowTerrainGenEvent {
            private boolean cancelled = false;

            public Pre(ChunkAccess chunk, GenerationStep.Decoration phase, PillowGenerationContext context) {
                super(chunk, phase, context);
            }

            public boolean isCancelled() { return cancelled; }
            public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
        }

        /**
         * Post-generation event
         */
        public static class Post extends PillowTerrainGenEvent {
            public Post(ChunkAccess chunk, GenerationStep.Decoration phase, PillowGenerationContext context) {
                super(chunk, phase, context);
            }
        }
    }

    @SubscribeEvent
    public static void onPreTerrainGen(PillowTerrainGenEvent.Pre event) {
        if (event.getPhase() == GenerationStep.Decoration.RAW_GENERATION) {
            handleRawGeneration(event);
        }
    }

    @SubscribeEvent
    public static void onPostTerrainGen(PillowTerrainGenEvent.Post event) {
        if (event.getPhase() == GenerationStep.Decoration.RAW_GENERATION) {
            finalizeTerrainGeneration(event);
        }
    }

    private static void handleRawGeneration(PillowTerrainGenEvent.Pre event) {
        if (!event.getContext().isPillowBiome()) return;

        ChunkAccess chunk = event.getChunk();
        DataStructureManager.updateHeightmaps(chunk, event.getContext());

        GoutrosStrangeBiomes.LOGGER.debug("Pre-processing pillow terrain for chunk {}",
                chunk.getPos());
    }

    private static void finalizeTerrainGeneration(PillowTerrainGenEvent.Post event) {
        if (!event.getContext().isPillowBiome()) return;

        ChunkAccess chunk = event.getChunk();
        PillowChunkData data = PillowChunkData.get(chunk);

        // Mark as having custom terrain
        if (event.getContext().isPillowBiome()) {
            // Force custom terrain flag since we processed it
            if (!data.hasCustomTerrain()) {
                // Trigger custom terrain flag by setting a height
                data.setPillowHeight(8, 8, chunk.getHeight(
                        net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, 8, 8));
            }
        }

        GoutrosStrangeBiomes.LOGGER.debug("Finalized pillow terrain for chunk {}",
                chunk.getPos());
    }

    /**
     * Fire custom terrain generation events
     */
    public static void fireTerrainGenEvent(ChunkAccess chunk, GenerationStep.Decoration phase,
                                           PillowGenerationContext context) {

        PillowTerrainGenEvent.Pre preEvent = new PillowTerrainGenEvent.Pre(chunk, phase, context);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(preEvent);

        if (!preEvent.isCancelled()) {
            PillowTerrainGenEvent.Post postEvent = new PillowTerrainGenEvent.Post(chunk, phase, context);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(postEvent);
        }
    }
}
