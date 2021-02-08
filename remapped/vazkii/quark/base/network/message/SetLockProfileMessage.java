package vazkii.quark.base.network.message;

import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;
import vazkii.quark.content.tweaks.module.LockRotationModule;
import vazkii.quark.content.tweaks.module.LockRotationModule.LockProfile;

public class SetLockProfileMessage implements IMessage {

	private static final long serialVersionUID = 1037317801540162515L;

	public LockProfile profile;
	
	public SetLockProfileMessage() { }
	
	public SetLockProfileMessage(LockProfile profile) {
		this.profile = profile;
	}
	
	@Override
	public boolean receive(Context context) {
		context.enqueueWork(() -> LockRotationModule.setProfile(context.getSender(), profile));
		return true;
	}
	
}
