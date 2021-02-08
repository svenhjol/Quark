package vazkii.quark.base.module;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import vazkii.quark.api.event.ModuleLoadedEvent;
import vazkii.quark.api.event.ModuleStateChangedEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.config.ConfigFlagManager;

public class QuarkModule {

	public String displayName = "";
	public String lowercaseName = "";
	public String description = "";
	public List<String> antiOverlap = null;
	public boolean hasSubscriptions = false;
	public List<Dist> subscriptionTarget = Lists.newArrayList(Dist.CLIENT, Dist.DEDICATED_SERVER);
	public boolean enabledByDefault = true;
	public boolean missingDep = false;
	
	private boolean firstLoad = true;
	public boolean enabled = false;
	public boolean configEnabled = false;
	public boolean ignoreAntiOverlap = false;

	public void construct() {
		// NO-OP
	}
	
	@OnlyIn(Dist.CLIENT)
	public void constructClient() {
		// NO-OP
	}

	public void modulesStarted() {
		// NO-OP
	}
	
	public void configChanged() {
		// NO-OP
	}

	@OnlyIn(Dist.CLIENT)
	public void configChangedClient() {
		// NO-OP
	}
	
	public void earlySetup() {
		// NO-OP
	}
	
	public void setup() {
		// NO-OP
	}
	
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		// NO-OP
	}

	@OnlyIn(Dist.CLIENT)
	public void modelRegistry() {
		// NO-OP
	}
	
	@OnlyIn(Dist.CLIENT)
	public void textureStitch(TextureStitchEvent.Pre event) {
		// NO-OP
	}

	@OnlyIn(Dist.CLIENT)
	public void postTextureStitch(TextureStitchEvent.Post event) {
		// NO-OP
	}
	
	public void loadComplete() {
		// NO-OP
	}
	
	@OnlyIn(Dist.CLIENT)
	public void firstClientTick() {
		// NO-OP
	}
	
	public void pushFlags(ConfigFlagManager manager) {
		// NO-OP
	}
	
	protected void enqueue(Runnable r) {
		ModuleLoader.INSTANCE.enqueue(r);
	}
	
	public final void setEnabled(boolean enabled) {
		configEnabled = enabled;
		if(firstLoad) {
			Quark.LOG.info("Loading Module " + displayName);
			MinecraftForge.EVENT_BUS.post(new ModuleLoadedEvent(lowercaseName));
		}
		firstLoad = false;
		
		if(missingDep)
			enabled = false;
		else if(!ignoreAntiOverlap && antiOverlap != null) {
			ModList list = ModList.get();
			for(String s : antiOverlap)
				if(list.isLoaded(s)) {
					enabled = false;
					break;
				}
		}
		
		setEnabledAndManageSubscriptions(enabled);
	}
	
	private void setEnabledAndManageSubscriptions(boolean enabled) {
		if(MinecraftForge.EVENT_BUS.post(new ModuleStateChangedEvent(lowercaseName, enabled)))
			enabled = false;
		
		boolean wasEnabled = this.enabled;
		this.enabled = enabled;
		
		if(hasSubscriptions && subscriptionTarget.contains(FMLEnvironment.dist) && wasEnabled != enabled) {
			if(enabled)
				MinecraftForge.EVENT_BUS.register(this);
			else MinecraftForge.EVENT_BUS.unregister(this);
		}
	}
	
}
