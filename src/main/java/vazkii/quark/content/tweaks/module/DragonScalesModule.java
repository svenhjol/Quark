package vazkii.quark.content.tweaks.module;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tweaks.recipe.ElytraDuplicationRecipe;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class DragonScalesModule extends QuarkModule {
	
	public static Item dragon_scale;
	
	@Override
	public void construct() {
		ForgeRegistries.RECIPE_SERIALIZERS.register(ElytraDuplicationRecipe.SERIALIZER.setRegistryName("quark:elytra_duplication"));
		
		dragon_scale = new QuarkItem("dragon_scale", this, new Item.Properties().group(ItemGroup.MATERIALS));
	}
	
	@SubscribeEvent
	public void onEntityTick(LivingUpdateEvent event) {
		if(event.getEntityLiving() instanceof EnderDragonEntity && !event.getEntity().getEntityWorld().isRemote) {
			EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();

			if(dragon.getFightManager() != null && dragon.getFightManager().hasPreviouslyKilledDragon() && dragon.deathTicks == 100) {
				Vector3d pos = dragon.getPositionVec();
				ItemEntity item = new ItemEntity(dragon.world, pos.x, pos.y, pos.z, new ItemStack(dragon_scale, 1));
				dragon.world.addEntity(item);
			}
		}
	}
	
}
