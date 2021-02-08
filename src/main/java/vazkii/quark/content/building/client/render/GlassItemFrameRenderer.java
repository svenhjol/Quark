package vazkii.quark.content.building.client.render;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.common.MinecraftForge;
import vazkii.quark.base.Quark;
import vazkii.quark.content.building.entity.GlassItemFrameEntity;

/**
 * @author WireSegal
 * Created at 11:58 AM on 8/25/19.
 */

@OnlyIn(Dist.CLIENT)
public class GlassItemFrameRenderer extends EntityRenderer<GlassItemFrameEntity> {

	private static final ModelResourceLocation LOCATION_MODEL = new ModelResourceLocation(new ResourceLocation(Quark.MOD_ID, "glass_frame"), "inventory");

	private static final List<Direction> SIGN_DIRECTIONS = Arrays.asList(new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST });
	
	private static BannerTileEntity banner = new BannerTileEntity();
	private final ModelRenderer bannerModel;

	private final Minecraft mc = Minecraft.getInstance();
	private final ItemRenderer itemRenderer;
	private final ItemFrameRenderer defaultRenderer;

	public GlassItemFrameRenderer(EntityRendererManager renderManagerIn, ItemRenderer itemRendererIn) {
		super(renderManagerIn);
		bannerModel = BannerTileEntityRenderer.getModelRender();
		this.itemRenderer = itemRendererIn;
		this.defaultRenderer = (ItemFrameRenderer) renderManagerIn.renderers.get(EntityType.ITEM_FRAME);
	}

	@Override
	public void render(GlassItemFrameEntity p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
		super.render(p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
		p_225623_4_.push();
		Direction direction = p_225623_1_.getHorizontalFacing();
		Vector3d Vector3d = this.getRenderOffset(p_225623_1_, p_225623_3_);
		p_225623_4_.translate(-Vector3d.getX(), -Vector3d.getY(), -Vector3d.getZ());
		p_225623_4_.translate((double)direction.getXOffset() * 0.46875D, (double)direction.getYOffset() * 0.46875D, (double)direction.getZOffset() * 0.46875D);
		p_225623_4_.rotate(Vector3f.XP.rotationDegrees(p_225623_1_.rotationPitch));
		p_225623_4_.rotate(Vector3f.YP.rotationDegrees(180.0F - p_225623_1_.rotationYaw));
		BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
		ModelManager modelmanager = blockrendererdispatcher.getBlockModelShapes().getModelManager();

		ItemStack itemstack = p_225623_1_.getDisplayedItem();

		if (itemstack.isEmpty()) {
			p_225623_4_.push();
			p_225623_4_.translate(-0.5D, -0.5D, -0.5D);
			blockrendererdispatcher.getBlockModelRenderer().renderModelBrightnessColor(p_225623_4_.getLast(), p_225623_5_.getBuffer(Atlases.getCutoutBlockType()), (BlockState)null, modelmanager.getModel(LOCATION_MODEL), 1.0F, 1.0F, 1.0F, p_225623_6_, OverlayTexture.NO_OVERLAY);
			p_225623_4_.pop();
		} else {
			renderItemStack(p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_, itemstack);
		}

		p_225623_4_.pop();
	}

	@Override
	public Vector3d getRenderOffset(GlassItemFrameEntity p_225627_1_, float p_225627_2_) {
		return new Vector3d((double)((float)p_225627_1_.getHorizontalFacing().getXOffset() * 0.3F), -0.25D, (double)((float)p_225627_1_.getHorizontalFacing().getZOffset() * 0.3F));
	}

	@Override
	public ResourceLocation getEntityTexture(GlassItemFrameEntity p_110775_1_) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}

	@Override
	protected boolean canRenderName(GlassItemFrameEntity p_177070_1_) {
		if (Minecraft.isGuiEnabled() && !p_177070_1_.getDisplayedItem().isEmpty() && p_177070_1_.getDisplayedItem().hasDisplayName() && this.renderManager.pointedEntity == p_177070_1_) {
			double d0 = this.renderManager.squareDistanceTo(p_177070_1_);
			float f = p_177070_1_.isDiscrete() ? 32.0F : 64.0F;
			return d0 < (double)(f * f);
		} else {
			return false;
		}
	}

	@Override
	protected void renderName(GlassItemFrameEntity p_225629_1_, ITextComponent p_225629_2_, MatrixStack p_225629_3_, IRenderTypeBuffer p_225629_4_, int p_225629_5_) {
		super.renderName(p_225629_1_, p_225629_1_.getDisplayedItem().getDisplayName(), p_225629_3_, p_225629_4_, p_225629_5_);
	}

	protected void renderItemStack(GlassItemFrameEntity itemFrame, float p_225623_2_, float p_225623_3_, MatrixStack matrix, IRenderTypeBuffer buff, int p_225623_6_, ItemStack stack) {
		if (!stack.isEmpty()) {
			matrix.push();
			MapData mapdata = FilledMapItem.getMapData(stack, itemFrame.world);
			
			sign: if(itemFrame.isOnSign()) {
				BlockPos back = itemFrame.getBehindPos();
				BlockState state = itemFrame.world.getBlockState(back);
				
				Direction ourDirection = itemFrame.getHorizontalFacing().getOpposite();
				
				int signRotation = state.get(StandingSignBlock.ROTATION);
				Direction signDirection = SIGN_DIRECTIONS.get(signRotation / 4);
				if(signRotation % 4 == 0 ? (signDirection != ourDirection) : (signDirection.getOpposite() == ourDirection))
					break sign;
					
				int ourRotation = SIGN_DIRECTIONS.indexOf(ourDirection) * 4;
				int rotation = signRotation - ourRotation;
				float angle = -rotation * 22.5F;

				matrix.translate(0, 0.35, 0.8);
				matrix.scale(0.4F, 0.4F, 0.4F);
				matrix.translate(0, 0, 0.5);
				matrix.rotate(Vector3f.YP.rotationDegrees(angle));
				matrix.translate(0, 0, -0.5);
				matrix.translate(0, 0, -0.085);
			}
			
			int rotation = mapdata != null ? itemFrame.getRotation() % 4 * 2 : itemFrame.getRotation();
			matrix.rotate(Vector3f.ZP.rotationDegrees((float) rotation * 360.0F / 8.0F));
			
			if (!MinecraftForge.EVENT_BUS.post(new RenderItemInFrameEvent(itemFrame, defaultRenderer, matrix, buff, p_225623_6_))) {
				if (mapdata != null) {
					matrix.rotate(Vector3f.ZP.rotationDegrees(180.0F));
					matrix.scale(0.0078125F, 0.0078125F, 0.0078125F);
					matrix.translate(-64.0F, -64.0F, 64F);
					this.mc.gameRenderer.getMapItemRenderer().renderMap(matrix, buff, mapdata, true, p_225623_6_);
				} else {
					float s = 1.5F;
					if (stack.getItem() instanceof BannerItem) {
						banner.loadFromItemStack(stack, ((BannerItem) stack.getItem()).getColor());
						List<Pair<BannerPattern, DyeColor>> patterns = banner.getPatternList();

						matrix.push();
						matrix.translate(0.0001F, -0.5001F, 0.55F);
						matrix.scale(0.799999F, 0.399999F, 0.5F);
						BannerTileEntityRenderer.func_230180_a_(matrix, buff, p_225623_6_, OverlayTexture.NO_OVERLAY, bannerModel, ModelBakery.LOCATION_BANNER_BASE, true, patterns);
						matrix.pop();
					}
					else {
						if (stack.getItem() instanceof ShieldItem) {
							s = 4F;
							matrix.translate(-0.25F, 0F, 0.5F);
							matrix.scale(s, s, s);
						} else {
							matrix.translate(0F, 0F, 0.475F);
							matrix.scale(s, s, s);
						}
						matrix.scale(0.5F, 0.5F, 0.5F);
						this.itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, p_225623_6_, OverlayTexture.NO_OVERLAY, matrix, buff);
					}
				}
			}

			matrix.pop();
		}
	}
}
