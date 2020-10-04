package vazkii.quark.building.block;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import vazkii.arl.interf.IBlockItemProvider;
import vazkii.quark.automation.module.PistonsMoveTileEntitiesModule;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.Module;
import vazkii.quark.building.module.RopeModule;

public class RopeBlock extends QuarkBlock implements IBlockItemProvider {

	private static final VoxelShape SHAPE = createCuboidShape(6, 0, 6, 10, 16, 10);

	public RopeBlock(String regname, Module module, ItemGroup creativeTab, Settings properties) {
		super(regname, module, creativeTab, properties);
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}
	
	@Override
	public BlockItem provideItemBlock(Block block, Item.Settings properties) {
		return new BlockItem(block, properties) {
			@Override
			public boolean doesSneakBypassUse(ItemStack stack, WorldView world, BlockPos pos, PlayerEntity player) {
				return world.getBlockState(pos).getBlock() instanceof RopeBlock;
			}
		};
	}

	@Override
	@SuppressWarnings("deprecation")
	public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if(hand == Hand.MAIN_HAND) {
			ItemStack stack = player.getStackInHand(hand);
			if(stack.getItem() == asItem() && !player.isSneaky()) {
				if(pullDown(worldIn, pos)) {
					if(!player.isCreative())
						stack.decrement(1);
					
					worldIn.playSound(null, pos, soundGroup.getPlaceSound(), SoundCategory.BLOCKS, 0.5F, 1F);
					return ActionResult.SUCCESS;
				}
			} else if (stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent()) {
				return FluidUtil.interactWithFluidHandler(player, hand, worldIn, getBottomPos(worldIn, pos), Direction.UP) ? ActionResult.SUCCESS : ActionResult.PASS;
			} else if (stack.getItem() == Items.GLASS_BOTTLE) {
				BlockPos bottomPos = getBottomPos(worldIn, pos);
				BlockState stateAt = worldIn.getBlockState(bottomPos);
				if (stateAt.getMaterial() == Material.WATER) {
					Vec3d playerPos = player.getPos();
					worldIn.playSound(player, playerPos.x, playerPos.y, playerPos.z, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
					stack.decrement(1);
					ItemStack bottleStack = PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER);
					player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));

					if (stack.isEmpty())
						player.setStackInHand(hand, bottleStack);
					else if (!player.inventory.insertStack(bottleStack))
						player.dropItem(bottleStack, false);


					return ActionResult.SUCCESS;
				}

				return ActionResult.PASS;
			} else {
				if(pullUp(worldIn, pos)) {
					if(!player.isCreative()) {
						if(!player.giveItemStack(new ItemStack(this)))
							player.dropItem(new ItemStack(this), false);
					}
					
					worldIn.playSound(null, pos, soundGroup.getBreakSound(), SoundCategory.BLOCKS, 0.5F, 1F);
					return ActionResult.SUCCESS;
				}
			}
		}
		
		return ActionResult.PASS;
	}

	public boolean pullUp(World world, BlockPos pos) {
		BlockPos basePos = pos;
		
		while(true) {
			pos = pos.down();
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() != this)
				break;
		}
		
		BlockPos ropePos = pos.up();
		if(ropePos.equals(basePos))
			return false;

		world.setBlockState(ropePos, Blocks.AIR.getDefaultState());
		moveBlock(world, pos, ropePos);
		
		return true;
	}
	
	public boolean pullDown(World world, BlockPos pos) {
		boolean can;
		boolean endRope = false;
		boolean wasAirAtEnd = false;
		
		do {
			pos = pos.down();
			if (!World.method_24794(pos))
				return false;

			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			
			if(block == this)
				continue;
			
			if(endRope) {
				can = wasAirAtEnd || world.isAir(pos) || state.getMaterial().isReplaceable();
				break;
			}
			
			endRope = true;
			wasAirAtEnd = world.isAir(pos);
		} while(true);
		
		if(can) {
			BlockPos ropePos = pos.up();
			moveBlock(world, ropePos, pos);
			
			BlockState ropePosState = world.getBlockState(ropePos);

			if(world.isAir(ropePos) || ropePosState.getMaterial().isReplaceable()) {
				world.setBlockState(ropePos, getDefaultState());
				return true;
			}
		}
		
		return false;
	}

	private BlockPos getBottomPos(World worldIn, BlockPos pos) {
		Block block = this;
		while (block == this) {
			pos = pos.down();
			BlockState state = worldIn.getBlockState(pos);
			block = state.getBlock();
		}

		return pos;

	}

	private void moveBlock(World world, BlockPos srcPos, BlockPos dstPos) {
		BlockState state = world.getBlockState(srcPos);
		Block block = state.getBlock();
		
		if(state.getHardness(world, srcPos) == -1 || !state.canPlaceAt(world, dstPos) || block.isAir(state, world, srcPos) ||
				state.getPistonBehavior() != PistonBehavior.NORMAL || block == Blocks.OBSIDIAN)
			return;
		
		BlockEntity tile = world.getBlockEntity(srcPos);
		if(tile != null) {
			if(RopeModule.forceEnableMoveTileEntities ? PistonsMoveTileEntitiesModule.shouldMoveTE(state) : PistonsMoveTileEntitiesModule.shouldMoveTE(true, state))
				return;

			tile.markRemoved();
		}
		
		world.setBlockState(srcPos, Blocks.AIR.getDefaultState());
		world.setBlockState(dstPos, state);
		
		if(tile != null) {
			tile.setPos(dstPos);
			BlockEntity target = BlockEntity.createFromTag(state, tile.toTag(new CompoundTag())); // create
			if (target != null) {
				world.setBlockEntity(dstPos, target);

				target.resetBlock();

			}
		}

		world.updateNeighborsAlways(dstPos, state.getBlock());
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
		BlockPos upPos = pos.up();
		BlockState upState = worldIn.getBlockState(upPos);
		return upState.getBlock() == this || upState.isSideSolidFullSquare(worldIn, upPos, Direction.DOWN);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if(!state.canPlaceAt(worldIn, pos)) {
			worldIn.syncWorldEvent(2001, pos, Block.getRawIdFromState(worldIn.getBlockState(pos)));
			dropStacks(state, worldIn, pos);
			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}

	@Override
	public boolean isLadder(BlockState state, WorldView world, BlockPos pos, LivingEntity entity) {
		return true;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return 30;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return 60;
	}

}
