package vazkii.quark.base.network.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;
import vazkii.quark.addons.oddities.container.MatrixEnchantingContainer;
import vazkii.quark.addons.oddities.tile.MatrixEnchantingTableTileEntity;

public class MatrixEnchanterOperationMessage implements IMessage {

	private static final long serialVersionUID = 2272401655489445173L;
	
	public int operation;
	public int arg0, arg1, arg2;
	
	public MatrixEnchanterOperationMessage() { }
	
	public MatrixEnchanterOperationMessage(int operation, int arg0, int arg1, int arg2) {
		this.operation = operation;
		this.arg0 = arg0;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}

	@Override
	public boolean receive(Context context) {
		context.enqueueWork(() -> {
			ServerPlayerEntity player = context.getSender();
			Container container = player.openContainer;
			
			if(container instanceof MatrixEnchantingContainer) {
				MatrixEnchantingTableTileEntity enchanter = ((MatrixEnchantingContainer) container).enchanter;
				enchanter.onOperation(player, operation, arg0, arg1, arg2);
			}
		});
		
		return true;
	}
	
}
