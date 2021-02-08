/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [May 14, 2019, 12:53 AM (EST)]
 */
package vazkii.quark.base.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import vazkii.arl.network.IMessage;
import vazkii.quark.content.automation.base.ChainHandler;
import vazkii.quark.content.automation.module.ChainLinkageModule;

import java.util.UUID;

public class SyncChainMessage implements IMessage {

	private static final long serialVersionUID = -2700317487934809872L;

	public static final UUID NULL_UUID = new UUID(0, 0);

	public int vehicle;
	public UUID other;

	public SyncChainMessage() {
		// NO-OP
	}

	public SyncChainMessage(int vehicle, UUID other) {
		this.vehicle = vehicle;
		this.other = other == null ? NULL_UUID : other;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean receive(NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			World world = Minecraft.getInstance().world;
			if (world != null) {
				Entity boatEntity = world.getEntityByID(vehicle);
				if (boatEntity == null)
					ChainLinkageModule.queueChainUpdate(vehicle, other);
				else
					ChainHandler.setLink(boatEntity, other, false);
			}
		});
		return true;
	}
}
