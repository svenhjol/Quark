package vazkii.quark.base.client.config.gui.widget;

import java.util.Arrays;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import vazkii.quark.base.client.handler.TopLayerTooltipHandler;

public class IconButton extends ButtonWidget {

	private final ItemStack icon;
	
	public IconButton(int x, int y, int w, int h, Text text, ItemStack icon, PressAction onClick) {
		super(x, y, w, h, text, onClick);
		this.icon = icon;
	}
	
	@Override
	public void render(MatrixStack mstack, int mouseX, int mouseY, float pticks) {
		super.render(mstack, mouseX, mouseY, pticks);
		
		if(!active && mouseX >= x && mouseY >= y && mouseX < (x + width) && mouseY < (y + height))
			TopLayerTooltipHandler.setTooltip(Arrays.asList(I18n.translate("quark.gui.config.missingaddon")), mouseX, mouseY);
		
		MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(icon, x + 5, y + 2);
	}

}
