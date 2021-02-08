package vazkii.quark.content.building.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
		attachmentPos = blockPos;
		this.setFacing(face);
	}

	@Override
	public boolean canStayAttached() {
		return super.canStayAttached() || isOnSign();
	}

	public BlockPos getBehindPos() {
		return attachmentPos.offset(facing.getOpposite());
	}
	
	public boolean isOnSign() {
		BlockState blockstate = world.getBlockState(getBehindPos());
		return blockstate.getBlock().isIn(BlockTags.STANDING_SIGNS);
	}

	@Nullable
	@Override
	public ItemEntity dropStack(@Nonnull ItemStack stack, float offset) {
		if (stack.getItem() == Items.ITEM_FRAME && !didHackery) {
			stack = new ItemStack(ItemFramesModule.glassFrame);
			didHackery = true;
		}

		return super.dropStack(stack, offset);
	}

	@Nonnull
	@Override
	public ItemStack getPickedResult(HitResult target) {
		ItemStack held = getHeldItemStack();
		if (held.isEmpty())
			return new ItemStack(ItemFramesModule.glassFrame);
		else
			return held.copy();
	}

	@Nonnull
	@Override
	public Packet<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketByteBuf buffer) {
		buffer.writeBlockPos(this.attachmentPos);
		buffer.writeVarInt(this.facing.getId());
	}

	@Override
	public void readSpawnData(PacketByteBuf buffer) {
		this.attachmentPos = buffer.readBlockPos();
		this.setFacing(Direction.byId(buffer.readVarInt()));
	}
}
