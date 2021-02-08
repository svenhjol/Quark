package vazkii.quark.base.client.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Formatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.GeneralConfig;

@EventBusSubscriber(modid = Quark.MOD_ID, value = Dist.CLIENT)
public class NetworkProfilingHandler {

	private static Map<String, Info> map = new HashMap<>();
	
	public static void receive(String name) {
		if(GeneralConfig.enableNetworkProfiling) {
			if(!map.containsKey(name))
				map.put(name, new Info());
			map.get(name).add();
		}
	}
	
	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void showF3(RenderGameOverlayEvent.Text event) {
		if(GeneralConfig.enableNetworkProfiling) {
			event.getLeft().add("");
			
			for(String s : map.keySet()) {
				Info i = map.get(s);
				int c = i.tick();
				if(c > 0) {
					double cd = ((double) c) / 5.0;
					Formatting tf = (System.currentTimeMillis() - i.getLast() < 100) ? Formatting.RED : Formatting.RESET;
					
					event.getLeft().add(tf + "PACKET " + s + ": " + cd + "/s");
				}
			}
		}
	}
	
	private static class Info {
		
		private static List<Long> times = new ArrayList<>(100);
		long last;
		
		public void add() {
			last = System.currentTimeMillis();
			times.add(last);
		}
		
		public int tick() {
			long curr = System.currentTimeMillis();
			long limit = curr - 5000;
			times.removeIf(t -> t < limit);
			
			return times.size();
		}
		
		public long getLast() {
			return last;
		}
		
	}
	
}
