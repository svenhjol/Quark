package vazkii.quark.addons.oddities.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import vazkii.quark.addons.oddities.tile.MatrixEnchantingTableTileEntity;

public class MatrixEnchantingTableTileEntityRenderer extends TileEntityRenderer<MatrixEnchantingTableTileEntity> {

	public MatrixEnchantingTableTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public void render(MatrixEnchantingTableTileEntity te, float pticks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
		float time = te.tickCount + pticks;

		float f1 = te.bookRotation - te.bookRotationPrev;
		while (f1 >= Math.PI)
			f1 -= (Math.PI * 2F);
		while (f1 < -Math.PI)
			f1 += (Math.PI * 2F);

		float rot = te.bookRotationPrev + f1 * pticks;
		float bookOpen = te.bookSpreadPrev + (te.bookSpread - te.bookSpreadPrev) * pticks;

		renderBook(te, time, rot, pticks, matrix, buffer, light, overlay);

		ItemStack item = te.getStackInSlot(0);
		if(!item.isEmpty())
			renderItem(item, time, bookOpen, rot, matrix, buffer, light, overlay);
	}

	private void renderItem(ItemStack item, float time, float bookOpen, float rot, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
		matrix.push();
		matrix.translate(0.5F, 0.8F, 0.5F);
		matrix.scale(0.6F, 0.6F, 0.6F);

		rot *= -180F / (float) Math.PI;
		rot -= 90F;
		rot *= bookOpen;

		matrix.rotate(Vector3f.YP.rotationDegrees(rot));
		matrix.translate(0, bookOpen * 1.4F, Math.sin(bookOpen * Math.PI));
		matrix.rotate(Vector3f.XP.rotationDegrees(-90F * (bookOpen - 1F)));

		float trans = (float) Math.sin(time * 0.06) * bookOpen * 0.2F;
		matrix.translate(0F, trans, 0F);

		ItemRenderer render = Minecraft.getInstance().getItemRenderer();
		render.renderItem(item, ItemCameraTransforms.TransformType.FIXED, light, overlay, matrix, buffer);
		matrix.pop();
	}

	public static final RenderMaterial TEXTURE_BOOK = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation("entity/enchanting_table_book"));
	private final BookModel modelBook = new BookModel();

	// Copy of vanilla's book render
	private void renderBook(MatrixEnchantingTableTileEntity tileEntityIn, float time, float bookRot, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
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
		matrixStackIn.rotate(Vector3f.YP.rotation(-f2));
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(80.0F));
		float f3 = MathHelper.lerp(partialTicks, tileEntityIn.pageFlipPrev, tileEntityIn.pageFlip);
		float f4 = MathHelper.frac(f3 + 0.25F) * 1.6F - 0.3F;
		float f5 = MathHelper.frac(f3 + 0.75F) * 1.6F - 0.3F;
		float f6 = MathHelper.lerp(partialTicks, tileEntityIn.bookSpreadPrev, tileEntityIn.bookSpread);
		this.modelBook.setBookState(f, MathHelper.clamp(f4, 0.0F, 1.0F), MathHelper.clamp(f5, 0.0F, 1.0F), f6);
		IVertexBuilder ivertexbuilder = TEXTURE_BOOK.getBuffer(bufferIn, RenderType::getEntitySolid);
		this.modelBook.render(matrixStackIn, ivertexbuilder, combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.pop();
	}

}
