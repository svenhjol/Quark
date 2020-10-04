package vazkii.quark.base.proxy;

import java.time.LocalDateTime;
import java.time.Month;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import vazkii.quark.base.client.GoVoteHandler;
import vazkii.quark.base.client.config.IngameConfigHandler;
import vazkii.quark.base.handler.ContributorRewardHandler;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.config.IConfigCallback;

@Environment(EnvType.CLIENT)
public class ClientProxy extends CommonProxy {

	public static boolean jingleBellsMotherfucker = false;
	
	@Override
	public void start() {
		LocalDateTime now = LocalDateTime.now();
		if(now.getMonth() == Month.DECEMBER && now.getDayOfMonth() >= 16 || now.getMonth() == Month.JANUARY && now.getDayOfMonth() <= 6)
			jingleBellsMotherfucker = true;
		
		super.start();
		
		ModuleLoader.INSTANCE.clientStart();
	}

	@Override
	public void registerListeners(IEventBus bus) {
		super.registerListeners(bus);

		bus.addListener(this::clientSetup);
		bus.addListener(this::modelRegistry);
		bus.addListener(this::textureStitch);
		bus.addListener(this::postTextureStitch);
	}

	public void clientSetup(FMLClientSetupEvent event) {
		GoVoteHandler.init();
		RenderLayerHandler.init();
		ModuleLoader.INSTANCE.clientSetup();
	}

	public void modelRegistry(ModelRegistryEvent event) {
		ModuleLoader.INSTANCE.modelRegistry();
	}
	
	public void textureStitch(TextureStitchEvent.Pre event) {
		ModuleLoader.INSTANCE.textureStitch(event);
	}

	public void postTextureStitch(TextureStitchEvent.Post event) {
		ModuleLoader.INSTANCE.postTextureStitch(event);
	}

	@Override	
	public void handleQuarkConfigChange() {
		super.handleQuarkConfigChange();

		ModuleLoader.INSTANCE.configChangedClient();
		IngameConfigHandler.INSTANCE.refresh();

		MinecraftClient mc = MinecraftClient.getInstance();
		mc.submit(() -> {
			if(mc.isIntegratedServerRunning() && mc.player != null && mc.getServer() != null)
				mc.player.sendSystemMessage(new TranslatableText("quark.misc.reloaded"), null);
		});
	}
	
	@Override
	protected void initContributorRewards() {
		ContributorRewardHandler.getLocalName();
		super.initContributorRewards();
	}
	
	@Override
	public IConfigCallback getConfigCallback() {
		return IngameConfigHandler.INSTANCE;
	}

}
