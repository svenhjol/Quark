package vazkii.quark.content.tools.client.render;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import vazkii.arl.util.ClientTicker;
import vazkii.quark.content.tools.tile.CloudTileEntity;

public class CloudTileEntityRenderer extends BlockEntityRenderer<CloudTileEntity> {

	public CloudTileEntityRenderer(BlockEntityRenderDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public void render(CloudTileEntity te, float pticks, MatrixStack matrix, VertexConsumerProvider buffer, int light, int overlay) {
		MinecraftClient mc = MinecraftClient.getInstance();
		
		float scale = ((float) (te.liveTime - pticks + Math.sin(ClientTicker.total * 0.2F) * -10F) / 200F) * 0.6F;
		
		if(scale > 0) {
			matrix.translate(0.5, 0.5, 0.5);
			matrix.scale(scale, scale, scale);
			mc.getItemRenderer().renderItem(new ItemStack(Blocks.WHITE_CONCRETE), Mode.NONE, 240, OverlayTexture.DEFAULT_UV, matrix, buffer);
		}
	}

}
