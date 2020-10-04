package vazkii.quark.api;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface IMagnetMoveAction {

	void onMagnetMoved(World world, BlockPos pos, Direction direction, BlockState state, BlockEntity tile);
	
}
