package vazkii.quark.content.tools.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.module.BottledCloudModule;
import vazkii.quark.content.tools.tile.CloudTileEntity;

public class CloudBlock extends QuarkBlock {

	public CloudBlock(QuarkModule module) {
		super("cloud", module, null, 
				Block.Properties.of(Material.ORGANIC_PRODUCT)
				.sounds(BlockSoundGroup.WOOL)
				.strength(0)
				.nonOpaque());
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult raytrace) {
		ItemStack stack = player.getStackInHand(hand);
		
		if(stack.getItem() == Items.GLASS_BOTTLE) {
			fillBottle(player, player.inventory.selectedSlot);
			world.removeBlock(pos, false);
			return ActionResult.SUCCESS;
		}
		
		if(stack.getItem() instanceof BlockItem) {
			BlockItem bitem = (BlockItem) stack.getItem();
			Block block = bitem.getBlock();
			
			ItemUsageContext context = new ItemUsageContext(player, hand, new BlockHitResult(new Vec3d(0.5F, 1F, 0.5F), raytrace.getSide(), pos, false));
			ItemPlacementContext bcontext = new ItemPlacementContext(context);
			
			BlockState stateToPlace = block.getPlacementState(bcontext);
			if(stateToPlace != null && stateToPlace.canPlaceAt(world, pos)) {
				world.setBlockState(pos, stateToPlace);
				world.playSound(player, pos, stateToPlace.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1F, 1F);
				
				if(!player.isCreative()) {
					stack.decrement(1);
					fillBottle(player, 0);
				}
				
				return ActionResult.SUCCESS;
			}
		}
		
		return ActionResult.PASS;
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, HitResult target, BlockView world, BlockPos pos, PlayerEntity player) {
		return ItemStack.EMPTY;
	}
	
	private void fillBottle(PlayerEntity player, int startIndex) {
		PlayerInventory inv = player.inventory;
		for(int i = startIndex ; i < inv.size(); i++) {
			ItemStack stackInSlot = inv.getStack(i);
			if(stackInSlot.getItem() == Items.GLASS_BOTTLE) {
				stackInSlot.decrement(1);
				
				ItemStack give = new ItemStack(BottledCloudModule.bottled_cloud);
				if(!player.giveItemStack(give))
					player.dropItem(give, false);
				return;
			}
		}
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockView world) {
		return new CloudTileEntity();
	}

}
