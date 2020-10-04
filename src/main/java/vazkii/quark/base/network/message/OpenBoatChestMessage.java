package vazkii.quark.base.network.message;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraftforge.fml.network.NetworkEvent;
import vazkii.arl.network.IMessage;
import vazkii.quark.management.entity.ChestPassengerEntity;

import javax.annotation.Nonnull;
import java.util.List;

public class OpenBoatChestMessage implements IMessage {

	private static final long serialVersionUID = 4454710003473142954L;

	@Override
	public boolean receive(NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			PlayerEntity player = context.getSender();

			if(player != null && player.hasVehicle() && player.currentScreenHandler == player.playerScreenHandler) {
				Entity riding = player.getVehicle();
				if(riding instanceof BoatEntity) {
					List<Entity> passengers = riding.getPassengerList();
					for(Entity passenger : passengers) {
						if (passenger instanceof ChestPassengerEntity) {
							player.openHandledScreen(new NamedScreenHandlerFactory() {
								@Nonnull
								@Override
								public Text getDisplayName() {
									return new TranslatableText("container.chest");
								}

								@Nonnull
								@Override
								public ScreenHandler createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity player) {
									return GenericContainerScreenHandler.createGeneric9x3(id, inventory, (ChestPassengerEntity) passenger);
								}
							});

							break;
						}
					}
				}
			}
		});
		
		return true;
	}

}
