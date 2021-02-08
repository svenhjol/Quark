package vazkii.quark.base.world.config;

import net.minecraft.util.math.BlockPos;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.module.config.IConfigType;

import java.util.Random;
import java.util.function.Consumer;

public class OrePocketConfig implements IConfigType {

	@Config
	@Config.Min(0)
	@Config.Max(255)
	private int minHeight;

	@Config
	@Config.Min(0)
	@Config.Max(255)
	private int maxHeight;

	@Config
	@Config.Min(0)
	public int clusterSize;

	@Config
	@Config.Min(0)
	public int clusterCount;

	public OrePocketConfig(int minHeight, int maxHeight, int clusterSize, int clusterCount) {
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		this.clusterSize = clusterSize;
		this.clusterCount = clusterCount;
	}

	public int getRandomHeight(Random rand) {
		return minHeight + rand.nextInt(maxHeight - minHeight);
	}

	public void forEach(BlockPos chunkCorner, Random rand, Consumer<BlockPos> callback) {
		for (int i = 0; i < clusterCount; i++) {
			int x = chunkCorner.getX() + rand.nextInt(16);
			int y = getRandomHeight(rand);
			int z = chunkCorner.getZ() + rand.nextInt(16);

			callback.accept(new BlockPos(x, y, z));
		}
	}

}
