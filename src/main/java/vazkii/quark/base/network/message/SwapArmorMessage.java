package vazkii.quark.base.network.message;

import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;
import vazkii.quark.content.management.module.RightClickArmorModule;

public class SwapArmorMessage implements IMessage {

	private static final long serialVersionUID = 249125038970669436L;
	
	public int slot;
	
	public SwapArmorMessage() { }
	
	public SwapArmorMessage(int slot) { 
		this.slot = slot;
	}
		
	@Override
	public boolean receive(Context context) {
		context.enqueueWork(() -> {
			RightClickArmorModule.swap(context.getSender(), slot);
		});
		
		return false;
	}

}
