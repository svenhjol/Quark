package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import vazkii.quark.content.automation.module.ChainLinkageModule;
import vazkii.quark.content.tweaks.module.SpringySlimeModule;

@Mixin(AbstractMinecartEntity.class)
public class AbstractMinecartEntityMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	private void updateChain(CallbackInfo callbackInfo) {
		ChainLinkageModule.onEntityUpdate((AbstractMinecartEntity) (Object) this);
	}

	@Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/minecart/AbstractMinecartEntity;remove()V"))
	private void attackEntityFrom$recordMotion(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		SpringySlimeModule.recordMotion((AbstractMinecartEntity) (Object) this);
	}

	@Inject(method = "killMinecart", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/minecart/AbstractMinecartEntity;remove()V"))
	private void killMinecart$recordMotion(CallbackInfo callbackInfo) {
		SpringySlimeModule.recordMotion((AbstractMinecartEntity) (Object) this);
	}
}
