package vazkii.quark.content.client.module;

import java.util.List;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardKeyPressedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseClickedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.client.handler.ModKeybindHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.CLIENT, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class BackButtonKeybind extends QuarkModule {

	@OnlyIn(Dist.CLIENT)
	private static KeyBinding backKey;
	
	@OnlyIn(Dist.CLIENT)
	private static List<Widget> widgets;

	@Override
	public void clientSetup() {
		backKey = ModKeybindHandler.initMouse("back", 4, ModKeybindHandler.MISC_GROUP);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void openGui(GuiScreenEvent.InitGuiEvent event) {
		widgets = event.getWidgetList();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onKeyInput(KeyboardKeyPressedEvent.Post event) {
		if(backKey.getKey().getType() == Type.KEYSYM && event.getKeyCode() == backKey.getKey().getKeyCode())
			clicc();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onMouseInput(MouseClickedEvent.Post event) {
		if(backKey.getKey().getType() == Type.MOUSE && event.getButton() == backKey.getKey().getKeyCode())
			clicc();
	}

	private void clicc() {
		ImmutableSet<String> buttons = ImmutableSet.of(
				I18n.format("gui.back"),
				I18n.format("gui.done"), 
				I18n.format("gui.cancel"), 
				I18n.format("gui.toTitle"),
				I18n.format("gui.toMenu"));

		// Iterate this way to ensure we match the more important back buttons first
		for(String b : buttons)
			for(Widget w : widgets) {
				if(w instanceof Button && ((Button) w).getMessage().getString().equals(b) && w.visible && w.active) {
					w.onClick(0, 0);
					return;
				}
			}
		
		Minecraft mc = Minecraft.getInstance();
		if(mc.world != null)
			mc.displayGuiScreen(null);
	}

}
