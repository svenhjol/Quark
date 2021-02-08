package vazkii.quark.base.network.message;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.network.NetworkEvent;
import vazkii.arl.network.IMessage;
import vazkii.quark.content.management.module.ItemSharingModule;

/**
 * @author WireSegal
 * Created at 1:43 PM on 9/1/19.
 */
public class LinkItemMessage implements IMessage {

    private static final long serialVersionUID = -1925519137930388889L;

    public ItemStack stack;

    public LinkItemMessage() {
        // NO-OP
    }

    public LinkItemMessage(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean receive(NetworkEvent.Context context) {
        context.enqueueWork(() -> ItemSharingModule.linkItem(context.getSender(), stack));
        return false;
    }
}
