package vazkii.quark.base.network.message;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;
import vazkii.quark.content.tweaks.module.SimpleHarvestModule;

public class HarvestMessage implements IMessage {

	private static final long serialVersionUID = -51788488328591145L;
	
	public BlockPos pos;

	public HarvestMessage() { }

	public HarvestMessage(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public boolean receive(Context context) {
		context.enqueueWork(() -> SimpleHarvestModule.click(context.getSender(), pos));
		return true;
	}

}
