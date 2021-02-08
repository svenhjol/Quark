package vazkii.quark.base.handler;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonBlockStructureHelper;
import net.minecraft.block.material.PushReaction;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.quark.api.ICollateralMover;
import vazkii.quark.api.ICollateralMover.MoveResult;
import vazkii.quark.api.IConditionalSticky;
import vazkii.quark.api.IIndirectConnector;

public class QuarkPistonStructureHelper extends PistonBlockStructureHelper {

	private final PistonBlockStructureHelper parent;

	private final World world;
	private final BlockPos pistonPos;
	private final BlockPos blockToMove;
	private final Direction moveDirection;
	private final List<BlockPos> toMove = Lists.<BlockPos>newArrayList();
	private final List<BlockPos> toDestroy = Lists.<BlockPos>newArrayList();

	public QuarkPistonStructureHelper(PistonBlockStructureHelper parent, World worldIn, BlockPos posIn, Direction pistonFacing, boolean extending) {
		super(worldIn, posIn, pistonFacing, extending);
		this.parent = parent;

		this.world = worldIn;
		this.pistonPos = posIn;
		if(extending) {
			this.moveDirection = pistonFacing;
			this.blockToMove = posIn.offset(pistonFacing);
		} else {
			this.moveDirection = pistonFacing.getOpposite();
			this.blockToMove = posIn.offset(pistonFacing, 2);
		}
	}

	@Override
	public boolean canMove() {
		if(!GeneralConfig.usePistonLogicRepl)
			return parent.canMove();

		toMove.clear();
		toDestroy.clear();
		BlockState iblockstate = world.getBlockState(blockToMove);

		if(!PistonBlock.canPush(iblockstate, world, blockToMove, moveDirection, false, moveDirection)) {
			if(iblockstate.getPushReaction() == PushReaction.DESTROY) {
				toDestroy.add(blockToMove);
				return true;
			} else return false;
		}
		else if(!addBlockLine(blockToMove, moveDirection))
			return false;
		else {
			for(int i = 0; i < toMove.size(); ++i) {
				BlockPos blockpos = toMove.get(i);

				if(addBranchingBlocks(world, blockpos, isBlockBranching(world, blockpos)) == MoveResult.PREVENT)
					return false;
			}

			return true;
		}
	}
	
	private boolean addBlockLine(BlockPos origin, Direction face) {
		final int max = GeneralConfig.pistonPushLimit; 

		BlockPos target = origin;
		BlockState iblockstate = world.getBlockState(target);

		if(iblockstate.getBlock().isAir(iblockstate, world, origin) 
				|| !PistonBlock.canPush(iblockstate, world, origin, moveDirection, false, face)
				|| origin.equals(pistonPos)
				|| toMove.contains(origin))
			return true;

		else {
			int lineLen = 1;

			if(lineLen + toMove.size() > max) 
				return false;
			else {
				BlockPos oldPos = origin;
				BlockState oldState = world.getBlockState(origin); 
				
				boolean skippingNext = false;
				while(true) {
					if(!isBlockBranching(world, target))
						break;
					
					MoveResult res = getBranchResult(world, target);
					if(res == MoveResult.PREVENT)
						return false;
					else if(res != MoveResult.MOVE) {
						skippingNext = true;
						break;
					}
					
					target = origin.offset(moveDirection.getOpposite(), lineLen);
					iblockstate = world.getBlockState(target);
					
					if(iblockstate.getBlock().isAir(iblockstate, world, target) || !PistonBlock.canPush(iblockstate, world, target, moveDirection, false, moveDirection.getOpposite()) || target.equals(pistonPos))
						break;
					
					if(getStickCompatibility(world, iblockstate, oldState, target, oldPos, moveDirection) != MoveResult.MOVE)
						break;
					
					oldState = iblockstate;
					oldPos = target;

					lineLen++;
					
					if(lineLen + toMove.size() > max)
						return false;
				}

				int i1 = 0;

				for(int j = lineLen - 1; j >= 0; --j) {
					BlockPos movePos = origin.offset(moveDirection.getOpposite(), j);
					if(toDestroy.contains(movePos))
						break;
					
					toMove.add(movePos);
					i1++;
				}
				
				if(skippingNext)
					return true;
				
				int j1 = 1;

				while(true) {
					BlockPos blockpos1 = origin.offset(moveDirection, j1);
					int k = toMove.indexOf(blockpos1);
					
					MoveResult res = MoveResult.MOVE;

					if(k > -1) {
						reorderListAtCollision(i1, k);

						for(int l = 0; l <= k + i1; ++l) {
							BlockPos blockpos2 = toMove.get(l);

							res = addBranchingBlocks(world, blockpos2, isBlockBranching(world, blockpos2));
							
							if(res == MoveResult.PREVENT)
								return false;
						}

						return true;
					}
					
					if(res == MoveResult.MOVE) {
						iblockstate = world.getBlockState(blockpos1);

						if(iblockstate.getBlock().isAir(iblockstate, world, blockpos1))
							return true;

						if(!PistonBlock.canPush(iblockstate, world, blockpos1, moveDirection, true, moveDirection) || blockpos1.equals(pistonPos))
							return false;

						if(iblockstate.getPushReaction() == PushReaction.DESTROY) {
							toDestroy.add(blockpos1);
							toMove.remove(blockpos1);
							return true;
						}

						boolean doneFinding = false;
						if(isBlockBranching(world, blockpos1)) {
							res = getBranchResult(world, blockpos1);
							if(res == MoveResult.PREVENT)
								return false;
							
							if(res != MoveResult.MOVE)
								doneFinding = true;
						}
						
						if(toMove.size() >= max)
							return false;
						
						toMove.add(blockpos1);

						++i1;
						++j1;
						
						if(doneFinding)
							return true;
					}
				}
			}
		}
	}

