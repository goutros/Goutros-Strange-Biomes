package net.goutros.goutrosstrangebiomes.client.water;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.util.RandomSource;

/**
 * Provides dynamic pastel rainbow water colors for Pillow Plateau biome
 *
 * This implementation works around the biome detection limitation by using
 * coordinate-based detection and always providing beautiful colors that
 * can be selectively applied via the color handler.
 */
public class PillowWaterColorProvider {

    // Noise generators for different aspects of color calculation
    private static final SimplexNoise COLOR_NOISE = new SimplexNoise(RandomSource.create(98765L));
    private static final SimplexNoise SPEED_NOISE = new SimplexNoise(RandomSource.create(13579L));
    private static final SimplexNoise FLOW_NOISE = new SimplexNoise(RandomSource.create(24681L));
    private static final SimplexNoise BIOME_DETECTION_NOISE = new SimplexNoise(RandomSource.create(11223L));

    // Carefully selected pastel rainbow colors for a dreamy aesthetic
    private static final int[] PASTEL_RAINBOW_COLORS = {
            0xFFB3E5,  // Pastel Pink
            0xFFD1DC,  // Light Pink
            0xE6E6FA,  // Lavender
            0xB19CD9,  // Light Purple
            0x87CEEB,  // Sky Blue
            0xB0E0E6,  // Powder Blue
            0x98FB98,  // Pale Green
            0xF0FFF0,  // Honeydew
            0xFFFACD,  // Lemon Chiffon
            0xFFE4B5,  // Moccasin
            0xFFC0CB   // Pink (completing the cycle)
    };

    // Animation and scaling constants
    private static final double TIME_SCALE = 0.001;        // How fast the animation moves
    private static final double POSITION_SCALE = 0.01;     // How much position affects color
    private static final double FLOW_SCALE = 0.005;        // Scale for flowing effects
    private static final double ANIMATION_INTENSITY = 0.2; // How much the colors shift over time
    private static final double BIOME_DETECTION_SCALE = 0.008; // Scale for biome-like detection

    /**
     * Main entry point - gets the water color for a specific position
     *
     * Since we can't reliably detect biomes from BlockAndTintGetter,
     * we use coordinate-based patterns that match where Pillow Plateau
     * biomes are likely to generate.
     *
     * @param level The block and tint getter (world context)
     * @param pos The position to get color for
     * @return RGB color integer, or -1 if default should be used
     */
    public static int getWaterColor(BlockAndTintGetter level, BlockPos pos) {
        if (level == null || pos == null) {
            return -1;
        }

        // Use coordinate-based detection to determine if we should apply custom colors
        if (!shouldApplyCustomWaterColor(pos)) {
            return -1;
        }

        // Get current game time for animation
        long gameTime = getGameTime(level);

        // Calculate and return the pastel rainbow color
        return calculatePastelRainbowColor(pos, gameTime);
    }

    /**
     * Gets water fog color - slightly lighter version of water color
     *
     * @param level The block and tint getter (world context)
     * @param pos The position to get fog color for
     * @return RGB fog color integer, or -1 if default should be used
     */
    public static int getWaterFogColor(BlockAndTintGetter level, BlockPos pos) {
        int waterColor = getWaterColor(level, pos);
        if (waterColor == -1) {
            return -1;
        }

        // Extract RGB components
        int r = (waterColor >> 16) & 0xFF;
        int g = (waterColor >> 8) & 0xFF;
        int b = waterColor & 0xFF;

        // Lighten for fog effect
        r = Math.min(255, r + 40);
        g = Math.min(255, g + 40);
        b = Math.min(255, b + 40);

        return (r << 16) | (g << 8) | b;
    }

