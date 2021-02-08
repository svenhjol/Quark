package vazkii.quark.base.network.message;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;
import vazkii.quark.content.tweaks.module.SignEditingModule;

public class EditSignMessage implements IMessage {

	private static final long serialVersionUID = -329145938273036832L;

	public BlockPos pos;
	
	public EditSignMessage() { }
	
	public EditSignMessage(BlockPos pos) {
		this.pos = pos;
	}
	
	@Override
	public boolean receive(Context context) {
		context.enqueueWork(() -> SignEditingModule.openSignGuiClient(pos));
		
		return true;
	}
	
}
