package vazkii.quark.base.network.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import vazkii.arl.network.IMessage;
import vazkii.quark.content.tweaks.module.DoubleDoorOpeningModule;

public class DoubleDoorMessage implements IMessage {

	private static final long serialVersionUID = 8024722624953236124L;
	
	public BlockPos pos;
	
	public DoubleDoorMessage() { }
	
	public DoubleDoorMessage(BlockPos pos) {
		this.pos = pos;
	}

	private World extractWorld(ServerPlayerEntity entity) {
		return entity == null ? null : entity.world;
	}

	@Override
	public boolean receive(Context context) {
		context.enqueueWork(() -> DoubleDoorOpeningModule.openDoor(extractWorld(context.getSender()), pos));
		return true;
	}

}
