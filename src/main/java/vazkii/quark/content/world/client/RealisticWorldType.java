package vazkii.quark.content.world.client;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraftforge.common.world.ForgeWorldType;
import vazkii.quark.base.Quark;
import vazkii.quark.content.world.gen.RealisticChunkGenerator;

public class RealisticWorldType extends ForgeWorldType {

	final String name;
	
	public RealisticWorldType(String name, boolean large) {
		super(large ? RealisticWorldType::getChunkGeneratorBig : RealisticWorldType::getChunkGenerator);
		setRegistryName(new ResourceLocation(Quark.MOD_ID, name));
		this.name = name;
	}
	
	@Override
	public String getTranslationKey() {
		return String.format("generator.%s.%s", Quark.MOD_ID, name);
	}

	static ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<DimensionSettings> settings, long seed) {
		return new RealisticChunkGenerator(new OverworldBiomeProvider(seed, false, false, biomeRegistry), seed,
				() -> settings.getOrThrow(DimensionSettings.field_242734_c));
	}

	static ChunkGenerator getChunkGeneratorBig(Registry<Biome> biomeRegistry, Registry<DimensionSettings> settings, long seed) {
		return new RealisticChunkGenerator(new OverworldBiomeProvider(seed, false, true, biomeRegistry), seed,
				() -> settings.getOrThrow(DimensionSettings.field_242734_c));
	}
	
}
