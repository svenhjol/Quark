package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import vazkii.quark.content.tools.item.PickarangItem;
import vazkii.quark.content.tweaks.module.HoeHarvestingModule;

@Mixin(Enchantment.class)
public class EnchantmentMixin {

	@Inject(method = "canApply", at = @At("RETURN"), cancellable = true)
	private void canApply(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (!callbackInfoReturnable.getReturnValue()) {
			Enchantment enchantment = (Enchantment) (Object) this;
			callbackInfoReturnable.setReturnValue(canPiercingApply(enchantment, stack) || HoeHarvestingModule.canFortuneApply(enchantment, stack));
		}
	}

	private static boolean canPiercingApply(Enchantment enchantment, ItemStack stack) {
		return enchantment == Enchantments.PIERCING && stack.getItem() instanceof PickarangItem;
	}
	
}
