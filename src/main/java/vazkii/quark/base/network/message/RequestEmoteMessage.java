package vazkii.quark.base.network.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;
import vazkii.quark.base.handler.ContributorRewardHandler;
import vazkii.quark.base.network.QuarkNetwork;

public class RequestEmoteMessage implements IMessage {

	private static final long serialVersionUID = -8569122937119059414L;
	
	public String emote;
	
	public RequestEmoteMessage() { }
	
	public RequestEmoteMessage(String emote) {
		this.emote = emote;
	}
	
	@Override
	public boolean receive(Context context) {
		ServerPlayerEntity player = context.getSender();
		if (player != null && player.server != null)
			context.enqueueWork(() -> QuarkNetwork.sendToAllPlayers(
					new DoEmoteMessage(emote, player.getUniqueID(), ContributorRewardHandler.getTier(player)),
					player.server));
		return true;
	}

}
