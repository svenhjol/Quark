package vazkii.quark.content.automation.client.render;

import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.automation.module.PistonsMoveTileEntitiesModule;

public class QuarkPistonTileEntityRenderer {

	public static boolean renderPistonBlock(PistonBlockEntity piston, float pTicks, MatrixStack matrix, VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn) {
		if (!ModuleLoader.INSTANCE.isModuleEnabled(PistonsMoveTileEntitiesModule.class) || piston.getProgress(pTicks) > 1.0F)
			return false;

		BlockState state = piston.getPushedBlock();
		BlockPos truePos = piston.getPos();
		BlockEntity tile = PistonsMoveTileEntitiesModule.getMovement(piston.getWorld(), truePos);
		Vec3d offset = new Vec3d(piston.getRenderOffsetX(pTicks), piston.getRenderOffsetY(pTicks), piston.getRenderOffsetZ(pTicks));
		
		return renderTESafely(piston.getWorld(), truePos, state, tile, piston, pTicks, offset, matrix, bufferIn, combinedLightIn, combinedOverlayIn);
	}
	
	public static boolean renderTESafely(World world, BlockPos truePos, BlockState state, BlockEntity tile, BlockEntity sourceTE, float pTicks, Vec3d offset, MatrixStack matrix, VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn) {
		Block block = state.getBlock();
		String id = Objects.toString(block.getRegistryName());
		
		MatrixStack.Entry currEntry = matrix.peek();
		render: try {
			if(tile == null || (block == Blocks.PISTON_HEAD) || PistonsMoveTileEntitiesModule.renderBlacklist.contains(id))
				break render;
			
			matrix.push();
			BlockEntityRenderer<BlockEntity> tileentityrenderer = BlockEntityRenderDispatcher.INSTANCE.get(tile);
			if(tileentityrenderer != null) {
				tile.setLocation(sourceTE.getWorld(), sourceTE.getPos());
				tile.cancelRemoval();

				matrix.translate(offset.x, offset.y, offset.z);

				tile.cachedState = state;
				tileentityrenderer.render(tile, pTicks, matrix, bufferIn, combinedLightIn, combinedOverlayIn);
			}
		} catch(Throwable e) {
			Quark.LOG.warn(id + " can't be rendered for piston TE moving", e);
			PistonsMoveTileEntitiesModule.renderBlacklist.add(id);
			return false;
		} finally {
			while(matrix.peek() != currEntry)
				matrix.pop();
		}
		
		return state.getRenderType() != BlockRenderType.MODEL;
	}

}
