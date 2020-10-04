package vazkii.quark.mobs.item;

import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.Module;
import vazkii.quark.mobs.entity.EnumStonelingVariant;
import vazkii.quark.mobs.entity.StonelingEntity;
import vazkii.quark.mobs.module.StonelingsModule;

public class DiamondHeartItem extends QuarkItem {

	public DiamondHeartItem(String regname, Module module, Settings properties) {
		super(regname, module, properties);
	}

	@Nonnull
	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity player = context.getPlayer();
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		Hand hand = context.getHand();
		Direction facing = context.getSide();

		if (player != null) {
			BlockState stateAt = world.getBlockState(pos);
			ItemStack stack = player.getStackInHand(hand);

			if (player.canPlaceOn(pos, facing, stack) && stateAt.getHardness(world, pos) != -1) {

				EnumStonelingVariant variant = null;
				for (EnumStonelingVariant possibleVariant : EnumStonelingVariant.values()) {
					if (possibleVariant.getBlocks().contains(stateAt.getBlock()))
						variant = possibleVariant;
				}

				if (variant != null) {
					if (!world.isClient) {
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
						world.syncWorldEvent(2001, pos, Block.getRawIdFromState(stateAt));

						StonelingEntity stoneling = new StonelingEntity(StonelingsModule.stonelingType, world);
						stoneling.updatePosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
						stoneling.setPlayerMade(true);
						stoneling.yaw = player.yaw + 180F;
						stoneling.initialize(world, world.getLocalDifficulty(pos), SpawnReason.STRUCTURE, variant, null);
						world.spawnEntity(stoneling);
						
						if(player instanceof ServerPlayerEntity)
							Criteria.SUMMONED_ENTITY.trigger((ServerPlayerEntity) player, stoneling);

						if (!player.abilities.creativeMode)
							stack.decrement(1);
					}

					return ActionResult.SUCCESS;
				}
			}
		}
		
		return ActionResult.PASS;
	}

	@Nonnull
	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.UNCOMMON;
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

}
