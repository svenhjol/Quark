package vazkii.quark.base.network.message;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;
import vazkii.quark.tweaks.client.emote.EmoteHandler;

import java.util.UUID;

public class DoEmoteMessage implements IMessage {

	private static final long serialVersionUID = -7952633556330869633L;
	
	public String emote;
	public UUID playerUUID;
	public int tier;
	
	public DoEmoteMessage() { }
	
	public DoEmoteMessage(String emote, UUID playerUUID, int tier) {
		this.emote = emote;
		this.playerUUID = playerUUID;
		this.tier = tier;
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public boolean receive(Context context) {
		context.enqueueWork(() -> {
			World world = MinecraftClient.getInstance().world;
			PlayerEntity player = world.getPlayerByUuid(playerUUID);
			EmoteHandler.putEmote(player, emote, tier);
		});
		
		return true;
	}

}
