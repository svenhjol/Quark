package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import vazkii.quark.content.management.module.ItemSharingModule;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "getTextComponent", at = @At("RETURN"), cancellable = true)
	private void createStackComponent(CallbackInfoReturnable<ITextComponent> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(ItemSharingModule.createStackComponent((ItemStack) (Object) this, (IFormattableTextComponent) callbackInfoReturnable.getReturnValue()));
	}
}
