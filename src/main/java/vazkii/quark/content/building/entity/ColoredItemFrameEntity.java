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
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.content.building.module.ItemFramesModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ColoredItemFrameEntity extends ItemFrameEntity implements IEntityAdditionalSpawnData {

	private static final DataParameter<Integer> COLOR = EntityDataManager.createKey(ColoredItemFrameEntity.class, DataSerializers.VARINT);
	private static final String TAG_COLOR = "DyeColor";

	private boolean didHackery = false;
	
	public ColoredItemFrameEntity(EntityType<? extends ColoredItemFrameEntity> type, World worldIn) {
		super(type, worldIn);
	}

	public ColoredItemFrameEntity(World worldIn, BlockPos blockPos, Direction face, int color) {
		super(ItemFramesModule.coloredFrameEntity, worldIn);
		hangingPosition = blockPos;
		this.updateFacingWithBoundingBox(face);
		dataManager.set(COLOR, color);
	}

	@Override
	protected void registerData() {
		super.registerData();

		dataManager.register(COLOR, 0);
	}

	public DyeColor getColor() {
		return DyeColor.byId(getColorIndex());
	}

	public int getColorIndex() {
		return dataManager.get(COLOR);
	}

	@Nullable
	@Override
	public ItemEntity entityDropItem(@Nonnull ItemStack stack, float offset) {
		if (stack.getItem() == Items.ITEM_FRAME && !didHackery) {
			stack = new ItemStack(ItemFramesModule.getColoredFrame(getColor()));
			didHackery = true;
		}
			
		return super.entityDropItem(stack, offset);
	}

	@Nonnull
	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		ItemStack held = getDisplayedItem();
		if (held.isEmpty())
			return new ItemStack(ItemFramesModule.getColoredFrame(getColor()));
		else
			return held.copy();
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putInt(TAG_COLOR, getColorIndex());
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		dataManager.set(COLOR, compound.getInt(TAG_COLOR));
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeVarInt(this.getColorIndex());
		buffer.writeBlockPos(this.hangingPosition);
		buffer.writeVarInt(this.facingDirection.getIndex());
	}

	@Override
	public void readSpawnData(PacketBuffer buffer) {
		dataManager.set(COLOR, buffer.readVarInt());
		this.hangingPosition = buffer.readBlockPos();
		this.updateFacingWithBoundingBox(Direction.byIndex(buffer.readVarInt()));
	}
}
