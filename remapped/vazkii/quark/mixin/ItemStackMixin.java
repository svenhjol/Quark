package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import vazkii.quark.content.management.module.ItemSharingModule;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "toHoverableText", at = @At("RETURN"), cancellable = true)
	private void createStackComponent(CallbackInfoReturnable<Text> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(ItemSharingModule.createStackComponent((ItemStack) (Object) this, (MutableText) callbackInfoReturnable.getReturnValue()));
	}
}
