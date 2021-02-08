package vazkii.quark.content.tools.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import vazkii.arl.util.ClientTicker;
import vazkii.quark.content.tools.tile.CloudTileEntity;

public class CloudTileEntityRenderer extends TileEntityRenderer<CloudTileEntity> {

	public CloudTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public void render(CloudTileEntity te, float pticks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		
		float scale = ((float) (te.liveTime - pticks + Math.sin(ClientTicker.total * 0.2F) * -10F) / 200F) * 0.6F;
		
		if(scale > 0) {
			matrix.translate(0.5, 0.5, 0.5);
			matrix.scale(scale, scale, scale);
			mc.getItemRenderer().renderItem(new ItemStack(Blocks.WHITE_CONCRETE), TransformType.NONE, 240, OverlayTexture.NO_OVERLAY, matrix, buffer);
		}
	}

}
