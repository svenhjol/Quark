package vazkii.quark.content.world.module;

import java.util.Locale;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.world.client.RealisticWorldType;
import vazkii.quark.content.world.gen.RealisticChunkGenerator;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.WORLD, hasSubscriptions = true, subscribeOn = Dist.DEDICATED_SERVER)
public class RealisticWorldGenModule extends QuarkModule {

	public static final Identifier REALISTIC_RES = new Identifier("quark", "realistic");

	public static final RegistryKey<ChunkGeneratorSettings> REALISTIC_KEY = RegistryKey.of(Registry.NOISE_SETTINGS_WORLDGEN, REALISTIC_RES);

	@Override
	public void construct() {
		Registry.register(Registry.CHUNK_GENERATOR, REALISTIC_RES, RealisticChunkGenerator.CODEC);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void constructClient() {
		RegistryHelper.register(new RealisticWorldType("realistic", false));
		RegistryHelper.register(new RealisticWorldType("realistic_large_biomes", true));
	}

}
