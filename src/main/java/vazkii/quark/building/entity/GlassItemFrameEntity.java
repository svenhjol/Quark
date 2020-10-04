package vazkii.quark.building.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.building.module.ItemFramesModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
