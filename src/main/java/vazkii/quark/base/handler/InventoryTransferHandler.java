package vazkii.quark.base.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import vazkii.quark.api.ITransferManager;
import vazkii.quark.api.QuarkCapabilities;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.management.module.EasyTransferingModule;

public class InventoryTransferHandler {

	public static void transfer(PlayerEntity player, boolean isRestock, boolean smart) {
		if(!ModuleLoader.INSTANCE.isModuleEnabled(EasyTransferingModule.class) || player.isSpectator() || !accepts(player.currentScreenHandler, player))
			return;

		//		if(!useContainer && !player.getEntityWorld().getWorldInfo().getGameRulesInstance().getBoolean(StoreToChests.GAME_RULE)) {
		//			disableClientDropoff(player);
		//			return;
		//		}

		Transfer transfer = isRestock ? new Restock(player, smart) : new Transfer(player, smart);
		transfer.execute();
	}

	//	public static void disableClientDropoff(PlayerEntity player) {
	//		if(player instanceof ServerPlayerEntity)
	//			NetworkHandler.INSTANCE.sendTo(new MessageDisableDropoffClient(), (ServerPlayerEntity) player);
	//	}
	//
	//	public static IItemHandler getInventory(PlayerEntity player, World world, BlockPos pos) {
	//		TileEntity te = world.getTileEntity(pos);
	//
	//		if(te == null)
	//			return null;
	//
	//		boolean accept = isValidChest(player, te);
	//		if(accept) {
	//			Supplier<IItemHandler> supplier = () -> {
	//				IItemHandler innerRet = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null);
	//				if(innerRet == null && te instanceof IInventory)
	//					innerRet = new InvWrapper((IInventory) te);
	//
	//				return innerRet;
	//			};
	//
	//			if(hasProvider(te))
	//				return getProvider(te).getTransferItemHandler(supplier);
	//			else return supplier.get();
	//		}
	//
	//		return null;
	//	}

	private static boolean hasProvider(Object te) {
		return te instanceof BlockEntity && ((BlockEntity) te).getCapability(QuarkCapabilities.TRANSFER).isPresent();
	}

	private static ITransferManager getProvider(Object te) {
		return ((BlockEntity) te).getCapability(QuarkCapabilities.TRANSFER).orElse(null);
	}


	public static boolean accepts(ScreenHandler container, PlayerEntity player) {
		if (hasProvider(container))
			return getProvider(container).acceptsTransfer(player);

		return container.slots.size() - player.inventory.main.size() >= 27;
	}

	public static class Transfer {

		public final PlayerEntity player;
		public final boolean smart;
		//		public final boolean useContainer;

		public final List<Pair<IItemHandler, Double>> itemHandlers = new ArrayList<>();

		public Transfer(PlayerEntity player, boolean smart/*, boolean useContainer*/) {
			this.player = player;
			//			this.useContainer = useContainer;
			this.smart = smart;
		}

		public void execute() {
			locateItemHandlers();

			if(itemHandlers.isEmpty())
				return;

			if(smart)
				smartTransfer();
			else roughTransfer();

			player.playerScreenHandler.sendContentUpdates();
			//			if(useContainer)
			player.currentScreenHandler.sendContentUpdates();
		}

		public void smartTransfer() {
			transfer((stack, handler) -> {
				int slots = handler.getSlots();
				for(int i = 0; i < slots; i++) {
					ItemStack stackAt = handler.getStackInSlot(i);
					if(stackAt.isEmpty())
						continue;

					boolean itemEqual = stack.getItem() == stackAt.getItem();
					boolean damageEqual = stack.getDamage() == stackAt.getDamage();
					boolean nbtEqual = ItemStack.areTagsEqual(stackAt, stack);

					if(itemEqual && damageEqual && nbtEqual)
						return true;

					if(stack.isDamageable() && stack.getMaxCount() == 1 && itemEqual && nbtEqual)
						return true;
				}

				return false;
			});
		}

		public void roughTransfer() {
			transfer((stack, handler) -> true);
		}

		public void locateItemHandlers() {
			//			if(useContainer) {
			ScreenHandler c = player.currentScreenHandler;
			for(Slot s : c.slots) {
				Inventory inv = s.inventory;
				if(inv != player.inventory) {
					itemHandlers.add(Pair.of(ContainerWrapper.provideWrapper(s, c), 0.0));
					break;
				}
			}
			//			} else {
			//				BlockPos playerPos = player.getPosition();
			//				int range = 6;
			//
			//				for(int i = -range; i < range * 2 + 1; i++)
			//					for(int j = -range; j < range * 2 + 1; j++)
			//						for(int k = -range; k < range * 2 + 1; k++) {
			//							BlockPos pos = playerPos.add(i, j, k);
			//							findHandler(pos);
			//						}
			//
			//				itemHandlers.sort(Comparator.comparingDouble(Pair::getRight));
			//			}
		}

