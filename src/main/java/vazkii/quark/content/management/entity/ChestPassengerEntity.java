package vazkii.quark.content.management.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.content.management.module.ChestsInBoatsModule;

import javax.annotation.Nonnull;

public class ChestPassengerEntity extends Entity implements IInventory {

	private final NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
	
	private static final DataParameter<ItemStack> CHEST_TYPE = EntityDataManager.createKey(ChestPassengerEntity.class, DataSerializers.ITEMSTACK);
	private static final String TAG_CHEST_TYPE = "chestType";

	public ChestPassengerEntity(EntityType<? extends ChestPassengerEntity> type, World worldIn) {
		super(type, worldIn);
		noClip = true;
	}
	
	public ChestPassengerEntity(World worldIn, ItemStack stack) {
		this(ChestsInBoatsModule.chestPassengerEntityType, worldIn);

		ItemStack newStack = stack.copy();
		newStack.setCount(1);
		dataManager.set(CHEST_TYPE, newStack);
	}

	@Override
	protected void registerData() {
		dataManager.register(CHEST_TYPE, new ItemStack(Blocks.CHEST));
	}

	@Override
	public void tick() {
		super.tick();
		
		if(!isAlive())
			return;
		
		if(!isPassenger() && !world.isRemote)
			remove();

		Entity riding = getRidingEntity();
		if (riding != null) {
			rotationYaw = riding.prevRotationYaw;
		}
	}
	
	@Override
	public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
		return false;
	}

	@Override
	public boolean canBeAttackedWithItem() {
		return false;
	}

	@Override
	public int getSizeInventory() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		for(ItemStack itemstack : items)
			if(!itemstack.isEmpty())
				return false;

		return true;
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int index) {
		return items.get(index);
	}

	@Nonnull
	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(items, index, count);
	}

	@Nonnull
	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack itemstack = items.get(index);

		if(itemstack.isEmpty())
			return ItemStack.EMPTY;
		else {
			items.set(index, ItemStack.EMPTY);
			return itemstack;
		}
	}

	@Override
	public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
		items.set(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
		// NO-OP
	}

	@Override
	public boolean isUsableByPlayer(@Nonnull PlayerEntity player) {
		return isAlive() && player.getDistanceSq(this) <= 64;
	}

	@Override
	public void openInventory(@Nonnull PlayerEntity player) {
		// NO-OP
	}

	@Override
	public void closeInventory(@Nonnull PlayerEntity player) {
		// NO-OP
	}

	@Override
	public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
		return true;
	}

	@Override
	public void clear() {
		items.clear();
	}

	@Override
	protected void readAdditional(@Nonnull CompoundNBT compound) {
		ItemStackHelper.loadAllItems(compound, items);

		CompoundNBT itemCmp = compound.getCompound(TAG_CHEST_TYPE);
		ItemStack stack = ItemStack.read(itemCmp);
		if(!stack.isEmpty())
			dataManager.set(CHEST_TYPE, stack);

	}

	@Override
	protected void writeAdditional(@Nonnull CompoundNBT compound) {
		ItemStackHelper.saveAllItems(compound, items);

		CompoundNBT itemCmp = new CompoundNBT();
		dataManager.get(CHEST_TYPE).write(itemCmp);
		compound.put(TAG_CHEST_TYPE, itemCmp);

	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void remove() {
		if(!world.isRemote) {
			InventoryHelper.dropInventoryItems(world, this, this);
			entityDropItem(getChestType());
		}
		
		super.remove();
	}
	
	public ItemStack getChestType() {
		return dataManager.get(CHEST_TYPE);
	}

}
