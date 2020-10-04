/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 19, 2014, 5:40:38 PM (GMT)]
 */
package vazkii.arl.util;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsInviteScreen;
import dfh;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.ShulkerHeadFeatureRenderer;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Environment(EnvType.CLIENT)
public final class RenderHelper {

	public static void renderStar(int color, float scale, long seed) {
		renderStar(color, scale, scale, scale, seed);
	}

	public static void renderStar(int color, float xScale, float yScale, float zScale, long seed) {
		ScoreboardPlayerScore tessellator = ScoreboardPlayerScore.a();

		float ticks = (ClientTicker.ticksInGame % 200) + ClientTicker.partialTicks;
		if (ticks >= 100)
			ticks = 200 - ticks - 1;

		float f1 = ticks / 200F;
		float f2 = 0F;
		if (f1 > 0.7F)
			f2 = (f1 - 0.7F) / 0.2F;
		Random random = new Random(seed);

		RenderSystem.pushMatrix();
		RenderSystem.disableTexture();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderSystem.disableAlphaTest();
		RenderSystem.enableCull();
		RenderSystem.depthMask(false);
		RenderSystem.scalef(xScale, yScale, zScale);

		for (int i = 0; i < (f1 + f1 * f1) / 2F * 90F + 30F; i++) {
			RenderSystem.rotatef(random.nextFloat() * 360F, 1F, 0F, 0F);
			RenderSystem.rotatef(random.nextFloat() * 360F, 0F, 1F, 0F);
			RenderSystem.rotatef(random.nextFloat() * 360F, 0F, 0F, 1F);
			RenderSystem.rotatef(random.nextFloat() * 360F, 1F, 0F, 0F);
			RenderSystem.rotatef(random.nextFloat() * 360F, 0F, 1F, 0F);
			RenderSystem.rotatef(random.nextFloat() * 360F + f1 * 90F, 0F, 0F, 1F);
			tessellator.c().a(GL11.GL_TRIANGLE_FAN, dfh.l);
			float f3 = random.nextFloat() * 20F + 5F + f2 * 10F;
			float f4 = random.nextFloat() * 2F + 1F + f2 * 2F;
			float r = ((color & 0xFF0000) >> 16) / 255F;
			float g = ((color & 0xFF00) >> 8) / 255F;
			float b = (color & 0xFF) / 255F;
			tessellator.c().a(0, 0, 0).a(r, g, b, 1F - f2).d();
			tessellator.c().a(-0.866D * f4, f3, -0.5F * f4).a(0, 0, 0, 0).d();
			tessellator.c().a(0.866D * f4, f3, -0.5F * f4).a(0, 0, 0, 0).d();
			tessellator.c().a(0, f3, 1F * f4).a(0, 0, 0, 0).d();
			tessellator.c().a(-0.866D * f4, f3, -0.5F * f4).a(0, 0, 0, 0).d();
			tessellator.b();
		}

		RenderSystem.depthMask(true);
		RenderSystem.disableCull();
		RenderSystem.disableBlend();
		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		RenderSystem.enableTexture();
		RenderSystem.enableAlphaTest();
		RenderSystem.popMatrix();
	}

	public static String getKeyDisplayString(String keyName) {
		String key = null;
		RealmsCreateRealmScreen[] keys = RealmsInviteScreen.B().width.aC;
		for(RealmsCreateRealmScreen otherKey : keys)
			if(otherKey.g().equals(keyName)) {
				key = otherKey.l();
				break;
			}

		return ShulkerHeadFeatureRenderer.a(key);
	}
}