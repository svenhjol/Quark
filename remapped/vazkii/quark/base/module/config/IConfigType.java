package vazkii.quark.base.module.config;

public interface IConfigType {

	default void onReload(ConfigFlagManager flagManager) { }
	
}
