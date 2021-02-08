package vazkii.quark.addons.oddities.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.addons.oddities.module.MatrixEnchantingModule;
import vazkii.quark.addons.oddities.tile.MatrixEnchantingTableTileEntity;
import vazkii.quark.api.IEnchantmentInfluencer;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.building.block.CandleBlock;

public class MatrixEnchantingTableBlock extends EnchantingTableBlock {

	public MatrixEnchantingTableBlock() {
		super(Block.Properties.from(Blocks.ENCHANTING_TABLE));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MatrixEnchantingTableTileEntity();
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult raytrace) {
		if(!(worldIn.getTileEntity(pos) instanceof MatrixEnchantingTableTileEntity))
			worldIn.setTileEntity(pos, createTileEntity(state, worldIn));

		if(ModuleLoader.INSTANCE.isModuleEnabled(MatrixEnchantingModule.class)) {
			if(player instanceof ServerPlayerEntity)
				NetworkHooks.openGui((ServerPlayerEntity) player, (MatrixEnchantingTableTileEntity) worldIn.getTileEntity(pos), pos);
		} else {
			if(!worldIn.isRemote) {
				INamedContainerProvider provider = new SimpleNamedContainerProvider((p_220147_2_, p_220147_3_, p_220147_4_) -> {
					return new EnchantmentContainer(p_220147_2_, p_220147_3_, IWorldPosCallable.of(worldIn, pos));
				}, ((MatrixEnchantingTableTileEntity) worldIn.getTileEntity(pos)).getDisplayName());
				player.openContainer(provider);
			}
		}

		return ActionResultType.SUCCESS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		boolean enabled = ModuleLoader.INSTANCE.isModuleEnabled(MatrixEnchantingModule.class);
		boolean showInfluences = enabled && MatrixEnchantingModule.allowInfluencing;
		boolean allowUnderwater = enabled && MatrixEnchantingModule.allowUnderwaterEnchanting;
		
		for(int i = -2; i <= 2; ++i)
			for(int j = -2; j <= 2; ++j) {
				if(i > -2 && i < 2 && j == -1)
					j = 2;

				if(rand.nextInt(16) == 0)
					for(int k = 0; k <= 1; ++k) {
						BlockPos blockpos = pos.add(i, k, j);
						BlockState state = worldIn.getBlockState(blockpos); 
						if(state.getEnchantPowerBonus(worldIn, pos) > 0) {
							BlockPos test = pos.add(i / 2, 0, j / 2);
							if(!(worldIn.isAirBlock(test) || (allowUnderwater && worldIn.getBlockState(test).getBlock() == Blocks.WATER)))
								break;
							
							if(showInfluences && state.getBlock() instanceof IEnchantmentInfluencer) {
								DyeColor color = ((IEnchantmentInfluencer) state.getBlock()).getEnchantmentInfluenceColor(worldIn, blockpos, state);
								
								if(color != null) {
									float[] comp = color.getColorComponentValues();
									
									int steps = 20;
									double dx = (double) (pos.getX() - blockpos.getX()) / steps;
									double dy = (double) (pos.getY() - blockpos.getY()) / steps;
									double dz = (double) (pos.getZ() - blockpos.getZ()) / steps;

									for(int p = 0; p < steps; p++) {
										if(rand.nextDouble() < 0.5)
											continue;
										
										double px = blockpos.getX() + 0.5 + dx * p + rand.nextDouble() * 0.2 - 0.1;
										double py = blockpos.getY() + 0.5 + dy * p + Math.sin((double) p / steps * Math.PI) * 0.5 + rand.nextDouble() * 0.2 - 0.1;
										double pz = blockpos.getZ() + 0.5 + dz * p + rand.nextDouble() * 0.2 - 0.1;
										
										worldIn.addParticle(new RedstoneParticleData(comp[0], comp[1], comp[2], 1F), px, py, pz, 0, 0, 0);
									}
								}
							}

							worldIn.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5, i + rand.nextFloat() - 0.5, k - rand.nextFloat() - 1.0, j + rand.nextFloat() - 0.5);
						}
					}
			}
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

		if(stack.hasDisplayName()) {
			TileEntity tileentity = worldIn.getTileEntity(pos);

			if(tileentity instanceof MatrixEnchantingTableTileEntity)
				((MatrixEnchantingTableTileEntity) tileentity).setCustomName(stack.getDisplayName());
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		TileEntity tileentity = worldIn.getTileEntity(pos);

		if(tileentity instanceof MatrixEnchantingTableTileEntity) {
			MatrixEnchantingTableTileEntity enchanter = (MatrixEnchantingTableTileEntity) tileentity;
			enchanter.dropItem(0);
			enchanter.dropItem(1);
		}

		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

}
