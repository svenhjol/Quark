package vazkii.quark.base.world;

import java.util.Random;
import java.util.function.BooleanSupplier;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;

public class ConditionalConfiguredFeature<FC extends FeatureConfig, F extends Feature<FC>> extends ConfiguredFeature<FC, F> {

	public final ConfiguredFeature<FC, F> parent;
	public final BooleanSupplier condition;

	public ConditionalConfiguredFeature(ConfiguredFeature<FC, F> parent, BooleanSupplier condition) {
		super(parent.feature, parent.config);
		this.parent = parent;
		this.condition = condition;
	}

	@Override // place
	public boolean generate(StructureWorldAccess p_242765_1_, ChunkGenerator p_242765_2_, Random p_242765_3_, BlockPos p_242765_4_) {
		return condition.getAsBoolean() && super.generate(p_242765_1_, p_242765_2_, p_242765_3_, p_242765_4_);
	}

}
