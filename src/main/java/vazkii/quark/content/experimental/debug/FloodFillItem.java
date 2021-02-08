package vazkii.quark.content.experimental.debug;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.QuarkModule;

public class FloodFillItem extends QuarkItem {

	public FloodFillItem(QuarkModule module) {
		super("flood_filler", module, new Item.Properties());
	}	
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		World world = context.getWorld();
		floodFill(world, context.getPos());
		
		return ActionResultType.SUCCESS;
	}
	
	private void floodFill(World world, BlockPos pos) {
		boolean clickedBarrier = world.getBlockState(pos).getBlock() == Blocks.BARRIER;
		Queue<BlockPos> candidates = new ArrayDeque<>(1000);
		
		int blocks = 0;
		BlockState barrier = clickedBarrier ? Blocks.CAVE_AIR.getDefaultState() : Blocks.BARRIER.getDefaultState();
		candidates.add(pos);
		while(!candidates.isEmpty()) {
			blocks++;
			if(blocks > 32000) {
				new RuntimeException("Flood fill did way too much shit").printStackTrace();
				return;
			}
			
			BlockPos candidate = candidates.poll();
			for(Direction dir : Direction.values()) {
				BlockPos newCandidate = candidate.offset(dir);
				if(clickedBarrier ? world.getBlockState(newCandidate).getBlock() == Blocks.BARRIER : world.isAirBlock(newCandidate)) {
					candidates.add(newCandidate);
					world.setBlockState(newCandidate, barrier, 0);
				}
			}
		}
	}
	
}
