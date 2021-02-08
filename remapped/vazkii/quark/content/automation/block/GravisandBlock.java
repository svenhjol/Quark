package vazkii.quark.content.automation.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.automation.entity.GravisandEntity;

public class GravisandBlock extends QuarkBlock {

	public GravisandBlock(String regname, QuarkModule module, ItemGroup creativeTab, Settings properties) {
		super(regname, module, creativeTab, properties);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
		checkRedstone(world, pos);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		checkRedstone(worldIn, pos);
	}

	private void checkRedstone(World worldIn, BlockPos pos) {
        boolean powered = worldIn.isReceivingRedstonePower(pos);

        if(powered)
        	worldIn.getBlockTickScheduler().schedule(pos, this, 2);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public int getComparatorOutput(BlockState blockState, World worldIn, BlockPos pos) {
		return 15;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		if(!worldIn.isClient) {
			if(checkFallable(worldIn, pos))
				for(Direction face : Direction.values()) {
					BlockPos offPos = pos.offset(face);
					BlockState offState = worldIn.getBlockState(offPos);
					
					if(offState.getBlock() == this)
			        	worldIn.getBlockTickScheduler().schedule(offPos, this, 2);
				}
		}
	}

	private boolean checkFallable(World worldIn, BlockPos pos) {
		if(!worldIn.isClient) {
			if(tryFall(worldIn, pos, Direction.DOWN))
				return true;
			else return tryFall(worldIn, pos, Direction.UP);
		}
		
		return false;
	}
	
	private boolean tryFall(World worldIn, BlockPos pos, Direction facing) {
		BlockPos target = pos.offset(facing);
		if((worldIn.isAir(target) || canFallThrough(worldIn, pos, worldIn.getBlockState(target))) && pos.getY() >= 0) {
			GravisandEntity entity = new GravisandEntity(worldIn, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, facing.getOffsetY());
			worldIn.spawnEntity(entity);
			return true;
		}
		
		return false;
	}
	
    public static boolean canFallThrough(WorldView world, BlockPos pos, BlockState state) {
		Block block = state.getBlock();
		Material material = state.getMaterial();
		return state.isAir(world, pos) || block == Blocks.FIRE || material.isLiquid() || material.isReplaceable();
    }

}
