package vazkii.quark.content.management.module;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.SimilarBlockTypeHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.management.capability.ShulkerBoxDropIn;
import vazkii.quark.base.module.ModuleCategory;

/**
 * @author WireSegal
 * Created at 10:10 AM on 9/3/19.
 */
@LoadModule(category = ModuleCategory.MANAGEMENT, hasSubscriptions = true)
public class ShulkerBoxRightClickModule extends QuarkModule {

    private static final ResourceLocation SHULKER_BOX_CAP = new ResourceLocation(Quark.MOD_ID, "shulker_box_drop_in");

    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<ItemStack> event) {
        if(SimilarBlockTypeHandler.isShulkerBox(event.getObject()))
            event.addCapability(SHULKER_BOX_CAP, new ShulkerBoxDropIn());
    }
}
