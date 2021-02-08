package vazkii.quark.content.mobs.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction.Location;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
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
	
	public static Tag<Block> foxhoundSpawnableTag;
	
	@Override
	public void construct() {
		foxhoundType = EntityType.Builder.create(FoxhoundEntity::new, SpawnGroup.CREATURE)
				.setDimensions(0.8F, 0.8F)
				.setTrackingRange(80)
				.setUpdateInterval(3)
				.setShouldReceiveVelocityUpdates(true)
				.makeFireImmune()
				.setCustomClientFactory((spawnEntity, world) -> new FoxhoundEntity(foxhoundType, world))
				.build("foxhound");
		RegistryHelper.register(foxhoundType, "foxhound");

		EntitySpawnHandler.registerSpawn(this, foxhoundType, SpawnGroup.MONSTER, Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, FoxhoundEntity::spawnPredicate, spawnConfig);
		EntitySpawnHandler.track(this, foxhoundType, SpawnGroup.MONSTER, lesserSpawnConfig, true);
		
		EntitySpawnHandler.addEgg(foxhoundType, 0x890d0d, 0xf2af4b, spawnConfig);
	}

	@Override
	public void setup() {
		DefaultAttributeRegistry.put(foxhoundType, WolfEntity.createWolfAttributes().build());
		
		foxhoundSpawnableTag = BlockTags.createOptional(new Identifier(Quark.MOD_ID, "foxhound_spawnable"));
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(foxhoundType, FoxhoundRenderer::new);
	}

	@SubscribeEvent
	public void onAggro(LivingSetAttackTargetEvent event) {
		if(event.getTarget() != null 
				&& event.getEntityLiving().getType() == EntityType.IRON_GOLEM 
				&& event.getTarget().getType() == foxhoundType 
				&& ((FoxhoundEntity) event.getTarget()).isTamed())
			((IronGolemEntity) event.getEntityLiving()).setTarget(null);
	}
}
