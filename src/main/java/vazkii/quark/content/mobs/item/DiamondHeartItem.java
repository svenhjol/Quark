package vazkii.quark.content.mobs.item;

import javax.annotation.Nonnull;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.mobs.entity.EnumStonelingVariant;
import vazkii.quark.content.mobs.entity.StonelingEntity;
import vazkii.quark.content.mobs.module.StonelingsModule;

public class DiamondHeartItem extends QuarkItem {

	public DiamondHeartItem(String regname, QuarkModule module, Properties properties) {
		super(regname, module, properties);
	}

	@Nonnull
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		Hand hand = context.getHand();
		Direction facing = context.getFace();

		if (player != null) {
			BlockState stateAt = world.getBlockState(pos);
			ItemStack stack = player.getHeldItem(hand);

			if (player.canPlayerEdit(pos, facing, stack) && stateAt.getBlockHardness(world, pos) != -1) {

				EnumStonelingVariant variant = null;
				for (EnumStonelingVariant possibleVariant : EnumStonelingVariant.values()) {
					if (possibleVariant.getBlocks().contains(stateAt.getBlock()))
						variant = possibleVariant;
				}

				if (variant != null) {
					if (!world.isRemote && world instanceof IServerWorld) {
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
						world.playEvent(2001, pos, Block.getStateId(stateAt));

						StonelingEntity stoneling = new StonelingEntity(StonelingsModule.stonelingType, world);
						stoneling.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
						stoneling.setPlayerMade(true);
						stoneling.rotationYaw = player.rotationYaw + 180F;
						stoneling.onInitialSpawn((IServerWorld) world, world.getDifficultyForLocation(pos), SpawnReason.STRUCTURE, variant, null);
						world.addEntity(stoneling);
						
						if(player instanceof ServerPlayerEntity)
							CriteriaTriggers.SUMMONED_ENTITY.trigger((ServerPlayerEntity) player, stoneling);

						if (!player.abilities.isCreativeMode)
							stack.shrink(1);
					}

					return ActionResultType.SUCCESS;
				}
			}
		}
		
		return ActionResultType.PASS;
	}

	@Nonnull
	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.UNCOMMON;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

}
