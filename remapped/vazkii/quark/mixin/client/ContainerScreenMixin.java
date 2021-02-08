package vazkii.quark.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vazkii.quark.content.management.module.EasyTransferingModule;

@Mixin(HandledScreen.class)
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
