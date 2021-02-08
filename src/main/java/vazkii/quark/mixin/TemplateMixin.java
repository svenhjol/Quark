package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.template.Template;
import vazkii.quark.content.building.module.VariantChestsModule;

@Mixin(Template.class)
public class TemplateMixin {

	@ModifyVariable(method = "func_237146_a_(Lnet/minecraft/world/IServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/template/PlacementSettings;Ljava/util/Random;I)Z",
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/feature/template/Template$BlockInfo;nbt:Lnet/minecraft/nbt/CompoundNBT;", ordinal = 0),
			index = 21)
	private BlockState captureLocalBlockstate(BlockState state) {
		return VariantChestsModule.getGenerationChestBlockState(state);
	}
}
	