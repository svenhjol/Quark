package vazkii.quark.base.world;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.decorator.NopeDecoratorConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.world.generator.IGenerator;

public class WorldGenHandler {

	private static Map<GenerationStep.Feature, SortedSet<WeightedGenerator>> generators = new HashMap<>();

	public static void loadComplete() {
		for(GenerationStep.Feature stage : GenerationStep.Feature.values()) {
			ConfiguredFeature<?, ?> feature = new DeferedFeature(stage).configure(FeatureConfig.DEFAULT).createDecoratedFeature(new ChunkCornerPlacement().configure(NopeDecoratorConfig.DEFAULT));
			ForgeRegistries.BIOMES.forEach(biome -> biome.addFeature(stage, feature));
		}
	}
	
	public static void addGenerator(Module module, IGenerator generator, GenerationStep.Feature stage, int weight) {
		WeightedGenerator weighted = new WeightedGenerator(module, generator, weight);
		if(!generators.containsKey(stage))
			generators.put(stage, new TreeSet<>());

		generators.get(stage).add(weighted);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void conditionalizeFeatures(GenerationStep.Feature stage, BiPredicate<Feature<? extends FeatureConfig>, FeatureConfig> pred, BooleanSupplier condition) {
		ForgeRegistries.BIOMES.forEach(b -> {
			List<ConfiguredFeature<?, ?>> features = b.getFeaturesForStep(stage);

			for(int i = 0; i < features.size(); i++) {
				ConfiguredFeature<?, ?> configuredFeature = features.get(i);

				if(!(configuredFeature instanceof ConditionalConfiguredFeature)) {
					Feature<?> feature = configuredFeature.feature;
					FeatureConfig config = configuredFeature.config;

					if(config instanceof DecoratedFeatureConfig) {
						DecoratedFeatureConfig dconfig = (DecoratedFeatureConfig) config;
						feature = dconfig.feature.feature;
						config = dconfig.feature.config;
					}

					if(pred.test(feature, config)) {
						ConditionalConfiguredFeature conditional = new ConditionalConfiguredFeature(configuredFeature, condition);
						features.set(i, conditional);
					}
				}
			}
		});
	}

	public static void generateChunk(ServerWorldAccess seedReader, StructureAccessor structureManager, ChunkGenerator generator, BlockPos pos, GenerationStep.Feature stage) {
		if(!(seedReader instanceof ChunkRegion))
			return;

		ChunkRegion region = (ChunkRegion) seedReader;
		ChunkRandom random = new ChunkRandom();
		long seed = random.setPopulationSeed(region.getSeed(), region.getCenterChunkX() * 16, region.getCenterChunkZ() * 16);
		int stageNum = stage.ordinal() * 10000;

		if(generators.containsKey(stage)) {
			SortedSet<WeightedGenerator> set = generators.get(stage);

			for(WeightedGenerator wgen : set) {
				IGenerator gen = wgen.generator;

				if(wgen.module.enabled && gen.canGenerate(region)) {
					if(GeneralConfig.enableWorldgenWatchdog) {
						final int finalStageNum = stageNum;
						stageNum = watchdogRun(gen, () -> gen.generate(finalStageNum, seed, stage, region, generator, structureManager, random, pos), 1, TimeUnit.MINUTES);
					} else stageNum = gen.generate(stageNum, seed, stage, region, generator, structureManager, random, pos);
				}
			}
		}
	}
	
	private static int watchdogRun(IGenerator gen, Callable<Integer> run, int time, TimeUnit unit) {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<Integer> future = exec.submit(run);
		exec.shutdown();
		
		try {
			return future.get(time, unit);
		} catch(Exception e) {
			throw new RuntimeException("Error generating " + gen, e);
		} 
	}

}
