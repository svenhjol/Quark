package vazkii.quark.content.building.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.content.building.module.ItemFramesModule;

public class GlassItemFrameEntity extends ItemFrameEntity implements IEntityAdditionalSpawnData {

	private boolean didHackery = false;

	public GlassItemFrameEntity(EntityType<? extends GlassItemFrameEntity> type, World worldIn) {
		super(type, worldIn);
	}

	public GlassItemFrameEntity(World worldIn, BlockPos blockPos, Direction face) {
		super(ItemFramesModule.glassFrameEntity, worldIn);
		hangingPosition = blockPos;
		this.updateFacingWithBoundingBox(face);
	}

	@Override
	public boolean onValidSurface() {
		return super.onValidSurface() || isOnSign();
	}

	public BlockPos getBehindPos() {
		return hangingPosition.offset(facingDirection.getOpposite());
	}
	
	public boolean isOnSign() {
		BlockState blockstate = world.getBlockState(getBehindPos());
		return blockstate.getBlock().isIn(BlockTags.STANDING_SIGNS);
	}

	@Nullable
	@Override
	public ItemEntity entityDropItem(@Nonnull ItemStack stack, float offset) {
		if (stack.getItem() == Items.ITEM_FRAME && !didHackery) {
			stack = new ItemStack(ItemFramesModule.glassFrame);
			didHackery = true;
		}

		return super.entityDropItem(stack, offset);
	}

	@Nonnull
	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		ItemStack held = getDisplayedItem();
		if (held.isEmpty())
			return new ItemStack(ItemFramesModule.glassFrame);
		else
			return held.copy();
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeBlockPos(this.hangingPosition);
		buffer.writeVarInt(this.facingDirection.getIndex());
	}

	@Override
	public void readSpawnData(PacketBuffer buffer) {
		this.hangingPosition = buffer.readBlockPos();
		this.updateFacingWithBoundingBox(Direction.byIndex(buffer.readVarInt()));
	}
}
