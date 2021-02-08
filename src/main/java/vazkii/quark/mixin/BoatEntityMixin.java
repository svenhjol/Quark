package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.item.ItemStack;
import vazkii.quark.content.automation.module.ChainLinkageModule;
import vazkii.quark.content.experimental.module.GameNerfsModule;
import vazkii.quark.content.management.entity.ChestPassengerEntity;
import vazkii.quark.content.tools.item.PickarangItem;

@Mixin(BoatEntity.class)
public class BoatEntityMixin {

	@Inject(method = "getControllingPassenger", at = @At("RETURN"), cancellable = true)
	private void ensurePassengerIsNotChest(CallbackInfoReturnable<Entity> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(ensurePassengerIsNotChest(callbackInfoReturnable.getReturnValue()));
	}

	@Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/BoatEntity;remove()V"))
	private void attackEntityFrom$dropChain(CallbackInfoReturnable<Boolean> callbackInfo) {
		ChainLinkageModule.drop((BoatEntity) (Object) this);
	}

	@Inject(method = "updateFallState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/BoatEntity;remove()V"))
	private void updateFallState$dropChain(CallbackInfo callbackInfo) {
		ChainLinkageModule.drop((BoatEntity) (Object) this);
	}
	
	@Inject(method = "getBoatGlide", at = @At("RETURN"), cancellable = true)
	private void getBoatGlide(CallbackInfoReturnable<Float> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(GameNerfsModule.getBoatGlide(callbackInfoReturnable.getReturnValueF()));
	}
	
	private static Entity ensurePassengerIsNotChest(Entity passenger) {
		if (passenger instanceof ChestPassengerEntity)
			return null;
		return passenger;
	}
}
