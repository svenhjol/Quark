package vazkii.quark.base.module;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.item.IQuarkItem;
import vazkii.quark.base.module.config.ConfigResolver;

public final class ModuleLoader {
	
	public static final ModuleLoader INSTANCE = new ModuleLoader(); 
	
	private Map<Class<? extends QuarkModule>, QuarkModule> foundModules = new HashMap<>();
	
	private ConfigResolver config;
	private boolean clientTicked = false;
	private ParallelDispatchEvent event;
	
	private ModuleLoader() { }
	
	public void start() {
		findModules();
		dispatch(QuarkModule::construct);
		dispatch(QuarkModule::modulesStarted);
		resolveConfigSpec();
	}
	
	@OnlyIn(Dist.CLIENT)
	public void clientStart() {
		dispatch(QuarkModule::constructClient);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void findModules() {
		ModuleFinder finder = new ModuleFinder();
		finder.findModules();
		foundModules = finder.getFoundModules();
	}
	
	private void resolveConfigSpec() {
		config = new ConfigResolver();
		config.makeSpec();
	}
	
	public void configChanged() {
		config.configChanged();
		dispatch(QuarkModule::configChanged);
	}

	@OnlyIn(Dist.CLIENT)
	public void configChangedClient() {
		dispatch(QuarkModule::configChangedClient);
	}
	
	public void setup(ParallelDispatchEvent event) {
		this.event = event;
		dispatch(QuarkModule::earlySetup);
		Quark.proxy.handleQuarkConfigChange();
		dispatch(QuarkModule::setup);
		event = null;
	}

	@OnlyIn(Dist.CLIENT)
	public void clientSetup(ParallelDispatchEvent event) {
		this.event = event;
		dispatch(QuarkModule::clientSetup);
		event = null;
	}

	@OnlyIn(Dist.CLIENT)
	public void modelRegistry() {
		dispatch(QuarkModule::modelRegistry);
	}

	@OnlyIn(Dist.CLIENT)
	public void textureStitch(TextureStitchEvent.Pre event) {
		dispatch(m -> m.textureStitch(event));
	}

	@OnlyIn(Dist.CLIENT)
	public void postTextureStitch(TextureStitchEvent.Post event) {
		dispatch(m -> m.postTextureStitch(event));
	}
	
	public void loadComplete(ParallelDispatchEvent event) {
		this.event = event;
		dispatch(QuarkModule::loadComplete);
		event = null;
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void firstClientTick(ClientTickEvent event) {
		if(!clientTicked && event.phase == Phase.END) {
			dispatch(m -> m.firstClientTick());
			clientTicked = true;
		}
	}
	
	private void dispatch(Consumer<QuarkModule> run) {
		foundModules.values().forEach(run);
	}
	
	void enqueue(Runnable r) {
		Preconditions.checkNotNull(event);
		event.enqueueWork(r);
	}
	
	public boolean isModuleEnabled(Class<? extends QuarkModule> moduleClazz) {
		QuarkModule module = getModuleInstance(moduleClazz);
		return module != null && module.enabled;
	}
	
	public QuarkModule getModuleInstance(Class<? extends QuarkModule> moduleClazz) {
		return foundModules.get(moduleClazz);
	}
	
	public boolean isItemEnabled(Item i) {
		if(i instanceof IQuarkItem) {
			IQuarkItem qi = (IQuarkItem) i;
			if(!qi.isEnabled())
				return false;
		}
		else if(i instanceof BlockItem) {
			BlockItem bi = (BlockItem) i;
			Block b = bi.getBlock();
			if(b instanceof IQuarkBlock) {
				IQuarkBlock qb = (IQuarkBlock) b;
				if(!qb.isEnabled())
					return false;
			}
		}
		
		return true;
	}
	
}
