package vazkii.quark.content.client.tooltip;

import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.item.FoodComponent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import vazkii.quark.content.client.module.ImprovedTooltipsModule;

public class FoodTooltips {

	@Environment(EnvType.CLIENT)
	public static void makeTooltip(ItemTooltipEvent event) {
		if(event.getItemStack().isFood()) {
			FoodComponent food = event.getItemStack().getItem().getFoodComponent();
			if (food != null) {
				int pips = food.getHunger();
				int len = (int) Math.ceil((double) pips / ImprovedTooltipsModule.foodDivisor);

				StringBuilder s = new StringBuilder(" ");
				for (int i = 0; i < len; i++)
					s.append("  ");

				int saturationSimplified = 0;
				float saturation = food.getSaturationModifier();
				if(saturation < 1) {
					if(saturation > 0.7)
						saturationSimplified = 1;
					else if(saturation > 0.5)
						saturationSimplified = 2;
					else if(saturation > 0.2)
						saturationSimplified = 3;
					else saturationSimplified = 4;
				}

				Text spaces = new LiteralText(s.toString());
				Text saturationText = new TranslatableText("quark.misc.saturation" + saturationSimplified).formatted(Formatting.GRAY);
				List<Text> tooltip = event.getToolTip();

				if (tooltip.isEmpty()) {
					tooltip.add(spaces);
					if(ImprovedTooltipsModule.showSaturation)
						tooltip.add(saturationText);
				}
				else {
					tooltip.add(1, spaces);
					if(ImprovedTooltipsModule.showSaturation)
						tooltip.add(2, saturationText);
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static void renderTooltip(RenderTooltipEvent.PostText event) {
		if(event.getStack().isFood()) {
			FoodComponent food = event.getStack().getItem().getFoodComponent();
			if (food != null) {
				RenderSystem.color3f(1F, 1F, 1F);
				MinecraftClient mc = MinecraftClient.getInstance();
				MatrixStack matrix = event.getMatrixStack();
				mc.getTextureManager().bindTexture(ForgeIngameGui.GUI_ICONS_TEXTURE);
				int pips = food.getHunger();

				boolean poison = false;
				for (Pair<StatusEffectInstance, Float> effect : food.getStatusEffects()) {
					if (effect.getFirst() != null && effect.getFirst().getEffectType() != null && effect.getFirst().getEffectType().getType() == StatusEffectType.HARMFUL) {
						poison = true;
						break;
					}
				}

				int count = (int) Math.ceil((double) pips / ImprovedTooltipsModule.foodDivisor);
				int y = TooltipUtils.shiftTextByLines(event.getLines(), event.getY() + 10);

				for (int i = 0; i < count; i++) {
					int x = event.getX() + i * 9 - 1;

					int u = 16;
					if (poison)
						u += 117;
					int v = 27;

					DrawableHelper.drawTexture(matrix, x, y, u, v, 9, 9, 256, 256);

					u = 52;
					if (pips % 2 != 0 && i == 0)
						u += 9;
					if (poison)
						u += 36;

					DrawableHelper.drawTexture(matrix, x, y, u, v, 9, 9, 256, 256);
				}
			}
		}
	}

}
