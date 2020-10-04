package vazkii.quark.management.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.management.module.ChestsInBoatsModule;

import javax.annotation.Nonnull;

public class ChestPassengerEntity extends Entity implements Inventory {

	private final DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
	
	private static final TrackedData<ItemStack> CHEST_TYPE = DataTracker.registerData(ChestPassengerEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final String TAG_CHEST_TYPE = "chestType";

	public ChestPassengerEntity(EntityType<? extends ChestPassengerEntity> type, World worldIn) {
		super(type, worldIn);
		noClip = true;
	}
	
	public ChestPassengerEntity(World worldIn, ItemStack stack) {
		this(ChestsInBoatsModule.chestPassengerEntityType, worldIn);

		ItemStack newStack = stack.copy();
		newStack.setCount(1);
		dataTracker.set(CHEST_TYPE, newStack);
	}

	@Override
	protected void initDataTracker() {
		dataTracker.startTracking(CHEST_TYPE, new ItemStack(Blocks.CHEST));
	}

	@Override
	public void tick() {
		super.tick();
		
		if(!isAlive())
			return;
		
		if(!hasVehicle() && !world.isClient)
			remove();

		Entity riding = getVehicle();
		if (riding != null) {
			yaw = riding.prevYaw;
		}
	}
	
	@Override
	public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public int size() {
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
	public ItemStack getStack(int index) {
		return items.get(index);
	}

	@Nonnull
	@Override
	public ItemStack removeStack(int index, int count) {
		return Inventories.splitStack(items, index, count);
	}

	@Nonnull
	@Override
	public ItemStack removeStack(int index) {
		ItemStack itemstack = items.get(index);

		if(itemstack.isEmpty())
			return ItemStack.EMPTY;
		else {
			items.set(index, ItemStack.EMPTY);
			return itemstack;
		}
	}

	@Override
	public void setStack(int index, @Nonnull ItemStack stack) {
		items.set(index, stack);
	}

	@Override
	public int getMaxCountPerStack() {
		return 64;
	}

	@Override
	public void markDirty() {
		// NO-OP
	}

	@Override
	public boolean canPlayerUse(@Nonnull PlayerEntity player) {
		return isAlive() && player.squaredDistanceTo(this) <= 64;
	}

	@Override
	public void onOpen(@Nonnull PlayerEntity player) {
		// NO-OP
	}

	@Override
	public void onClose(@Nonnull PlayerEntity player) {
		// NO-OP
	}

	@Override
	public boolean isValid(int index, @Nonnull ItemStack stack) {
		return true;
	}

	@Override
	public void clear() {
		items.clear();
	}

	@Override
	protected void readCustomDataFromTag(@Nonnull CompoundTag compound) {
		Inventories.fromTag(compound, items);

		CompoundTag itemCmp = compound.getCompound(TAG_CHEST_TYPE);
		ItemStack stack = ItemStack.fromTag(itemCmp);
		if(!stack.isEmpty())
			dataTracker.set(CHEST_TYPE, stack);

	}

	@Override
	protected void writeCustomDataToTag(@Nonnull CompoundTag compound) {
		Inventories.toTag(compound, items);

		CompoundTag itemCmp = new CompoundTag();
		dataTracker.get(CHEST_TYPE).toTag(itemCmp);
		compound.put(TAG_CHEST_TYPE, itemCmp);

	}

	@Nonnull
	@Override
	public Packet<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void remove() {
		if(!world.isClient) {
			ItemScatterer.spawn(world, this, this);
			dropStack(getChestType());
		}
		
		super.remove();
	}
	
	public ItemStack getChestType() {
		return dataTracker.get(CHEST_TYPE);
	}

}
