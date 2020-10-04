package vazkii.quark.client.module;

import java.util.List;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardKeyPressedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseClickedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.client.ModKeybindHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.CLIENT, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class BackButtonKeybind extends Module {

	@Environment(EnvType.CLIENT)
	private static KeyBinding backKey;
	
	@Environment(EnvType.CLIENT)
	private static List<AbstractButtonWidget> widgets;

	@Override
	public void clientSetup() {
		backKey = ModKeybindHandler.initMouse("back", 4, ModKeybindHandler.MISC_GROUP);
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void openGui(GuiScreenEvent.InitGuiEvent event) {
		widgets = event.getWidgetList();
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onKeyInput(KeyboardKeyPressedEvent.Post event) {
		if(backKey.getKey().getCategory() == Type.KEYSYM && event.getKeyCode() == backKey.getKey().getCode())
			clicc();
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onMouseInput(MouseClickedEvent.Post event) {
		if(backKey.getKey().getCategory() == Type.MOUSE && event.getButton() == backKey.getKey().getCode())
			clicc();
	}

	private void clicc() {
		ImmutableSet<String> buttons = ImmutableSet.of(
				I18n.translate("gui.back"),
				I18n.translate("gui.done"), 
				I18n.translate("gui.cancel"), 
				I18n.translate("gui.toTitle"),
				I18n.translate("gui.toMenu"));

		// Iterate this way to ensure we match the more important back buttons first
		for(String b : buttons)
			for(AbstractButtonWidget w : widgets) {
				if(w instanceof ButtonWidget && ((ButtonWidget) w).getMessage().equals(b)) {
					w.onClick(0, 0);
					return;
				}
			}
		
		MinecraftClient mc = MinecraftClient.getInstance();
		if(mc.world != null)
			mc.openScreen(null);
	}

}
