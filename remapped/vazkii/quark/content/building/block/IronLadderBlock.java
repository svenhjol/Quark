package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.module.VariantLaddersModule;

public class IronLadderBlock extends VariantLadderBlock {

	private static final BlockSoundGroup SOUND_TYPE = new BlockSoundGroup(1.0F, 1.0F, SoundEvents.BLOCK_METAL_BREAK, SoundEvents.BLOCK_LADDER_STEP, SoundEvents.BLOCK_METAL_PLACE, SoundEvents.BLOCK_METAL_HIT, SoundEvents.BLOCK_LADDER_FALL);

	public IronLadderBlock(QuarkModule module) {
		super("iron", module, Block.Properties.of(Material.SUPPORTED)
				.strength(0.8F)
				.sounds(SOUND_TYPE)
				.nonOpaque(), false);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
		Direction facing = state.get(FACING);
		boolean solid = facing.getAxis() != Axis.Y && worldIn.getBlockState(pos.offset(facing.getOpposite())).isSideSolidFullSquare(worldIn, pos.offset(facing.getOpposite()), facing);
		BlockState topState = worldIn.getBlockState(pos.up());
		return solid || (topState.getBlock() == this && (facing.getAxis() == Axis.Y || topState.get(FACING) == facing));
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
		if(!stateIn.canPlaceAt(worldIn, currentPos))
			return Blocks.AIR.getDefaultState();

		return super.getStateForNeighborUpdate(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && VariantLaddersModule.enableIronLadder;
	}

}
