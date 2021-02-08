package vazkii.quark.content.building.module;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Functions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;

@LoadModule(category = ModuleCategory.BUILDING)
public class MorePottedPlantsModule extends QuarkModule {

	private static Map<Block, Block> tintedBlocks = new HashMap<>();
	
	@Override
	public void construct() {
		add(Blocks.BEETROOTS, "beetroot");
		add(Blocks.SWEET_BERRY_BUSH, "berries");
		add(Blocks.CARROTS, "carrot");
		add(Blocks.CHORUS_FLOWER, "chorus");
		add(Blocks.COCOA, "cocoa_bean");
		Block grass = add(Blocks.GRASS, "grass");
		add(Blocks.PEONY, "peony");
		Block largeFern = add(Blocks.LARGE_FERN, "large_fern");
		add(Blocks.LILAC, "lilac");
		add(Blocks.MELON_STEM, "melon");
		add(Blocks.NETHER_SPROUTS, "nether_sprouts");
		add(Blocks.NETHER_WART, "nether_wart");
		add(Blocks.POTATOES, "potato");
		add(Blocks.PUMPKIN_STEM, "pumpkin");
		add(Blocks.ROSE_BUSH, "rose");
		VariantHandler.addFlowerPot(Blocks.SEA_PICKLE, "sea_pickle", p -> p.setLightLevel(b -> 3));
		Block sugarCane = add(Blocks.SUGAR_CANE, "sugar_cane");
		add(Blocks.SUNFLOWER, "sunflower");
		Block tallGrass = add(Blocks.TALL_GRASS, "tall_grass");
		add(Blocks.TWISTING_VINES, "twisting_vines");
		Block vine = add(Blocks.VINE, "vine");
		add(Blocks.WEEPING_VINES, "weeping_vines");
		add(Blocks.WHEAT, "wheat");
		
		tintedBlocks.put(grass, Blocks.GRASS);
		tintedBlocks.put(largeFern, Blocks.LARGE_FERN);
		tintedBlocks.put(sugarCane, Blocks.SUGAR_CANE);
		tintedBlocks.put(tallGrass, Blocks.TALL_GRASS);
		tintedBlocks.put(vine, Blocks.VINE);
	}
	
	private FlowerPotBlock add(Block block, String name) {
		return VariantHandler.addFlowerPot(block, name, Functions.identity());
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		for(Block b : tintedBlocks.keySet()) {
			BlockState tState = tintedBlocks.get(b).getDefaultState();
			IBlockColor color = (state, worldIn, pos, tintIndex) -> Minecraft.getInstance().getBlockColors().getColor(tState, worldIn, pos, tintIndex);
			Minecraft.getInstance().getBlockColors().register(color, b);
		}
	}
	
}
