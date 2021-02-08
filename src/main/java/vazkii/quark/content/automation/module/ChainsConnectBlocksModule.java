package vazkii.quark.content.automation.module;

import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChainBlock;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.quark.api.IIndirectConnector;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.AUTOMATION)
public class ChainsConnectBlocksModule extends QuarkModule {

	public static boolean staticEnabled;
	
	@Override
	public void construct() {
		IIndirectConnector.INDIRECT_STICKY_BLOCKS.add(Pair.of(ChainConnection.PREDICATE, ChainConnection.INSTANCE));
	}
	
	@Override
	public void configChanged() {
		staticEnabled = enabled;
	}
	
	public static class ChainConnection implements IIndirectConnector {

		public static ChainConnection INSTANCE = new ChainConnection();
		public static Predicate<BlockState> PREDICATE = s -> s.getBlock() == Blocks.CHAIN;
		
		@Override
		public boolean isEnabled() {
			return ChainsConnectBlocksModule.staticEnabled;
		}
		
		@Override
		public boolean canConnectIndirectly(World world, BlockPos ourPos, BlockPos sourcePos, BlockState ourState, BlockState sourceState) {
			Axis axis = ourState.get(ChainBlock.AXIS);
			
			switch(axis) {
			case X:
				if(ourPos.getX() == sourcePos.getX())
					return false;
				break;
			case Y:
				if(ourPos.getY() == sourcePos.getY())
					return false;
				break;
			case Z:
				if(ourPos.getZ() == sourcePos.getZ())
					return false;
			}
			
			if(sourceState.getBlock() == ourState.getBlock()) {
				Axis otherAxis = sourceState.get(ChainBlock.AXIS);
				return axis == otherAxis;
			}
			
			return true;
		}
		
	}
	
}
