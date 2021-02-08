package vazkii.quark.api.config;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;

@OnlyIn(Dist.CLIENT)
public interface IQuarkConfig {

	public static class Holder {
		
		/**
		 * Access this only after construct. clientSetup is safe.
		 */
		public static IQuarkConfig instance = null;
		
	}

	/**
	 * Registers a category to be shown inside the q menu. Unless you have a really good reason, register only one per mod
	 * in the interest of keeping things somewhat clean.<br><br>
	 * 
	 * @param modid Your mod ID. This will be used to map the configs per mod, and automatically handle refreshing the config
	 * whenever {@link ModConfigEvent} is fired. <b>Note:</b> If your config is loaded in any way where ModConfigEvent wouldn't
	 * fire for it, you must call {@link IConfigCategory#refresh()} manually.
	 * @param name The name of the category. This is a display only name and bears no internal significance.
	 * @param onChangedCallback The function to be called when this config changes. You can use {@link IQuarkConfig#writeToFileCallback(String)} 
	 * to generate a callback that'll automatically regenerate the relevant config file. This function is called every time the player
	 * clicks the Save button in the quark config screen, even if the config didn't change.
	 * @return The top level category you can then add stuff onto.
	 */
	public IExternalCategory registerExternalCategory(String modid, String name, Consumer<IExternalCategory> onChangedCallback);

	/**
	 * Creates a callback to output the config contents to the given file.
	 * @see {@link IQuarkConfig#writeToFileCallback(String)} for a helper.
	 */
	public Consumer<IExternalCategory> writeToFileCallback(File file);

	/**
	 *	{@link IQuarkConfig#writeToFileCallback(File)} helper. Passes in {@code new File("config", filename + ".toml")}.
	 */
	public default Consumer<IExternalCategory> writeToFileCallback(String filename) {
		return writeToFileCallback(new File("config", filename + ".toml"));
	}
	
	public default Pair<IExternalCategory, IExternalCategory> registerClientCommonExternalCategory(String modid, String name, Consumer<IExternalCategory> commonCallback, Consumer<IExternalCategory> clientCallback) {
		IExternalCategory main = registerExternalCategory(modid, name, (c) -> {
			Map<String, IConfigCategory> topLevel = c.getTopLevelCategories();
			((IExternalCategory) topLevel.get("common")).commit();
			((IExternalCategory) topLevel.get("client")).commit();
			c.updateDirty();
		});
		
		IExternalCategory common = main.addTopLevelCategory("common", commonCallback);
		IExternalCategory client = main.addTopLevelCategory("client", clientCallback);

		return Pair.of(common, client);
	}
	
}
