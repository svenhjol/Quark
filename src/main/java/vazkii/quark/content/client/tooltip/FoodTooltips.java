package vazkii.quark.content.client.tooltip;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.Food;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import vazkii.quark.content.client.module.ImprovedTooltipsModule;

public class FoodTooltips {

	@OnlyIn(Dist.CLIENT)
	public static void makeTooltip(ItemTooltipEvent event) {
		if(event.getItemStack().isFood()) {
			Food food = event.getItemStack().getItem().getFood();
			if (food != null) {
				int pips = food.getHealing();
				int len = (int) Math.ceil((double) pips / ImprovedTooltipsModule.foodDivisor);

				StringBuilder s = new StringBuilder(" ");
				for (int i = 0; i < len; i++)
					s.append("  ");

				int saturationSimplified = 0;
				float saturation = food.getSaturation();
				if(saturation < 1) {
					if(saturation > 0.7)
						saturationSimplified = 1;
					else if(saturation > 0.5)
						saturationSimplified = 2;
					else if(saturation > 0.2)
						saturationSimplified = 3;
					else saturationSimplified = 4;
				}

				ITextComponent spaces = new StringTextComponent(s.toString());
				ITextComponent saturationText = new TranslationTextComponent("quark.misc.saturation" + saturationSimplified).mergeStyle(TextFormatting.GRAY);
				List<ITextComponent> tooltip = event.getToolTip();

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

	@OnlyIn(Dist.CLIENT)
	public static void renderTooltip(RenderTooltipEvent.PostText event) {
		if(event.getStack().isFood()) {
			Food food = event.getStack().getItem().getFood();
			if (food != null) {
				RenderSystem.color3f(1F, 1F, 1F);
				Minecraft mc = Minecraft.getInstance();
				MatrixStack matrix = event.getMatrixStack();
				mc.getTextureManager().bindTexture(ForgeIngameGui.GUI_ICONS_LOCATION);
				int pips = food.getHealing();

				boolean poison = false;
				for (Pair<EffectInstance, Float> effect : food.getEffects()) {
					if (effect.getFirst() != null && effect.getFirst().getPotion() != null && effect.getFirst().getPotion().getEffectType() == EffectType.HARMFUL) {
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

					AbstractGui.blit(matrix, x, y, u, v, 9, 9, 256, 256);

					u = 52;
					if (pips % 2 != 0 && i == 0)
						u += 9;
					if (poison)
						u += 36;

					AbstractGui.blit(matrix, x, y, u, v, 9, 9, 256, 256);
				}
			}
		}
	}

}
