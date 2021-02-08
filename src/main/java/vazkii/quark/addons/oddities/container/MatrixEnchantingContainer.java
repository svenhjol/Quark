package vazkii.quark.addons.oddities.container;

import javax.annotation.Nonnull;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import vazkii.quark.addons.oddities.module.MatrixEnchantingModule;
import vazkii.quark.addons.oddities.tile.MatrixEnchantingTableTileEntity;

public class MatrixEnchantingContainer extends Container {

	public final MatrixEnchantingTableTileEntity enchanter;

	public MatrixEnchantingContainer(int id, PlayerInventory playerInv, MatrixEnchantingTableTileEntity tile) {
		super(MatrixEnchantingModule.containerType, id);
		enchanter = tile;

		// Item Slot
		addSlot(new Slot(tile, 0, 15, 20) {
			@Override 
			public int getSlotStackLimit() { 
				return 1; 
			}
		});

		// Lapis Slot
		addSlot(new Slot(tile, 1, 15, 44) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return isLapis(stack);
			}
		});

		// Output Slot
		addSlot(new Slot(tile, 2, 59, 32) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return false;
			}

			@Nonnull
			@Override
			public ItemStack onTake(PlayerEntity thePlayer, @Nonnull ItemStack stack) {
				finish(thePlayer, stack);
				return super.onTake(thePlayer, stack);
			}
		});

		// Player Inv
		for(int i = 0; i < 3; ++i)
			for(int j = 0; j < 9; ++j)
				addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
		for(int k = 0; k < 9; ++k)
			addSlot(new Slot(playerInv, k, 8 + k * 18, 142));
	}
	
	public static MatrixEnchantingContainer fromNetwork(int windowId, PlayerInventory playerInventory, PacketBuffer buf) {
		BlockPos pos = buf.readBlockPos();
		MatrixEnchantingTableTileEntity te = (MatrixEnchantingTableTileEntity) playerInventory.player.world.getTileEntity(pos);
		return new MatrixEnchantingContainer(windowId, playerInventory, te);
	}

	private boolean isLapis(ItemStack stack) {
		return stack.getItem().isIn(Tags.Items.GEMS_LAPIS);
	}

	private void finish(PlayerEntity player, ItemStack stack) {
		enchanter.setInventorySlotContents(0, ItemStack.EMPTY);

		player.addStat(Stats.ENCHANT_ITEM);

		if(player instanceof ServerPlayerEntity)
			CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayerEntity) player, stack, 1);

		player.world.playSound(null, enchanter.getPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F,  player.world.rand.nextFloat() * 0.1F + 0.9F);
	}

	@Override
	public boolean canInteractWith(@Nonnull PlayerEntity playerIn) {
		World world = enchanter.getWorld();
		BlockPos pos = enchanter.getPos();
		if(world.getBlockState(pos).getBlock() != Blocks.ENCHANTING_TABLE)
			return false;
		else
			return playerIn.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack originalStack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack stackInSlot = slot.getStack();
			originalStack = stackInSlot.copy();

			if(index < 3) {
				if (!mergeItemStack(stackInSlot, 3, 39, true))
					return ItemStack.EMPTY;
			}
			else if(isLapis(stackInSlot)) {
				if(!mergeItemStack(stackInSlot, 1, 2, true))
					return ItemStack.EMPTY;
			}
			else {
				if(inventorySlots.get(0).getHasStack() || !inventorySlots.get(0).isItemValid(stackInSlot))
					return ItemStack.EMPTY;

				if(stackInSlot.hasTag()) // Forge: Fix MC-17431
					inventorySlots.get(0).putStack(stackInSlot.split(1));

				else if(!stackInSlot.isEmpty()) {
					inventorySlots.get(0).putStack(new ItemStack(stackInSlot.getItem(), 1));
					stackInSlot.shrink(1);
				}
			}

			if(stackInSlot.isEmpty())
				slot.putStack(ItemStack.EMPTY);
			else slot.onSlotChanged();

			if(stackInSlot.getCount() == originalStack.getCount())
				return ItemStack.EMPTY;

			slot.onTake(playerIn, stackInSlot);
		}

		return originalStack;
	}

}