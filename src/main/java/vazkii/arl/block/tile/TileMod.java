/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 21, 2014, 9:18:28 PM (GMT)]
 */
package vazkii.arl.block.tile;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import vazkii.arl.util.VanillaPacketDispatcher;

public abstract class TileMod extends BlockEntity {

	public TileMod(BlockEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Nonnull
	@Override
	public CompoundTag toTag(CompoundTag par1nbtTagCompound) {
		CompoundTag nbt = super.toTag(par1nbtTagCompound);

		writeSharedNBT(par1nbtTagCompound);
		return nbt;
	}

	@Override
	public void fromTag(BlockState p_230337_1_, CompoundTag p_230337_2_) {
		super.fromTag(p_230337_1_, p_230337_2_);

		readSharedNBT(p_230337_2_);
	}

	public void writeSharedNBT(CompoundTag cmp) {
		// NO-OP
	}

	public void readSharedNBT(CompoundTag cmp) {
		// NO-OP
	}
	
	public void sync() {
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}
	
	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		CompoundTag cmp = new CompoundTag();
		writeSharedNBT(cmp);
		return new BlockEntityUpdateS2CPacket(getPos(), 0, cmp);
	}
	
	@Override
	public void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket packet) {
		super.onDataPacket(net, packet);
		readSharedNBT(packet.getCompoundTag());
	}

}
