package vazkii.quark.base.client;

import java.awt.Color;
import java.util.Calendar;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.loading.FMLPaths;
import vazkii.arl.util.ClientTicker;
import vazkii.quark.base.Quark;
import vazkii.quark.base.client.config.IngameConfigHandler;
import vazkii.quark.base.client.screen.QHomeScreen;
import vazkii.quark.base.handler.ContributorRewardHandler;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.handler.MiscUtil;

@EventBusSubscriber(modid = Quark.MOD_ID, value = Dist.CLIENT)
public class QButtonHandler {

	@SubscribeEvent
	public static void onGuiInit(GuiScreenEvent.InitGuiEvent event) {
		Screen gui = event.getGui();
		
		if(GeneralConfig.enableQButton && (gui instanceof TitleScreen || gui instanceof GameMenuScreen)) {
			ImmutableSet<String> targets = GeneralConfig.qButtonOnRight 
					? ImmutableSet.of(I18n.translate("fml.menu.modoptions"), I18n.translate("menu.online"))
					: ImmutableSet.of(I18n.translate("menu.options"), I18n.translate("fml.menu.mods"));
					
			System.out.println(targets);
					
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
	
	private static class QButton extends ButtonWidget {

		private final boolean gay;
		
		public QButton(int x, int y) {
			super(x, y, 20, 20, new LiteralText("q"), QButton::click);
			gay = Calendar.getInstance().get(Calendar.MONTH) + 1 == 6;
		}
		
		@Override
		public int getFGColor() {
			return gay ? Color.HSBtoRGB((ClientTicker.total / 200F), 1F, 1F) : 0x48DDBC;
		}
		
		@Override
		public void renderButton(MatrixStack mstack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
			super.renderButton(mstack, p_renderButton_1_, p_renderButton_2_, p_renderButton_3_);
			
			if(ContributorRewardHandler.localPatronTier > 0) {
				RenderSystem.color3f(1F, 1F, 1F);
				int tier = Math.min(4, ContributorRewardHandler.localPatronTier);
				int u = 256 - tier * 9;
				int v = 26;
				
				MinecraftClient.getInstance().textureManager.bindTexture(MiscUtil.GENERAL_ICONS);
				drawTexture(mstack, x - 2, y - 2, u, v, 9, 9);
			}
		}
		
		public static void click(ButtonWidget b) {
			MinecraftClient.getInstance().openScreen(new QHomeScreen(MinecraftClient.getInstance().currentScreen));
			IngameConfigHandler.INSTANCE.debug();
		}
		
	}
	
}
