package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import vazkii.quark.content.tools.module.ColorRunesModule;

@Mixin(BipedArmorLayer.class)
public class BipedArmorLayerMixin<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> {

	@Inject(method = "getArmorModelHook", at = @At("HEAD"), remap = false)
	private void setColorRuneTargetStack(T entity, ItemStack itemStack, EquipmentSlotType slot, A model, CallbackInfoReturnable<A> callbackInfoReturnable) {
		ColorRunesModule.setTargetStack(itemStack);
	}
}
