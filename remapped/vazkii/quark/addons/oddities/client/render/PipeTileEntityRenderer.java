package vazkii.quark.addons.oddities.client.render;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import vazkii.quark.addons.oddities.tile.PipeTileEntity;
import vazkii.quark.addons.oddities.tile.PipeTileEntity.ConnectionType;
import vazkii.quark.addons.oddities.tile.PipeTileEntity.PipeItem;
import vazkii.quark.base.Quark;

public class PipeTileEntityRenderer extends BlockEntityRenderer<PipeTileEntity> {

	private static final ModelIdentifier LOCATION_MODEL = new ModelIdentifier(new Identifier(Quark.MOD_ID, "pipe_flare"), "inventory");
	
	private Random random = new Random();
	
	public PipeTileEntityRenderer(BlockEntityRenderDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public void render(PipeTileEntity te, float pticks, MatrixStack matrix, VertexConsumerProvider buffer, int light, int overlay) {
		matrix.push();
		matrix.translate(0.5, 0.5, 0.5);
		ItemRenderer render = MinecraftClient.getInstance().getItemRenderer();
		Iterator<PipeItem> items = te.getItemIterator();

		while(items.hasNext())
			renderItem(items.next(), render, matrix, buffer, pticks, light, overlay);
		
		BlockRenderManager blockrendererdispatcher = MinecraftClient.getInstance().getBlockRenderManager();
		BakedModelManager modelmanager = blockrendererdispatcher.getModels().getModelManager();
		BakedModel model = modelmanager.getModel(LOCATION_MODEL);
		for(Direction d : Direction.values())
			renderFlare(te, blockrendererdispatcher, model, matrix, buffer, pticks, light, overlay, d);
		
		matrix.pop();
	}
	
	@SuppressWarnings("deprecation")
	private void renderFlare(PipeTileEntity te, BlockRenderManager disp, BakedModel model, MatrixStack matrix, VertexConsumerProvider buffer, float partial, int light, int overlay, Direction dir) {
		ConnectionType type = PipeTileEntity.getConnectionTo(te.getWorld(), te.getPos(), dir);
		if(type.isFlared) {
			matrix.push();
			switch(dir.getAxis()) {
			case X:
				matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-dir.asRotation()));
				break;
			case Z:
				matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(dir.asRotation()));
				break;
			case Y:
				matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90F));
				if(dir == Direction.UP)
					matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180F));
				
				break;
			}
			
			matrix.translate(-0.5, -0.5, type.flareShift);
			disp.getModelRenderer().render(matrix.peek(), buffer.getBuffer(TexturedRenderLayers.getEntityCutout()), null, model, 1.0F, 1.0F, 1.0F, light, OverlayTexture.DEFAULT_UV);
			matrix.pop();
		}
	}
	
	private void renderItem(PipeItem item, ItemRenderer render, MatrixStack matrix, VertexConsumerProvider buffer, float partial, int light, int overlay) {
		matrix.push();

		float scale = 0.4F;
		float fract = item.getTimeFract(partial);
		float shiftFract = fract - 0.5F;
		Direction face = item.outgoingFace;
		if(fract < 0.5)
			face = item.incomingFace.getOpposite();

		float offX = (face.getOffsetX() * 1F);
		float offY = (face.getOffsetY() * 1F);
		float offZ = (face.getOffsetZ() * 1F);
		matrix.translate(offX * shiftFract, offY * shiftFract, offZ * shiftFract);

		matrix.scale(scale, scale, scale);

		float speed = 4F;
		matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((item.timeInWorld + partial) * speed));

        int seed = item.stack.isEmpty() ? 187 : Item.getRawId(item.stack.getItem());
        random.setSeed(seed);
		
		int count = getModelCount(item.stack);
		for(int i = 0; i < count; i++) {
			matrix.push();
			if(i > 0) {
				float spread = 0.15F;
                float x = (this.random.nextFloat() * 2.0F - 1.0F) * spread;
                float y = (this.random.nextFloat() * 2.0F - 1.0F) * spread;
                float z = (this.random.nextFloat() * 2.0F - 1.0F) * spread;
                matrix.translate(x, y, z);
			}
			
			render.renderItem(item.stack, ModelTransformation.Mode.FIXED, light, overlay, matrix, buffer);
			matrix.pop();
		}
		matrix.pop();
	}

	// RenderEntityItem copy
	protected int getModelCount(ItemStack stack) {
		if(stack.getCount() > 48)
			return 5;
		
		if(stack.getCount() > 32)
			return 4;
		
		if(stack.getCount() > 16)
			return 3;
		
		if (stack.getCount() > 1)
			return 2;

		return 1;
	}
	
}
