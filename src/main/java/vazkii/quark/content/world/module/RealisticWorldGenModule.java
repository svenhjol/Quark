package vazkii.quark.content.world.module;

import java.util.Locale;
import java.util.Optional;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerProperties;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.ServerWorldInfo;
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

	public static final ResourceLocation REALISTIC_RES = new ResourceLocation("quark", "realistic");

	public static final RegistryKey<DimensionSettings> REALISTIC_KEY = RegistryKey.getOrCreateKey(Registry.NOISE_SETTINGS_KEY, REALISTIC_RES);

	@Override
	public void construct() {
		Registry.register(Registry.CHUNK_GENERATOR_CODEC, REALISTIC_RES, RealisticChunkGenerator.CODEC);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void constructClient() {
		RegistryHelper.register(new RealisticWorldType("realistic", false));
		RegistryHelper.register(new RealisticWorldType("realistic_large_biomes", true));
	}

}
