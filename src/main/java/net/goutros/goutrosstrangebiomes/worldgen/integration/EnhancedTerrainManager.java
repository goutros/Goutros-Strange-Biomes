package net.goutros.goutrosstrangebiomes.worldgen.integration;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.worldgen.surface.ModSurfaceRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import terrablender.api.SurfaceRuleManager;

import static net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes.LOGGER;

@EventBusSubscriber(modid = GoutrosStrangeBiomes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class EnhancedTerrainManager {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // This should be called BEFORE any worlds are created
            SurfaceRuleManager.addSurfaceRules(
                    SurfaceRuleManager.RuleCategory.OVERWORLD,
                    GoutrosStrangeBiomes.MOD_ID,
                    ModSurfaceRules.pillowPlateauSurface()
            );

            LOGGER.info("Surface rules registered with TerraBlender");
        });
    }
}