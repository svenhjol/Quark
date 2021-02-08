package vazkii.quark.content.client.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.client.handler.ModKeybindHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;

@LoadModule(category = ModuleCategory.CLIENT, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class AutoWalkKeybindModule extends QuarkModule {

	@Environment(EnvType.CLIENT)
	private KeyBinding keybind;

	private boolean autorunning;
	private boolean hadAutoJump;

	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		if(enabled)
			keybind = ModKeybindHandler.init("autorun", "caps.lock", ModKeybindHandler.ACCESSIBILITY_GROUP);
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onMouseInput(InputEvent.MouseInputEvent event) {
		acceptInput();
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		acceptInput();
	}

	private void acceptInput() {
		MinecraftClient mc = MinecraftClient.getInstance();

		if(mc.options.keyForward.isPressed()) {
			if(autorunning)
				mc.options.autoJump = hadAutoJump;
			
			autorunning = false;
		}
		
		else if(keybind.isPressed()) {
			autorunning = !autorunning;

			if(autorunning) {
				hadAutoJump = mc.options.autoJump;
				mc.options.autoJump = true;
			} else mc.options.autoJump = hadAutoJump;
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onInput(InputUpdateEvent event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if(mc.player != null && autorunning) {
			event.getMovementInput().pressingForward = true;
			// [VanillaCopy] magic numbers copied from net.minecraft.util.MovementInputFromOptions
			event.getMovementInput().movementForward = ((ClientPlayerEntity) event.getPlayer()).shouldSlowDown() ? 0.3F : 1F;
		}
	}

}
