package vazkii.quark.base.client.handler;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.InputMappings.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import vazkii.quark.base.client.util.SortedKeyBinding;

@OnlyIn(Dist.CLIENT)
public class ModKeybindHandler {
	
	public static final String MISC_GROUP = "quark.gui.keygroup.misc";
	public static final String INV_GROUP = "quark.gui.keygroup.inv";
	public static final String EMOTE_GROUP = "quark.gui.keygroup.emote";
	public static final String ACCESSIBILITY_GROUP = "quark.gui.keygroup.accessibility";

	public static KeyBinding init(String s, String key, String group) {
		return init(s, key, "key.keyboard.", group, true);
	}

	public static KeyBinding init(String s, String key, String group, int sortPriority) {
		return init(s, key, "key.keyboard.", group, sortPriority, true);
	}
	
	public static KeyBinding initMouse(String s, int key, String group) {
		return init(s, Integer.toString(key), "key.mouse.", group, true);
	}

	public static KeyBinding initMouse(String s, int key, String group, int sortPriority) {
		return init(s, Integer.toString(key), "key.mouse.", group, sortPriority, true);
	}
	
	public static KeyBinding init(String s, String key, String keyType, String group, boolean prefix) {
		KeyBinding kb = new KeyBinding(prefix ? ("quark.keybind." + s) : s, (keyType.contains("mouse") ? Type.MOUSE : Type.KEYSYM),
				(key == null ? InputMappings.INPUT_INVALID :
						InputMappings.getInputByName(keyType + key)).getKeyCode(),
				group);
		ClientRegistry.registerKeyBinding(kb);
		return kb;
	}

	public static KeyBinding init(String s, String key, String keyType, String group, int sortPriority, boolean prefix) {
		KeyBinding kb = new SortedKeyBinding(prefix ? ("quark.keybind." + s) : s, (keyType.contains("mouse") ? Type.MOUSE : Type.KEYSYM),
				(key == null ? InputMappings.INPUT_INVALID :
						InputMappings.getInputByName(keyType + key)).getKeyCode(),
				group, sortPriority);
		ClientRegistry.registerKeyBinding(kb);
		return kb;
	}
	
}
