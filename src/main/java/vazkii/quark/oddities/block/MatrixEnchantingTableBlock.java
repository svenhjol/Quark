package vazkii.quark.oddities.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import vazkii.quark.api.IEnchantmentInfluencer;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.building.block.CandleBlock;
import vazkii.quark.oddities.module.MatrixEnchantingModule;
import vazkii.quark.oddities.tile.MatrixEnchantingTableTileEntity;

public class MatrixEnchantingTableBlock extends EnchantingTableBlock {

	public MatrixEnchantingTableBlock() {
		super(Block.Properties.copy(Blocks.ENCHANTING_TABLE));
	}

	@Override
	public BlockEntity createTileEntity(BlockState state, BlockView world) {
		return new MatrixEnchantingTableTileEntity();
	}

	@Override
	public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockHitResult raytrace) {
		if(!(worldIn.getBlockEntity(pos) instanceof MatrixEnchantingTableTileEntity))
			worldIn.setBlockEntity(pos, createTileEntity(state, worldIn));

		if(ModuleLoader.INSTANCE.isModuleEnabled(MatrixEnchantingModule.class)) {
			if(player instanceof ServerPlayerEntity)
				NetworkHooks.openGui((ServerPlayerEntity) player, (MatrixEnchantingTableTileEntity) worldIn.getBlockEntity(pos), pos);
		} else {
			if(!worldIn.isClient) {
				NamedScreenHandlerFactory provider = new SimpleNamedScreenHandlerFactory((p_220147_2_, p_220147_3_, p_220147_4_) -> {
					return new EnchantmentScreenHandler(p_220147_2_, p_220147_3_, ScreenHandlerContext.create(worldIn, pos));
				}, ((MatrixEnchantingTableTileEntity) worldIn.getBlockEntity(pos)).getDisplayName());
				player.openHandledScreen(provider);
			}
		}

		return ActionResult.SUCCESS;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
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
							if(!(worldIn.isAir(test) || (allowUnderwater && worldIn.getBlockState(test).getBlock() == Blocks.WATER)))
								break;
							
							if(showInfluences && state.getBlock() instanceof IEnchantmentInfluencer) {
								DyeColor color = ((IEnchantmentInfluencer) state.getBlock()).getEnchantmentInfluenceColor(worldIn, blockpos, state);
								
								if(color != null) {
									float[] comp = color.getColorComponents();
									
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
										
										worldIn.addParticle(new DustParticleEffect(comp[0], comp[1], comp[2], 1F), px, py, pz, 0, 0, 0);
									}
								}
							}

							worldIn.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5, i + rand.nextFloat() - 0.5, k - rand.nextFloat() - 1.0, j + rand.nextFloat() - 0.5);
						}
					}
			}
	}

	@Override
	public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onPlaced(worldIn, pos, state, placer, stack);

		if(stack.hasCustomName()) {
			BlockEntity tileentity = worldIn.getBlockEntity(pos);

			if(tileentity instanceof MatrixEnchantingTableTileEntity)
				((MatrixEnchantingTableTileEntity) tileentity).setCustomName(stack.getName());
		}
	}

	@Override
	public void onStateReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		BlockEntity tileentity = worldIn.getBlockEntity(pos);

		if(tileentity instanceof MatrixEnchantingTableTileEntity) {
			MatrixEnchantingTableTileEntity enchanter = (MatrixEnchantingTableTileEntity) tileentity;
			enchanter.dropItem(0);
			enchanter.dropItem(1);
		}

		super.onStateReplaced(state, worldIn, pos, newState, isMoving);
	}

}
