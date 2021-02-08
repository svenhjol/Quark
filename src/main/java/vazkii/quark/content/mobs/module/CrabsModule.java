package vazkii.quark.content.mobs.module;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.handler.BrewingHandler;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.recipe.FlagIngredient;
import vazkii.quark.base.util.QuarkEffect;
import vazkii.quark.base.world.EntitySpawnHandler;
import vazkii.quark.base.world.config.BiomeTypeConfig;
import vazkii.quark.base.world.config.EntitySpawnConfig;
import vazkii.quark.content.mobs.client.render.CrabRenderer;
import vazkii.quark.content.mobs.entity.CrabEntity;

/**
 * @author WireSegal
 * Created at 7:28 PM on 9/22/19.
 */
@LoadModule(category = ModuleCategory.MOBS, hasSubscriptions = true)
public class CrabsModule extends QuarkModule {

	public static EntityType<CrabEntity> crabType;

	@Config
	public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(5, 1, 3, new BiomeTypeConfig(false, Biome.Category.BEACH));

	@Config(flag = "crab_brewing")
	public static boolean enableBrewing = true;

	@Override
	public void construct() {
		new QuarkItem("crab_leg", this, new Item.Properties()
				.group(ItemGroup.FOOD)
				.food(new Food.Builder()
						.meat()
						.hunger(1)
						.saturation(0.3F)
						.build()));

		new QuarkItem("cooked_crab_leg", this, new Item.Properties()
				.group(ItemGroup.FOOD)
				.food(new Food.Builder()
						.meat()
						.hunger(8)
						.saturation(0.8F)
						.build()));

		Item shell = new QuarkItem("crab_shell", this, new Item.Properties().group(ItemGroup.BREWING))
				.setCondition(() -> enableBrewing);

		Effect resilience = new QuarkEffect("resilience", EffectType.BENEFICIAL, 0x5b1a04);
		resilience.addAttributesModifier(Attributes.KNOCKBACK_RESISTANCE, "2ddf3f0a-f386-47b6-aeb0-6bd32851f215", 0.5, AttributeModifier.Operation.ADDITION);

		BrewingHandler.addPotionMix("crab_brewing",
				() -> new FlagIngredient(Ingredient.fromItems(shell), "crabs"), resilience);

		crabType = EntityType.Builder.<CrabEntity>create(CrabEntity::new, EntityClassification.CREATURE)
				.size(0.9F, 0.5F)
				.setTrackingRange(80)
				.setUpdateInterval(3)
				.setShouldReceiveVelocityUpdates(true)
				.setCustomClientFactory((spawnEntity, world) -> new CrabEntity(crabType, world))
				.build("crab");
		RegistryHelper.register(crabType, "crab");

		EntitySpawnHandler.registerSpawn(this, crabType, EntityClassification.CREATURE, PlacementType.ON_GROUND, Type.MOTION_BLOCKING_NO_LEAVES, CrabEntity::spawnPredicate, spawnConfig);
		EntitySpawnHandler.addEgg(crabType, 0x893c22, 0x916548, spawnConfig);
	}

	@Override
	public void setup() {
		GlobalEntityTypeAttributes.put(crabType, CrabEntity.prepareAttributes().create());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(crabType, CrabRenderer::new);
	}
}
