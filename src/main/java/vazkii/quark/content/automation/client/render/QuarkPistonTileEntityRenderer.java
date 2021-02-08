package vazkii.quark.content.automation.client.render;

import java.util.Objects;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.automation.module.PistonsMoveTileEntitiesModule;

public class QuarkPistonTileEntityRenderer {

	public static boolean renderPistonBlock(PistonTileEntity piston, float pTicks, MatrixStack matrix, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		if (!ModuleLoader.INSTANCE.isModuleEnabled(PistonsMoveTileEntitiesModule.class) || piston.getProgress(pTicks) > 1.0F)
			return false;

		BlockState state = piston.getPistonState();
		BlockPos truePos = piston.getPos();
		TileEntity tile = PistonsMoveTileEntitiesModule.getMovement(piston.getWorld(), truePos);
		Vector3d offset = new Vector3d(piston.getOffsetX(pTicks), piston.getOffsetY(pTicks), piston.getOffsetZ(pTicks));
		
		return renderTESafely(piston.getWorld(), truePos, state, tile, piston, pTicks, offset, matrix, bufferIn, combinedLightIn, combinedOverlayIn);
	}
	
	public static boolean renderTESafely(World world, BlockPos truePos, BlockState state, TileEntity tile, TileEntity sourceTE, float pTicks, Vector3d offset, MatrixStack matrix, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		Block block = state.getBlock();
		String id = Objects.toString(block.getRegistryName());
		
		MatrixStack.Entry currEntry = matrix.getLast();
		render: try {
			if(tile == null || (block == Blocks.PISTON_HEAD) || PistonsMoveTileEntitiesModule.renderBlacklist.contains(id))
				break render;
			
			matrix.push();
			TileEntityRenderer<TileEntity> tileentityrenderer = TileEntityRendererDispatcher.instance.getRenderer(tile);
			if(tileentityrenderer != null) {
				tile.setWorldAndPos(sourceTE.getWorld(), sourceTE.getPos());
				tile.validate();

				matrix.translate(offset.x, offset.y, offset.z);

				tile.cachedBlockState = state;
				tileentityrenderer.render(tile, pTicks, matrix, bufferIn, combinedLightIn, combinedOverlayIn);
			}
		} catch(Throwable e) {
			Quark.LOG.warn(id + " can't be rendered for piston TE moving", e);
			PistonsMoveTileEntitiesModule.renderBlacklist.add(id);
			return false;
		} finally {
			while(matrix.getLast() != currEntry)
				matrix.pop();
		}
		
		return state.getRenderType() != BlockRenderType.MODEL;
	}

}
