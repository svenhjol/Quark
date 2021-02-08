package vazkii.quark.content.tweaks.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class ReplaceScaffoldingModule extends QuarkModule {
	
	@Config(description = "How many times the algorithm for finding out where a block would be placed is allowed to turn. If you set this to large values (> 3) it may start producing weird effects.")
	public int maxBounces = 1;

	@SubscribeEvent
	public void onInteract(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		BlockState state = world.getBlockState(pos);
		PlayerEntity player = event.getPlayer();
		if(state.getBlock() == Blocks.SCAFFOLDING && !player.isSneaky()) {
			Direction dir = event.getFace();
			ItemStack stack = event.getItemStack();
			Hand hand = event.getHand();
			
			if(stack.getItem() instanceof BlockItem) {
				BlockItem bitem = (BlockItem) stack.getItem();
				Block block = bitem.getBlock();
				
				if(block != Blocks.SCAFFOLDING && !bitem.isIn(ItemTags.BEDS)) {
					BlockPos last = getLastInLine(world, pos, dir);
					
					ItemUsageContext context = new ItemUsageContext(player, hand, new BlockHitResult(new Vec3d(0.5F, 1F, 0.5F), dir, last, false));
					ItemPlacementContext bcontext = new ItemPlacementContext(context);
					
					BlockState stateToPlace = block.getPlacementState(bcontext);
					if(stateToPlace != null && stateToPlace.canPlaceAt(world, last)) {
						BlockState currState = world.getBlockState(last);
						world.setBlockState(last, stateToPlace);
						
						BlockPos testUp = last.up();
						BlockState testUpState = world.getBlockState(testUp);
						if(testUpState.getBlock() == Blocks.SCAFFOLDING && !stateToPlace.isSideSolidFullSquare(world, last, Direction.UP)) {
							world.setBlockState(last, currState);
							return;
						}
						
						world.playSound(player, last, stateToPlace.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1F, 1F);
						
						if(!player.isCreative()) {
							stack.decrement(1);
							
							ItemStack giveStack = new ItemStack(Items.SCAFFOLDING);
							if(!player.giveItemStack(giveStack))
								player.dropItem(giveStack, false);
						}
						
						event.setCanceled(true);
						event.setCancellationResult(ActionResult.SUCCESS);
					}
				}
			}
		}
	}
	
	private BlockPos getLastInLine(World world, BlockPos start, Direction clickDir) {
		BlockPos result = getLastInLineOrNull(world, start, clickDir);
		if(result != null)
			return result;
		
		if(clickDir != Direction.UP) {
			result = getLastInLineOrNull(world, start, Direction.UP);
			if(result != null)
				return result;
		}
		
		for(Direction horizontal : MiscUtil.HORIZONTALS)
			if(horizontal != clickDir) {
				result = getLastInLineOrNull(world, start, horizontal);
				if(result != null)
					return result;
			}
		
		if(clickDir != Direction.DOWN) {
			result = getLastInLineOrNull(world, start, Direction.DOWN);
			if(result != null)
				return result;
		}
		
		return start;
	}
	
	private BlockPos getLastInLineOrNull(World world, BlockPos start, Direction dir) {
		BlockPos last = getLastInLineRecursive(world, start, dir, maxBounces);
		if(last.equals(start))
			return null;
		
		return last;
	}
	
	private BlockPos getLastInLineRecursive(World world, BlockPos start, Direction dir, int bouncesAllowed) {
		BlockPos curr = start;
		BlockState currState = world.getBlockState(start);
		Block currBlock = currState.getBlock();
		
		while(true) {
			BlockPos test = curr.offset(dir);
			if(!world.canSetBlock(test))
				break;
			
			BlockState testState = world.getBlockState(test);
			if(testState.getBlock() == currBlock)
				curr = test;
			else break;
		}
		
		if(!curr.equals(start) && bouncesAllowed > 0) {
			BlockPos maxDist = null;
			double maxDistVal = -1;
			
			for(Direction dir2 : Direction.values())
				if(dir.getAxis() != dir2.getAxis()) {
					BlockPos bounceStart = curr.offset(dir2);
					if(world.getBlockState(bounceStart).getBlock() == currBlock) {
						BlockPos testDist = getLastInLineRecursive(world, bounceStart, dir2, bouncesAllowed - 1);
						double testDistVal = testDist.getManhattanDistance(curr);
						if(testDistVal > maxDistVal)
							maxDist = testDist;
					}
				}
			
			if(maxDist != null)
				curr = maxDist;
		}
		
		return curr;
	}
	
}
