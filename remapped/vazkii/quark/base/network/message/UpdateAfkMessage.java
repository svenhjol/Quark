package vazkii.quark.base.network.message;

import net.minecraftforge.fml.network.NetworkEvent;
import vazkii.arl.network.IMessage;
import vazkii.quark.content.tweaks.module.ImprovedSleepingModule;

public class UpdateAfkMessage implements IMessage {

	private static final long serialVersionUID = -6449994327791980078L;

	public boolean afk;
	
	public UpdateAfkMessage() { }

	public UpdateAfkMessage(boolean afk) {
		this.afk = afk;
	}

	@Override
	public boolean receive(NetworkEvent.Context context) {
		context.enqueueWork(() -> ImprovedSleepingModule.updateAfk(context.getSender(), afk));
		return true;
	}

}
