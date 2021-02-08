package vazkii.quark.base.world.generator.multichunk;

import java.util.Random;
import java.util.stream.IntStream;

import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.world.config.ClusterSizeConfig;
import vazkii.quark.base.world.config.IBiomeConfig;

public class ClusterShape {
	
	private final BlockPos src;
	private final Vector3d radius;
	private final PerlinNoiseGenerator noiseGenerator;
	
	public ClusterShape(BlockPos src, Vector3d radius, PerlinNoiseGenerator noiseGenerator) {
		this.src = src;
		this.radius = radius;
		this.noiseGenerator = noiseGenerator;
	}
	
	public boolean isInside(BlockPos pos) {
		return noiseDiff(pos) > 0;
	}

	// For result of this method, positive result is the valid spots and negative is outside.
	// 0 is edge of the cluster which is very useful for speedups in checks in
	// UndergroundSpaceGenerator as some checks should happen only at edges of cave.
	// You can use these kinds of tricks for speedups towards edge of other clusters as well.
	public double noiseDiff(BlockPos pos) {
		// normalize distances by the radius
		double dx = (double) (pos.getX() - src.getX()) / radius.x;
		double dy = (double) (pos.getY() - src.getY()) / radius.y;
		double dz = (double) (pos.getZ() - src.getZ()) / radius.z;

		double r = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if(r > 1)
			return -1;
		if(GeneralConfig.useFastWorldgen)
			return 1;

		// convert to spherical
		double phi = Math.atan2(dz, dx);
		double theta = r == 0 ? 0 : Math.acos(dy / r);

		// use phi, theta + the src pos to get noisemap uv
		double xn = phi + src.getX();
		double yn = theta + src.getZ();
		double noise = noiseGenerator.noiseAt(xn, yn, false);

		// when nearing the end of the loop, lerp back to the start to prevent it cutting off
		double cutoff = 0.75 * Math.PI;
		if(phi > cutoff) {
			double noise0 = noiseGenerator.noiseAt(-Math.PI + src.getX(), yn, false);
			noise = MathHelper.lerp((phi - cutoff) / (Math.PI - cutoff), noise, noise0);
		}

		// accept if within constrains
		double maxR = noise + 0.5;
		return maxR - r;
	}

	public int getUpperBound() {
		return (int) Math.ceil(src.getY() + radius.getY());
	}
	
	public int getLowerBound() {
		return (int) Math.floor(src.getY() - radius.getY());
	}
	
	public static class Provider {
		
		private final ClusterSizeConfig config;
		private final PerlinNoiseGenerator noiseGenerator;
		
		public Provider(ClusterSizeConfig config, long seed) {
			this.config = config;
			noiseGenerator = new PerlinNoiseGenerator(new SharedSeedRandom(seed), IntStream.rangeClosed(-4, 4));
		}
		
		public ClusterShape around(BlockPos src) {
			Random rand = randAroundBlockPos(src);
			
			int radiusX = config.horizontalSize + rand.nextInt(config.horizontalVariation);
			int radiusY = config.verticalSize + rand.nextInt(config.verticalVariation);
			int radiusZ = config.horizontalSize + rand.nextInt(config.horizontalVariation);
					
			return new ClusterShape(src, new Vector3d(radiusX, radiusY, radiusZ), noiseGenerator);
		}
		
		public int getRadius() {
			return config.horizontalSize + config.horizontalVariation;
		}
		
		public int getRarity() {
			return config.rarity;
		}
		
		public int getRandomYLevel(Random rand) {
			return config.minYLevel + (config.minYLevel == config.maxYLevel ? 0 : rand.nextInt(Math.max(config.maxYLevel, config.minYLevel) - Math.min(config.maxYLevel, config.minYLevel)));
		}

		public IBiomeConfig getBiomeTypes() {
			return config.biomes;
		}
		
		public Random randAroundBlockPos(BlockPos pos) {
			return new Random(31 * (31 * (31 + pos.getX()) + pos.getY()) + pos.getZ()); 
		}
		
	}
	
}