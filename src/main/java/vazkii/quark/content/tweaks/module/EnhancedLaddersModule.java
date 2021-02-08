package vazkii.quark.content.tweaks.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.building.module.VariantLaddersModule;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class EnhancedLaddersModule extends QuarkModule {

	@Config.Max(0)
	@Config
    public double fallSpeed = -0.2;

	private static ITag<Item> laddersTag;
	
	@Override
	public void setup() {
		laddersTag = ItemTags.createOptional(new ResourceLocation(Quark.MOD_ID, "ladders"));
	}
	
	@SuppressWarnings("deprecation")
	private static boolean canAttachTo(BlockState state, Block ladder, IWorldReader world, BlockPos pos, Direction facing) {
		if(ladder == VariantLaddersModule.iron_ladder)
			return VariantLaddersModule.iron_ladder.isValidPosition(state, world, pos);
		if (ladder instanceof LadderBlock) {
			BlockPos offset = pos.offset(facing);
			BlockState blockstate = world.getBlockState(offset);
			return !blockstate.canProvidePower() && blockstate.isSolidSide(world, offset, facing); 
		}

		return false;
	}

	@SubscribeEvent
	public void onInteract(PlayerInteractEvent.RightClickBlock event) {
		PlayerEntity player = event.getPlayer();
		Hand hand = event.getHand();
		ItemStack stack = player.getHeldItem(hand);

		if(!stack.isEmpty() && stack.getItem().isIn(laddersTag)) {
			Block block = Block.getBlockFromItem(stack.getItem());
			World world = event.getWorld();
			BlockPos pos = event.getPos();
			while(world.getBlockState(pos).getBlock() == block) {
				event.setCanceled(true);
				BlockPos posDown = pos.down();

				if(World.isOutsideBuildHeight(posDown))
					break;

				BlockState stateDown = world.getBlockState(posDown);

				if(stateDown.getBlock() == block)
					pos = posDown;
				else {
					boolean water = stateDown.getBlock() == Blocks.WATER;
					if(water || stateDown.getBlock().isAir(stateDown, world, posDown)) {
						BlockState copyState = world.getBlockState(pos);

						Direction facing = copyState.get(LadderBlock.FACING);
						if(canAttachTo(copyState, block, world, posDown, facing.getOpposite())) {
							world.setBlockState(posDown, copyState.with(BlockStateProperties.WATERLOGGED, water));
							world.playSound(null, posDown.getX(), posDown.getY(), posDown.getZ(), SoundEvents.BLOCK_LADDER_PLACE, SoundCategory.BLOCKS, 1F, 1F);

							if(world.isRemote)
								player.swingArm(hand);

							if(!player.isCreative()) {
								stack.shrink(1);

								if(stack.getCount() <= 0)
									player.setHeldItem(hand, ItemStack.EMPTY);
							}
						}
					}
					break;
				} 
			}
		}
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if(event.phase == TickEvent.Phase.START) {
			PlayerEntity player = event.player;
			if(player.isOnLadder() && player.world.isRemote) {
				BlockPos playerPos = player.getPosition();
				BlockPos downPos = playerPos.down();
				
				boolean scaffold = player.world.getBlockState(playerPos).getBlock() == Blocks.SCAFFOLDING;
				if(player.isCrouching() == scaffold &&
						player.moveForward == 0 &&
						player.moveVertical <= 0 &&
						player.moveStrafing == 0 &&
						player.rotationPitch > 70 &&
						!player.isJumping &&
						!player.abilities.isFlying &&
						player.world.getBlockState(downPos).isLadder(player.world, downPos, player)) {
					Vector3d move = new Vector3d(0, fallSpeed, 0);
					player.setBoundingBox(player.getBoundingBox().offset(move));						
					player.move(MoverType.SELF, Vector3d.ZERO);
				}
			}
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onInput(InputUpdateEvent event) {
		PlayerEntity player = event.getPlayer();
		if(player.isOnLadder() && Minecraft.getInstance().currentScreen != null && !(player.moveForward == 0 && player.rotationPitch > 70)) {
			MovementInput input = event.getMovementInput();
			if(input != null)
				input.sneaking = true; // sneaking
		}
	}

}
