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
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.client.block.ChestAnimationProgress;
import net.minecraft.network.PacketDeflater;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.text.LiteralText;
import vazkii.arl.util.VanillaPacketDispatcher;

public abstract class TileMod extends TripwireHookBlock {

	public TileMod(TurtleEggBlock<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Nonnull
	@Override
	public PacketDeflater a(PacketDeflater par1nbtTagCompound) {
		PacketDeflater nbt = super.a(par1nbtTagCompound);

		writeSharedNBT(par1nbtTagCompound);
		return nbt;
	}

	@Override
	public void a(ChestAnimationProgress p_230337_1_, PacketDeflater p_230337_2_) {
		super.a(p_230337_1_, p_230337_2_);

		readSharedNBT(p_230337_2_);
	}

	public void writeSharedNBT(PacketDeflater cmp) {
		// NO-OP
	}

	public void readSharedNBT(PacketDeflater cmp) {
		// NO-OP
	}
	
	public void sync() {
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}
	
	@Override
	public LightUpdateS2CPacket a() {
		PacketDeflater cmp = new PacketDeflater();
		writeSharedNBT(cmp);
		return new LightUpdateS2CPacket(o(), 0, cmp);
	}
	
	@Override
	public void onDataPacket(LiteralText net, LightUpdateS2CPacket packet) {
		super.onDataPacket(net, packet);
		readSharedNBT(packet.d());
	}

}
