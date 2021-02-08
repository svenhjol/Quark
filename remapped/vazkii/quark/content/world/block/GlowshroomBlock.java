package vazkii.quark.content.world.block;

import java.util.Random;
import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.MushroomPlantBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.world.module.underground.GlowshroomUndergroundBiomeModule;

public class GlowshroomBlock extends MushroomPlantBlock implements IQuarkBlock {

	private final QuarkModule module;
	private BooleanSupplier enabledSupplier = () -> true;

	public GlowshroomBlock(QuarkModule module) {
		super(AbstractBlock.Settings.of(Material.PLANT, MaterialColor.CYAN)
				.noCollision()
				.ticksRandomly()
				.breakInstantly()
				.sounds(BlockSoundGroup.GRASS)
				.postProcess((s, r, p) -> true)
				.luminance(b -> 14)
				.ticksRandomly());
		
		this.module = module;
		RegistryHelper.registerBlock(this, "glowshroom");
		RegistryHelper.setCreativeTab(this, ItemGroup.DECORATIONS);
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}
	
	@Override
	public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
		BlockPos blockpos = pos.down();
		return worldIn.getBlockState(blockpos).getBlock() == GlowshroomUndergroundBiomeModule.glowcelium;
	}
	
	@Override
	public boolean canSustainPlant(@Nonnull BlockState state, @Nonnull BlockView world, BlockPos pos, @Nonnull Direction facing, IPlantable plantable) {
		return state.getBlock() == GlowshroomUndergroundBiomeModule.glowcelium;
	}
	
	@Override
	public void scheduledTick(@Nonnull BlockState state, @Nonnull ServerWorld worldIn, @Nonnull BlockPos pos, Random rand) {
		if(rand.nextInt(GlowshroomUndergroundBiomeModule.glowshroomGrowthRate) == 0) {
			int i = 5;

			for(BlockPos targetPos : BlockPos.iterate(pos.add(-4, -1, -4), pos.add(4, 1, 4))) {
				if(worldIn.getBlockState(targetPos).getBlock() == this) {
					--i;

					if(i <= 0)
						return;
				}
			}

			BlockPos shiftedPos = pos.add(rand.nextInt(3) - 1, rand.nextInt(2) - rand.nextInt(2), rand.nextInt(3) - 1);

			for(int k = 0; k < 4; ++k) {
				if (worldIn.isAir(shiftedPos) && state.canPlaceAt(worldIn, shiftedPos))
					pos = shiftedPos;

				shiftedPos = pos.add(rand.nextInt(3) - 1, rand.nextInt(2) - rand.nextInt(2), rand.nextInt(3) - 1);
			}

			if(worldIn.isAir(shiftedPos) && state.canPlaceAt(worldIn, shiftedPos))
				worldIn.setBlockState(shiftedPos, getDefaultState(), 2);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.randomDisplayTick(stateIn, worldIn, pos, rand);

		if(rand.nextInt(20) == 0)
			worldIn.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.2 + rand.nextDouble() * 0.6, pos.getY() + 0.3, pos.getZ() + 0.2 + rand.nextDouble() * 0.6, 0, 0, 0);
	}

	@Override
	public boolean isFertilizable(@Nonnull BlockView worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean isClient) {
		return GlowshroomUndergroundBiomeModule.enableHugeGlowshrooms;
	}

	@Override
	public boolean canGrow(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		return GlowshroomUndergroundBiomeModule.enableHugeGlowshrooms && rand.nextFloat() < 0.4;
	}

	@Override
	public void grow(@Nonnull ServerWorld worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		if(GlowshroomUndergroundBiomeModule.enableHugeGlowshrooms) {
			worldIn.removeBlock(pos, false);
			if(!HugeGlowshroomBlock.place(worldIn, rand, pos))
				worldIn.setBlockState(pos, getDefaultState());
		}
	}

	@Override
	public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.addStacksForDisplay(group, items);
	}


	@Override
	public GlowshroomBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

	@Nullable
	@Override
	public QuarkModule getModule() {
		return module;
	}
}
