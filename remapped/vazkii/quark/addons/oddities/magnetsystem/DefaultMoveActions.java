package vazkii.quark.addons.oddities.magnetsystem;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import vazkii.quark.api.IMagnetMoveAction;

public class DefaultMoveActions {

	public static void addActions(HashMap<Block, IMagnetMoveAction> map) {
		map.put(Blocks.STONECUTTER, DefaultMoveActions::stonecutterMoved);
		map.put(Blocks.HOPPER, DefaultMoveActions::hopperMoved);
	}
	
	private static void stonecutterMoved(World world, BlockPos pos, Direction direction, BlockState state, BlockEntity tile) {
		if(!world.isClient) {
			BlockPos up = pos.up();
			BlockState breakState = world.getBlockState(up);
			double hardness = breakState.getHardness(world, up); 
			if(hardness > -1 && hardness < 3)
				world.breakBlock(up, true);
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void hopperMoved(World world, BlockPos pos, Direction direction, BlockState state, BlockEntity tile) {
		if(!world.isClient && tile instanceof HopperBlockEntity) {
			HopperBlockEntity hopper = (HopperBlockEntity) tile;
			hopper.setCooldown(0);
			
			Direction dir = state.get(HopperBlock.FACING);
			BlockPos offPos = pos.offset(dir);
			BlockPos targetPos = pos.offset(direction);
			if(offPos.equals(targetPos))
				return;
			
			if(world.isAir(offPos))
				for(int i = 0; i < hopper.size(); i++) {
					ItemStack stack = hopper.getStack(i);
					if(!stack.isEmpty()) {
						ItemStack drop = stack.copy();
						drop.setCount(1);
						hopper.removeStack(i, 1);
						
						boolean shouldDrop = true;
						if(drop.getItem() instanceof BlockItem) {
							BlockPos farmlandPos = offPos.down();
							if(world.isAir(farmlandPos))
								farmlandPos = farmlandPos.down();
							
							if(world.getBlockState(farmlandPos).getBlock() == Blocks.FARMLAND) {
								Block seedType = ((BlockItem) drop.getItem()).getBlock();
								if(seedType instanceof IPlantable) {
									BlockPos seedPos = farmlandPos.up();
									if(seedType.canPlaceAt(state, world, seedPos)) {
										BlockState seedState = seedType.getDefaultState();
										((ServerWorld) world).playSound(null, seedPos, seedType.getSoundGroup(seedState).getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
										
										world.setBlockState(seedPos, seedState);
										shouldDrop = false;
									}
								}
							}
						}
						
						if(shouldDrop) {
							double x = pos.getX() + 0.5 + ((double) dir.getOffsetX() * 0.7);
							double y = pos.getY() + 0.15 + ((double) dir.getOffsetY() * 0.4);
							double z = pos.getZ() + 0.5 + ((double) dir.getOffsetZ() * 0.7);
							ItemEntity entity = new ItemEntity(world, x, y, z, drop);
							entity.setVelocity(Vec3d.ZERO);
							world.spawnEntity(entity);
						}

						return;
					}
				}
		}
	}
	
}
