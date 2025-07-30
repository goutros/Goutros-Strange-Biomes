package net.goutros.goutrosstrangebiomes.client.renderer;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.client.model.YarnCatModel;
import net.goutros.goutrosstrangebiomes.entity.YarnCatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public class YarnCatRenderer extends MobRenderer<YarnCatEntity, YarnCatModel<YarnCatEntity>> {

    // Base white texture that gets tinted
    private static final ResourceLocation BASE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, "textures/entity/yarn_cat.png");

    // Overlay texture for eyes, nose, and other non-dyeable parts
    private static final ResourceLocation OVERLAY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, "textures/entity/yarn_cat_overlay.png");

    public YarnCatRenderer(EntityRendererProvider.Context context) {
        super(context, new YarnCatModel<>(context.bakeLayer(YarnCatModel.LAYER_LOCATION)), 0.4F);
        this.addLayer(new YarnCatOverlayLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(YarnCatEntity entity) {
        return BASE_TEXTURE;
    }

    @Override
    protected void scale(YarnCatEntity entity, PoseStack poseStack, float partialTickTime) {
        // Remove scaling - let the model show at its natural Blockbench size
        // Only scale babies slightly
        if (entity.isBaby()) {
            poseStack.scale(0.7F, 0.7F, 0.7F);
        }
    }

    @Override
    protected void setupRotations(YarnCatEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        // FIXED: Ensure the entity is not rotated upside down
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);

        // If the entity is still appearing upside down, you can add a corrective rotation here:
        // poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F)); // Uncomment if still upside down
    }

    /**
     * Overlay layer for rendering non-dyeable parts like eyes, nose, whiskers, etc.
     */
    public static class YarnCatOverlayLayer extends RenderLayer<YarnCatEntity, YarnCatModel<YarnCatEntity>> {

        public YarnCatOverlayLayer(YarnCatRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           YarnCatEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

            // Get the color (rainbow if jeb_)
            Vector3f color = entity.getDynamicColor();
            float r = color.x();
            float g = color.y();
            float b = color.z();

            // Convert to ARGB int
            int colorInt = 0xFF000000
                    | ((int)(r * 255) << 16)
                    | ((int)(g * 255) << 8)
                    | ((int)(b * 255));

            // Base texture with dynamic tint
            VertexConsumer baseConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
            this.getParentModel().renderToBuffer(poseStack, baseConsumer, packedLight, OverlayTexture.NO_OVERLAY, colorInt);

            // Overlay (eyes/pupils) rendered full-bright white, untinted
            VertexConsumer overlayConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(OVERLAY_TEXTURE));
            this.getParentModel().renderToBuffer(poseStack, overlayConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
    }

    /**
     * Helper method to convert DyeColor to RGB color for compatibility with mods
     */
    public static int getDyeColorRGB(YarnCatEntity entity) {
        return entity.getTextureDiffuseColor();
    }

    /**
     * Alternative method for getting firework color (useful for particle effects)
     */
    public static int getFireworkColorRGB(YarnCatEntity entity) {
        return entity.getFireworkColor();
    }
}