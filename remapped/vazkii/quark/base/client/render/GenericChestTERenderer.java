package vazkii.quark.base.client.render;

import java.util.Calendar;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.block.ChestAnimationProgress;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

// A copy of ChestTileEntityRenderer from vanilla but less private
public abstract class GenericChestTERenderer<T extends BlockEntity & ChestAnimationProgress> extends BlockEntityRenderer<T> {
	
	public final ModelPart field_228862_a_;
	public final ModelPart field_228863_c_;
	public final ModelPart field_228864_d_;
	public final ModelPart field_228865_e_;
	public final ModelPart field_228866_f_;
	public final ModelPart field_228867_g_;
	public final ModelPart field_228868_h_;
	public final ModelPart field_228869_i_;
	public final ModelPart field_228870_j_;
	public boolean isChristmas;

	public GenericChestTERenderer(BlockEntityRenderDispatcher p_i226008_1_) {
		super(p_i226008_1_);
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
			this.isChristmas = true;
		}

		this.field_228863_c_ = new ModelPart(64, 64, 0, 19);
		this.field_228863_c_.addCuboid(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);
		this.field_228862_a_ = new ModelPart(64, 64, 0, 0);
		this.field_228862_a_.addCuboid(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
		this.field_228862_a_.pivotY = 9.0F;
		this.field_228862_a_.pivotZ = 1.0F;
		this.field_228864_d_ = new ModelPart(64, 64, 0, 0);
		this.field_228864_d_.addCuboid(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
		this.field_228864_d_.pivotY = 8.0F;
		this.field_228866_f_ = new ModelPart(64, 64, 0, 19);
		this.field_228866_f_.addCuboid(1.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F, 0.0F);
		this.field_228865_e_ = new ModelPart(64, 64, 0, 0);
		this.field_228865_e_.addCuboid(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F, 0.0F);
		this.field_228865_e_.pivotY = 9.0F;
		this.field_228865_e_.pivotZ = 1.0F;
		this.field_228867_g_ = new ModelPart(64, 64, 0, 0);
		this.field_228867_g_.addCuboid(15.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F, 0.0F);
		this.field_228867_g_.pivotY = 8.0F;
		this.field_228869_i_ = new ModelPart(64, 64, 0, 19);
		this.field_228869_i_.addCuboid(0.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F, 0.0F);
		this.field_228868_h_ = new ModelPart(64, 64, 0, 0);
		this.field_228868_h_.addCuboid(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F, 0.0F);
		this.field_228868_h_.pivotY = 9.0F;
		this.field_228868_h_.pivotZ = 1.0F;
		this.field_228870_j_ = new ModelPart(64, 64, 0, 0);
		this.field_228870_j_.addCuboid(0.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F, 0.0F);
		this.field_228870_j_.pivotY = 8.0F;
	}

	public void render(T p_225616_1_, float p_225616_2_, MatrixStack p_225616_3_, VertexConsumerProvider p_225616_4_, int p_225616_5_, int p_225616_6_) {
		World world = p_225616_1_.getWorld();
		boolean flag = world != null;
		BlockState blockstate = flag ? p_225616_1_.getCachedState() : Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);
		ChestType chesttype = blockstate.getEntries().containsKey(ChestBlock.CHEST_TYPE) ? blockstate.get(ChestBlock.CHEST_TYPE) : ChestType.SINGLE;
		Block block = blockstate.getBlock();
		if (block instanceof AbstractChestBlock) {
			AbstractChestBlock<?> abstractchestblock = (AbstractChestBlock) block;
			boolean flag1 = chesttype != ChestType.SINGLE;
			p_225616_3_.push();
			float f = blockstate.get(ChestBlock.FACING).asRotation();
			p_225616_3_.translate(0.5D, 0.5D, 0.5D);
			p_225616_3_.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-f));
			p_225616_3_.translate(-0.5D, -0.5D, -0.5D);
			DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> icallbackwrapper;
			if (flag) {
				icallbackwrapper = abstractchestblock.getBlockEntitySource(blockstate, world, p_225616_1_.getPos(), true);
			} else {
				icallbackwrapper = DoubleBlockProperties.PropertyRetriever::getFallback; // getFallback
			}
			
			// getAnimationProgressRetreiver
			float f1 = icallbackwrapper.apply(ChestBlock.getAnimationProgressRetriever((ChestAnimationProgress)p_225616_1_)).get(p_225616_2_);
			f1 = 1.0F - f1;
			f1 = 1.0F - f1 * f1 * f1;
			int i = icallbackwrapper.apply(new LightmapCoordinatesRetriever<>()).applyAsInt(p_225616_5_);
			SpriteIdentifier material = getMaterialFinal(p_225616_1_, chesttype); // <- Changed here
			if(material != null) {
				VertexConsumer ivertexbuilder = material.getVertexConsumer(p_225616_4_, RenderLayer::getEntityCutout);
				if (flag1) {
					if (chesttype == ChestType.LEFT) {
						this.func_228871_a_(p_225616_3_, ivertexbuilder, this.field_228868_h_, this.field_228870_j_, this.field_228869_i_, f1, i, p_225616_6_);
					} else {
						this.func_228871_a_(p_225616_3_, ivertexbuilder, this.field_228865_e_, this.field_228867_g_, this.field_228866_f_, f1, i, p_225616_6_);
					}
				} else {
					this.func_228871_a_(p_225616_3_, ivertexbuilder, this.field_228862_a_, this.field_228864_d_, this.field_228863_c_, f1, i, p_225616_6_);
				}
			}

			p_225616_3_.pop();
		}
	}
	
	public final SpriteIdentifier getMaterialFinal(T t, ChestType type) {
		if(isChristmas)
			return TexturedRenderLayers.getChestTexture(t, type, this.isChristmas);

		return getMaterial(t, type);
	}
	
	public abstract SpriteIdentifier getMaterial(T t, ChestType type);

	public void func_228871_a_(MatrixStack p_228871_1_, VertexConsumer p_228871_2_, ModelPart p_228871_3_, ModelPart p_228871_4_, ModelPart p_228871_5_, float p_228871_6_, int p_228871_7_, int p_228871_8_) {
		p_228871_3_.pitch = -(p_228871_6_ * ((float)Math.PI / 2F));
		p_228871_4_.pitch = p_228871_3_.pitch;
		p_228871_3_.render(p_228871_1_, p_228871_2_, p_228871_7_, p_228871_8_);
		p_228871_4_.render(p_228871_1_, p_228871_2_, p_228871_7_, p_228871_8_);
		p_228871_5_.render(p_228871_1_, p_228871_2_, p_228871_7_, p_228871_8_);
	}
}
