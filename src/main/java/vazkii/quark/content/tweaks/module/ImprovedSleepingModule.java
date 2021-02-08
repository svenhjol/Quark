package vazkii.quark.content.tweaks.module;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
				TranslationTextComponent text = new TranslationTextComponent("quark.misc.now_afk");
				text.mergeStyle(TextFormatting.AQUA);
				SpamlessChatMessage.sendToPlayer(player, AFK_MSG, text);
			} else {
				player.getPersistentData().putBoolean(TAG_AFK, false);
				TranslationTextComponent text = new TranslationTextComponent("quark.misc.left_afk");
				text.mergeStyle(TextFormatting.AQUA);
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

		boolean isDay = world.getSkylightSubtracted() < 4;
		int msgCount = 10;
		int msg = world.rand.nextInt(msgCount);
		
		TranslationTextComponent message = new TranslationTextComponent(world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE) ?
				(isDay ? "quark.misc.day_has_passed" : ("quark.misc.night_has_passed" + msg)) :
				(isDay ? "quark.misc.day_no_passage" : "quark.misc.night_no_passage"));
		message.setStyle(message.getStyle().applyFormatting(TextFormatting.GOLD));

		for (ServerPlayerEntity player : server.getPlayerList().getPlayers())
			SpamlessChatMessage.sendToPlayer(player, SLEEP_MSG, message);
	}

	private static boolean doesPlayerCountForSleeping(PlayerEntity player) {
		return !player.isSpectator() && !player.getPersistentData().getBoolean(TAG_AFK);
	}

	private static boolean isPlayerSleeping(PlayerEntity player) {
		return player.isPlayerFullyAsleep();
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
			player.getPersistentData().putLong(TAG_JUST_SLEPT, player.world.getGameTime());
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		World world = event.world;
		MinecraftServer server = world.getServer();

		if (event.side == LogicalSide.CLIENT ||
				!world.getDimensionKey().getLocation().equals(DimensionType.OVERWORLD.getLocation()) ||
				event.phase != TickEvent.Phase.END ||
				server == null)
			return;

		if (isEveryoneAsleep(world)) {
			if (world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE) && world instanceof ServerWorld) {
				long time = world.getDayTime() + 24000L;
				((ServerWorld) world).func_241114_a_(ForgeEventFactory.onSleepFinished((ServerWorld) world, time - time % 24000L, world.getDayTime()));
			}

			world.getPlayers().stream().filter(LivingEntity::isSleeping).forEach(PlayerEntity::wakeUp);
			if (world.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
				((ServerWorld) world).resetRainAndThunder();
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
			boolean isDay = world.getCelestialAngleRadians(0F) < 0.5;

			int requiredPlayers = Math.max((int) Math.ceil((legitPlayers * percentReq)), 0);

			StringTextComponent sibling = new StringTextComponent("(" + sleepingPlayers.size() + "/" + requiredPlayers + ")");

			StringTextComponent sleepingList = new StringTextComponent("");

			for(String s : sleepingPlayers)
				sleepingList.append(new StringTextComponent("\n\u2714 " + s).mergeStyle(TextFormatting.GREEN));
			for(String s : nonSleepingPlayers)
				sleepingList.append(new StringTextComponent("\n\u2718 " + s).mergeStyle(TextFormatting.RED));

			TranslationTextComponent hoverText = new TranslationTextComponent("quark.misc.sleeping_list_header", sleepingList);

			HoverEvent hover = new HoverEvent(Action.SHOW_TEXT, hoverText.deepCopy());
			sibling.setStyle(sibling.getStyle().setHoverEvent(hover));
			sibling.getStyle().setUnderlined(true);

			String newPlayer = newSleepingPlayers.isEmpty() ? wasSleepingPlayers.get(0) : newSleepingPlayers.get(0);
			String translationKey = isDay ?
					(newSleepingPlayers.isEmpty() ? "quark.misc.person_not_napping" : "quark.misc.person_napping") :
					(newSleepingPlayers.isEmpty() ? "quark.misc.person_not_sleeping" : "quark.misc.person_sleeping");

			TranslationTextComponent message = new TranslationTextComponent(translationKey, newPlayer);
			message.mergeStyle(TextFormatting.GOLD);
			message.appendString(" ");

			message.append(sibling.deepCopy());

			for (ServerPlayerEntity player : server.getPlayerList().getPlayers())
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
				TranslationTextComponent text = new TranslationTextComponent("quark.misc.left_afk");
				text.mergeStyle(TextFormatting.AQUA);

				if (lastPlayer instanceof ServerPlayerEntity)
					SpamlessChatMessage.sendToPlayer(lastPlayer, AFK_MSG, text);
			}
		}
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.END && Minecraft.getInstance().world != null) {
			timeSinceKeystroke++;

			if(timeSinceKeystroke == afkTime)
				QuarkNetwork.sendToServer(new UpdateAfkMessage(true));
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onKeystroke(InputEvent.KeyInputEvent event) {
		registerPress();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onKeystroke(GuiScreenEvent.KeyboardKeyEvent event) {
		registerPress();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onPlayerClick(PlayerInteractEvent event) {
		registerPress();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onMousePress(GuiScreenEvent.MouseInputEvent event) {
		registerPress();
	}

	private void registerPress() {
		if(timeSinceKeystroke >= afkTime && Minecraft.getInstance().world != null)
			QuarkNetwork.sendToServer(new UpdateAfkMessage(false));
		timeSinceKeystroke = 0;
	}

}
