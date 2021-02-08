package vazkii.quark.base.client.config.gui.widget;

import java.awt.Color;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.ARBInvalidateSubdata;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import vazkii.arl.util.ClientTicker;
import vazkii.quark.base.client.config.IngameConfigHandler;
import vazkii.quark.base.client.config.gui.QuarkConfigHomeScreen;
import vazkii.quark.base.client.handler.TopLayerTooltipHandler;
import vazkii.quark.base.handler.ContributorRewardHandler;
import vazkii.quark.base.handler.MiscUtil;

public class QButton extends Button {
	
	private static final HashMap<String, Pair<Integer, Integer>> BIRTHDAYS = new LinkedHashMap<>();
	static {
		BIRTHDAYS.put("MCVinnyq", Pair.of(9, 4)); // 9 April
		BIRTHDAYS.put("Vazkii", Pair.of(22, 11)); // 22 November
		BIRTHDAYS.put("Wire Segal", Pair.of(23, 9)); // 23 September
		BIRTHDAYS.put("Quark", Pair.of(21, 3)); // 21 March
	}

	private final boolean gay;
	
	private int birthdayIndex = -1;
	private String birthday;
	
	public QButton(int x, int y) {
		super(x, y, 20, 20, new StringTextComponent("q"), QButton::click);
		
		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DATE);
		gay = month == 6;
		
		int i = 0;
		for(String s : BIRTHDAYS.keySet()) {
			Pair<Integer, Integer> date = BIRTHDAYS.get(s);
			if(month == date.getRight() && day == date.getLeft()) {
				birthdayIndex = i;
				birthday = s;
				break;
			}
			i++;
		}
	}
	
	@Override
	public int getFGColor() {
		return gay ? Color.HSBtoRGB((ClientTicker.total / 200F), 1F, 1F) : 0x48DDBC;
	}
	
	@Override
	public void renderButton(MatrixStack mstack, int mouseX, int mouseY, float pticks) {
		super.renderButton(mstack, mouseX, mouseY, pticks);
		
		int iconIndex = Math.min(4, ContributorRewardHandler.localPatronTier);
		if(birthdayIndex > -1)
			iconIndex = birthdayIndex + 1;
		
		if(iconIndex > 0) {
			RenderSystem.color3f(1F, 1F, 1F);
			int rx = x - 2;
			int ry = y - 2;
			
			int w = 9;
			int h = 9;
			
			int v = 26;
			
			if(birthdayIndex > -1) {
				rx -= 3;
				ry -= 2;
				w = 10;
				h = 10;
				v = 44;
				
				boolean hovered = mouseX >= x && mouseY >= y && mouseX < (x + width) && mouseY < (y + height);
				if(hovered)
					TopLayerTooltipHandler.setTooltip(Arrays.asList(I18n.format("quark.gui.config.bday", TextFormatting.YELLOW + birthday + TextFormatting.RESET)), mouseX, mouseY);
			}
			
			int u = 256 - iconIndex * w;
			
			Minecraft.getInstance().textureManager.bindTexture(MiscUtil.GENERAL_ICONS);
			blit(mstack, rx, ry, u, v, w, h);
		}
	}
	
	public static void click(Button b) {
		Minecraft.getInstance().displayGuiScreen(new QuarkConfigHomeScreen(Minecraft.getInstance().currentScreen));
		IngameConfigHandler.INSTANCE.debug();
	}
	
}