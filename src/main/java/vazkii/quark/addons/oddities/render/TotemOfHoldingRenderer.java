package vazkii.quark.addons.oddities.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.util.ClientTicker;
import vazkii.quark.addons.oddities.entity.TotemOfHoldingEntity;
import vazkii.quark.addons.oddities.module.TotemOfHoldingModule;

import javax.annotation.Nonnull;

/**
 * @author WireSegal
 * Created at 2:01 PM on 3/30/20.
 */
@OnlyIn(Dist.CLIENT)
public class TotemOfHoldingRenderer extends EntityRenderer<TotemOfHoldingEntity> {

    public TotemOfHoldingRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @SuppressWarnings("deprecation")
	@Override
    public void render(TotemOfHoldingEntity entity, float entityYaw, float partialTicks, @Nonnull MatrixStack matrixStackIn, @Nonnull IRenderTypeBuffer bufferIn, int packedLightIn) {
        int deathTicks = entity.getDeathTicks();
        boolean dying = entity.isDying();
        float time = ClientTicker.ticksInGame + partialTicks;
        float scale = !dying ? 1F : Math.max(0, TotemOfHoldingEntity.DEATH_TIME - (deathTicks + partialTicks)) / TotemOfHoldingEntity.DEATH_TIME;
        float rotation = time + (!dying ? 0 : (deathTicks + partialTicks) * 5);
        double translation = !dying ? (Math.sin(time * 0.03) * 0.1) : ((deathTicks + partialTicks) / TotemOfHoldingEntity.DEATH_TIME * 5);

        Minecraft mc = Minecraft.getInstance();
        BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
        ModelManager modelManager = mc.getModelManager();

        matrixStackIn.push();
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(rotation));
        matrixStackIn.translate(0, translation, 0);
        matrixStackIn.scale(scale, scale, scale);
        matrixStackIn.translate(-0.5, 0, -0.5);
        dispatcher.getBlockModelRenderer().
                renderModelBrightnessColor(matrixStackIn.getLast(), bufferIn.getBuffer(Atlases.getCutoutBlockType()),
                        null,
                        modelManager.getModel(TotemOfHoldingModule.MODEL_LOC), 1.0F, 1.0F, 1.0F, packedLightIn, OverlayTexture.NO_OVERLAY);
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
            Minecraft mc = Minecraft.getInstance();
            return !mc.gameSettings.hideGUI && mc.objectMouseOver != null &&
                    mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY &&
                    ((EntityRayTraceResult) mc.objectMouseOver).hitInfo == entity;
        }

        return false;
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull TotemOfHoldingEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}
