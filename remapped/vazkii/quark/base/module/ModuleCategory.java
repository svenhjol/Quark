package vazkii.quark.base.module;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import vazkii.quark.base.Quark;

public enum ModuleCategory {

	// Categories
	AUTOMATION("automation", Items.REDSTONE),
	BUILDING("building", Items.BRICKS),
	MANAGEMENT("management", Items.CHEST),
	TOOLS("tools", Items.IRON_PICKAXE),
	TWEAKS("tweaks", Items.NAUTILUS_SHELL),
	WORLD("world", Items.GRASS_BLOCK),
	MOBS("mobs", Items.PIG_SPAWN_EGG),
	CLIENT("client", Items.ENDER_EYE),
	ODDITIES("oddities", Items.CHORUS_FRUIT, Quark.ODDITIES_ID),
	EXPERIMENTAL("experimental", Items.TNT);
	
	public final String name;
	public final Item item;
	public final String requiredMod;
	
	public boolean enabled;
	
	private List<QuarkModule> ownedModules = new ArrayList<>();
	
	ModuleCategory(String name, Item item, String requiredMod) {
		this.name = name;
		this.item = item;
		this.requiredMod = requiredMod;
		this.enabled = true;
	}
	
	ModuleCategory(String name, Item item) {
		this(name, item, null);
	}
	
	public void addModule(QuarkModule module) {
		ownedModules.add(module);
	}
	
	public List<QuarkModule> getOwnedModules() {
		return ownedModules;
	}
	
}
