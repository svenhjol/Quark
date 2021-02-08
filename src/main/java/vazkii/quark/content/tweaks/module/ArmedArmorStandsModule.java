package vazkii.quark.content.tweaks.module;

import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

/**
 * @author WireSegal
 * Created at 8:40 AM on 8/27/19.
 */
@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class ArmedArmorStandsModule extends QuarkModule {
    @SubscribeEvent
    public void entityConstruct(EntityEvent.EntityConstructing event) {
        if(event.getEntity() instanceof ArmorStandEntity) {
            ArmorStandEntity stand = (ArmorStandEntity) event.getEntity();
            if(!stand.getShowArms())
                setShowArms(stand, true);
        }
    }

    private void setShowArms(ArmorStandEntity e, boolean showArms) {
        e.getDataManager().set(ArmorStandEntity.STATUS, setBit(e.getDataManager().get(ArmorStandEntity.STATUS), 4, showArms));
    }

    private byte setBit(byte status, int bitFlag, boolean value) {
        if (value)
            status = (byte)(status | bitFlag);
        else
            status = (byte)(status & ~bitFlag);

        return status;
    }
}
