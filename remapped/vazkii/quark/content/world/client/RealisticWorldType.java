package vazkii.quark.content.world.client;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldType;
import vazkii.quark.base.Quark;
import vazkii.quark.content.world.gen.RealisticChunkGenerator;

public class RealisticWorldType extends ForgeWorldType {

	final String name;
	
	public RealisticWorldType(String name, boolean large) {
		super(large ? RealisticWorldType::getChunkGeneratorBig : RealisticWorldType::getChunkGenerator);
		setRegistryName(new Identifier(Quark.MOD_ID, name));
		this.name = name;
	}
	
	@Override
	public String getTranslationKey() {
		return String.format("generator.%s.%s", Quark.MOD_ID, name);
	}

	static ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> settings, long seed) {
		return new RealisticChunkGenerator(new VanillaLayeredBiomeSource(seed, false, false, biomeRegistry), seed,
				() -> settings.getOrThrow(ChunkGeneratorSettings.OVERWORLD));
	}

	static ChunkGenerator getChunkGeneratorBig(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> settings, long seed) {
		return new RealisticChunkGenerator(new VanillaLayeredBiomeSource(seed, false, true, biomeRegistry), seed,
				() -> settings.getOrThrow(ChunkGeneratorSettings.OVERWORLD));
	}
	
}