		//		public void findHandler(BlockPos pos) {
		//			IItemHandler handler = getInventory(player, player.getEntityWorld(), pos);
		//			if(handler != null)
		//				itemHandlers.add(Pair.of(handler, player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
		//		}

		public void transfer(TransferPredicate predicate) {
			PlayerInventory inv = player.inventory;

			for(int i = PlayerInventory.getHotbarSize(); i < inv.main.size(); i++) {
				ItemStack stackAt = inv.getStack(i);

				if(!stackAt.isEmpty()/* && !FavoriteItems.isItemFavorited(stackAt)*/) {
					ItemStack ret = insert(stackAt, predicate);
					if(!ItemStack.areEqual(stackAt, ret))
						inv.setStack(i, ret);
				}
			}
		}

		public ItemStack insert(ItemStack stack, TransferPredicate predicate) {
			ItemStack ret = stack.copy();
			for(Pair<IItemHandler, Double> pair : itemHandlers) {
				IItemHandler handler = pair.getLeft();
				ret = insertInHandler(handler, ret, predicate);
				if(ret.isEmpty())
					return ItemStack.EMPTY;
			}

			return ret;
		}

		public ItemStack insertInHandler(IItemHandler handler, final ItemStack stack, TransferPredicate predicate) {
			if(predicate.test(stack, handler)) {
				ItemStack retStack = ItemHandlerHelper.insertItemStacked(handler, stack, false);
				if(!retStack.isEmpty())
					retStack = retStack.copy();
				else 
					return retStack;

				return retStack;
			}

			return stack;
		}

	}

	public static class Restock extends Transfer {

		public Restock(PlayerEntity player, boolean filtered) {
			super(player, filtered);
		}

		@Override
		public void transfer(TransferPredicate predicate) {
			IItemHandler inv = itemHandlers.get(0).getLeft();
			IItemHandler playerInv = new PlayerInvWrapper(player.inventory);

			for(int i = inv.getSlots() - 1; i >= 0; i--) {
				ItemStack stackAt = inv.getStackInSlot(i);

				if(!stackAt.isEmpty()) {
					ItemStack copy = stackAt.copy();
					ItemStack ret = insertInHandler(playerInv, copy, predicate);

					if(!ItemStack.areEqual(stackAt, ret)) {
						inv.extractItem(i, stackAt.getMaxCount(), false);
						if(!ret.isEmpty())
							inv.insertItem(i, ret, false);
					}
				}
			}
		}
	}

	public static class PlayerInvWrapper extends InvWrapper {

		public PlayerInvWrapper(Inventory inv) {
			super(inv);
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if(stack.isEmpty())
				stack = stack.copy();

			return super.insertItem(slot, stack, simulate);
		}

		@Override
		public int getSlots() {
			return super.getSlots() - 5;
		}

	}

	public static class ContainerWrapper extends InvWrapper {

		private final ScreenHandler container;

		public static IItemHandler provideWrapper(Slot slot, ScreenHandler container) {
			if (slot instanceof SlotItemHandler) {
				IItemHandler handler = ((SlotItemHandler) slot).getItemHandler();
				if (hasProvider(handler)) {
					return getProvider(handler).getTransferItemHandler(() -> handler);
				} else {
					return handler;
				}
			} else {
				return provideWrapper(slot.inventory, container);
			}
		}

		public static IItemHandler provideWrapper(Inventory inv, ScreenHandler container) {
			if(hasProvider(inv))
				return getProvider(inv).getTransferItemHandler(() -> new ContainerWrapper(inv, container));
			return new ContainerWrapper(inv, container);
		}

		private ContainerWrapper(Inventory inv, ScreenHandler container) {
			super(inv);
			this.container = container;
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			Slot containerSlot = getSlot(slot);
			if(containerSlot == null || !containerSlot.canInsert(stack))
				return stack;

			return super.insertItem(slot, stack, simulate);
		}

		private Slot getSlot(int slotId) {
			Inventory inv = getInv();
			for(Slot slot : container.slots)
				if(slot.inventory == inv && slot.getSlotIndex() == slotId)
					return slot;

			return null;
		}

	}

	public interface TransferPredicate extends BiPredicate<ItemStack, IItemHandler> { }

}