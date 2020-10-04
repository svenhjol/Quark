package vazkii.quark.oddities.container;

import javax.annotation.Nonnull;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import vazkii.quark.oddities.module.MatrixEnchantingModule;
import vazkii.quark.oddities.tile.MatrixEnchantingTableTileEntity;

public class MatrixEnchantingContainer extends ScreenHandler {

	public final MatrixEnchantingTableTileEntity enchanter;

	public MatrixEnchantingContainer(int id, PlayerInventory playerInv, MatrixEnchantingTableTileEntity tile) {
		super(MatrixEnchantingModule.containerType, id);
		enchanter = tile;

		// Item Slot
		addSlot(new Slot(tile, 0, 15, 20) {
			@Override 
			public int getMaxStackAmount() { 
				return 1; 
			}
		});

		// Lapis Slot
		addSlot(new Slot(tile, 1, 15, 44) {
			@Override
			public boolean canInsert(ItemStack stack) {
				return isLapis(stack);
			}
		});

		// Output Slot
		addSlot(new Slot(tile, 2, 59, 32) {
			@Override
			public boolean canInsert(ItemStack stack) {
				return false;
			}

			@Nonnull
			@Override
			public ItemStack onTakeItem(PlayerEntity thePlayer, @Nonnull ItemStack stack) {
				finish(thePlayer, stack);
				return super.onTakeItem(thePlayer, stack);
			}
		});

		// Player Inv
		for(int i = 0; i < 3; ++i)
			for(int j = 0; j < 9; ++j)
				addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
		for(int k = 0; k < 9; ++k)
			addSlot(new Slot(playerInv, k, 8 + k * 18, 142));
	}
	
	public static MatrixEnchantingContainer fromNetwork(int windowId, PlayerInventory playerInventory, PacketByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		MatrixEnchantingTableTileEntity te = (MatrixEnchantingTableTileEntity) playerInventory.player.world.getBlockEntity(pos);
		return new MatrixEnchantingContainer(windowId, playerInventory, te);
	}

	private boolean isLapis(ItemStack stack) {
		return stack.getItem().isIn(Tags.Items.GEMS_LAPIS);
	}

	private void finish(PlayerEntity player, ItemStack stack) {
		enchanter.setStack(0, ItemStack.EMPTY);

		player.incrementStat(Stats.ENCHANT_ITEM);

		if(player instanceof ServerPlayerEntity)
			Criteria.ENCHANTED_ITEM.trigger((ServerPlayerEntity) player, stack, 1);

		player.world.playSound(null, enchanter.getPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F,  player.world.random.nextFloat() * 0.1F + 0.9F);
	}

	@Override
	public boolean canUse(@Nonnull PlayerEntity playerIn) {
		World world = enchanter.getWorld();
		BlockPos pos = enchanter.getPos();
		if(world.getBlockState(pos).getBlock() != Blocks.ENCHANTING_TABLE)
			return false;
		else
			return playerIn.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
	}

	@Nonnull
	@Override
	public ItemStack transferSlot(PlayerEntity playerIn, int index) {
		ItemStack originalStack = ItemStack.EMPTY;
		Slot slot = slots.get(index);

		if (slot != null && slot.hasStack()) {
			ItemStack stackInSlot = slot.getStack();
			originalStack = stackInSlot.copy();

			if(index < 3) {
				if (!insertItem(stackInSlot, 3, 39, true))
					return ItemStack.EMPTY;
			}
			else if(isLapis(stackInSlot)) {
				if(!insertItem(stackInSlot, 1, 2, true))
					return ItemStack.EMPTY;
			}
			else {
				if(slots.get(0).hasStack() || !slots.get(0).canInsert(stackInSlot))
					return ItemStack.EMPTY;

				if(stackInSlot.hasTag()) // Forge: Fix MC-17431
					slots.get(0).setStack(stackInSlot.split(1));

				else if(!stackInSlot.isEmpty()) {
					slots.get(0).setStack(new ItemStack(stackInSlot.getItem(), 1));
					stackInSlot.decrement(1);
				}
			}

			if(stackInSlot.isEmpty())
				slot.setStack(ItemStack.EMPTY);
			else slot.markDirty();

			if(stackInSlot.getCount() == originalStack.getCount())
				return ItemStack.EMPTY;

			slot.onTakeItem(playerIn, stackInSlot);
		}

		return originalStack;
	}

}