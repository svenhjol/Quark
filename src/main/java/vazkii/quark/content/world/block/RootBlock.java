package vazkii.quark.content.world.block;

import java.util.Random;
import java.util.function.BooleanSupplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import vazkii.quark.base.block.QuarkVineBlock;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.QuarkModule;

public class RootBlock extends QuarkVineBlock implements IGrowable {

	public RootBlock(QuarkModule module) {
		super(module, "root", true);
	}
	
	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		return true;
	}
	
	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
		return false;
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if(!worldIn.isRemote && worldIn.rand.nextInt(2) == 0)
			grow(worldIn, random, pos, state);
	}
	
	public static void growMany(IWorld world, Random rand, BlockPos pos, BlockState state, float stopChance) {
		BlockPos next = pos;
		
		do {
			next = growAndReturnLastPos(world, next, state);
		} while(next != null && rand.nextFloat() >= stopChance);
	}

	public static BlockPos growAndReturnLastPos(IWorld world, BlockPos pos, BlockState state) {
		BlockPos down = pos.down();
		
		for(Direction facing : MiscUtil.HORIZONTALS) {
			BooleanProperty prop = getPropertyFor(facing);
			if(state.get(prop)) {
				BlockPos ret = growInFacing(world, down, facing);
				if(ret != null) {
					BlockState setState = state.getBlock().getDefaultState().with(prop, true);
					world.setBlockState(ret, setState, 2);
					return ret;
				}
				
				break;
			}
		}
		
		return null;
	}
	
	public static BlockPos growInFacing(IWorld world, BlockPos pos, Direction facing) {
		if(!world.isAirBlock(pos))
			return null;
		
		BlockPos check = pos.offset(facing);
		if(isAcceptableNeighbor(world, check, facing.getOpposite()))
			return pos;
		
		pos = check;
		if(!world.isAirBlock(check))
			return null;
		
		check = pos.offset(facing);
		if(isAcceptableNeighbor(world, check, facing.getOpposite()))
			return pos;
		
		return null;
	}

	public static boolean isAcceptableNeighbor(IWorld world, BlockPos pos, Direction side) {
		BlockState iblockstate = world.getBlockState(pos);
		return Block.doesSideFillSquare(iblockstate.getCollisionShape(world, pos), side) && iblockstate.getMaterial() == Material.ROCK;
	}

	@Override
	public boolean canGrow(IBlockReader world, BlockPos pos, BlockState state, boolean client) {
		return world.getLightValue(pos) < 7;
	}

	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, BlockState state) {
		return rand.nextFloat() < 0.4;
	}
	
	@Override
	public void grow(ServerWorld world, Random rand, BlockPos pos, BlockState state) {
		growAndReturnLastPos(world, pos, state);
	}
	
}
