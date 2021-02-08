package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import vazkii.quark.content.management.module.EasyTransferingModule;

@Mixin(ContainerScreen.class)
public class ContainerScreenMixin {

	@ModifyVariable(method = "mouseClicked(DDI)Z",
			at = @At("STORE"),
			index = 15)
	private boolean hasShiftDownClick(boolean curr) {
		return EasyTransferingModule.hasShiftDown(curr);
	}
	
	@ModifyVariable(method = "mouseReleased(DDI)Z",
			at = @At("STORE"),
			index = 12)
	private boolean hasShiftDownRelease(boolean curr) {
		return EasyTransferingModule.hasShiftDown(curr);
	}
	
}
