package vazkii.quark.world.module;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.function.BooleanSupplier;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.gen.GenerationStep.Feature;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.OreGenerator;
import vazkii.quark.world.config.BigStoneClusterConfig;
import vazkii.quark.world.config.StoneTypeConfig;

@LoadModule(category = ModuleCategory.WORLD, hasSubscriptions = true)
public class NewStoneTypesModule extends Module {

	@Config(flag = "marble") private static boolean enableMarble = true;
	@Config(flag = "limestone") private static boolean enableLimestone = true;
	@Config(flag = "jasper") private static boolean enableJasper = true;
	@Config(flag = "slate") private static boolean enableSlate = true;
	@Config(flag = "basalt") private static boolean enableVoidstone = true;
	
	public static boolean enabledWithMarble, enabledWithLimestone, enabledWithJasper, enabledWithSlate, enabledWithVoidstone;
	
	@Config public static StoneTypeConfig marble = new StoneTypeConfig();
	@Config public static StoneTypeConfig limestone = new StoneTypeConfig();
	@Config public static StoneTypeConfig jasper = new StoneTypeConfig();
	@Config public static StoneTypeConfig slate = new StoneTypeConfig();
	@Config public static StoneTypeConfig voidstone = new StoneTypeConfig(DimensionConfig.end(false));
	
	public static Block marbleBlock, limestoneBlock, jasperBlock, slateBlock, basaltBlock;

	public static Map<Block, Block> polishedBlocks = Maps.newHashMap();
	
	private Queue<Runnable> defers = new ArrayDeque<>();
	
	@Override
	public void construct() {
		marbleBlock = makeStone("marble", marble, BigStoneClustersModule.marble, () -> enableMarble, MaterialColor.QUARTZ);
		limestoneBlock = makeStone("limestone", limestone, BigStoneClustersModule.limestone, () -> enableLimestone, MaterialColor.STONE);
		jasperBlock = makeStone("jasper", jasper, BigStoneClustersModule.jasper, () -> enableJasper, MaterialColor.RED_TERRACOTTA);
		slateBlock = makeStone("slate", slate, BigStoneClustersModule.slate, () -> enableSlate, MaterialColor.ICE);
		basaltBlock = makeStone("basalt", voidstone, BigStoneClustersModule.voidstone, () -> enableVoidstone, MaterialColor.BLACK);
	}
	
	private Block makeStone(String name, StoneTypeConfig config, BigStoneClusterConfig bigConfig, BooleanSupplier enabledCond, MaterialColor color) {
		BooleanSupplier trueEnabledCond = () -> !bigConfig.enabled && enabledCond.getAsBoolean();
		Block.Properties props = Block.Properties.of(Material.STONE, color)
				.requiresTool() // needs tool
				.harvestTool(ToolType.PICKAXE)
				.strength(1.5F, 6.0F); 
		
		QuarkBlock normal = new QuarkBlock(name, this, ItemGroup.BUILDING_BLOCKS, props).setCondition(enabledCond);
		QuarkBlock polished = new QuarkBlock("polished_" + name, this, ItemGroup.BUILDING_BLOCKS, props).setCondition(enabledCond);
		polishedBlocks.put(normal, polished);

		VariantHandler.addSlabStairsWall(normal);
		VariantHandler.addSlabAndStairs(polished);
		
		defers.add(() ->
			WorldGenHandler.addGenerator(this, new OreGenerator(config.dimensions, config.oregen, normal.getDefaultState(), OreGenerator.ALL_DIMS_STONE_MATCHER, trueEnabledCond), Feature.UNDERGROUND_ORES, WorldGenWeights.NEW_STONES)
		);
		
		return normal;
	}
	
	@Override
	public void configChanged() {
		enabledWithMarble = enableMarble && this.enabled;
		enabledWithLimestone = enableLimestone && this.enabled;
		enabledWithJasper = enableJasper && this.enabled;
		enabledWithSlate = enableSlate && this.enabled;
		enabledWithVoidstone = enableVoidstone && this.enabled;
	}
	
	@Override
	public void setup() {
		while(!defers.isEmpty())
			defers.poll().run();
	}
	
}
