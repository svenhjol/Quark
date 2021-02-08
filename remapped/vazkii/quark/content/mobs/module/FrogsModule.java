package vazkii.quark.content.mobs.module;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction.Location;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.handler.BrewingHandler;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.recipe.FlagIngredient;
import vazkii.quark.base.world.EntitySpawnHandler;
import vazkii.quark.base.world.config.BiomeTypeConfig;
import vazkii.quark.base.world.config.EntitySpawnConfig;
import vazkii.quark.content.mobs.client.render.FrogRenderer;
import vazkii.quark.content.mobs.entity.FrogEntity;

@LoadModule(category = ModuleCategory.MOBS, hasSubscriptions = true)
public class FrogsModule extends QuarkModule {

	public static EntityType<FrogEntity> frogType;

	@Config
	public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(40, 1, 3, new BiomeTypeConfig(false, Biome.Category.SWAMP));

	@Config(flag = "frog_brewing") 
	public static boolean enableBrewing = true;
	
	@Config public static boolean enableBigFunny = false;

	@Override
	public void construct() {
		new QuarkItem("frog_leg", this, new Item.Settings()
				.group(ItemGroup.FOOD)
				.food(new FoodComponent.Builder()
						.meat()
						.hunger(2)
						.saturationModifier(0.3F)
						.build()));

		new QuarkItem("cooked_frog_leg", this, new Item.Settings()
				.group(ItemGroup.FOOD)
				.food(new FoodComponent.Builder()
						.meat()
						.hunger(4)
						.saturationModifier(1.25F)
						.build()));

		Item goldenLeg = new QuarkItem("golden_frog_leg", this, new Item.Settings()
				.group(ItemGroup.BREWING)
				.food(new FoodComponent.Builder()
						.meat()
						.hunger(4)
						.saturationModifier(2.5F)
						.build()))
				.setCondition(() -> enableBrewing);
		
		BrewingHandler.addPotionMix("frog_brewing",
				() -> new FlagIngredient(Ingredient.ofItems(goldenLeg), "frogs"),
				Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING);
		
		frogType = EntityType.Builder.<FrogEntity>create(FrogEntity::new, SpawnGroup.CREATURE)
				.setDimensions(0.65F, 0.5F)
				.setTrackingRange(80)
				.setUpdateInterval(3)
				.setShouldReceiveVelocityUpdates(true)
				.setCustomClientFactory((spawnEntity, world) -> new FrogEntity(frogType, world))
				.build("frog");
		RegistryHelper.register(frogType, "frog");
		
		EntitySpawnHandler.registerSpawn(this, frogType, SpawnGroup.CREATURE, Location.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn, spawnConfig);
		EntitySpawnHandler.addEgg(frogType, 0xbc9869, 0xffe6ad, spawnConfig);
	}
	
	@Override
	public void setup() {
		DefaultAttributeRegistry.put(frogType, FrogEntity.prepareAttributes().build());
	}
	
	@Override
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(frogType, FrogRenderer::new);
	}

}
