package vazkii.quark.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.gen.chunk.NoiseConfig;
import net.minecraft.world.gen.chunk.SurfaceChunkGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RealisticChunkGenerator extends SurfaceChunkGenerator {
	public static final Codec<RealisticChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			BiomeSource.field_24713.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
			Codec.LONG.fieldOf("seed").stable().forGetter(generator -> generator.seed),
			ChunkGeneratorType.field_24781.fieldOf("settings").forGetter(generator -> generator.field_24774))
			.apply(instance, instance.stable(RealisticChunkGenerator::new)));
	private final long seed;

	public RealisticChunkGenerator(BiomeSource biomeProvider, long seed, ChunkGeneratorType settings) {
		super(biomeProvider, seed, settings);
		this.seed = seed;
	}

	@Override
	protected Codec<? extends ChunkGenerator> method_28506() {
		return CODEC;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ChunkGenerator withSeed(long p_230349_1_) {
		return new RealisticChunkGenerator(this.biomeSource.withSeed(p_230349_1_), p_230349_1_, this.field_24774);
	}

	@Override
	public void sampleNoiseColumn(double[] noiseColumn, int noiseX, int noiseZ) {
		NoiseConfig settings = this.field_24774.method_28559();
		double densityMax;
		double variance;
		if (this.field_24777 != null) {
			densityMax = TheEndBiomeSource.getNoiseAt(this.field_24777, noiseX, noiseZ) - 8.0F;
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
			float centerDepth = this.biomeSource.getBiomeForNoiseGen(noiseX, seaLevel, noiseZ).getDepth();

			// Biome interpolation
			for(int localX = -2; localX <= 2; ++localX) {
				for(int localZ = -2; localZ <= 2; ++localZ) {
					Biome biome = this.biomeSource.getBiomeForNoiseGen(noiseX + localX, seaLevel, noiseZ + localZ);
					float depth = biome.getDepth();
					float scale = biome.getScale();

					float weightScale = depth > centerDepth ? 0.5F : 1.0F;
					float weightAt = weightScale * field_24775[localX + 2 + (localZ + 2) * 5] / (depth + 2.0F);
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
		double randomDensityOffset = settings.hasRandomDensityOffset() ? this.method_28553(noiseX, noiseZ) : 0.0D;
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
