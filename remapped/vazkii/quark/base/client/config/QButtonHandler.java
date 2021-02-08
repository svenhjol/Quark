package vazkii.quark.base.client.config;

import java.util.List;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
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
		
		if(GeneralConfig.enableQButton && (gui instanceof TitleScreen || gui instanceof GameMenuScreen)) {
			ImmutableSet<String> targets = GeneralConfig.qButtonOnRight 
					? ImmutableSet.of(I18n.translate("fml.menu.modoptions"), I18n.translate("menu.online"))
					: ImmutableSet.of(I18n.translate("menu.options"), I18n.translate("fml.menu.mods"));
					
			List<AbstractButtonWidget> widgets = event.getWidgetList();
			for(AbstractButtonWidget b : widgets)
				if(targets.contains(b.getMessage().getString())) {
					ButtonWidget qButton = new QButton(b.x + (GeneralConfig.qButtonOnRight ? 103 : -24), b.y);
					event.addWidget(qButton);
					return;
				}
		}
	}
	
	public static void openFile() {
		Util.getOperatingSystem().open(FMLPaths.CONFIGDIR.get().toFile());
	}
	
}
