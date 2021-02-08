package vazkii.quark.content.tweaks.module;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.aurelienribon.tweenengine.Tween;
import vazkii.quark.base.client.handler.ModKeybindHandler;
import vazkii.quark.base.handler.ContributorRewardHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.RequestEmoteMessage;
import vazkii.quark.content.tweaks.client.emote.CustomEmoteIconResourcePack;
import vazkii.quark.content.tweaks.client.emote.EmoteBase;
import vazkii.quark.content.tweaks.client.emote.EmoteDescriptor;
import vazkii.quark.content.tweaks.client.emote.EmoteHandler;
import vazkii.quark.content.tweaks.client.emote.ModelAccessor;
import vazkii.quark.content.tweaks.client.gui.EmoteButton;
import vazkii.quark.content.tweaks.client.gui.TranslucentButton;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class EmotesModule extends QuarkModule {

	private static final Set<String> DEFAULT_EMOTE_NAMES = ImmutableSet.of(
			"no",
			"yes",
			"wave", 
			"salute", 
			"cheer",
			"clap", 
			"think",
			"point", 
			"shrug",
			"headbang",
			"weep",
			"facepalm");

	private static final Set<String> PATREON_EMOTES = ImmutableSet.of(
			"dance", 
			"tpose", 
			"dab",
			"jet",
			"exorcist",
			"zombie");

	public static final int EMOTE_BUTTON_WIDTH = 25;
	public static final int EMOTES_PER_ROW = 3;

	@Config(description = "The enabled default emotes. Remove from this list to disable them. You can also re-order them, if you feel like it.")
	public static List<String> enabledEmotes = Lists.newArrayList(DEFAULT_EMOTE_NAMES);
	
	@Config(description = "The list of Custom Emotes to be loaded.\nWatch the tutorial on Custom Emotes to learn how to make your own: https://youtu.be/ourHUkan6aQ") 
	public static List<String> customEmotes = Lists.newArrayList();
	
	@Config(description = "Enable this to make custom emotes read the file every time they're triggered so you can edit on the fly.\nDO NOT ship enabled this in a modpack, please.")
	public static boolean customEmoteDebug = false;
	
	public static boolean emotesVisible = false;
	public static File emotesDir;
	
	@Environment(EnvType.CLIENT)
	public static CustomEmoteIconResourcePack resourcePack;

	@Environment(EnvType.CLIENT)
	private static Map<KeyBinding, String> emoteKeybinds;

	@Override
	public void constructClient() {
		MinecraftClient mc = MinecraftClient.getInstance();
		emotesDir = new File(mc.runDirectory, "/config/quark_emotes");
		if(!emotesDir.exists())
			emotesDir.mkdirs();
		
		mc.getResourcePackManager().addPackFinder(new ResourcePackProvider() {

			@Override
			public void register(Consumer<ResourcePackProfile> packConsumer, ResourcePackProfile.Factory packInfoFactory) {
				resourcePack = new CustomEmoteIconResourcePack();
				
				String name = "quark:emote_resources";
				ResourcePackProfile t = ResourcePackProfile.of(name, true, () -> resourcePack, packInfoFactory, ResourcePackProfile.InsertionPosition.TOP, tx->tx);
				packConsumer.accept(t);
			}
		});
	}
	
	@Override
	public void clientSetup() {
		Tween.registerAccessor(BipedEntityModel.class, ModelAccessor.INSTANCE);

		int sortOrder = 0;

		emoteKeybinds = new HashMap<>();
		for (String s : DEFAULT_EMOTE_NAMES)
			emoteKeybinds.put(ModKeybindHandler.init("quark.emote." + s, null, "", ModKeybindHandler.EMOTE_GROUP, sortOrder++, false), s);
		for (String s : PATREON_EMOTES)
			emoteKeybinds.put(ModKeybindHandler.init("patreon_emote." + s, null, ModKeybindHandler.EMOTE_GROUP, sortOrder++), s);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void configChangedClient() {
		EmoteHandler.clearEmotes();

		for(String s : enabledEmotes) {
			if (DEFAULT_EMOTE_NAMES.contains(s))
				EmoteHandler.addEmote(s);
		}

		for(String s : PATREON_EMOTES)
			EmoteHandler.addEmote(s);
		
		for(String s : customEmotes)
			EmoteHandler.addCustomEmote(s);
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void initGui(GuiScreenEvent.InitGuiEvent.Post event) {
		Screen gui = event.getGui();
		if(gui instanceof ChatScreen) {
			Map<Integer, List<EmoteDescriptor>> descriptorSorting = new TreeMap<>();

			for (EmoteDescriptor desc : EmoteHandler.emoteMap.values()) {
				if (desc.getTier() <= ContributorRewardHandler.localPatronTier) {
					List<EmoteDescriptor> descriptors = descriptorSorting.computeIfAbsent(desc.getTier(), k -> new LinkedList<>());

					descriptors.add(desc);
				}
			}

			int rows = 0;
			int row = 0;
			int tierRow, rowPos;

			Set<Integer> keys = descriptorSorting.keySet();
			for(int tier : keys) {
				List<EmoteDescriptor> descriptors = descriptorSorting.get(tier);
				if (descriptors != null) {
					rows += descriptors.size() / 3;
					if (descriptors.size() % 3 != 0)
						rows++;
				}
			}

			List<ButtonWidget> emoteButtons = new LinkedList<>();
			for (int tier : keys) {
				rowPos = 0;
				tierRow = 0;
				List<EmoteDescriptor> descriptors = descriptorSorting.get(tier);
				if (descriptors != null) {
					for (EmoteDescriptor desc : descriptors) {
						int rowSize = Math.min(descriptors.size() - tierRow * EMOTES_PER_ROW, EMOTES_PER_ROW);

						int x = gui.width - (EMOTE_BUTTON_WIDTH * (EMOTES_PER_ROW + 1)) + (((rowPos + 1) * 2 + EMOTES_PER_ROW - rowSize) * EMOTE_BUTTON_WIDTH / 2 + 1);
						int y = gui.height - (40 + EMOTE_BUTTON_WIDTH * (rows - row));

						ButtonWidget button = new EmoteButton(x, y, desc, (b) -> {
							String name = desc.getRegistryName();
							QuarkNetwork.sendToServer(new RequestEmoteMessage(name));
						});
						emoteButtons.add(button);
						
						button.visible = emotesVisible;
						button.active = emotesVisible;
						event.addWidget(button);

						if (++rowPos == EMOTES_PER_ROW) {
							tierRow++;
							row++;
							rowPos = 0;
						}
					}
				}
				if (rowPos != 0)
					row++;
			}
			
			event.addWidget(new TranslucentButton(gui.width - 1 - EMOTE_BUTTON_WIDTH * EMOTES_PER_ROW, gui.height - 40, EMOTE_BUTTON_WIDTH * EMOTES_PER_ROW, 20, 
					new TranslatableText("quark.gui.button.emotes"),
					(b) -> {
						for(ButtonWidget bt : emoteButtons)
							if(bt instanceof EmoteButton) {
								bt.visible = !bt.visible;
								bt.active = !bt.active;
							}

						emotesVisible = !emotesVisible;
					}));
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if(mc.isWindowFocused()) {
			for(KeyBinding key : emoteKeybinds.keySet()) {
				if (key.isPressed()) {
					String emote = emoteKeybinds.get(key);
					QuarkNetwork.sendToServer(new RequestEmoteMessage(emote));
					return;
				}
			}
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void drawHUD(RenderGameOverlayEvent.Post event) {
		if(event.getType() == ElementType.ALL) {
			MinecraftClient mc = MinecraftClient.getInstance();
			Window res = event.getWindow();
			MatrixStack matrix = event.getMatrixStack();
			EmoteBase emote = EmoteHandler.getPlayerEmote(mc.player);
			if(emote != null && emote.timeDone < emote.totalTime) {
				Identifier resource = emote.desc.texture;
				int x = res.getScaledWidth() / 2 - 16;
				int y = res.getScaledHeight() / 2 - 60;
				float transparency = 1F;
				float tween = 5F;

				if(emote.timeDone < tween)
					transparency = emote.timeDone / tween;
				else if(emote.timeDone > emote.totalTime - tween)
					transparency = (emote.totalTime - emote.timeDone) / tween;

				RenderSystem.pushMatrix();
				RenderSystem.disableLighting();
				RenderSystem.enableBlend();
				RenderSystem.disableAlphaTest();

				RenderSystem.color4f(1F, 1F, 1F, transparency);
				mc.getTextureManager().bindTexture(resource);
				Screen.drawTexture(matrix, x, y, 0, 0, 32, 32, 32, 32);
				RenderSystem.enableBlend();

				String name = I18n.translate(emote.desc.getTranslationKey());
				mc.textRenderer.drawWithShadow(matrix, name, res.getScaledWidth() / 2f - mc.textRenderer.getWidth(name) / 2f, y + 34, 0xFFFFFF + (((int) (transparency * 255F)) << 24));
				RenderSystem.popMatrix();
			}
		}
	}	


	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void renderTick(RenderTickEvent event) {
		EmoteHandler.onRenderTick(MinecraftClient.getInstance());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	@Environment(EnvType.CLIENT)
	public void preRenderLiving(RenderLivingEvent.Pre<PlayerEntity, ?> event) {
		if(event.getEntity() instanceof PlayerEntity)
			EmoteHandler.preRender((PlayerEntity) event.getEntity());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	@Environment(EnvType.CLIENT)
	public void postRenderLiving(RenderLivingEvent.Post<PlayerEntity, ?> event) {
		if(event.getEntity() instanceof PlayerEntity)
			EmoteHandler.postRender((PlayerEntity) event.getEntity());
	}


}
