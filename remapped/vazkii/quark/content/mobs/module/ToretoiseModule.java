package vazkii.quark.content.mobs.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction.Location;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.item.Items;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.EntitySpawnHandler;
import vazkii.quark.base.world.config.BiomeTypeConfig;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.config.EntitySpawnConfig;
import vazkii.quark.content.mobs.client.render.ToretoiseRenderer;
import vazkii.quark.content.mobs.entity.ToretoiseEntity;

@LoadModule(category = ModuleCategory.MOBS, hasSubscriptions = true)
public class ToretoiseModule extends QuarkModule {

	public static EntityType<ToretoiseEntity> toretoiseType;
	
	@Config public static int maxYLevel = 32;
	
	@Config
	public static DimensionConfig dimensions = DimensionConfig.overworld(false);
	
	@Config 
	public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(100, 1, 1, new BiomeTypeConfig(true, Biome.Category.NONE, Biome.Category.NETHER, Biome.Category.THEEND));
	
	@Override
	public void construct() {
		toretoiseType = EntityType.Builder.<ToretoiseEntity>create(ToretoiseEntity::new, SpawnGroup.CREATURE)
				.setDimensions(2F, 1F)
				.setTrackingRange(80)
				.setUpdateInterval(3)
				.setShouldReceiveVelocityUpdates(true)
				.makeFireImmune()
				.setCustomClientFactory((spawnEntity, world) -> new ToretoiseEntity(toretoiseType, world))
				.build("toretoise");

		RegistryHelper.register(toretoiseType, "toretoise");
		
		EntitySpawnHandler.registerSpawn(this, toretoiseType, SpawnGroup.MONSTER, Location.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, ToretoiseEntity::spawnPredicate, spawnConfig);
		EntitySpawnHandler.addEgg(toretoiseType, 0x55413b, 0x383237, spawnConfig);
	}
	
	@Override
	public void setup() {
		DefaultAttributeRegistry.put(toretoiseType, ToretoiseEntity.prepareAttributes().build());
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(toretoiseType, ToretoiseRenderer::new);
	}
	
}
