package vazkii.quark.base.client.config;

import java.util.List;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.loading.FMLPaths;
import vazkii.quark.base.Quark;
import vazkii.quark.base.client.config.gui.widget.QButton;
import vazkii.quark.base.handler.GeneralConfig;

@EventBusSubscriber(modid = Quark.MOD_ID, value = Dist.CLIENT)
public class QButtonHandler {

	@SubscribeEvent
	public static void onGuiInit(GuiScreenEvent.InitGuiEvent event) {
		Screen gui = event.getGui();
		
		if(GeneralConfig.enableQButton && (gui instanceof MainMenuScreen || gui instanceof IngameMenuScreen)) {
			ImmutableSet<String> targets = GeneralConfig.qButtonOnRight 
					? ImmutableSet.of(I18n.format("fml.menu.modoptions"), I18n.format("menu.online"))
					: ImmutableSet.of(I18n.format("menu.options"), I18n.format("fml.menu.mods"));
					
			List<Widget> widgets = event.getWidgetList();
			for(Widget b : widgets)
				if(targets.contains(b.getMessage().getString())) {
					Button qButton = new QButton(b.x + (GeneralConfig.qButtonOnRight ? 103 : -24), b.y);
					event.addWidget(qButton);
					return;
				}
		}
	}
	
	public static void openFile() {
		Util.getOSType().openFile(FMLPaths.CONFIGDIR.get().toFile());
	}
	
}
