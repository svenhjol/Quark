package vazkii.quark.base.network.message;

import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;
import vazkii.quark.base.handler.SortingHandler;

public class SortInventoryMessage implements IMessage {

	private static final long serialVersionUID = -4340505435110793951L;

	public boolean forcePlayer;
	
	public SortInventoryMessage() { }
	
	public SortInventoryMessage(boolean forcePlayer) { 
		this.forcePlayer = forcePlayer;
	}
	
	@Override
	public boolean receive(Context context) {
		context.enqueueWork(() -> SortingHandler.sortInventory(context.getSender(), forcePlayer));
		return true;
	}

}
