package vazkii.quark.tweaks.module;

import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

/**
 * @author WireSegal
 * Created at 8:40 AM on 8/27/19.
 */
@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class ArmedArmorStandsModule extends Module {
    @SubscribeEvent
    public void entityConstruct(EntityEvent.EntityConstructing event) {
        if(event.getEntity() instanceof ArmorStandEntity) {
            ArmorStandEntity stand = (ArmorStandEntity) event.getEntity();
            if(!stand.shouldShowArms())
                setShowArms(stand, true);
        }
    }

    private void setShowArms(ArmorStandEntity e, boolean showArms) {
        e.getDataTracker().set(ArmorStandEntity.ARMOR_STAND_FLAGS, setBit(e.getDataTracker().get(ArmorStandEntity.ARMOR_STAND_FLAGS), 4, showArms));
    }

    private byte setBit(byte status, int bitFlag, boolean value) {
        if (value)
            status = (byte)(status | bitFlag);
        else
            status = (byte)(status & ~bitFlag);

        return status;
    }
}
