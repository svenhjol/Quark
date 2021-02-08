package vazkii.quark.base.client.handler;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import vazkii.quark.base.Quark;

@EventBusSubscriber(modid = Quark.MOD_ID, value = Dist.CLIENT)
public class RequiredModTooltipHandler {

	private static Map<Item, String> ITEMS = new HashMap<>();
	
	public static void map(Item item, String mod) {
		ITEMS.put(item, mod);
	}
	
	public static void map(Block block, String mod) {
		ITEMS.put(block.asItem(), mod);
	}
	
	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void onTooltip(ItemTooltipEvent event) {
		Item item = event.getItemStack().getItem();
		if(ITEMS.containsKey(item)) {
			String mod = ITEMS.get(item);
			if(!ModList.get().isLoaded(mod))
				event.getToolTip().add(new TranslatableText("quark.misc.mod_disabled", mod).formatted(Formatting.GRAY));
		}
	}
	
}
