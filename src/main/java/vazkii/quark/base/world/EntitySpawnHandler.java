package vazkii.quark.base.world;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntitySpawnPlacementRegistry.IPlacementPredicate;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.Quark;
import vazkii.quark.base.item.QuarkSpawnEggItem;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.world.config.CostSensitiveEntitySpawnConfig;
import vazkii.quark.base.world.config.EntitySpawnConfig;

@EventBusSubscriber(modid = Quark.MOD_ID)
public class EntitySpawnHandler {

	private static List<TrackedSpawnConfig> trackedSpawnConfigs = new LinkedList<>();

	public static <T extends MobEntity> void registerSpawn(QuarkModule module, EntityType<T> entityType, EntityClassification classification, PlacementType placementType, Heightmap.Type heightMapType, IPlacementPredicate<T> placementPredicate, EntitySpawnConfig config) {
		EntitySpawnPlacementRegistry.register(entityType, placementType, heightMapType, placementPredicate);

		track(module, entityType, classification, config, false);
	}
	
	public static <T extends MobEntity> void track(QuarkModule module, EntityType<T> entityType, EntityClassification classification, EntitySpawnConfig config, boolean secondary) {
		config.setModule(module);
		trackedSpawnConfigs.add(new TrackedSpawnConfig(entityType, classification, config, secondary));
	}

	public static void addEgg(EntityType<?> entityType, int color1, int color2, EntitySpawnConfig config) {
		addEgg(entityType, color1, color2, config.module, config::isEnabled);
	}

	public static void addEgg(EntityType<?> entityType, int color1, int color2, QuarkModule module, BooleanSupplier enabledSupplier) {
		new QuarkSpawnEggItem(entityType, color1,  color2, entityType.getRegistryName().getPath() + "_spawn_egg", module, 
				new Item.Properties().group(ItemGroup.MISC))
		.setCondition(enabledSupplier);
	}

	@SubscribeEvent
	public static void onBiomeLoaded(BiomeLoadingEvent ev) {
		MobSpawnInfoBuilder builder = ev.getSpawns();

		for(TrackedSpawnConfig c : trackedSpawnConfigs) {
			List<MobSpawnInfo.Spawners> l = builder.getSpawner(c.classification);
			if(!c.secondary)
				l.removeIf(e -> e.type.equals(c.entityType));
			
			if(c.config.isEnabled() && c.config.biomes.canSpawn(ev.getName(), ev.getCategory()))
				l.add(c.entry);
				
			if(c.config instanceof CostSensitiveEntitySpawnConfig) {
				CostSensitiveEntitySpawnConfig csc = (CostSensitiveEntitySpawnConfig) c.config;
				builder.withSpawnCost(c.entityType, csc.spawnCost, csc.maxCost);
			}
		}
	}

	public static void refresh() {
		for(TrackedSpawnConfig c : trackedSpawnConfigs)
			c.refresh();
	}

	private static class TrackedSpawnConfig {

		final EntityType<?> entityType;
		final EntityClassification classification;
		final EntitySpawnConfig config;
		final boolean secondary;
		MobSpawnInfo.Spawners entry;

		TrackedSpawnConfig(EntityType<?> entityType, EntityClassification classification, EntitySpawnConfig config, boolean secondary) {
			this.entityType = entityType;
			this.classification = classification;
			this.config = config;
			this.secondary = secondary;
			refresh();
		}

		void refresh() {
			entry = new MobSpawnInfo.Spawners(entityType, config.spawnWeight, Math.min(config.minGroupSize, config.maxGroupSize), Math.max(config.minGroupSize, config.maxGroupSize));
		}

	}

}
