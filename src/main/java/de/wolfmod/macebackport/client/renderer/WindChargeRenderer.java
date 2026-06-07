package de.wolfmod.macebackport.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.wolfmod.macebackport.MaceBackportMod;
import de.wolfmod.macebackport.client.model.WindChargeModel;
import de.wolfmod.macebackport.entity.WindChargeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class WindChargeRenderer extends EntityRenderer<WindChargeEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MaceBackportMod.MODID, "textures/entity/projectiles/wind_charge.png");

    private final WindChargeModel model;

    public WindChargeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WindChargeModel(WindChargeModel.createBodyLayer().bakeRoot());
    }

    @Override
    public void render(WindChargeEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.65F, 0.65F, 0.65F);
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTick, 0.0F, 0.0F);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        this.model.renderToBuffer(poseStack, buffer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WindChargeEntity entity) {
        return TEXTURE;
    }
}
