package vazkii.quark.world.gen.underground;

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.world.gen.UndergroundBiomeGenerator.Context;

public abstract class UndergroundBiome {
	
	private static Tag<Block> fillerTag = null;
	
	public static final Predicate<BlockState> STONE_TYPES_MATCHER = (state) -> {
		if(state == null)
			return false;
		
		Block block = state.getBlock();
		if(fillerTag == null)
			fillerTag = BlockTags.register(Quark.MOD_ID + ":underground_biome_replaceable");
		
		return block.isIn(fillerTag);
	};
	
	public double dungeonChance;

	public final void fill(Context context, BlockPos pos) {
		WorldAccess world = context.world;
		BlockState state = world.getBlockState(pos);
		if(state.getHardness(world, pos) == -1 || world.isSkyVisibleAllowingSea(pos))
			return;

		if(isFloor(world, pos, state)) {
			context.floorList.add(pos);
			fillFloor(context, pos, state);
		} else if(isCeiling(world, pos, state)) {
			context.ceilingList.add(pos);
			fillCeiling(context, pos, state);
		} else if(isWall(world, pos, state)) {
			context.wallMap.put(pos, getBorderSide(world, pos));
			fillWall(context, pos, state);
		} else if(isInside(state)) {
			context.insideList.add(pos);
			fillInside(context, pos, state);
		}
	}

	public abstract void fillFloor(Context context, BlockPos pos, BlockState state);
	public abstract void fillCeiling(Context context, BlockPos pos, BlockState state);
	public abstract void fillWall(Context context, BlockPos pos, BlockState state);
	public abstract void fillInside(Context context, BlockPos pos, BlockState state);
	
	public void finalFloorPass(Context context, BlockPos pos) {
		// NO-OP
	}

	public void finalCeilingPass(Context context, BlockPos pos) {
		// NO-OP
	}

	public void finalWallPass(Context context, BlockPos pos) {
		// NO-OP
	}

	public void finalInsidePass(Context context, BlockPos pos) {
		// NO-OP
	}

	public boolean isFloor(WorldAccess world, BlockPos pos, BlockState state) {
		if(!state.isOpaqueFullCube(world, pos))
			return false;

		BlockPos upPos = pos.up();
		return world.isAir(upPos) || world.getBlockState(upPos).getMaterial().isReplaceable();
	}

	public boolean isCeiling(WorldAccess world, BlockPos pos, BlockState state) {
		if(!state.isOpaqueFullCube(world, pos))
			return false;

		BlockPos downPos = pos.down();
		return world.isAir(downPos) || world.getBlockState(downPos).getMaterial().isReplaceable();
	}

	public boolean isWall(WorldAccess world, BlockPos pos, BlockState state) {
		if(!state.isOpaqueFullCube(world, pos) || !STONE_TYPES_MATCHER.test(state))
			return false;

		return isBorder(world, pos);
	}

	public Direction getBorderSide(WorldAccess world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		for(Direction facing : MiscUtil.HORIZONTALS) {
			BlockPos offsetPos = pos.offset(facing);
			BlockState stateAt = world.getBlockState(offsetPos);
			
			if(state != stateAt && world.isAir(offsetPos) || stateAt.getMaterial().isReplaceable())
				return facing;
		}

		return null;
	}
	
	public boolean isBorder(WorldAccess world, BlockPos pos) {
		return getBorderSide(world, pos) != null;
	}

	public boolean isInside(BlockState state) {
		return STONE_TYPES_MATCHER.test(state);
	}
	
	public static BlockRotation rotationFromFacing(Direction facing) {
		switch(facing) {
		case SOUTH:
			return BlockRotation.CLOCKWISE_180;
		case WEST:
			return BlockRotation.COUNTERCLOCKWISE_90;
		case EAST:
			return BlockRotation.CLOCKWISE_90;
		default:
			return BlockRotation.NONE;
		}
	}

}
