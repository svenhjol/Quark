package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.item.ItemStack;
import vazkii.quark.content.tools.item.PickarangItem;

@Mixin(DamageEnchantment.class)
public class DamageEnchantmentMixin {

	@Inject(method = "canApply", at = @At("RETURN"), cancellable = true)
	private void canSharpnessApply(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (!callbackInfoReturnable.getReturnValue()) {
			callbackInfoReturnable.setReturnValue(stack.getItem() instanceof PickarangItem);
		}
	}
}
