package vazkii.quark.world.client;

import net.minecraft.client.world.GeneratorType;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import vazkii.quark.world.gen.RealisticChunkGenerator;

public class RealisticGenScreen extends GeneratorType {

	public RealisticGenScreen() {
		super("quark.realistic");
		VALUES.add(this);
	}

	@Override
	protected ChunkGenerator method_29076(long seed) {
		return new RealisticChunkGenerator(new VanillaLayeredBiomeSource(seed, false, false), seed, ChunkGeneratorType.Preset.OVERWORLD.getChunkGeneratorType());
	}
}
