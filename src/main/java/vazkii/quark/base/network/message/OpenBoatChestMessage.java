package vazkii.quark.base.network.message;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import vazkii.arl.network.IMessage;
import vazkii.quark.content.management.entity.ChestPassengerEntity;

import javax.annotation.Nonnull;
import java.util.List;

public class OpenBoatChestMessage implements IMessage {

	private static final long serialVersionUID = 4454710003473142954L;

	@Override
	public boolean receive(NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			PlayerEntity player = context.getSender();

			if(player != null && player.isPassenger() && player.openContainer == player.container) {
				Entity riding = player.getRidingEntity();
				if(riding instanceof BoatEntity) {
					List<Entity> passengers = riding.getPassengers();
					for(Entity passenger : passengers) {
						if (passenger instanceof ChestPassengerEntity) {
							player.openContainer(new INamedContainerProvider() {
								@Nonnull
								@Override
								public ITextComponent getDisplayName() {
									return new TranslationTextComponent("container.chest");
								}

								@Nonnull
								@Override
								public Container createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity player) {
									return ChestContainer.createGeneric9X3(id, inventory, (ChestPassengerEntity) passenger);
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
