package vazkii.quark.content.tools.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.module.BottledCloudModule;
import vazkii.quark.content.tools.tile.CloudTileEntity;

public class CloudBlock extends QuarkBlock {

	public CloudBlock(QuarkModule module) {
		super("cloud", module, null, 
				Block.Properties.create(Material.CLAY)
				.sound(SoundType.CLOTH)
				.hardnessAndResistance(0)
				.notSolid());
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult raytrace) {
		ItemStack stack = player.getHeldItem(hand);
		
		if(stack.getItem() == Items.GLASS_BOTTLE) {
			fillBottle(player, player.inventory.currentItem);
			world.removeBlock(pos, false);
			return ActionResultType.SUCCESS;
		}
		
		if(stack.getItem() instanceof BlockItem) {
			BlockItem bitem = (BlockItem) stack.getItem();
			Block block = bitem.getBlock();
			
			ItemUseContext context = new ItemUseContext(player, hand, new BlockRayTraceResult(new Vector3d(0.5F, 1F, 0.5F), raytrace.getFace(), pos, false));
			BlockItemUseContext bcontext = new BlockItemUseContext(context);
			
			BlockState stateToPlace = block.getStateForPlacement(bcontext);
			if(stateToPlace != null && stateToPlace.isValidPosition(world, pos)) {
				world.setBlockState(pos, stateToPlace);
				world.playSound(player, pos, stateToPlace.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, 1F, 1F);
				
				if(!player.isCreative()) {
					stack.shrink(1);
					fillBottle(player, 0);
				}
				
				return ActionResultType.SUCCESS;
			}
		}
		
		return ActionResultType.PASS;
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return ItemStack.EMPTY;
	}
	
	private void fillBottle(PlayerEntity player, int startIndex) {
		PlayerInventory inv = player.inventory;
		for(int i = startIndex ; i < inv.getSizeInventory(); i++) {
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot.getItem() == Items.GLASS_BOTTLE) {
				stackInSlot.shrink(1);
				
				ItemStack give = new ItemStack(BottledCloudModule.bottled_cloud);
				if(!player.addItemStackToInventory(give))
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
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new CloudTileEntity();
	}

}
