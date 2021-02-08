package vazkii.quark.content.tweaks.module;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.Action;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.SpamlessChatMessage;
import vazkii.quark.base.network.message.UpdateAfkMessage;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class ImprovedSleepingModule extends QuarkModule {

	private int timeSinceKeystroke;
	private static List<String> sleepingPlayers = new ArrayList<>();

	@Config
	public static boolean enableAfk = true;

	@Config
	@Config.Min(value = 0, exclusive = true)
	public static int afkTime = 2 * 1200;

	@Config
	@Config.Min(value = 0, exclusive = true)
	@Config.Max(1)
	public static double percentReq = 1;

	private static final String TAG_JUST_SLEPT = "quark:slept";
	private static final String TAG_AFK = "quark:afk";

	private static final int AFK_MSG = "quark afk".hashCode();
	private static final int SLEEP_MSG = "quark sleep".hashCode();

	public static void updateAfk(PlayerEntity player, boolean afk) {
		if(!ModuleLoader.INSTANCE.isModuleEnabled(ImprovedSleepingModule.class) || !enableAfk)
			return;

		if(player.world.getPlayers().size() != 1) {
			if(afk) {
				player.getPersistentData().putBoolean(TAG_AFK, true);
				TranslatableText text = new TranslatableText("quark.misc.now_afk");
				text.formatted(Formatting.AQUA);
				SpamlessChatMessage.sendToPlayer(player, AFK_MSG, text);
			} else {
				player.getPersistentData().putBoolean(TAG_AFK, false);
				TranslatableText text = new TranslatableText("quark.misc.left_afk");
				text.formatted(Formatting.AQUA);
				SpamlessChatMessage.sendToPlayer(player, AFK_MSG, text);
			}
		}
	}

	public static boolean isEveryoneAsleep(boolean parent) {
		if(!ModuleLoader.INSTANCE.isModuleEnabled(ImprovedSleepingModule.class))
			return parent;

		return false;
	}

	public static boolean isEveryoneAsleep(World world) {
		Pair<Integer, Integer> counts = getPlayerCounts(world);
		int legitPlayers = counts.getLeft();
		int sleepingPlayers = counts.getRight();

		int reqPlayers = Math.max(1, (int) (percentReq * (double) legitPlayers));
		return (legitPlayers > 0 && ((float) sleepingPlayers / (float) reqPlayers) >= 1);
	}

	public static void whenNightPasses(ServerWorld world) {
		MinecraftServer server = world.getServer();

		if (world.getPlayers().size() == 1)
			return;

		boolean isDay = world.getAmbientDarkness() < 4;
		int msgCount = 10;
		int msg = world.random.nextInt(msgCount);
		
		TranslatableText message = new TranslatableText(world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE) ?
				(isDay ? "quark.misc.day_has_passed" : ("quark.misc.night_has_passed" + msg)) :
				(isDay ? "quark.misc.day_no_passage" : "quark.misc.night_no_passage"));
		message.setStyle(message.getStyle().withFormatting(Formatting.GOLD));

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
			SpamlessChatMessage.sendToPlayer(player, SLEEP_MSG, message);
	}

	private static boolean doesPlayerCountForSleeping(PlayerEntity player) {
		return !player.isSpectator() && !player.getPersistentData().getBoolean(TAG_AFK);
	}

	private static boolean isPlayerSleeping(PlayerEntity player) {
		return player.isSleepingLongEnough();
	}

	private static Pair<Integer, Integer> getPlayerCounts(World world) {
		int legitPlayers = 0;
		int sleepingPlayers = 0;
		for(PlayerEntity player : world.getPlayers())
			if(doesPlayerCountForSleeping(player)) {
				legitPlayers++;

				if(isPlayerSleeping(player))
					sleepingPlayers++;
			}

		return Pair.of(legitPlayers, sleepingPlayers);
	}

	@SubscribeEvent
	public void onWakeUp(PlayerWakeUpEvent event) {
		PlayerEntity player = event.getPlayer();
		if (/*event.shouldSetSpawn() && */!event.updateWorld() && !event.wakeImmediately())
			player.getPersistentData().putLong(TAG_JUST_SLEPT, player.world.getTime());
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		World world = event.world;
		MinecraftServer server = world.getServer();

		if (event.side == LogicalSide.CLIENT ||
				!world.getRegistryKey().getValue().equals(DimensionType.OVERWORLD_REGISTRY_KEY.getValue()) ||
				event.phase != TickEvent.Phase.END ||
				server == null)
			return;

		if (isEveryoneAsleep(world)) {
			if (world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE) && world instanceof ServerWorld) {
				long time = world.getTimeOfDay() + 24000L;
				((ServerWorld) world).setTimeOfDay(ForgeEventFactory.onSleepFinished((ServerWorld) world, time - time % 24000L, world.getTimeOfDay()));
			}

			world.getPlayers().stream().filter(LivingEntity::isSleeping).forEach(PlayerEntity::wakeUp);
			if (world.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
				((ServerWorld) world).resetWeather();
			}

			if (world instanceof ServerWorld)
				whenNightPasses((ServerWorld) world);
			ImprovedSleepingModule.sleepingPlayers.clear();
			return;
		}
		
		List<String> sleepingPlayers = new ArrayList<>();
		List<String> newSleepingPlayers = new ArrayList<>();
		List<String> wasSleepingPlayers = new ArrayList<>();
		List<String> nonSleepingPlayers = new ArrayList<>();
		int legitPlayers = 0;

		for(PlayerEntity player : world.getPlayers()) {
			if (doesPlayerCountForSleeping(player)) {
				String name = player.getGameProfile().getName();
				if (isPlayerSleeping(player)) {
					if (!ImprovedSleepingModule.sleepingPlayers.contains(name))
						newSleepingPlayers.add(name);
					sleepingPlayers.add(name);
				} else {
					if (ImprovedSleepingModule.sleepingPlayers.contains(name))
						wasSleepingPlayers.add(name);
					nonSleepingPlayers.add(name);
				}

				legitPlayers++;
			}
		}

		ImprovedSleepingModule.sleepingPlayers = sleepingPlayers;

		if((!newSleepingPlayers.isEmpty() || !wasSleepingPlayers.isEmpty()) && world.getPlayers().size() != 1) {
			boolean isDay = world.getSkyAngleRadians(0F) < 0.5;

			int requiredPlayers = Math.max((int) Math.ceil((legitPlayers * percentReq)), 0);

			LiteralText sibling = new LiteralText("(" + sleepingPlayers.size() + "/" + requiredPlayers + ")");

			LiteralText sleepingList = new LiteralText("");

			for(String s : sleepingPlayers)
				sleepingList.append(new LiteralText("\n\u2714 " + s).formatted(Formatting.GREEN));
			for(String s : nonSleepingPlayers)
				sleepingList.append(new LiteralText("\n\u2718 " + s).formatted(Formatting.RED));

			TranslatableText hoverText = new TranslatableText("quark.misc.sleeping_list_header", sleepingList);

			HoverEvent hover = new HoverEvent(Action.SHOW_TEXT, hoverText.shallowCopy());
			sibling.setStyle(sibling.getStyle().withHoverEvent(hover));
			sibling.getStyle().setUnderlined(true);

			String newPlayer = newSleepingPlayers.isEmpty() ? wasSleepingPlayers.get(0) : newSleepingPlayers.get(0);
			String translationKey = isDay ?
					(newSleepingPlayers.isEmpty() ? "quark.misc.person_not_napping" : "quark.misc.person_napping") :
					(newSleepingPlayers.isEmpty() ? "quark.misc.person_not_sleeping" : "quark.misc.person_sleeping");

			TranslatableText message = new TranslatableText(translationKey, newPlayer);
			message.formatted(Formatting.GOLD);
			message.append(" ");

			message.append(sibling.shallowCopy());

			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
				SpamlessChatMessage.sendToPlayer(player, SLEEP_MSG, message);
		}
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		World logoutWorld = event.getPlayer().world;
		List<? extends PlayerEntity> players = logoutWorld.getPlayers();
		if(players.size() == 1) {
			PlayerEntity lastPlayer = players.get(0);
			if(lastPlayer.getPersistentData().getBoolean(TAG_AFK)) {
				lastPlayer.getPersistentData().putBoolean(TAG_AFK, false);
				TranslatableText text = new TranslatableText("quark.misc.left_afk");
				text.formatted(Formatting.AQUA);

				if (lastPlayer instanceof ServerPlayerEntity)
					SpamlessChatMessage.sendToPlayer(lastPlayer, AFK_MSG, text);
			}
		}
	}
	
	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.END && MinecraftClient.getInstance().world != null) {
			timeSinceKeystroke++;

			if(timeSinceKeystroke == afkTime)
				QuarkNetwork.sendToServer(new UpdateAfkMessage(true));
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onKeystroke(InputEvent.KeyInputEvent event) {
		registerPress();
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onKeystroke(GuiScreenEvent.KeyboardKeyEvent event) {
		registerPress();
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onPlayerClick(PlayerInteractEvent event) {
		registerPress();
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onMousePress(GuiScreenEvent.MouseInputEvent event) {
		registerPress();
	}

	private void registerPress() {
		if(timeSinceKeystroke >= afkTime && MinecraftClient.getInstance().world != null)
			QuarkNetwork.sendToServer(new UpdateAfkMessage(false));
		timeSinceKeystroke = 0;
	}

}
