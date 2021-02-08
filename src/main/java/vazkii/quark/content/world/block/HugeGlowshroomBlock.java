package vazkii.quark.content.world.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HugeMushroomBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.world.module.underground.GlowshroomUndergroundBiomeModule;

public class HugeGlowshroomBlock extends HugeMushroomBlock {

	private final QuarkModule module;

	public HugeGlowshroomBlock(String name, QuarkModule module) {
		super(Block.Properties.from(Blocks.RED_MUSHROOM_BLOCK)
				.setLightLevel(b -> 14)
				.tickRandomly()
				.notSolid());

		this.module = module;
		RegistryHelper.registerBlock(this, name);
		RegistryHelper.setCreativeTab(this, ItemGroup.DECORATIONS);
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.TRANSLUCENT);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.animateTick(stateIn, worldIn, pos, rand);

		if(rand.nextInt(10) == 0)
			worldIn.addParticle(ParticleTypes.END_ROD, pos.getX() + rand.nextDouble(), pos.getY() + rand.nextDouble(), pos.getZ() + rand.nextDouble(), 0, 0, 0);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("deprecation")
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() == this || super.isSideInvisible(state, adjacentBlockState, side);
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}
	
//	@Override
//	@SuppressWarnings("deprecation")
//	public boolean isNormalCube(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
//		return false;
//	}

	// Vanilla copy paste, touch only if you dare
	public static boolean place(World worldIn, Random rand, BlockPos pos) {
		int i = rand.nextInt(3) + 4;
		if (rand.nextInt(12) == 0) {
			i *= 2;
		}

		int j = pos.getY();
		if (j >= 1 && j + i + 1 < 256) {
			Block block = worldIn.getBlockState(pos.down()).getBlock();
			if (block != GlowshroomUndergroundBiomeModule.glowcelium) {
				return false;
			} else {
				BlockPos.Mutable blockpos$mutableblockpos = new BlockPos.Mutable();

				for(int k = 0; k <= i; ++k) {
					int l = 0;
					if (k < i && k >= i - 3) {
						l = 2;
					} else if (k == i) {
						l = 1;
					}

					for(int i1 = -l; i1 <= l; ++i1) {
						for(int j1 = -l; j1 <= l; ++j1) {
							BlockState blockstate = worldIn.getBlockState(blockpos$mutableblockpos.setPos(pos).move(i1, k, j1));
							if (!blockstate.isAir(worldIn, blockpos$mutableblockpos) && !blockstate.isIn(BlockTags.LEAVES)) { // isIn
								return false;
							}
						}
					}
				}

				BlockState blockstate1 = GlowshroomUndergroundBiomeModule.glowshroom_block.getDefaultState().with(HugeMushroomBlock.DOWN, Boolean.valueOf(false));

				for(int l1 = i - 3; l1 <= i; ++l1) {
					int i2 = l1 < i ? 2 : 1;

					for(int l2 = -i2; l2 <= i2; ++l2) {
						for(int k1 = -i2; k1 <= i2; ++k1) {
							boolean flag = l2 == -i2;
							boolean flag1 = l2 == i2;
							boolean flag2 = k1 == -i2;
							boolean flag3 = k1 == i2;
							boolean flag4 = flag || flag1;
							boolean flag5 = flag2 || flag3;
							if (l1 >= i || flag4 != flag5) {
								blockpos$mutableblockpos.setPos(pos).move(l2, l1, k1);
								if (worldIn.getBlockState(blockpos$mutableblockpos).canBeReplacedByLeaves(worldIn, blockpos$mutableblockpos)) {
									worldIn.setBlockState(blockpos$mutableblockpos, blockstate1.with(HugeMushroomBlock.UP, Boolean.valueOf(l1 >= i - 1)).with(HugeMushroomBlock.WEST, Boolean.valueOf(l2 < 0)).with(HugeMushroomBlock.EAST, Boolean.valueOf(l2 > 0)).with(HugeMushroomBlock.NORTH, Boolean.valueOf(k1 < 0)).with(HugeMushroomBlock.SOUTH, Boolean.valueOf(k1 > 0)), 2);
								}
							}
						}
					}
				}

				BlockState blockstate2 = GlowshroomUndergroundBiomeModule.glowshroom_stem.getDefaultState().with(HugeMushroomBlock.UP, Boolean.valueOf(false)).with(HugeMushroomBlock.DOWN, Boolean.valueOf(false));

				for(int j2 = 0; j2 < i; ++j2) {
					blockpos$mutableblockpos.setPos(pos).move(Direction.UP, j2);
					if (worldIn.getBlockState(blockpos$mutableblockpos).canBeReplacedByLeaves(worldIn, blockpos$mutableblockpos)) {
						worldIn.setBlockState(blockpos$mutableblockpos, blockstate2, 3);
					}
				}

				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if(isEnabled() || group == ItemGroup.SEARCH)
			super.fillItemGroup(group, items);
	}

	public boolean isEnabled() {
		return module != null && module.enabled;
	}

}
