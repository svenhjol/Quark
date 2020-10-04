package vazkii.quark.tweaks.module;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.tweaks.recipe.ElytraDuplicationRecipe;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class DragonScalesModule extends Module {
	
	public static Item dragon_scale;
	
	@Override
	public void construct() {
		ForgeRegistries.RECIPE_SERIALIZERS.register(ElytraDuplicationRecipe.SERIALIZER);
		
		dragon_scale = new QuarkItem("dragon_scale", this, new Item.Settings().group(ItemGroup.MATERIALS));
	}
	
	@SubscribeEvent
	public void onEntityTick(LivingUpdateEvent event) {
		if(event.getEntityLiving() instanceof EnderDragonEntity && !event.getEntity().getEntityWorld().isClient) {
			EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();

			if(dragon.getFight() != null && dragon.getFight().hasPreviouslyKilled() && dragon.ticksSinceDeath == 100) {
				Vec3d pos = dragon.getPos();
				ItemEntity item = new ItemEntity(dragon.world, pos.x, pos.y, pos.z, new ItemStack(dragon_scale, 1));
				dragon.world.spawnEntity(item);
			}
		}
	}
	
}
