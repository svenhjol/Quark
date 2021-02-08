/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [19/06/2016, 23:52:04 (GMT)]
 */
package vazkii.quark.content.building.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.content.building.module.ItemFramesModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ColoredItemFrameEntity extends ItemFrameEntity implements IEntityAdditionalSpawnData {

	private static final TrackedData<Integer> COLOR = DataTracker.registerData(ColoredItemFrameEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final String TAG_COLOR = "DyeColor";

	private boolean didHackery = false;
	
	public ColoredItemFrameEntity(EntityType<? extends ColoredItemFrameEntity> type, World worldIn) {
		super(type, worldIn);
	}

	public ColoredItemFrameEntity(World worldIn, BlockPos blockPos, Direction face, int color) {
		super(ItemFramesModule.coloredFrameEntity, worldIn);
		attachmentPos = blockPos;
		this.setFacing(face);
		dataTracker.set(COLOR, color);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();

		dataTracker.startTracking(COLOR, 0);
	}

	public DyeColor getColor() {
		return DyeColor.byId(getColorIndex());
	}

	public int getColorIndex() {
		return dataTracker.get(COLOR);
	}

	@Nullable
	@Override
	public ItemEntity dropStack(@Nonnull ItemStack stack, float offset) {
		if (stack.getItem() == Items.ITEM_FRAME && !didHackery) {
			stack = new ItemStack(ItemFramesModule.getColoredFrame(getColor()));
			didHackery = true;
		}
			
		return super.dropStack(stack, offset);
	}

	@Nonnull
	@Override
	public ItemStack getPickedResult(HitResult target) {
		ItemStack held = getHeldItemStack();
		if (held.isEmpty())
			return new ItemStack(ItemFramesModule.getColoredFrame(getColor()));
		else
			return held.copy();
	}

	@Override
	public void writeCustomDataToTag(CompoundTag compound) {
		super.writeCustomDataToTag(compound);
		compound.putInt(TAG_COLOR, getColorIndex());
	}

	@Override
	public void readCustomDataFromTag(CompoundTag compound) {
		super.readCustomDataFromTag(compound);
		dataTracker.set(COLOR, compound.getInt(TAG_COLOR));
	}

	@Nonnull
	@Override
	public Packet<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketByteBuf buffer) {
		buffer.writeVarInt(this.getColorIndex());
		buffer.writeBlockPos(this.attachmentPos);
		buffer.writeVarInt(this.facing.getId());
	}

	@Override
	public void readSpawnData(PacketByteBuf buffer) {
		dataTracker.set(COLOR, buffer.readVarInt());
		this.attachmentPos = buffer.readBlockPos();
		this.setFacing(Direction.byId(buffer.readVarInt()));
	}
}
