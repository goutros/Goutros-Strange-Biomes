package net.goutros.goutrosstrangebiomes.worldgen.integration;

import net.goutros.goutrosstrangebiomes.worldgen.surface.ModSurfaceRules;
import net.goutros.goutrosstrangebiomes.worldgen.aquifer.PillowAquifer;
import net.goutros.goutrosstrangebiomes.worldgen.blending.PillowBlender;
import net.goutros.goutrosstrangebiomes.worldgen.carver.ModCarvers;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.api.SurfaceRuleManager;

/**
 * Enhanced TerrainManager that integrates all custom terrain systems
 */
public class EnhancedTerrainManager {

    /**
     * Initialize all terrain systems with TerraBlender
     */
    public static void initializeTerrainSystems() {
        // Register surface rules
        registerSurfaceRules();

        // Note: Aquifer, Blender, and Carver integration happens at chunk generation level
        // through the EnhancedChunkGenerator
    }

    private static void registerSurfaceRules() {
        // Register our pillow surface rules with TerraBlender
        SurfaceRuleManager.addSurfaceRules(
                SurfaceRuleManager.RuleCategory.OVERWORLD,
                "goutrosstrangebiomes",
                ModSurfaceRules.pillowPlateauSurface()
        );
    }
}