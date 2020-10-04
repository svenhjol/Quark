package vazkii.quark.tweaks.module;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class PoisonPotatoUsageModule extends Module {

	private static final String TAG_POISONED = "quark:poison_potato_applied";
	
	@Config public static double chance = 0.1;
	@Config public static boolean poisonEffect = true;
	
	@SubscribeEvent
	public void onInteract(EntityInteract event) {
		if(event.getTarget() instanceof PassiveEntity && event.getItemStack().getItem() == Items.POISONOUS_POTATO) {
			PassiveEntity ageable = (PassiveEntity) event.getTarget();
			if(ageable.isBaby() && !isEntityPoisoned(ageable)) {
				if(!event.getWorld().isClient) {
					Vec3d pos = ageable.getPos();
					if(ageable.world.random.nextDouble() < chance) {
						ageable.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5f, 0.25f);
						ageable.world.addParticle(ParticleTypes.ENTITY_EFFECT, pos.x, pos.y, pos.z, 0.2, 0.8, 0);
						poisonEntity(ageable);
						if (poisonEffect)
							ageable.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200));
					} else {
						ageable.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5f, 0.5f + ageable.world.random.nextFloat() / 2);
						ageable.world.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0, 0.1, 0);
					}

					if (!event.getPlayer().isCreative())
						event.getItemStack().decrement(1);

				} else event.getPlayer().swingHand(event.getHand());

			}
		}
	}
	
	@SubscribeEvent
	public void onEntityUpdate(LivingUpdateEvent event) {
		if(event.getEntity() instanceof AnimalEntity) {
			AnimalEntity animal = (AnimalEntity) event.getEntity();
			if(animal.isBaby() && isEntityPoisoned(animal))
				animal.setBreedingAge(-24000);
		}
	}
	
	private boolean isEntityPoisoned(Entity e) {
		return e.getPersistentData().getBoolean(TAG_POISONED);
	}
	
	private void poisonEntity(Entity e) {
		e.getPersistentData().putBoolean(TAG_POISONED, true);
	}
	
}
