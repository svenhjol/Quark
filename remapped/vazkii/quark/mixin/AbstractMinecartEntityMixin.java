package vazkii.quark.mixin;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.quark.content.automation.module.ChainLinkageModule;
import vazkii.quark.content.tweaks.module.SpringySlimeModule;

@Mixin(AbstractMinecartEntity.class)
public class AbstractMinecartEntityMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	private void updateChain(CallbackInfo callbackInfo) {
		ChainLinkageModule.onEntityUpdate((AbstractMinecartEntity) (Object) this);
	}

	@Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;remove()V"))
	private void attackEntityFrom$recordMotion(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		SpringySlimeModule.recordMotion((AbstractMinecartEntity) (Object) this);
	}

	@Inject(method = "dropItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/AbstractMinecartEntity;remove()V"))
	private void killMinecart$recordMotion(CallbackInfo callbackInfo) {
		SpringySlimeModule.recordMotion((AbstractMinecartEntity) (Object) this);
	}
}
