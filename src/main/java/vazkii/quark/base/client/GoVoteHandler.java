/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.quark.base.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import vazkii.quark.base.Quark;

/**
 * This class is released for public use via the Waive Clause of the Botania License.<br />
 * You are encouraged to copy and use it. Keep the same marker file path the same so multiple mods don't show the screen
 * at once.<br />
 * If you are uncomfortable with the network access to ip-api, feel free to remove it. The fallback is to examine the
 * computer's current locale.<br />
 * <br />
 * Quick Usage Guide:
 * <li>Copy to your mod</li>
 * <li>Call {@link #init} from {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent}</li>
 * <li>Replace {@link #BRAND} with your mod or group name.</li>
 */
@EventBusSubscriber(modid = Quark.MOD_ID, value = Dist.CLIENT)
public class GoVoteHandler {
	private static final String BRAND = "Vazkii's Mods";
	private static final String MARKER_PATH = ".vote2020_marker";
	private static final LocalDate ELECTION_DAY = LocalDate.of(2020, Month.NOVEMBER, 3);
	private static final String LINK = "https://vote.gov/";
	private static boolean shownThisSession = false;

	private static volatile boolean markerAlreadyExists = false;
	private static volatile String countryCode = Locale.getDefault().getCountry();

	public static void init() {
		try {
			Path path = Paths.get(MARKER_PATH);
			Files.createFile(path);
			Files.setAttribute(path, "dos:hidden", true);
		} catch (FileAlreadyExistsException ex) {
			Quark.LOG.debug("Go vote handler: Marker already exists");
			markerAlreadyExists = true;
			return;
		} catch (IOException ignored) {}

		if (isAfterElectionDay()) {
			return;
		}

		new Thread(() -> {
			try {
				URL url = new URL("http://ip-api.com/json/");
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(4000);
				conn.setReadTimeout(4000);
				try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
					Type typeToken = new TypeToken<Map<String, String>>() {}.getType();
					Map<String, String> map = new Gson().fromJson(reader, typeToken);
					countryCode = map.get("countryCode");
				}
			} catch (IOException ignored) {}
		}, "Go Vote Country Check").start();
	}

	private static boolean isAfterElectionDay() {
		return LocalDate.now().isAfter(ELECTION_DAY);
	}

	@SubscribeEvent
	public static void clientTick(GuiOpenEvent event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		Screen curr = event.getGui();
		if ((curr instanceof SelectWorldScreen || curr instanceof MultiplayerScreen) && shouldShow(mc)) {
			event.setGui(new GoVoteScreen(curr));
			shownThisSession = true;
		}
	}

	private static boolean shouldShow(MinecraftClient mc) {
		if (!isEnglish(mc) || shownThisSession || isAfterElectionDay() || markerAlreadyExists) {
			return false;
		}

		return "US".equals(countryCode);
	}

	private static boolean isEnglish(MinecraftClient mc) {
		return mc.getLanguageManager() != null
				&& mc.getLanguageManager().getLanguage() != null
				&& "English".equals(mc.getLanguageManager().getLanguage().getName());
	}

	private static class GoVoteScreen extends Screen {
		private static final int TICKS_PER_GROUP = 50;
		private final Screen parent;
		private int ticksElapsed = 0;
		private final List<List<Text>> message = new ArrayList<>();

		protected GoVoteScreen(Screen parent) {
			super(new LiteralText(""));
			this.parent = parent;
			addGroup(s("Please read the following message from " + BRAND + "."));
			addGroup(s("We are at a unique crossroads in the history of our country."));
			addGroup(s("In this time of heightened polarization,"),
					s("breakdown of political decorum, and fear,"));
			addGroup(s("it is tempting to succumb to apathy,"),
					s("to think that nothing you do will matter."));
			addGroup(LiteralText.EMPTY, s("But power is still in the hands of We, the People."));
			addGroup(s("The Constitution and its amendments guarantee every citizen the right to vote."));
			addGroup(s("And it is not only our right, but our ")
					.append(s("responsibility").formatted(Formatting.ITALIC, Formatting.GOLD))
					.append(s(" to do so.")));
			addGroup(s("Your vote matters. Always."));
			addGroup(LiteralText.EMPTY, s("Click anywhere to check if you are registered to vote."),
					s("The website is an official government site, unaffiliated with " + BRAND + "."));
			addGroup(s("Press ESC to exit. (This screen will not show up again.)"));
		}

		// Each group appears at the same time
		private void addGroup(Text... lines) {
			message.add(Arrays.asList(lines));
		}

		private static LiteralText s(String txt) {
			return new LiteralText(txt);
		}

		@Override
		public void tick() {
			super.tick();
			ticksElapsed++;
		}

		@Override
		public void render(MatrixStack mstack, int mx, int my, float pticks) {
			super.render(mstack, mx, my, pticks);

			fill(mstack, 0, 0, width, height, 0xFF696969);
			int middle = width / 2;
			int dist = 12;

			Text note1 = s("Note: If you can't vote in the United States,").formatted(Formatting.ITALIC);
			Text note2 = s("Please press ESC and carry on.").formatted(Formatting.ITALIC);
			drawCenteredText(mstack, textRenderer, note1, middle, 10, 0xFFFFFF);
			drawCenteredText(mstack, textRenderer, note2, middle, 22, 0xFFFFFF);

			int y = 46;
			for (int groupIdx = 0; groupIdx < message.size(); groupIdx++) {
				List<Text> group = message.get(groupIdx);
				if ((ticksElapsed - 20) > groupIdx * TICKS_PER_GROUP) {
					for (Text line : group) {
						drawCenteredText(mstack, textRenderer, line, middle, y, 0xFFFFFF);
						y += dist;
					}
				}
			}
		}

		@Nonnull
		@Override
		public String getNarrationMessage() {
			StringBuilder builder = new StringBuilder();
			for (List<Text> group : message) {
				for (Text line : group) {
					builder.append(line.getString());
				}
			}
			return builder.toString();
		}

		@Override
		public boolean keyPressed(int keycode, int scanCode, int modifiers) {
			if (keycode == GLFW.GLFW_KEY_ESCAPE) {
				client.openScreen(parent);
			}

			return super.keyPressed(keycode, scanCode, modifiers);
		}

		@Override
		public boolean mouseClicked(double x, double y, int modifiers) {
			if (ticksElapsed < 80) {
				return false;
			}

			if (modifiers == 0) {
				client.openScreen(new ConfirmChatLinkScreen(this::consume, LINK, true));
				return true;
			}

			return super.mouseClicked(x, y, modifiers);
		}

		private void consume(boolean doIt) {
			client.openScreen(this);
			if (doIt) {
				Util.getOperatingSystem().open(LINK);
			}
		}

	}

}