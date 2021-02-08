package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import vazkii.quark.content.tools.module.ColorRunesModule;

@Mixin(ArmorFeatureRenderer.class)
public class BipedArmorLayerMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {

	@Inject(method = "getArmorModelHook", at = @At("HEAD"), remap = false)
	private void setColorRuneTargetStack(T entity, ItemStack itemStack, EquipmentSlot slot, A model, CallbackInfoReturnable<A> callbackInfoReturnable) {
		ColorRunesModule.setTargetStack(itemStack);
	}
}
