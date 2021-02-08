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

import net.minecraft.block.Blocks;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
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
	@OnlyIn(Dist.CLIENT)
	public void keyboardEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		GameSettings settings = mc.gameSettings;
		if(InputMappings.isKeyDown(mc.getMainWindow().getHandle(), settings.keyBindChat.getKey().getKeyCode()) &&
				event.getGui() instanceof ContainerScreen && Screen.hasShiftDown()) {
			ContainerScreen<?> gui = (ContainerScreen<?>) event.getGui();
			
			List<? extends IGuiEventListener> children = gui.getEventListeners();
			for(IGuiEventListener c : children)
				if(c instanceof TextFieldWidget) {
					TextFieldWidget tf = (TextFieldWidget) c;
					if(tf.isFocused())
						return;
				}
			
			Slot slot = gui.getSlotUnderMouse();
			if(slot != null && slot.inventory != null) {
				ItemStack stack = slot.getStack();

				if(!stack.isEmpty() && !MinecraftForge.EVENT_BUS.post(new ClientChatEvent(stack.getTextComponent().getString()))) {
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
			ITextComponent comp = item.getTextComponent();
			ITextComponent fullComp = new TranslationTextComponent("chat.type.text", player.getDisplayName(), comp);

			PlayerList players = ((ServerPlayerEntity) player).server.getPlayerList();

			ServerChatEvent event = new ServerChatEvent((ServerPlayerEntity) player, comp.getString(), fullComp);
			if (!MinecraftForge.EVENT_BUS.post(event)) {
				players.func_232641_a_(fullComp, ChatType.CHAT, player.getUniqueID());

				ServerPlayNetHandler handler = ((ServerPlayerEntity) player).connection;
				int threshold = handler.chatSpamThresholdCount;
				threshold += 20;

				if (threshold > 200 && !players.canSendCommands(player.getGameProfile()))
					handler.onDisconnect(new TranslationTextComponent("disconnect.spam"));

				handler.chatSpamThresholdCount = threshold;
			}
		}

	}

	private static int chatX, chatY;

	public static IFormattableTextComponent createStackComponent(ItemStack stack, IFormattableTextComponent component) {
		if (!ModuleLoader.INSTANCE.isModuleEnabled(ItemSharingModule.class) || !renderItemsInChat)
			return component;
		Style style = component.getStyle();
		if (stack.getCount() > 64) {
			ItemStack copyStack = stack.copy();
			copyStack.setCount(64);
			style = style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemHover(copyStack)));
			component.mergeStyle(style);
		}

		IFormattableTextComponent out = new StringTextComponent("   ");
		out.setStyle(style);
		return out.append(component);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void getChatPos(RenderGameOverlayEvent.Chat event) {
		chatX = event.getPosX();
		chatY = event.getPosY();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT) // TODO wire fix this thanks
	public void renderSymbols(RenderGameOverlayEvent.Post event) {
		if (!renderItemsInChat)
			return;

		Minecraft mc = Minecraft.getInstance();
		IngameGui gameGui = mc.ingameGUI;
		NewChatGui chatGui = gameGui.getChatGUI();
		if (event.getType() == RenderGameOverlayEvent.ElementType.CHAT) {
			int updateCounter = gameGui.getTicks();
			List<ChatLine<ITextComponent>> lines = chatGui.chatLines;
			int shift = chatGui.scrollPos;

			int idx = shift;

			while (idx < lines.size() && (idx - shift) < chatGui.getLineCount()) {
				ChatLine<ITextComponent> line = lines.get(idx);
				StringBuilder before = new StringBuilder();

				ITextComponent lineProperties = line.getLineString();

				int captureIndex = idx;
				lineProperties.getComponentWithStyle((style, str) -> {
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

	@OnlyIn(Dist.CLIENT)
	private static void render(Minecraft mc, NewChatGui chatGui, int updateCounter, String before, ChatLine<ITextComponent> line, int lineHeight, Style style, String str) {
		HoverEvent hoverEvent = style.getHoverEvent();
		if (hoverEvent != null && hoverEvent.getAction() == HoverEvent.Action.SHOW_ITEM) {
			HoverEvent.ItemHover contents = hoverEvent.getParameter(HoverEvent.Action.SHOW_ITEM);

			ItemStack stack = contents != null ? contents.createStack() : ItemStack.EMPTY;

			if (stack.isEmpty())
				stack = new ItemStack(Blocks.BARRIER); // for invalid icon

			int timeSinceCreation = updateCounter - line.getUpdatedCounter();
			if (chatGui.getChatOpen()) timeSinceCreation = 0;

			if (timeSinceCreation < 200) {
				float chatOpacity = (float) mc.gameSettings.chatOpacity * 0.9f + 0.1f;
				float fadeOut = MathHelper.clamp((1 - timeSinceCreation / 200f) * 10, 0, 1);
				float alpha = fadeOut * fadeOut * chatOpacity;

				int x = chatX + 3 + mc.fontRenderer.getStringWidth(before);
				int y = chatY - mc.fontRenderer.FONT_HEIGHT * lineHeight;

				if (alpha > 0) {
					alphaValue = alpha;

					RenderSystem.pushMatrix();
					RenderSystem.translatef(x - 2, y - 2, -2);
					RenderSystem.scalef(0.65f, 0.65f, 0.65f);
					mc.getItemRenderer().renderItemIntoGUI(stack, 0, 0);
					RenderSystem.popMatrix();

					alphaValue = 1F;
				}
			}
		}
	}

	// used in a mixin because rendering overrides are cursed by necessity hahayes
	public static float alphaValue = 1F;
}
