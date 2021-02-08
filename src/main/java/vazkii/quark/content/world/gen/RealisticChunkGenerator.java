package vazkii.quark.content.world.gen;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.EndBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.settings.NoiseSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RealisticChunkGenerator extends NoiseChunkGenerator {
	
	public static final Codec<RealisticChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			BiomeProvider.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeProvider),
			Codec.LONG.fieldOf("seed").stable().forGetter(generator -> generator.seed),
			DimensionSettings.field_236098_b_.fieldOf("settings").forGetter(generator -> generator.field_236080_h_))
			.apply(instance, instance.stable(RealisticChunkGenerator::new)));
	
	
	private final long seed;

	public RealisticChunkGenerator(BiomeProvider biomeProvider, long seed, Supplier<DimensionSettings> settings) {
		super(biomeProvider, seed, settings);
		this.seed = seed;
	}

	@Override
	protected Codec<? extends ChunkGenerator> func_230347_a_() {
		return CODEC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ChunkGenerator func_230349_a_(long seed) {
		return new RealisticChunkGenerator(this.biomeProvider.getBiomeProvider(seed), seed, this.field_236080_h_);
	}

	@Override
	public void fillNoiseColumn(double[] noiseColumn, int noiseX, int noiseZ) {
		NoiseSettings settings = this.field_236080_h_.get().getNoise();
		double densityMax;
		double variance;
		if (this.field_236083_v_ != null) {
			densityMax = EndBiomeProvider.getRandomNoise(this.field_236083_v_, noiseX, noiseZ) - 8.0F;
			if (densityMax > 0.0D) {
				variance = 0.25D;
			} else {
				variance = 1.0D;
			}
		} else {
			float weightedScale = 0.0F;
			float weightedDepth = 0.0F;
			float weight = 0.0F;
			int seaLevel = this.func_230356_f_();
			float centerDepth = this.biomeProvider.getNoiseBiome(noiseX, seaLevel, noiseZ).getDepth();

			// Biome interpolation
			for(int localX = -2; localX <= 2; ++localX) {
				for(int localZ = -2; localZ <= 2; ++localZ) {
					Biome biome = this.biomeProvider.getNoiseBiome(noiseX + localX, seaLevel, noiseZ + localZ);
					float depth = biome.getDepth();
					float scale = biome.getScale();

					float weightScale = depth > centerDepth ? 0.5F : 1.0F;
					float weightAt = weightScale * field_236081_j_[localX + 2 + (localZ + 2) * 5] / (depth + 2.0F);
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
		double topTarget = settings.func_236172_c_().func_236186_a_();
		double topSize = settings.func_236172_c_().func_236188_b_();
		double topOffset = settings.func_236172_c_().func_236189_c_();
		double bottomTarget = settings.func_236173_d_().func_236186_a_();
		double bottomSize = settings.func_236173_d_().func_236188_b_();
		double bottomOffset = settings.func_236173_d_().func_236189_c_();
		double randomDensityOffset = settings.func_236179_j_() ? this.func_236095_c_(noiseX, noiseZ) : 0.0D;
		double densityFactor = settings.func_236176_g_();
		double densityOffset = settings.func_236177_h_();

		for(int y = 0; y <= this.noiseSizeY; ++y) {
			double noise = this.func_222552_a(noiseX, y, noiseZ, horizontalNoiseScale, verticalNoiseScale, horizontalNoiseStretch, verticalNoiseStretch);
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
