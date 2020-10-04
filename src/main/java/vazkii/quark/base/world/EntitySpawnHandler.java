package vazkii.quark.base.world;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.SpawnRestriction.Location;
import net.minecraft.entity.SpawnRestriction.SpawnPredicate;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnEntry;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.item.QuarkSpawnEggItem;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.world.config.EntitySpawnConfig;

public class EntitySpawnHandler {

	private static List<TrackedSpawnConfig> trackedSpawnConfigs = new LinkedList<>();
	
	public static <T extends MobEntity> void registerSpawn(Module module, EntityType<T> entityType, SpawnGroup classification, Location placementType, Heightmap.Type heightMapType, SpawnPredicate<T> placementPredicate, EntitySpawnConfig config) {
		SpawnRestriction.register(entityType, placementType, heightMapType, placementPredicate);
        
        config.setModule(module);
        trackedSpawnConfigs.add(new TrackedSpawnConfig(entityType, classification, config));
	}
	
	public static void addEgg(EntityType<?> entityType, int color1, int color2, EntitySpawnConfig config) {
		addEgg(entityType, color1, color2, config.module, config::isEnabled);
	}
	
	public static void addEgg(EntityType<?> entityType, int color1, int color2, Module module, BooleanSupplier enabledSupplier) {
		new QuarkSpawnEggItem(entityType, color1,  color2, entityType.getRegistryName().getPath() + "_spawn_egg", module, 
				new Item.Settings().group(ItemGroup.MISC))
				.setCondition(enabledSupplier);
	}

	
	public static void refresh() {
		for(TrackedSpawnConfig c : trackedSpawnConfigs) {
			boolean enabled = c.config.isEnabled();
			c.refresh();
			
			for(Biome b : ForgeRegistries.BIOMES.getValues()) {
				List<SpawnEntry> l = b.getEntitySpawnList(c.classification);
				l.removeIf(e -> e.type == c.entityType);
				
				if(enabled && c.config.biomes.canSpawn(b))
					l.add(c.entry);
			}
		}
	}
	
	private static class TrackedSpawnConfig {

		final EntityType<?> entityType;
		final SpawnGroup classification;
		final EntitySpawnConfig config;
		SpawnEntry entry;
		
		TrackedSpawnConfig(EntityType<?> entityType, SpawnGroup classification, EntitySpawnConfig config) {
			this.entityType = entityType;
			this.classification = classification;
			this.config = config;
			refresh();
		}
		
		void refresh() {
			entry = new SpawnEntry(entityType, config.spawnWeight, Math.min(config.minGroupSize, config.maxGroupSize), Math.max(config.minGroupSize, config.maxGroupSize));
		}
		
	}

}
