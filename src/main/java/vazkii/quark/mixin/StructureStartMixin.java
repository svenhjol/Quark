package vazkii.quark.mixin;

import java.util.List;
import java.util.Random;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import vazkii.quark.content.building.module.VariantChestsModule;

@Mixin(StructureStart.class)
public class StructureStartMixin {

	@Shadow
	@Final
	protected List<StructurePiece> components;
	
	@Shadow
	@Final
	private Structure<?> structure;
	
	@Inject(method = "func_230366_a_", at = @At("HEAD"))
	public void injectReference(ISeedReader p_230366_1_, StructureManager p_230366_2_, ChunkGenerator p_230366_3_, Random p_230366_4_, MutableBoundingBox p_230366_5_, ChunkPos p_230366_6_, CallbackInfo callback) {
		VariantChestsModule.setActiveStructure(structure, components);
	}
	
	@Inject(method = "func_230366_a_", at = @At("RETURN"))
	public void resetReference(ISeedReader p_230366_1_, StructureManager p_230366_2_, ChunkGenerator p_230366_3_, Random p_230366_4_, MutableBoundingBox p_230366_5_, ChunkPos p_230366_6_, CallbackInfo callback) {
		VariantChestsModule.setActiveStructure(null, null);
	}
	
}