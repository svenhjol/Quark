package vazkii.quark.content.world.gen;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RealisticChunkGenerator extends NoiseChunkGenerator {
	
	public static final Codec<RealisticChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.populationSource),
			Codec.LONG.fieldOf("seed").stable().forGetter(generator -> generator.seed),
			ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter(generator -> generator.settings))
			.apply(instance, instance.stable(RealisticChunkGenerator::new)));
	
	
	private final long seed;

	public RealisticChunkGenerator(BiomeSource biomeProvider, long seed, Supplier<ChunkGeneratorSettings> settings) {
		super(biomeProvider, seed, settings);
		this.seed = seed;
	}

	@Override
	protected Codec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ChunkGenerator withSeed(long seed) {
		return new RealisticChunkGenerator(this.populationSource.withSeed(seed), seed, this.settings);
	}

	@Override
	public void sampleNoiseColumn(double[] noiseColumn, int noiseX, int noiseZ) {
		GenerationShapeConfig settings = this.settings.get().getGenerationShapeConfig();
		double densityMax;
		double variance;
		if (this.islandNoise != null) {
			densityMax = TheEndBiomeSource.getNoiseAt(this.islandNoise, noiseX, noiseZ) - 8.0F;
			if (densityMax > 0.0D) {
				variance = 0.25D;
			} else {
				variance = 1.0D;
			}
		} else {
			float weightedScale = 0.0F;
			float weightedDepth = 0.0F;
			float weight = 0.0F;
			int seaLevel = this.getSeaLevel();
			float centerDepth = this.populationSource.getBiomeForNoiseGen(noiseX, seaLevel, noiseZ).getDepth();

			// Biome interpolation
			for(int localX = -2; localX <= 2; ++localX) {
				for(int localZ = -2; localZ <= 2; ++localZ) {
					Biome biome = this.populationSource.getBiomeForNoiseGen(noiseX + localX, seaLevel, noiseZ + localZ);
					float depth = biome.getDepth();
					float scale = biome.getScale();

					float weightScale = depth > centerDepth ? 0.5F : 1.0F;
					float weightAt = weightScale * BIOME_WEIGHT_TABLE[localX + 2 + (localZ + 2) * 5] / (depth + 2.0F);
					weightedScale += scale * weightAt;
					weightedDepth += depth * weightAt;
					weight += weightAt;
				}
			}

			float scaledDepth = weightedDepth / weight;
			float scaledScale = weightedScale / weight;
			double finalDepth = (scaledDepth * 0.5F - 0.125F);
			double finalScale = (scaledScale * 0.9F + 0.1F);
			densityMax = finalDepth * 0.265625D;
			variance = 107.04 / finalScale;
		}

		double horizontalNoiseScale = 175.0;
		double verticalNoiseScale = 75.0;
		double horizontalNoiseStretch = horizontalNoiseScale / 165.0;
		double verticalNoiseStretch = verticalNoiseScale / 106.612;
		double topTarget = settings.getTopSlide().getTarget();
		double topSize = settings.getTopSlide().getSize();
		double topOffset = settings.getTopSlide().getOffset();
		double bottomTarget = settings.getBottomSlide().getTarget();
		double bottomSize = settings.getBottomSlide().getSize();
		double bottomOffset = settings.getBottomSlide().getOffset();
		double randomDensityOffset = settings.hasRandomDensityOffset() ? this.getRandomDensityAt(noiseX, noiseZ) : 0.0D;
		double densityFactor = settings.getDensityFactor();
		double densityOffset = settings.getDensityOffset();

		for(int y = 0; y <= this.noiseSizeY; ++y) {
			double noise = this.sampleNoise(noiseX, y, noiseZ, horizontalNoiseScale, verticalNoiseScale, horizontalNoiseStretch, verticalNoiseStretch);
			double yOffset = 1.0D - (double) y * 2.0D / (double)this.noiseSizeY + randomDensityOffset;
			double finalDensity = yOffset * densityFactor + densityOffset;
			double falloff = (finalDensity + densityMax) * variance;
			if (falloff > 0.0D) {
				noise = noise + falloff * 4.0D;
			} else {
				noise = noise + falloff;
			}

			if (topSize > 0.0D) {
				double target = ((double)(this.noiseSizeY - y) - topOffset) / topSize;
				noise = MathHelper.clampedLerp(topTarget, noise, target);
			}

			if (bottomSize > 0.0D) {
				double target = ((double) y - bottomOffset) / bottomSize;
				noise = MathHelper.clampedLerp(bottomTarget, noise, target);
			}

			noiseColumn[y] = noise;
		}
	}
}
