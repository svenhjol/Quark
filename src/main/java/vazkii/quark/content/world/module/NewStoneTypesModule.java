package vazkii.quark.content.world.module;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.function.BooleanSupplier;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.OreGenerator;
import vazkii.quark.content.world.block.MyaliteBlock;
import vazkii.quark.content.world.config.BigStoneClusterConfig;
import vazkii.quark.content.world.config.StoneTypeConfig;

@LoadModule(category = ModuleCategory.WORLD, hasSubscriptions = true)
public class NewStoneTypesModule extends QuarkModule {

	@Config(flag = "marble") private static boolean enableMarble = true;
	@Config(flag = "limestone") private static boolean enableLimestone = true;
	@Config(flag = "jasper") private static boolean enableJasper = true;
	@Config(flag = "slate") private static boolean enableSlate = true;
	@Config(flag = "basalt") private static boolean enableVoidstone = true;
	@Config(flag = "myalite") private static boolean enableMyalite = true;

	public static boolean enabledWithMarble, enabledWithLimestone, enabledWithJasper, enabledWithSlate, enabledWithVoidstone, enabledWithMyalite;
	
	@Config public static StoneTypeConfig marble = new StoneTypeConfig();
	@Config public static StoneTypeConfig limestone = new StoneTypeConfig();
	@Config public static StoneTypeConfig jasper = new StoneTypeConfig();
	@Config public static StoneTypeConfig slate = new StoneTypeConfig();
	@Config public static StoneTypeConfig voidstone = new StoneTypeConfig(DimensionConfig.end(false));
	@Config public static StoneTypeConfig myalite = new StoneTypeConfig(DimensionConfig.end(false));

	public static Block marbleBlock, limestoneBlock, jasperBlock, slateBlock, basaltBlock, myaliteBlock;

	public static Map<Block, Block> polishedBlocks = Maps.newHashMap();
	
	private Queue<Runnable> defers = new ArrayDeque<>();
	
	@Override
	public void construct() {
		marbleBlock = makeStone("marble", marble, BigStoneClustersModule.marble, () -> enableMarble, MaterialColor.QUARTZ);
		limestoneBlock = makeStone("limestone", limestone, BigStoneClustersModule.limestone, () -> enableLimestone, MaterialColor.STONE);
		jasperBlock = makeStone("jasper", jasper, BigStoneClustersModule.jasper, () -> enableJasper, MaterialColor.RED_TERRACOTTA);
		slateBlock = makeStone("slate", slate, BigStoneClustersModule.slate, () -> enableSlate, MaterialColor.ICE);
		basaltBlock = makeStone("basalt", voidstone, BigStoneClustersModule.voidstone, () -> enableVoidstone, MaterialColor.BLACK);
		myaliteBlock = makeStone("myalite", myalite, BigStoneClustersModule.myalite, () -> enableMyalite, MaterialColor.PURPLE, MyaliteBlock::new);
	}
	
	private Block makeStone(String name, StoneTypeConfig config, BigStoneClusterConfig bigConfig, BooleanSupplier enabledCond, MaterialColor color) {
		return makeStone(name, config, bigConfig, enabledCond, color, QuarkBlock::new);
	}
	
	private Block makeStone(String name, StoneTypeConfig config, BigStoneClusterConfig bigConfig, BooleanSupplier enabledCond, MaterialColor color, QuarkBlock.Constructor<QuarkBlock> constr) {
		BooleanSupplier trueEnabledCond = () -> (!ModuleLoader.INSTANCE.isModuleEnabled(BigStoneClustersModule.class) || !bigConfig.enabled) && enabledCond.getAsBoolean();
		
		Block.Properties props = Block.Properties.create(Material.ROCK, color)
				.setRequiresTool() // needs tool
				.harvestTool(ToolType.PICKAXE)
				.hardnessAndResistance(1.5F, 6.0F); 
		
		QuarkBlock normal = constr.make(name, this, ItemGroup.BUILDING_BLOCKS, props).setCondition(enabledCond);
		QuarkBlock polished = constr.make("polished_" + name, this, ItemGroup.BUILDING_BLOCKS, props).setCondition(enabledCond);
		polishedBlocks.put(normal, polished);

		VariantHandler.addSlabStairsWall(normal);
		VariantHandler.addSlabAndStairs(polished);
		
		defers.add(() ->
			WorldGenHandler.addGenerator(this, new OreGenerator(config.dimensions, config.oregen, normal.getDefaultState(), OreGenerator.ALL_DIMS_STONE_MATCHER, trueEnabledCond), Decoration.UNDERGROUND_ORES, WorldGenWeights.NEW_STONES)
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
		enabledWithMyalite = enableMyalite && this.enabled;
	}
	
	@Override
	public void setup() {
		while(!defers.isEmpty())
			defers.poll().run();
	}
	
}
