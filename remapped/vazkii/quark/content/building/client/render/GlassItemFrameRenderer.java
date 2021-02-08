package vazkii.quark.content.building.client.render;

import java.util.Arrays;
import java.util.List;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BannerItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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

@Environment(EnvType.CLIENT)
public class GlassItemFrameRenderer extends EntityRenderer<GlassItemFrameEntity> {

	private static final ModelIdentifier LOCATION_MODEL = new ModelIdentifier(new Identifier(Quark.MOD_ID, "glass_frame"), "inventory");

	private static final List<Direction> SIGN_DIRECTIONS = Arrays.asList(new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST });
	
	private static BannerBlockEntity banner = new BannerBlockEntity();
	private final ModelPart bannerModel;

	private final MinecraftClient mc = MinecraftClient.getInstance();
	private final ItemRenderer itemRenderer;
	private final ItemFrameEntityRenderer defaultRenderer;

	public GlassItemFrameRenderer(EntityRenderDispatcher renderManagerIn, ItemRenderer itemRendererIn) {
		super(renderManagerIn);
		bannerModel = BannerBlockEntityRenderer.createBanner();
		this.itemRenderer = itemRendererIn;
		this.defaultRenderer = (ItemFrameEntityRenderer) renderManagerIn.renderers.get(EntityType.ITEM_FRAME);
	}

	@Override
	public void render(GlassItemFrameEntity p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, VertexConsumerProvider p_225623_5_, int p_225623_6_) {
		super.render(p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
		p_225623_4_.push();
		Direction direction = p_225623_1_.getHorizontalFacing();
		Vec3d Vector3d = this.getRenderOffset(p_225623_1_, p_225623_3_);
		p_225623_4_.translate(-Vector3d.getX(), -Vector3d.getY(), -Vector3d.getZ());
		p_225623_4_.translate((double)direction.getOffsetX() * 0.46875D, (double)direction.getOffsetY() * 0.46875D, (double)direction.getOffsetZ() * 0.46875D);
		p_225623_4_.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(p_225623_1_.pitch));
		p_225623_4_.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F - p_225623_1_.yaw));
		BlockRenderManager blockrendererdispatcher = this.mc.getBlockRenderManager();
		BakedModelManager modelmanager = blockrendererdispatcher.getModels().getModelManager();

		ItemStack itemstack = p_225623_1_.getHeldItemStack();

		if (itemstack.isEmpty()) {
			p_225623_4_.push();
			p_225623_4_.translate(-0.5D, -0.5D, -0.5D);
			blockrendererdispatcher.getModelRenderer().render(p_225623_4_.peek(), p_225623_5_.getBuffer(TexturedRenderLayers.getEntityCutout()), (BlockState)null, modelmanager.getModel(LOCATION_MODEL), 1.0F, 1.0F, 1.0F, p_225623_6_, OverlayTexture.DEFAULT_UV);
			p_225623_4_.pop();
		} else {
			renderItemStack(p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_, itemstack);
		}

		p_225623_4_.pop();
	}

	@Override
	public Vec3d getRenderOffset(GlassItemFrameEntity p_225627_1_, float p_225627_2_) {
		return new Vec3d((double)((float)p_225627_1_.getHorizontalFacing().getOffsetX() * 0.3F), -0.25D, (double)((float)p_225627_1_.getHorizontalFacing().getOffsetZ() * 0.3F));
	}

	@Override
	public Identifier getEntityTexture(GlassItemFrameEntity p_110775_1_) {
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}

	@Override
	protected boolean canRenderName(GlassItemFrameEntity p_177070_1_) {
		if (MinecraftClient.isHudEnabled() && !p_177070_1_.getHeldItemStack().isEmpty() && p_177070_1_.getHeldItemStack().hasCustomName() && this.dispatcher.targetedEntity == p_177070_1_) {
			double d0 = this.dispatcher.getSquaredDistanceToCamera(p_177070_1_);
			float f = p_177070_1_.isSneaky() ? 32.0F : 64.0F;
			return d0 < (double)(f * f);
		} else {
			return false;
		}
	}

	@Override
	protected void renderName(GlassItemFrameEntity p_225629_1_, Text p_225629_2_, MatrixStack p_225629_3_, VertexConsumerProvider p_225629_4_, int p_225629_5_) {
		super.renderLabelIfPresent(p_225629_1_, p_225629_1_.getHeldItemStack().getName(), p_225629_3_, p_225629_4_, p_225629_5_);
	}

	protected void renderItemStack(GlassItemFrameEntity itemFrame, float p_225623_2_, float p_225623_3_, MatrixStack matrix, VertexConsumerProvider buff, int p_225623_6_, ItemStack stack) {
		if (!stack.isEmpty()) {
			matrix.push();
			MapState mapdata = FilledMapItem.getOrCreateMapState(stack, itemFrame.world);
			
			sign: if(itemFrame.isOnSign()) {
				BlockPos back = itemFrame.getBehindPos();
				BlockState state = itemFrame.world.getBlockState(back);
				
				Direction ourDirection = itemFrame.getHorizontalFacing().getOpposite();
				
				int signRotation = state.get(SignBlock.ROTATION);
				Direction signDirection = SIGN_DIRECTIONS.get(signRotation / 4);
				if(signRotation % 4 == 0 ? (signDirection != ourDirection) : (signDirection.getOpposite() == ourDirection))
					break sign;
					
				int ourRotation = SIGN_DIRECTIONS.indexOf(ourDirection) * 4;
				int rotation = signRotation - ourRotation;
				float angle = -rotation * 22.5F;

				matrix.translate(0, 0.35, 0.8);
				matrix.scale(0.4F, 0.4F, 0.4F);
				matrix.translate(0, 0, 0.5);
				matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(angle));
				matrix.translate(0, 0, -0.5);
				matrix.translate(0, 0, -0.085);
			}
			
			int rotation = mapdata != null ? itemFrame.getRotation() % 4 * 2 : itemFrame.getRotation();
			matrix.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion((float) rotation * 360.0F / 8.0F));
			
			if (!MinecraftForge.EVENT_BUS.post(new RenderItemInFrameEvent(itemFrame, defaultRenderer, matrix, buff, p_225623_6_))) {
				if (mapdata != null) {
					matrix.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
					matrix.scale(0.0078125F, 0.0078125F, 0.0078125F);
					matrix.translate(-64.0F, -64.0F, 64F);
					this.mc.gameRenderer.getMapRenderer().draw(matrix, buff, mapdata, true, p_225623_6_);
				} else {
					float s = 1.5F;
					if (stack.getItem() instanceof BannerItem) {
						banner.readFrom(stack, ((BannerItem) stack.getItem()).getColor());
						List<Pair<BannerPattern, DyeColor>> patterns = banner.getPatterns();

						matrix.push();
						matrix.translate(0.0001F, -0.5001F, 0.55F);
						matrix.scale(0.799999F, 0.399999F, 0.5F);
						BannerBlockEntityRenderer.method_29999(matrix, buff, p_225623_6_, OverlayTexture.DEFAULT_UV, bannerModel, ModelLoader.BANNER_BASE, true, patterns);
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
						this.itemRenderer.renderItem(stack, ModelTransformation.Mode.FIXED, p_225623_6_, OverlayTexture.DEFAULT_UV, matrix, buff);
					}
				}
			}

			matrix.pop();
		}
	}
}
