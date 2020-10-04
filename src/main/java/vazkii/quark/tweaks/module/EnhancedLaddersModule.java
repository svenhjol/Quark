package vazkii.quark.tweaks.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.building.module.VariantLaddersModule;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class EnhancedLaddersModule extends Module {

	@Config.Max(0)
	@Config
    public double fallSpeed = -0.2;

	private static Tag<Item> laddersTag;
	
	@Override
	public void setup() {
		laddersTag = ItemTags.register(Quark.MOD_ID + ":ladders");
	}
	
	@SuppressWarnings("deprecation")
	private static boolean canAttachTo(BlockState state, Block ladder, WorldView world, BlockPos pos, Direction facing) {
		if(ladder == VariantLaddersModule.iron_ladder)
			return VariantLaddersModule.iron_ladder.canPlaceAt(state, world, pos);
		if (ladder instanceof LadderBlock) {
			BlockPos offset = pos.offset(facing);
			BlockState blockstate = world.getBlockState(offset);
			return !blockstate.emitsRedstonePower() && blockstate.isSideSolidFullSquare(world, offset, facing); 
		}

		return false;
	}

	@SubscribeEvent
	public void onInteract(PlayerInteractEvent.RightClickBlock event) {
		PlayerEntity player = event.getPlayer();
		Hand hand = event.getHand();
		ItemStack stack = player.getStackInHand(hand);

		if(!stack.isEmpty() && stack.getItem().isIn(laddersTag)) {
			Block block = Block.getBlockFromItem(stack.getItem());
			World world = event.getWorld();
			BlockPos pos = event.getPos();
			while(world.getBlockState(pos).getBlock() == block) {
				event.setCanceled(true);
				BlockPos posDown = pos.down();

				if(World.isHeightInvalid(posDown))
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
							world.setBlockState(posDown, copyState.with(Properties.WATERLOGGED, water));
							world.playSound(null, posDown.getX(), posDown.getY(), posDown.getZ(), SoundEvents.BLOCK_LADDER_PLACE, SoundCategory.BLOCKS, 1F, 1F);

							if(world.isClient)
								player.swingHand(hand);

							if(!player.isCreative()) {
								stack.decrement(1);

								if(stack.getCount() <= 0)
									player.setStackInHand(hand, ItemStack.EMPTY);
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
			if(player.isClimbing() && player.world.isClient) {
				BlockPos playerPos = player.getBlockPos();
				BlockPos downPos = playerPos.down();
				
				boolean scaffold = player.world.getBlockState(playerPos).getBlock() == Blocks.SCAFFOLDING;
				if(player.isInSneakingPose() == scaffold &&
						player.forwardSpeed == 0 &&
						player.upwardSpeed <= 0 &&
						player.sidewaysSpeed == 0 &&
						player.pitch > 70 &&
						!player.jumping &&
						!player.abilities.flying &&
						player.world.getBlockState(downPos).isLadder(player.world, downPos, player)) {
					Vec3d move = new Vec3d(0, fallSpeed, 0);
					player.setBoundingBox(player.getBoundingBox().offset(move));						
					player.move(MovementType.SELF, Vec3d.ZERO);
				}
			}
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onInput(InputUpdateEvent event) {
		PlayerEntity player = event.getPlayer();
		if(player.isClimbing() && MinecraftClient.getInstance().currentScreen != null && !(player.forwardSpeed == 0 && player.pitch > 70)) {
			Input input = event.getMovementInput();
			if(input != null)
				input.sneaking = true; // sneaking
		}
	}

}
