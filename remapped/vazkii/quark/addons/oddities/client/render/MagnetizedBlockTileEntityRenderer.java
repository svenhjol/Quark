package vazkii.quark.addons.oddities.client.render;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import vazkii.quark.addons.oddities.tile.MagnetizedBlockTileEntity;
import vazkii.quark.content.automation.client.render.QuarkPistonTileEntityRenderer;

@Environment(EnvType.CLIENT)
public class MagnetizedBlockTileEntityRenderer extends BlockEntityRenderer<MagnetizedBlockTileEntity> {

	private BlockRenderManager blockRenderer = MinecraftClient.getInstance().getBlockRenderManager();
	
	public MagnetizedBlockTileEntityRenderer(BlockEntityRenderDispatcher d) {
		super(d);
	}

	@SuppressWarnings("deprecation")
	public void render(MagnetizedBlockTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn) {
		World world = tileEntityIn.getWorld();
		if (world != null) {
			BlockPos truepos = tileEntityIn.getPos();
			BlockPos blockpos = truepos.offset(tileEntityIn.getFacing().getOpposite());
			BlockState blockstate = tileEntityIn.getMagnetState();
			if (!blockstate.isAir() && !(tileEntityIn.getProgress(partialTicks) >= 1.0F)) {
				BlockEntity subTile = tileEntityIn.getSubTile();
				Vec3d offset = new Vec3d(tileEntityIn.getOffsetX(partialTicks), tileEntityIn.getOffsetY(partialTicks), tileEntityIn.getOffsetZ(partialTicks));
				if(QuarkPistonTileEntityRenderer.renderTESafely(world, truepos, blockstate, subTile, tileEntityIn, partialTicks, offset, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn))
					return;
				
				BlockModelRenderer.enableBrightnessCache();
				matrixStackIn.push();
				matrixStackIn.translate(offset.x, offset.y, offset.z);
				if (blockstate.getBlock() == Blocks.PISTON_HEAD && tileEntityIn.getProgress(partialTicks) <= 4.0F) {
					blockstate = blockstate.with(PistonHeadBlock.SHORT, Boolean.valueOf(true));
					renderStateModel(blockpos, blockstate, matrixStackIn, bufferIn, world, false, combinedOverlayIn);
				} else {
					renderStateModel(blockpos, blockstate, matrixStackIn, bufferIn, world, false, combinedOverlayIn);
				}

				matrixStackIn.pop();
				BlockModelRenderer.disableBrightnessCache();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void renderStateModel(BlockPos p_228876_1_, BlockState p_228876_2_, MatrixStack p_228876_3_, VertexConsumerProvider p_228876_4_, World p_228876_5_, boolean p_228876_6_, int p_228876_7_) {
		RenderLayer.getBlockLayers().stream().filter(t -> RenderLayers.canRenderInLayer(p_228876_2_, t)).forEach(rendertype -> {
			ForgeHooksClient.setRenderLayer(rendertype);
			VertexConsumer ivertexbuilder = p_228876_4_.getBuffer(rendertype);
			if (blockRenderer == null) 
				blockRenderer = MinecraftClient.getInstance().getBlockRenderManager();
			
			blockRenderer.getModelRenderer().render(p_228876_5_, blockRenderer.getModel(p_228876_2_), p_228876_2_, p_228876_1_, p_228876_3_, ivertexbuilder, p_228876_6_, new Random(), p_228876_2_.getRenderingSeed(p_228876_1_), p_228876_7_);
		});
		ForgeHooksClient.setRenderLayer(null);
	}
}
