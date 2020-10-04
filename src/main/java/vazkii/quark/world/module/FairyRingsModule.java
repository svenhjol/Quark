package vazkii.quark.world.module;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep.Feature;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.world.gen.FairyRingGenerator;

@LoadModule(category = ModuleCategory.WORLD)
public class FairyRingsModule extends Module {

	@Config public static double forestChance = 0.00625;
	@Config public static double  plainsChance = 0.0025;
	@Config public static DimensionConfig dimensions = new DimensionConfig(false, "minecraft:overworld");
	
	@Config(name = "Ores")
	public static List<String> oresRaw = Lists.newArrayList("minecraft:emerald_ore", "minecraft:diamond_ore"); 
	
	public static List<BlockState> ores;
	
	@Override
	public void setup() {
		WorldGenHandler.addGenerator(this, new FairyRingGenerator(dimensions), Feature.TOP_LAYER_MODIFICATION, WorldGenWeights.FAIRY_RINGS);
	}
	
	@Override
	public void configChanged() {
		ores = new ArrayList<>();
		for(String s : oresRaw) {
			Block b = ForgeRegistries.BLOCKS.getValue(new Identifier(s));
			if(b == null)
				new IllegalArgumentException("Block " + s + " does not exist!").printStackTrace();
			else ores.add(b.getDefaultState());
		}
	}

}
