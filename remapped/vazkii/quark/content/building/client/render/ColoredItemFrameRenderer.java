package vazkii.quark.content.building.client.render;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EntityType;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.Quark;
import vazkii.quark.content.building.entity.ColoredItemFrameEntity;

/**
 * @author WireSegal
 * Created at 11:58 AM on 8/25/19.
 */
@Environment(EnvType.CLIENT)
public class ColoredItemFrameRenderer extends EntityRenderer<ColoredItemFrameEntity> {

	private static final Identifier MAP_BACKGROUND_TEXTURES = new Identifier("textures/map/map_background.png");

	private static final Map<DyeColor, ModelIdentifier> LOCATIONS_MODEL = new HashMap<>();
	private static final Map<DyeColor, ModelIdentifier> LOCATIONS_MODEL_MAP = new HashMap<>();

	private final MinecraftClient mc = MinecraftClient.getInstance();
	private final ItemRenderer itemRenderer;
	private final ItemFrameEntityRenderer defaultRenderer;

	public ColoredItemFrameRenderer(EntityRenderDispatcher renderManagerIn, ItemRenderer itemRendererIn) {
		super(renderManagerIn);
		this.itemRenderer = itemRendererIn;
		this.defaultRenderer = (ItemFrameEntityRenderer) renderManagerIn.renderers.get(EntityType.ITEM_FRAME);

		for (DyeColor color : DyeColor.values()) {
			// reinstate when Forge fixes itself
			//            LOCATIONS_MODEL.put(color, new ModelResourceLocation(new ResourceLocation(Quark.MOD_ID, color.getName() + "_frame"), "map=false"));
			//            LOCATIONS_MODEL_MAP.put(color, new ModelResourceLocation(new ResourceLocation(Quark.MOD_ID, color.getName() + "_frame"), "map=true"));

			
			// func_176610_l = name()
			LOCATIONS_MODEL.put(color, new ModelIdentifier(new Identifier(Quark.MOD_ID, color.asString() + "_frame_empty"), "inventory"));
			LOCATIONS_MODEL_MAP.put(color, new ModelIdentifier(new Identifier(Quark.MOD_ID, color.asString() + "_frame_map"), "inventory"));
		}
	}

	@Override
	public void render(ColoredItemFrameEntity p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, VertexConsumerProvider p_225623_5_, int p_225623_6_) {
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
		DyeColor color = p_225623_1_.getColor();
		ModelIdentifier modelresourcelocation = p_225623_1_.getHeldItemStack().getItem() instanceof FilledMapItem ? LOCATIONS_MODEL_MAP.get(color) : LOCATIONS_MODEL.get(color);
		p_225623_4_.push();
		p_225623_4_.translate(-0.5D, -0.5D, -0.5D);
		blockrendererdispatcher.getModelRenderer().render(p_225623_4_.peek(), p_225623_5_.getBuffer(TexturedRenderLayers.getEntitySolid()), (BlockState)null, modelmanager.getModel(modelresourcelocation), 1.0F, 1.0F, 1.0F, p_225623_6_, OverlayTexture.DEFAULT_UV);
		p_225623_4_.pop();
		ItemStack itemstack = p_225623_1_.getHeldItemStack();
		if (!itemstack.isEmpty()) {
			MapState mapdata = FilledMapItem.getOrCreateMapState(itemstack, p_225623_1_.world);
			p_225623_4_.translate(0.0D, 0.0D, 0.4375D);
			int i = mapdata != null ? p_225623_1_.getRotation() % 4 * 2 : p_225623_1_.getRotation();
			p_225623_4_.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion((float)i * 360.0F / 8.0F));
			if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderItemInFrameEvent(p_225623_1_, defaultRenderer, p_225623_4_, p_225623_5_, p_225623_6_))) {
				if (mapdata != null) {
					p_225623_4_.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F));
					float f = 0.0078125F;
					p_225623_4_.scale(0.0078125F, 0.0078125F, 0.0078125F);
					p_225623_4_.translate(-64.0D, -64.0D, 0.0D);
					p_225623_4_.translate(0.0D, 0.0D, -1.0D);
					if (mapdata != null) {
						this.mc.gameRenderer.getMapRenderer().draw(p_225623_4_, p_225623_5_, mapdata, true, p_225623_6_);
					}
				} else {
					p_225623_4_.scale(0.5F, 0.5F, 0.5F);
					this.itemRenderer.renderItem(itemstack, ModelTransformation.Mode.FIXED, p_225623_6_, OverlayTexture.DEFAULT_UV, p_225623_4_, p_225623_5_);
				}
			}
		}

		p_225623_4_.pop();
	}

	@Override
	public Vec3d getRenderOffset(ColoredItemFrameEntity p_225627_1_, float p_225627_2_) {
		return new Vec3d((double)((float)p_225627_1_.getHorizontalFacing().getOffsetX() * 0.3F), -0.25D, (double)((float)p_225627_1_.getHorizontalFacing().getOffsetZ() * 0.3F));
	}

	@Override
	public Identifier getEntityTexture(ColoredItemFrameEntity p_110775_1_) {
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}

	@Override
	protected boolean canRenderName(ColoredItemFrameEntity p_177070_1_) {
		if (MinecraftClient.isHudEnabled() && !p_177070_1_.getHeldItemStack().isEmpty() && p_177070_1_.getHeldItemStack().hasCustomName() && this.dispatcher.targetedEntity == p_177070_1_) {
			double d0 = this.dispatcher.getSquaredDistanceToCamera(p_177070_1_);
			float f = p_177070_1_.isSneaky() ? 32.0F : 64.0F;
			return d0 < (double)(f * f);
		} else {
			return false;
		}
	}
	
	@Override
	protected void renderName(ColoredItemFrameEntity p_225629_1_, Text p_225629_2_, MatrixStack p_225629_3_, VertexConsumerProvider p_225629_4_, int p_225629_5_) {
		super.renderLabelIfPresent(p_225629_1_, p_225629_1_.getHeldItemStack().getName(), p_225629_3_, p_225629_4_, p_225629_5_);
	}
}
