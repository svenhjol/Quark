package vazkii.quark.world.module;

import java.util.Locale;
import java.util.Optional;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.world.gen.RealisticChunkGenerator;
import vazkii.quark.world.client.RealisticGenScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.level.LevelProperties;

@LoadModule(category = ModuleCategory.WORLD, hasSubscriptions = true, subscribeOn = Dist.DEDICATED_SERVER)
public class RealisticWorldGenModule extends Module {

	@Override
	public void construct() {
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier("quark", "realistic"), RealisticChunkGenerator.CODEC);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void constructClient() {
		new RealisticGenScreen();
	}

	private static GeneratorOptions createSettings(long seed, boolean generateFeatures, boolean generateBonusChest) {
		return new GeneratorOptions(seed, generateFeatures, generateBonusChest, GeneratorOptions.method_28608(DimensionType.method_28517(seed), new RealisticChunkGenerator(new VanillaLayeredBiomeSource(seed, false, false), seed, ChunkGeneratorType.Preset.OVERWORLD.getChunkGeneratorType())));
	}

	@SubscribeEvent
	public void onServerStart(FMLServerAboutToStartEvent event) {
		// Check that we're on the dedicated server before checking the world type
		if (event.getServer() instanceof MinecraftDedicatedServer) {
			MinecraftDedicatedServer server = (MinecraftDedicatedServer) event.getServer();
			String levelType = Optional.ofNullable((String)server.getProperties().properties.get("level-type")).map(str -> str.toLowerCase(Locale.ROOT)).orElse("default");

			// If the world type is realistic, then replace the worldgen data
			if (levelType.equals("realistic")) {
				if (server.getSaveProperties() instanceof LevelProperties) {
					LevelProperties worldInfo = (LevelProperties)server.getSaveProperties();
					worldInfo.field_25425 = createSettings(worldInfo.field_25425.getSeed(), worldInfo.field_25425.shouldGenerateStructures(), worldInfo.field_25425.hasBonusChest());
				}
				ServerPropertiesHandler properties = server.getProperties();
				properties.field_24623 = createSettings(properties.field_24623.getSeed(), properties.field_24623.shouldGenerateStructures(), properties.field_24623.hasBonusChest());
			}
		}
	}
}
