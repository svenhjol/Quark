package vazkii.quark.content.world.module;

import com.google.common.base.Functions;

import net.minecraft.block.Block;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Effects;
import net.minecraft.world.gen.GenerationStage.Decoration;
import vazkii.quark.base.handler.BrewingHandler;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.content.world.block.RootBlock;
import vazkii.quark.content.world.gen.CaveRootGenerator;

@LoadModule(category = ModuleCategory.WORLD)
public class CaveRootsModule extends QuarkModule {

	@Config public static int chunkAttempts = 300;
	@Config public static int minY = 16;
	@Config public static int maxY = 52;
	@Config public static DimensionConfig dimensions = DimensionConfig.overworld(false);
	@Config(flag = "cave_roots_brewing") public static boolean enableBrewing = true;
	
	public static Block root;
	public static Item rootItem;
	
	@Override
	public void construct() {
		root = new RootBlock(this);
		
		rootItem = new QuarkItem("root_item", this, new Item.Properties()
				.food(new Food.Builder()
						.hunger(3)
						.saturation(0.4F)
						.build())
				.group(ItemGroup.FOOD));

		BrewingHandler.addPotionMix("cave_roots_brewing",
				() -> Ingredient.fromItems(rootItem),
				Effects.RESISTANCE);
		
		VariantHandler.addFlowerPot(root, "cave_root", Functions.identity());
	}
	
	@Override
	public void setup() {
		WorldGenHandler.addGenerator(this, new CaveRootGenerator(dimensions), Decoration.UNDERGROUND_DECORATION, WorldGenWeights.CAVE_ROOTS);
		
		enqueue(() -> ComposterBlock.CHANCES.put(rootItem, 0.1F));
	}
	
}
