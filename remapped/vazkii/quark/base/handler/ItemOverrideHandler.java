package vazkii.quark.base.handler;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class ItemOverrideHandler {

	private static Map<Item, String> defaultItemKeys = new HashMap<>();
	private static Map<Block, String> defaultBlockKeys = new HashMap<>();
	
	public static void changeItemLocalizationKey(Item item, String newKey, boolean enabled) {
		if(!enabled) {
			if(defaultItemKeys.containsKey(item))
				changeItemLocalizationKey(item, defaultItemKeys.get(item));
		} else {
			String currKey = item.translationKey;
			if(!defaultItemKeys.containsKey(item))
				defaultItemKeys.put(item, currKey);
			
			changeItemLocalizationKey(item, newKey);
		}
	}
	
	public static void changeBlockLocalizationKey(Block block, String newKey, boolean enabled) {
		if(!enabled) {
			if(defaultBlockKeys.containsKey(block))
				changeBlockLocalizationKey(block, defaultBlockKeys.get(block));
		} else {
			String currKey = block.translationKey;
			if(!defaultBlockKeys.containsKey(block))
				defaultBlockKeys.put(block, currKey);
			
			changeBlockLocalizationKey(block, newKey);
		}
	}
	
	private static void changeItemLocalizationKey(Item item, String newKey) {
		item.translationKey = newKey;
	}
	
	
	private static void changeBlockLocalizationKey(Block block, String newKey) {
		block.translationKey = newKey;
	}
	
}
