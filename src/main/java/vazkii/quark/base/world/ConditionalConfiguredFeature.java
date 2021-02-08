package vazkii.quark.base.world;

import java.util.Random;
import java.util.function.BooleanSupplier;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.StructureManager;

public class ConditionalConfiguredFeature<FC extends IFeatureConfig, F extends Feature<FC>> extends ConfiguredFeature<FC, F> {

	public final ConfiguredFeature<FC, F> parent;
	public final BooleanSupplier condition;

	public ConditionalConfiguredFeature(ConfiguredFeature<FC, F> parent, BooleanSupplier condition) {
		super(parent.feature, parent.config);
		this.parent = parent;
		this.condition = condition;
	}

	@Override // place
	public boolean func_242765_a(ISeedReader p_242765_1_, ChunkGenerator p_242765_2_, Random p_242765_3_, BlockPos p_242765_4_) {
		return condition.getAsBoolean() && super.func_242765_a(p_242765_1_, p_242765_2_, p_242765_3_, p_242765_4_);
	}

}
