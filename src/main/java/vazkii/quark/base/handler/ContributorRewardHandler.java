package vazkii.quark.base.handler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.quark.base.Quark;

@Mod.EventBusSubscriber(modid = Quark.MOD_ID)
public class ContributorRewardHandler {

	private static final ImmutableSet<String> DEV_UUID = ImmutableSet.of(
			"8c826f34-113b-4238-a173-44639c53b6e6",
			"0d054077-a977-4b19-9df9-8a4d5bf20ec3",
			"458391f5-6303-4649-b416-e4c0d18f837a",
			"75c298f9-27c8-415b-9a16-329e3884054b");

	private static final Set<String> done = Collections.newSetFromMap(new WeakHashMap<>());

	private static Thread thread;
	private static boolean doneLoading;

	private static String name;

	private static final Map<String, Integer> tiers = new HashMap<>();

	public static int localPatronTier = 0;
	public static String featuredPatron = "N/A";

	@OnlyIn(Dist.CLIENT)
	public static void getLocalName() {
		name = Minecraft.getInstance().getSession().getUsername().toLowerCase(Locale.ROOT);
	}

	public static void init() {
		if (thread != null && thread.isAlive())
			return;
		
		doneLoading = false;
		thread = new ThreadContributorListLoader();
	}

	public static int getTier(PlayerEntity player) {
		return getTier(player.getGameProfile().getName());
	}
	
	public static int getTier(String name) {
		join();
		return tiers.getOrDefault(name.toLowerCase(Locale.ROOT), 0);
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onRenderPlayer(RenderPlayerEvent.Post event) {
		PlayerEntity player = event.getPlayer();
		String uuid = PlayerEntity.getUUID(player.getGameProfile()).toString();
		if(player instanceof AbstractClientPlayerEntity && DEV_UUID.contains(uuid) && !done.contains(uuid)) {
			AbstractClientPlayerEntity clientPlayer = (AbstractClientPlayerEntity) player;
			if(clientPlayer.hasPlayerInfo()) {
				NetworkPlayerInfo info = ((AbstractClientPlayerEntity) player).playerInfo;
				Map<MinecraftProfileTexture.Type, ResourceLocation> textures = info.playerTextures;
				ResourceLocation loc = new ResourceLocation("quark", "textures/misc/dev_cape.png");
				textures.put(MinecraftProfileTexture.Type.CAPE, loc);
				textures.put(MinecraftProfileTexture.Type.ELYTRA, loc);
				done.add(uuid);
			}
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.DEDICATED_SERVER)
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		ContributorRewardHandler.init();
	}
	
	private static void join() {
		if(!doneLoading)
			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
	}
	
	private static void load(Properties props) {
		List<String> allPatrons = new ArrayList<>(props.size());

		props.forEach((k, v) -> {
			String key = (String) k;
			String value = (String) v;
			
			int tier = Integer.parseInt(value);
			if(tier < 10)
				allPatrons.add(key);
			tiers.put(key.toLowerCase(Locale.ROOT), tier);
			
			if(name != null && key.toLowerCase(Locale.ROOT).equals(name))
				localPatronTier = tier;
		});
		
		if(!allPatrons.isEmpty())
			featuredPatron = allPatrons.get((int) (Math.random() * allPatrons.size()));

		doneLoading = true;
	}

	private static class ThreadContributorListLoader extends Thread {

		public ThreadContributorListLoader() {
			setName("Quark Contributor Loading Thread");
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			try {
				URL url = new URL("https://raw.githubusercontent.com/Vazkii/Quark/master/contributors.properties");
				Properties patreonTiers = new Properties();
				try (InputStreamReader reader = new InputStreamReader(url.openStream())) {
					patreonTiers.load(reader);
					load(patreonTiers);
				}
			} catch (IOException e) {
				Quark.LOG.error("Failed to load patreon information", e);
			}
		}

	}

}