	private void reorderListAtCollision(int p_177255_1_, int p_177255_2_) {
		List<BlockPos> list = Lists.<BlockPos>newArrayList();
		List<BlockPos> list1 = Lists.<BlockPos>newArrayList();
		List<BlockPos> list2 = Lists.<BlockPos>newArrayList();
		list.addAll(toMove.subList(0, p_177255_2_));
		list1.addAll(toMove.subList(toMove.size() - p_177255_1_, toMove.size()));
		list2.addAll(toMove.subList(p_177255_2_, toMove.size() - p_177255_1_));
		toMove.clear();
		toMove.addAll(list);
		toMove.addAll(list1);
		toMove.addAll(list2);
	}

	@SuppressWarnings("incomplete-switch")
	private MoveResult addBranchingBlocks(World world, BlockPos fromPos, boolean isSourceBranching) {
		BlockState state = world.getBlockState(fromPos);
		Block block = state.getBlock();
		
		Direction opposite = moveDirection.getOpposite();
		MoveResult retResult = MoveResult.SKIP;
		for(Direction face : Direction.values()) {
				MoveResult res = MoveResult.MOVE;
				BlockPos targetPos = fromPos.offset(face);
				BlockState targetState = world.getBlockState(targetPos);
				
				if(!isSourceBranching) {
					IIndirectConnector indirect = getIndirectStickiness(targetState);
					if(indirect != null && indirect.isEnabled() && indirect.canConnectIndirectly(world, targetPos, fromPos, targetState, state))
						res = getStickCompatibility(world, state, targetState, fromPos, targetPos, face);
					else res = MoveResult.SKIP;
				} 
				else {
					if(block instanceof ICollateralMover)
						res = ((ICollateralMover) block).getCollateralMovement(world, pistonPos, moveDirection, face, fromPos);
					else res = getStickCompatibility(world, state, targetState, fromPos, targetPos, face);
				}
				
				switch(res) {
				case PREVENT:
					return MoveResult.PREVENT;
				case MOVE:
					if(!addBlockLine(targetPos, face))
						return MoveResult.PREVENT;
					break;
				case BREAK:
					if(PistonBlock.canPush(targetState, world, targetPos, moveDirection, true, moveDirection)) {
						toDestroy.add(targetPos);
						toMove.remove(targetPos);
						return MoveResult.BREAK;
					}
					
					return MoveResult.PREVENT;
				}
				
				if(face == opposite)
					retResult = res;
			}
		
		return retResult;
	}

	private boolean isBlockBranching(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		return block instanceof ICollateralMover ? ((ICollateralMover) block).isCollateralMover(world, pistonPos, moveDirection, pos) : isBlockSticky(state);
	}
	
	private MoveResult getBranchResult(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if(block instanceof ICollateralMover)
			return ((ICollateralMover) block).getCollateralMovement(world, pistonPos, moveDirection, moveDirection, pos);
		
		return MoveResult.MOVE;
	}
	
	private MoveResult getStickCompatibility(World world, BlockState state1, BlockState state2, BlockPos pos1, BlockPos pos2, Direction face) {
		IConditionalSticky stick = getStickCondition(state1);
		if(stick != null && !stick.canStickToBlock(world, pistonPos, pos1, pos2, state1, state2, moveDirection))
			return MoveResult.SKIP;
		
		stick = getStickCondition(state2);
		if(stick != null && !stick.canStickToBlock(world, pistonPos, pos2, pos1, state2, state1, moveDirection))
			return MoveResult.SKIP;

		return MoveResult.MOVE;
	}
	
	private IConditionalSticky getStickCondition(BlockState state) {
		Block block = state.getBlock();
		if(block == Blocks.HONEY_BLOCK)
			return HoneyStickCondition.INSTANCE;
		
		if(block instanceof IConditionalSticky)
			return (IConditionalSticky) block;
		
		IIndirectConnector indirect = getIndirectStickiness(state);
		if(indirect != null && indirect.isEnabled())
			return indirect.getStickyCondition();
		
		return null;
	}

	@Nonnull
	@Override
	public List<BlockPos> getBlocksToMove() {
		if(!GeneralConfig.usePistonLogicRepl) 
			return parent.getBlocksToMove();

		return toMove;
	}

	@Nonnull
	@Override
	public List<BlockPos> getBlocksToDestroy() {
		if(!GeneralConfig.usePistonLogicRepl) 
			return parent.getBlocksToDestroy();

		return toDestroy;
	}
	
	private static IIndirectConnector getIndirectStickiness(BlockState state) {
		for(Pair<Predicate<BlockState>, IIndirectConnector> p : IIndirectConnector.INDIRECT_STICKY_BLOCKS)
			if(p.getLeft().test(state))
				return p.getRight();
		
		return null;
	}
	
	private static boolean isBlockSticky(BlockState state) {
		if(state.isStickyBlock())
			return true;
		
		IIndirectConnector indirect = getIndirectStickiness(state);
		return indirect != null && indirect.isEnabled();
	}
	
	private static class HoneyStickCondition implements IConditionalSticky {

		private static final HoneyStickCondition INSTANCE = new HoneyStickCondition();
		
		@Override
		public boolean canStickToBlock(World world, BlockPos pistonPos, BlockPos pos, BlockPos slimePos, BlockState state, BlockState slimeState, Direction direction) {
			Block block = state.getBlock();
			Block slime = slimeState.getBlock();
			
			// specifically utilize the vanilla sticky definition as to not break honey connections with blocks like chains
			return !slime.isStickyBlock(slimeState) || block == slime;
		}
		
	}

}