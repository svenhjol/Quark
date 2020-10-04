package vazkii.quark.oddities.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.util.ClientTicker;
import vazkii.quark.oddities.entity.TotemOfHoldingEntity;
import vazkii.quark.oddities.module.TotemOfHoldingModule;

import javax.annotation.Nonnull;

/**
 * @author WireSegal
 * Created at 2:01 PM on 3/30/20.
 */
@Environment(EnvType.CLIENT)
public class TotemOfHoldingRenderer extends EntityRenderer<TotemOfHoldingEntity> {

    public TotemOfHoldingRenderer(EntityRenderDispatcher manager) {
        super(manager);
    }

    @SuppressWarnings("deprecation")
	@Override
    public void render(TotemOfHoldingEntity entity, float entityYaw, float partialTicks, @Nonnull MatrixStack matrixStackIn, @Nonnull VertexConsumerProvider bufferIn, int packedLightIn) {
        int deathTicks = entity.getDeathTicks();
        boolean dying = entity.isDying();
        float time = ClientTicker.ticksInGame + partialTicks;
        float scale = !dying ? 1F : Math.max(0, TotemOfHoldingEntity.DEATH_TIME - (deathTicks + partialTicks)) / TotemOfHoldingEntity.DEATH_TIME;
        float rotation = time + (!dying ? 0 : (deathTicks + partialTicks) * 5);
        double translation = !dying ? (Math.sin(time * 0.03) * 0.1) : ((deathTicks + partialTicks) / TotemOfHoldingEntity.DEATH_TIME * 5);

        MinecraftClient mc = MinecraftClient.getInstance();
        BlockRenderManager dispatcher = mc.getBlockRenderManager();
        BakedModelManager modelManager = mc.getBakedModelManager();

        matrixStackIn.push();
        matrixStackIn.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(rotation));
        matrixStackIn.translate(0, translation, 0);
        matrixStackIn.scale(scale, scale, scale);
        matrixStackIn.translate(-0.5, 0, -0.5);
        dispatcher.getModelRenderer().
                render(matrixStackIn.peek(), bufferIn.getBuffer(TexturedRenderLayers.getEntityCutout()),
                        null,
                        modelManager.getModel(TotemOfHoldingModule.MODEL_LOC), 1.0F, 1.0F, 1.0F, packedLightIn, OverlayTexture.DEFAULT_UV);
        matrixStackIn.pop();

        super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    protected int getBlockLight(TotemOfHoldingEntity entityIn, BlockPos position) {
        return 15;
    }

    @Override
    protected boolean canRenderName(TotemOfHoldingEntity entity) {
        if (entity.hasCustomName()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            return !mc.options.hudHidden && mc.crosshairTarget != null &&
                    mc.crosshairTarget.getType() == HitResult.Type.ENTITY &&
                    ((EntityHitResult) mc.crosshairTarget).hitInfo == entity;
        }

        return false;
    }

    @Nonnull
    @Override
    public Identifier getEntityTexture(@Nonnull TotemOfHoldingEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEX;
    }
}
