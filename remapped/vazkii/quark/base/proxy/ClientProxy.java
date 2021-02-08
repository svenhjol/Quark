package vazkii.quark.base.proxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.Month;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.client.config.IngameConfigHandler;
import vazkii.quark.base.client.config.external.ExternalConfigHandler;
import vazkii.quark.base.client.config.gui.QuarkConfigHomeScreen;
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

		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
				() -> (minecraft, screen) -> new QuarkConfigHomeScreen(screen));

		copyProgrammerArtIfMissing();
		
		(new ExternalConfigHandler()).setAPIHandler();
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
		RenderLayerHandler.init();
		ModuleLoader.INSTANCE.clientSetup(event);
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
				for(int i = 0; i < 3; i++)
					mc.player.sendSystemMessage(new TranslatableText("quark.misc.reloaded" + i).formatted(i == 0 ? Formatting.AQUA : Formatting.WHITE), null);
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

	private static void copyProgrammerArtIfMissing() {
		File dir = new File(".", "resourcepacks");
		File target = new File(dir, "Quark Programmer Art.zip");

		if(!target.exists()) 
			try {
				dir.mkdirs();
				InputStream in = Quark.class.getResourceAsStream("/assets/quark/programmer_art.zip");
				FileOutputStream out = new FileOutputStream(target);
				
				byte[] buf = new byte[16384];
				int len = 0;
				while((len = in.read(buf)) > 0)
					out.write(buf, 0, len);

				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

}
