package vazkii.quark.content.mobs.module;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.EntitySpawnHandler;
import vazkii.quark.base.world.config.CostSensitiveEntitySpawnConfig;
import vazkii.quark.base.world.config.EntitySpawnConfig;
import vazkii.quark.base.world.config.StrictBiomeConfig;
import vazkii.quark.content.mobs.client.render.FoxhoundRenderer;
import vazkii.quark.content.mobs.entity.FoxhoundEntity;

/**
 * @author WireSegal
 * Created at 5:00 PM on 9/26/19.
 */
@LoadModule(category = ModuleCategory.MOBS, hasSubscriptions = true)
public class FoxhoundModule extends QuarkModule {

	public static EntityType<FoxhoundEntity> foxhoundType;

	@Config(description = "The chance coal will tame a foxhound")
	public static double tameChance = 0.05;

	@Config
	public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(30, 1, 2, new StrictBiomeConfig(false, "minecraft:nether_wastes", "minecraft:basalt_deltas"));
	
	@Config
	public static EntitySpawnConfig lesserSpawnConfig = new CostSensitiveEntitySpawnConfig(2, 1, 1, 0.7, 0.15, new StrictBiomeConfig(false, "minecraft:soul_sand_valley"));
	
	public static ITag<Block> foxhoundSpawnableTag;
	
	@Override
	public void construct() {
		foxhoundType = EntityType.Builder.create(FoxhoundEntity::new, EntityClassification.CREATURE)
				.size(0.8F, 0.8F)
				.setTrackingRange(80)
				.setUpdateInterval(3)
				.setShouldReceiveVelocityUpdates(true)
				.immuneToFire()
				.setCustomClientFactory((spawnEntity, world) -> new FoxhoundEntity(foxhoundType, world))
				.build("foxhound");
		RegistryHelper.register(foxhoundType, "foxhound");

		EntitySpawnHandler.registerSpawn(this, foxhoundType, EntityClassification.MONSTER, PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, FoxhoundEntity::spawnPredicate, spawnConfig);
		EntitySpawnHandler.track(this, foxhoundType, EntityClassification.MONSTER, lesserSpawnConfig, true);
		
		EntitySpawnHandler.addEgg(foxhoundType, 0x890d0d, 0xf2af4b, spawnConfig);
	}

	@Override
	public void setup() {
		GlobalEntityTypeAttributes.put(foxhoundType, WolfEntity.func_234233_eS_().create());
		
		foxhoundSpawnableTag = BlockTags.createOptional(new ResourceLocation(Quark.MOD_ID, "foxhound_spawnable"));
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(foxhoundType, FoxhoundRenderer::new);
	}

	@SubscribeEvent
	public void onAggro(LivingSetAttackTargetEvent event) {
		if(event.getTarget() != null 
				&& event.getEntityLiving().getType() == EntityType.IRON_GOLEM 
				&& event.getTarget().getType() == foxhoundType 
				&& ((FoxhoundEntity) event.getTarget()).isTamed())
			((IronGolemEntity) event.getEntityLiving()).setAttackTarget(null);
	}
}
