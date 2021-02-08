package vazkii.quark.content.world.block;

import java.util.Random;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

public class BlossomLeavesBlock extends LeavesBlock implements IQuarkBlock {

	private final QuarkModule module;
	private BooleanSupplier enabledSupplier = () -> true;
	
	public BlossomLeavesBlock(String colorName, QuarkModule module, MaterialColor color) {
		super(Block.Properties.create(Material.LEAVES, color)
				.hardnessAndResistance(0.2F)
				.tickRandomly()
				.sound(SoundType.PLANT)
				.harvestTool(ToolType.HOE)
				.notSolid()
				.setAllowsSpawn((s, r, p, t) -> false)
				.setSuffocates((s, r, p) -> false)
				.setBlocksVision((s, r, p) -> false));
		
		this.module = module;

		RegistryHelper.registerBlock(this, colorName + "_blossom_leaves");
		RegistryHelper.setCreativeTab(this, ItemGroup.DECORATIONS);
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT_MIPPED);
	}
	
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if(worldIn.isAirBlock(pos.down()) && rand.nextInt(5) == 0) {
			double windStrength = 5 + Math.cos((double) worldIn.getGameTime() / 2000) * 2;
			double windX = Math.cos((double) worldIn.getGameTime() / 1200) * windStrength;
			double windZ = Math.sin((double) worldIn.getGameTime() / 1000) * windStrength;
			
			worldIn.addParticle(new BlockParticleData(ParticleTypes.BLOCK, stateIn), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, windX, -1.0, windZ);
		}
	}

	@Nullable
	@Override
	public QuarkModule getModule() {
		return module;
	}

	@Override
	public BlossomLeavesBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

}
