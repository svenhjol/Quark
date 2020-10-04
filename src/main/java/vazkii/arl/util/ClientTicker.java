package vazkii.arl.util;

import com.mojang.realmsclient.gui.screens.RealmsInviteScreen;
import doq;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.arl.AutoRegLib;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = AutoRegLib.MOD_ID)
public final class ClientTicker {

	public static int ticksInGame = 0;
	public static float partialTicks = 0;
	public static float delta = 0;
	public static float total = 0;
	
	@Environment(EnvType.CLIENT)
	private static void calcDelta() {
		float oldTotal = total;
		total = ticksInGame + partialTicks;
		delta = total - oldTotal;
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void renderTick(RenderTickEvent event) {
		if(event.phase == Phase.START)
			partialTicks = event.renderTickTime;
		else calcDelta();
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void clientTickEnd(ClientTickEvent event) {
		if(event.phase == Phase.END) {
			doq gui = RealmsInviteScreen.B().y;
			if(gui == null || !gui.ay_()) {
				ticksInGame++;
				partialTicks = 0;
			}
			
			calcDelta();
		}
	}

}
