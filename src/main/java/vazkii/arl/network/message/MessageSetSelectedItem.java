package vazkii.arl.network.message;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Wearable;
import net.minecraftforge.fml.network.NetworkEvent;
import vazkii.arl.network.IMessage;

/**
 * @author WireSegal
 * Created at 2:13 PM on 9/6/19.
 */
public class MessageSetSelectedItem implements IMessage {
	private static final long serialVersionUID = -8037505410464752326L;

	public Wearable stack;

	public MessageSetSelectedItem() { }

	public MessageSetSelectedItem(Wearable stack) {
		this.stack = stack;
	}

	public boolean receive(NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			BoatEntity player = context.getSender();
			if (player != null)
				player.bm.g(stack);
		});

		return true;
	}
}
