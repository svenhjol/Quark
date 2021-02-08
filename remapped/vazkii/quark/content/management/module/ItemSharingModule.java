/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [06/06/2016, 01:40:29 (GMT)]
 */
package vazkii.quark.content.management.module;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.LinkItemMessage;

@LoadModule(category = ModuleCategory.MANAGEMENT, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class ItemSharingModule extends QuarkModule {

	@Config
	public static boolean renderItemsInChat = true;

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void keyboardEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		GameOptions settings = mc.options;
		if(InputUtil.isKeyPressed(mc.getWindow().getHandle(), settings.keyChat.getKey().getCode()) &&
				event.getGui() instanceof HandledScreen && Screen.hasShiftDown()) {
			HandledScreen<?> gui = (HandledScreen<?>) event.getGui();
			
			List<? extends Element> children = gui.children();
			for(Element c : children)
				if(c instanceof TextFieldWidget) {
					TextFieldWidget tf = (TextFieldWidget) c;
					if(tf.isFocused())
						return;
				}
			
			Slot slot = gui.getSlotUnderMouse();
			if(slot != null && slot.inventory != null) {
				ItemStack stack = slot.getStack();

				if(!stack.isEmpty() && !MinecraftForge.EVENT_BUS.post(new ClientChatEvent(stack.toHoverableText().getString()))) {
					QuarkNetwork.sendToServer(new LinkItemMessage(stack));
					event.setCanceled(true);
				}
			}
		}
	}

	public static void linkItem(PlayerEntity player, ItemStack item) {
		if(!ModuleLoader.INSTANCE.isModuleEnabled(ItemSharingModule.class))
			return;

		if(!item.isEmpty() && player instanceof ServerPlayerEntity) {
			Text comp = item.toHoverableText();
			Text fullComp = new TranslatableText("chat.type.text", player.getDisplayName(), comp);

			PlayerManager players = ((ServerPlayerEntity) player).server.getPlayerManager();

			ServerChatEvent event = new ServerChatEvent((ServerPlayerEntity) player, comp.getString(), fullComp);
			if (!MinecraftForge.EVENT_BUS.post(event)) {
				players.broadcastChatMessage(fullComp, MessageType.CHAT, player.getUuid());

				ServerPlayNetworkHandler handler = ((ServerPlayerEntity) player).networkHandler;
				int threshold = handler.messageCooldown;
				threshold += 20;

				if (threshold > 200 && !players.isOperator(player.getGameProfile()))
					handler.onDisconnected(new TranslatableText("disconnect.spam"));

				handler.messageCooldown = threshold;
			}
		}

	}

	private static int chatX, chatY;

	public static MutableText createStackComponent(ItemStack stack, MutableText component) {
		if (!ModuleLoader.INSTANCE.isModuleEnabled(ItemSharingModule.class) || !renderItemsInChat)
			return component;
		Style style = component.getStyle();
		if (stack.getCount() > 64) {
			ItemStack copyStack = stack.copy();
			copyStack.setCount(64);
			style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(copyStack)));
			component.fillStyle(style);
		}

		MutableText out = new LiteralText("   ");
		out.setStyle(style);
		return out.append(component);
	}

	@Environment(EnvType.CLIENT)
	@SubscribeEvent
	public void getChatPos(RenderGameOverlayEvent.Chat event) {
		chatX = event.getPosX();
		chatY = event.getPosY();
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void renderSymbols(RenderGameOverlayEvent.Post event) {
		if (!renderItemsInChat)
			return;

		MinecraftClient mc = MinecraftClient.getInstance();
		InGameHud gameGui = mc.inGameHud;
		ChatHud chatGui = gameGui.getChatHud();
		if (event.getType() == RenderGameOverlayEvent.ElementType.CHAT) {
			int updateCounter = gameGui.getTicks();
			List<ChatHudLine<Text>> lines = chatGui.messages;
			int shift = chatGui.scrolledLines;

			int idx = shift;

			while (idx < lines.size() && (idx - shift) < chatGui.getVisibleLineCount()) {
				ChatHudLine<Text> line = lines.get(idx);
				StringBuilder before = new StringBuilder();

				Text lineProperties = line.getText();

				int captureIndex = idx;
				lineProperties.visit((style, str) -> {
					if (str != null && str.startsWith("   ")) {
						render(mc, chatGui, updateCounter, before.toString(), line, captureIndex - shift, style, str);
					}
					before.append(str);
					return Optional.empty();
				}, lineProperties.getStyle());

				idx++;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private static void render(MinecraftClient mc, ChatHud chatGui, int updateCounter, String before, ChatHudLine<Text> line, int lineHeight, Style style, String str) {
		HoverEvent hoverEvent = style.getHoverEvent();
		if (hoverEvent != null && hoverEvent.getAction() == HoverEvent.Action.SHOW_ITEM) {
			HoverEvent.ItemStackContent contents = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);

			ItemStack stack = contents != null ? contents.asStack() : ItemStack.EMPTY;

			if (stack.isEmpty())
				stack = new ItemStack(Blocks.BARRIER); // for invalid icon

			int timeSinceCreation = updateCounter - line.getCreationTick();
			if (chatGui.isChatFocused()) timeSinceCreation = 0;

			if (timeSinceCreation < 200) {
				float chatOpacity = (float) mc.options.chatOpacity * 0.9f + 0.1f;
				float fadeOut = MathHelper.clamp((1 - timeSinceCreation / 200f) * 10, 0, 1);
				float alpha = fadeOut * fadeOut * chatOpacity;

				int x = chatX + 3 + mc.textRenderer.getWidth(before);
				int y = chatY - mc.textRenderer.fontHeight * lineHeight;

				if (alpha > 0) {
					alphaValue = alpha;

					RenderSystem.pushMatrix();
					RenderSystem.translatef(x - 2, y - 2, -2);
					RenderSystem.scalef(0.65f, 0.65f, 0.65f);
					mc.getItemRenderer().renderGuiItemIcon(stack, 0, 0);
					RenderSystem.popMatrix();

					alphaValue = 1F;
				}
			}
		}
	}

	// used in a mixin because rendering overrides are cursed by necessity hahayes
	public static float alphaValue = 1F;
}
