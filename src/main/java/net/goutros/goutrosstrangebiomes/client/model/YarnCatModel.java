package net.goutros.goutrosstrangebiomes.client.model;

import net.goutros.goutrosstrangebiomes.GoutrosStrangeBiomes;
import net.goutros.goutrosstrangebiomes.entity.YarnCatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class YarnCatModel<T extends YarnCatEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(GoutrosStrangeBiomes.MOD_ID, "yarn_cat"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart tail;
    private final ModelPart tail2;

    public YarnCatModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.frontLeftLeg = root.getChild("front_left_leg");
        this.frontRightLeg = root.getChild("front_right_leg");
        this.backLeftLeg = root.getChild("back_left_leg");
        this.backRightLeg = root.getChild("back_right_leg");
        this.tail = root.getChild("tail");
        this.tail2 = root.getChild("tail2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-2.5F, -3.0F, -4.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(5, 4).addBox(2.5F, 2.0F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 19).addBox(-2.5F, 2.0F, -2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 20).addBox(-1.5F, -0.02F, -5.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, -7.0F));

        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(28, 10).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -3.8F, 0.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(8, 28).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -3.8F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 2.5F, -8.0F, 4.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 14.0F, -8.5F, 1.5708F, 0.0F, 0.0F));

        PartDefinition front_left_leg = partdefinition.addOrReplaceChild("front_left_leg", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.1F, 16.1F, -5.0F));

        PartDefinition front_right_leg = partdefinition.addOrReplaceChild("front_right_leg", CubeListBuilder.create().texOffs(20, 10).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.1F, 16.1F, -5.0F));

        PartDefinition back_left_leg = partdefinition.addOrReplaceChild("back_left_leg", CubeListBuilder.create().texOffs(20, 24).addBox(-1.0F, 0.0F, 1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.1F, 20.0F, 2.0F));

        PartDefinition back_right_leg = partdefinition.addOrReplaceChild("back_right_leg", CubeListBuilder.create().texOffs(0, 28).addBox(-1.0F, 0.0F, 1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.1F, 20.0F, 2.0F));

        PartDefinition tail = partdefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(28, 0).addBox(-0.5F, 1.0F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.5F, 5.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition tail2 = partdefinition.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(28, 5).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.5F, 10.0F, 1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Reset all parts to default pose
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Head movement - FIXED: Reduced head rotation sensitivity
        this.head.yRot = netHeadYaw * 0.01745329252F; // Convert degrees to radians, reduced sensitivity
        this.head.xRot = headPitch * 0.01745329252F;

        // Use animation system
        this.animate(entity.idleAnimationState, YarnCatAnimations.idle, ageInTicks);
        this.animate(entity.walkAnimationState, YarnCatAnimations.walk, ageInTicks);
        this.animate(entity.sitAnimationState, YarnCatAnimations.sit, ageInTicks);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        // Handle baby scaling
        if (this.young) {
            poseStack.pushPose();
            poseStack.scale(0.6F, 0.6F, 0.6F);
            poseStack.translate(0.0F, 1.0F, 0.0F);
            this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            poseStack.popPose();
        } else {
            this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    // Getter methods for accessing model parts
    public ModelPart getHead() {
        return this.head;
    }

    public ModelPart getBody() {
        return this.body;
    }

    public ModelPart getTail() {
        return this.tail;
    }

    public ModelPart getTail2() {
        return this.tail2;
    }
}