package vazkii.quark.tweaks.module;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class SnowGolemPlayerHeadsModule extends Module {

	@SubscribeEvent
	public void onDrops(LivingDropsEvent event) {
		Entity e = event.getEntity();

		if(e.hasCustomName() && e instanceof SnowGolemEntity && event.getSource().getAttacker() != null && event.getSource().getAttacker() instanceof WitchEntity) {
			SnowGolemEntity snowman = (SnowGolemEntity) e;
			if(snowman.hasPumpkin()) { 
				ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
				ItemNBTHelper.setString(stack, "SkullOwner", e.getCustomName().getString());
				Vec3d pos = e.getPos();
				event.getDrops().add(new ItemEntity(e.getEntityWorld(), pos.x, pos.y, pos.z, stack));
			}
		}
	}
	
}
