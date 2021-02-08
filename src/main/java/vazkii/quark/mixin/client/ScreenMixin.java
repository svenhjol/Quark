package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screen.Screen;
import vazkii.quark.content.management.module.EasyTransferingModule;

@Mixin(Screen.class)
public class ScreenMixin {

	@Inject(method = "hasShiftDown", at = @At("RETURN"), cancellable = true)
	private static void hasShiftDown(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(EasyTransferingModule.hasShiftDown(callbackInfoReturnable.getReturnValue()));
	}
}
