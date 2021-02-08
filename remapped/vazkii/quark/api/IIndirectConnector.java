package vazkii.quark.api;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IIndirectConnector {
	
	public static List<Pair<Predicate<BlockState>, IIndirectConnector>> INDIRECT_STICKY_BLOCKS = new LinkedList<>();
	
	public default boolean isEnabled() {
		return true;
	}

	public default IConditionalSticky getStickyCondition() {
		return (w, pp, op, sp, os, ss, d) -> canConnectIndirectly(w, op, sp, os, ss);
	}
	
	public boolean canConnectIndirectly(World world, BlockPos ourPos, BlockPos sourcePos, BlockState ourState, BlockState sourceState);
	
}
