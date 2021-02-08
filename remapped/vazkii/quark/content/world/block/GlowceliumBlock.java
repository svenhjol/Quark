package vazkii.quark.content.world.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;

public class GlowceliumBlock extends QuarkBlock {

	public GlowceliumBlock(QuarkModule module) {
		super("glowcelium", module, ItemGroup.BUILDING_BLOCKS,
				Block.Properties.of(Material.SOLID_ORGANIC, MaterialColor.LIGHT_BLUE)
						.ticksRandomly()
						.strength(0.5F)
						.luminance(b -> 7)
						.harvestTool(ToolType.SHOVEL)
						.sounds(BlockSoundGroup.GRASS));
	}

	@Override
	@SuppressWarnings("deprecation")
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if(!worldIn.isClient) {
			if(!canExist(state, worldIn, pos))
				worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
			else for(int i = 0; i < 4; ++i) {
				BlockPos blockpos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                if(worldIn.getBlockState(blockpos).getBlock() == Blocks.DIRT && canGrowTo(state, worldIn, blockpos)) 
					worldIn.setBlockState(blockpos, getDefaultState());
			}
		}
	}

	// Some vanilla copypasta from SpreadableSnowyDirtBlock
	
	private static boolean canExist(BlockState state, WorldView world, BlockPos pos) {
		BlockPos blockpos = pos.up();
		BlockState blockstate = world.getBlockState(blockpos);
		int i = ChunkLightProvider.getRealisticOpacity(world, state, pos, blockstate, blockpos, Direction.UP, blockstate.getOpacity(world, blockpos));
		return i < world.getMaxLightLevel();
	}

	private static boolean canGrowTo(BlockState state, WorldView world, BlockPos pos) {
		BlockPos blockpos = pos.up();
		return canExist(state, world, pos) && !world.getFluidState(blockpos).isIn(FluidTags.WATER);
	}

	@Override
	@Environment(EnvType.CLIENT)
	   public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.randomDisplayTick(stateIn, worldIn, pos, rand);

		if(rand.nextInt(40) == 0)
			worldIn.addParticle(ParticleTypes.END_ROD, pos.getX() + rand.nextDouble(), pos.getY() + 1.15, pos.getZ() + rand.nextDouble(), 0, 0, 0);
	}
	
	@Override
	public boolean canSustainPlant(BlockState state, BlockView world, BlockPos pos, Direction facing, IPlantable plantable) {
		return Blocks.MYCELIUM.canSustainPlant(state, world, pos, facing, plantable);
	}

}