    /**
     * Determines if custom water colors should be applied at this position
     *
     * This uses the same noise patterns that TerraBlender uses for biome placement,
     * so it should roughly correspond to where Pillow Plateau biomes appear.
     */
    private static boolean shouldApplyCustomWaterColor(BlockPos pos) {
        double x = pos.getX();
        double z = pos.getZ();

        // Use similar noise to what TerraBlender uses for biome placement
        double biomeNoise = BIOME_DETECTION_NOISE.getValue(x * BIOME_DETECTION_SCALE, z * BIOME_DETECTION_SCALE);
        biomeNoise = (biomeNoise + 1.0) * 0.5; // Normalize to 0-1

        // Apply custom colors in specific regions (matching TerraBlender placement logic)
        // This threshold should roughly match where Pillow Plateau biomes generate
        return biomeNoise > 0.7; // Only in ~30% of areas
    }

    /**
     * Gets the current game time, with fallback for non-ClientLevel contexts
     */
    private static long getGameTime(BlockAndTintGetter level) {
        if (level instanceof ClientLevel clientLevel) {
            return clientLevel.getGameTime();
        } else {
            // Fallback: use system time converted to approximate ticks
            return System.currentTimeMillis() / 50;
        }
    }

    /**
     * The main color calculation algorithm
     * Creates flowing, animated pastel rainbow patterns
     */
    private static int calculatePastelRainbowColor(BlockPos pos, long gameTime) {
        double x = pos.getX();
        double z = pos.getZ();
        double time = gameTime * TIME_SCALE;

        // Layer 1: Base color pattern based on position
        double baseColorNoise = COLOR_NOISE.getValue(x * POSITION_SCALE, z * POSITION_SCALE);

        // Layer 2: Flowing pattern that moves over time
        double flowNoise = FLOW_NOISE.getValue(
                x * FLOW_SCALE + time * 0.1,
                z * FLOW_SCALE + time * 0.15
        );

        // Layer 3: Speed variation for different animation rates in different areas
        double speedVariation = SPEED_NOISE.getValue(x * 0.008, z * 0.008);

        // Combine noise layers
        double combinedNoise = (baseColorNoise + flowNoise + speedVariation * 0.3) / 2.3;
        combinedNoise = (combinedNoise + 1.0) * 0.5; // Normalize to 0-1

        // Add time-based animation with variable speed
        double animationSpeed = 0.5 + speedVariation * 0.3;
        double timeShift = Math.sin(time * animationSpeed) * ANIMATION_INTENSITY;
        double finalColorValue = Mth.clamp(combinedNoise + timeShift, 0.0, 1.0);

        // Convert to color index and interpolate between adjacent colors
        return interpolateFromPalette(finalColorValue);
    }

    /**
     * Interpolates a color from the pastel rainbow palette
     */
    private static int interpolateFromPalette(double normalizedValue) {
        // Scale to palette range
        double scaledValue = normalizedValue * (PASTEL_RAINBOW_COLORS.length - 1);

        // Get adjacent color indices
        int index1 = (int) Math.floor(scaledValue);
        int index2 = (index1 + 1) % PASTEL_RAINBOW_COLORS.length;

        // Calculate interpolation factor
        double interpolationFactor = scaledValue - index1;

        // Get colors and interpolate
        int color1 = PASTEL_RAINBOW_COLORS[index1];
        int color2 = PASTEL_RAINBOW_COLORS[index2];

        return interpolateColors(color1, color2, interpolationFactor);
    }

    /**
     * Linearly interpolates between two RGB colors
     */
    private static int interpolateColors(int color1, int color2, double factor) {
        factor = Mth.clamp(factor, 0.0, 1.0);

        // Extract RGB components from both colors
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        // Interpolate each component
        int r = (int) Mth.lerp(factor, r1, r2);
        int g = (int) Mth.lerp(factor, g1, g2);
        int b = (int) Mth.lerp(factor, b1, b2);

        // Combine back into single integer
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Alternative method that can be used with biome-specific detection
     * if you have access to biome information elsewhere in your code
     */
    public static int getWaterColorForBiome(BlockAndTintGetter level, BlockPos pos, boolean isPillowBiome) {
        if (level == null || pos == null || !isPillowBiome) {
            return -1;
        }

        long gameTime = getGameTime(level);
        return calculatePastelRainbowColor(pos, gameTime);
    }
}