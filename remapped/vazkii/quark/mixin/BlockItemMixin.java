package vazkii.quark.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import vazkii.quark.content.tweaks.module.LockRotationModule;

@Mixin(BlockItem.class)
public class BlockItemMixin {

	@Shadow
	@Nullable
	protected native BlockState getPlacementState(ItemPlacementContext context);

	@Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BlockItem;getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;"))
	private BlockState alterPlacementState(BlockItem self, ItemPlacementContext context) {
		return LockRotationModule.fixBlockRotation(getPlacementState(context), context);
	}
}
