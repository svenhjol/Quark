package vazkii.quark.base.world.config;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.module.config.ConfigFlagManager;

public class BiomeTypeConfig implements IBiomeConfig {

	private final Object mutex = new Object();
	
	@Config(name = "Biome Categories")
	@Config.Restriction({"none", "taiga", "extreme_hills", "jungle", "mesa", "plains", "savanna", "icy", "the_end", "beach", "forest",
			"ocean", "desert", "river", "swamp", "mushroom", "nether"})
	private List<String> categoryStrings;

	@Config
	private boolean isBlacklist;

	private List<Biome.Category> categories;

	public BiomeTypeConfig(boolean isBlacklist, Biome.Category... categories) {
		this.isBlacklist = isBlacklist;

		categoryStrings = new LinkedList<>();
		for (Biome.Category c : categories)
			categoryStrings.add(c.getName());
	}

	public BiomeTypeConfig(boolean isBlacklist, String... categories) {
		this.isBlacklist = isBlacklist;

		categoryStrings = new LinkedList<>();
		categoryStrings.addAll(Arrays.asList(categories));
	}

	@Override
	public boolean canSpawn(ResourceLocation res, Category category) {
		synchronized (mutex) {
			if(categories == null)
				updateTypes();
			
			for(Biome.Category c : categories)
				if(c.equals(category))
					return !isBlacklist;

			return isBlacklist;
		}
	}

	@Override
	public void onReload(ConfigFlagManager flagManager) {
		synchronized (mutex) {
			updateTypes();
		}
	}
	
	public void updateTypes() {
		categories = new LinkedList<>();
		for (String s : categoryStrings) {
			Biome.Category category = Biome.Category.byName(s);
			
			if (category != null)
				categories.add(category);
		}
	}

}
