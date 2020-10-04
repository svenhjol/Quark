package vazkii.quark.oddities.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import vazkii.quark.oddities.tile.MatrixEnchantingTableTileEntity;

public class MatrixEnchantingTableTileEntityRenderer extends BlockEntityRenderer<MatrixEnchantingTableTileEntity> {

	public MatrixEnchantingTableTileEntityRenderer(BlockEntityRenderDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public void render(MatrixEnchantingTableTileEntity te, float pticks, MatrixStack matrix, VertexConsumerProvider buffer, int light, int overlay) {
		float time = te.tickCount + pticks;

		float f1 = te.bookRotation - te.bookRotationPrev;
		while (f1 >= Math.PI)
			f1 -= (Math.PI * 2F);
		while (f1 < -Math.PI)
			f1 += (Math.PI * 2F);

		float rot = te.bookRotationPrev + f1 * pticks;
		float bookOpen = te.bookSpreadPrev + (te.bookSpread - te.bookSpreadPrev) * pticks;

		renderBook(te, time, rot, pticks, matrix, buffer, light, overlay);

		ItemStack item = te.getStack(0);
		if(!item.isEmpty())
			renderItem(item, time, bookOpen, rot, matrix, buffer, light, overlay);
	}

	private void renderItem(ItemStack item, float time, float bookOpen, float rot, MatrixStack matrix, VertexConsumerProvider buffer, int light, int overlay) {
		matrix.push();
		matrix.translate(0.5F, 0.8F, 0.5F);
		matrix.scale(0.6F, 0.6F, 0.6F);

		rot *= -180F / (float) Math.PI;
		rot -= 90F;
		rot *= bookOpen;

		matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(rot));
		matrix.translate(0, bookOpen * 1.4F, Math.sin(bookOpen * Math.PI));
		matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90F * (bookOpen - 1F)));

		float trans = (float) Math.sin(time * 0.06) * bookOpen * 0.2F;
		matrix.translate(0F, trans, 0F);

		ItemRenderer render = MinecraftClient.getInstance().getItemRenderer();
		render.renderItem(item, ModelTransformation.Mode.FIXED, light, overlay, matrix, buffer);
		matrix.pop();
	}

	public static final SpriteIdentifier TEXTURE_BOOK = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new Identifier("entity/enchanting_table_book"));
	private final BookModel modelBook = new BookModel();

	// Copy of vanilla's book render
	private void renderBook(MatrixEnchantingTableTileEntity tileEntityIn, float time, float bookRot, float partialTicks, MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn) {
		matrixStackIn.push();
		matrixStackIn.translate(0.5D, 0.75D, 0.5D);
		float f = (float) tileEntityIn.tickCount + partialTicks;
		matrixStackIn.translate(0.0D, (double)(0.1F + MathHelper.sin(f * 0.1F) * 0.01F), 0.0D);

		float f1;
		for(f1 = tileEntityIn.bookRotation - tileEntityIn.bookRotationPrev; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F)) {
			;
		}

		while(f1 < -(float)Math.PI) {
			f1 += ((float)Math.PI * 2F);
		}

		float f2 = tileEntityIn.bookRotationPrev + f1 * partialTicks;
		matrixStackIn.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(-f2));
		matrixStackIn.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(80.0F));
		float f3 = MathHelper.lerp(partialTicks, tileEntityIn.pageFlipPrev, tileEntityIn.pageFlip);
		float f4 = MathHelper.fractionalPart(f3 + 0.25F) * 1.6F - 0.3F;
		float f5 = MathHelper.fractionalPart(f3 + 0.75F) * 1.6F - 0.3F;
		float f6 = MathHelper.lerp(partialTicks, tileEntityIn.bookSpreadPrev, tileEntityIn.bookSpread);
		this.modelBook.setPageAngles(f, MathHelper.clamp(f4, 0.0F, 1.0F), MathHelper.clamp(f5, 0.0F, 1.0F), f6);
		VertexConsumer ivertexbuilder = TEXTURE_BOOK.getVertexConsumer(bufferIn, RenderLayer::getEntitySolid);
		this.modelBook.method_24184(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.pop();
	}

}
