package vazkii.quark.content.tweaks.module;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class PoisonPotatoUsageModule extends QuarkModule {

	private static final String TAG_POISONED = "quark:poison_potato_applied";
	
	@Config public static double chance = 0.1;
	@Config public static boolean poisonEffect = true;
	
	@SubscribeEvent
	public void onInteract(EntityInteract event) {
		if(event.getTarget() instanceof AgeableEntity && event.getItemStack().getItem() == Items.POISONOUS_POTATO) {
			AgeableEntity ageable = (AgeableEntity) event.getTarget();
			if(ageable.isChild() && !isEntityPoisoned(ageable)) {
				if(!event.getWorld().isRemote) {
					Vector3d pos = ageable.getPositionVec();
					if(ageable.world.rand.nextDouble() < chance) {
						ageable.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5f, 0.25f);
						ageable.world.addParticle(ParticleTypes.ENTITY_EFFECT, pos.x, pos.y, pos.z, 0.2, 0.8, 0);
						poisonEntity(ageable);
						if (poisonEffect)
							ageable.addPotionEffect(new EffectInstance(Effects.POISON, 200));
					} else {
						ageable.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5f, 0.5f + ageable.world.rand.nextFloat() / 2);
						ageable.world.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0, 0.1, 0);
					}

					if (!event.getPlayer().isCreative())
						event.getItemStack().shrink(1);

				} else event.getPlayer().swingArm(event.getHand());

			}
		}
	}
	
	@SubscribeEvent
	public void onEntityUpdate(LivingUpdateEvent event) {
		if(event.getEntity() instanceof AnimalEntity) {
			AnimalEntity animal = (AnimalEntity) event.getEntity();
			if(animal.isChild() && isEntityPoisoned(animal))
				animal.setGrowingAge(-24000);
		}
	}
	
	private boolean isEntityPoisoned(Entity e) {
		return e.getPersistentData().getBoolean(TAG_POISONED);
	}
	
	private void poisonEntity(Entity e) {
		e.getPersistentData().putBoolean(TAG_POISONED, true);
	}
	
}
