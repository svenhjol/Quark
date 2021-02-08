package vazkii.quark.content.tools.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.handler.QuarkSounds;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.entity.PickarangEntity;
import vazkii.quark.content.tools.module.PickarangModule;

public class PickarangItem extends QuarkItem {

	public final boolean isNetherite;
	
	public PickarangItem(String regname, QuarkModule module, Properties properties, boolean isNetherite) {
		super(regname, module, properties);
		this.isNetherite = isNetherite;
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.damageItem(2, attacker, (player) -> player.sendBreakAnimation(Hand.MAIN_HAND));
		return true;
	}

	@Override
	public boolean canHarvestBlock(BlockState blockIn) {
		switch (isNetherite ? PickarangModule.netheriteHarvestLevel : PickarangModule.harvestLevel) {
			case 0:
				return Items.WOODEN_PICKAXE.canHarvestBlock(blockIn) ||
						Items.WOODEN_AXE.canHarvestBlock(blockIn) ||
						Items.WOODEN_SHOVEL.canHarvestBlock(blockIn);
			case 1:
				return Items.STONE_PICKAXE.canHarvestBlock(blockIn) ||
						Items.STONE_AXE.canHarvestBlock(blockIn) ||
						Items.STONE_SHOVEL.canHarvestBlock(blockIn);
			case 2:
				return Items.IRON_PICKAXE.canHarvestBlock(blockIn) ||
						Items.IRON_AXE.canHarvestBlock(blockIn) ||
						Items.IRON_SHOVEL.canHarvestBlock(blockIn);
			default:
				return true;
		}
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return Math.max(isNetherite ? PickarangModule.netheriteDurability : PickarangModule.durability, 0);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, @Nonnull ToolType type, @Nullable PlayerEntity player, @Nullable BlockState state) {
		return isNetherite ? PickarangModule.netheriteHarvestLevel : PickarangModule.harvestLevel;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
		if (state.getBlockHardness(worldIn, pos) != 0)
			stack.damageItem(1, entityLiving, (player) -> player.sendBreakAnimation(Hand.MAIN_HAND));
		return true;
	}

	@Nonnull
	@Override
	@SuppressWarnings("ConstantConditions")
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, @Nonnull Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setHeldItem(handIn, ItemStack.EMPTY);
		int eff = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemstack);
		Vector3d pos = playerIn.getPositionVec();
        worldIn.playSound(null, pos.x, pos.y, pos.z, QuarkSounds.ENTITY_PICKARANG_THROW, SoundCategory.NEUTRAL, 0.5F + eff * 0.14F, 0.4F / (worldIn.rand.nextFloat() * 0.4F + 0.8F));

        if(!worldIn.isRemote)  {
        	int slot = handIn == Hand.OFF_HAND ? playerIn.inventory.getSizeInventory() - 1 : playerIn.inventory.currentItem;
        	PickarangEntity entity = new PickarangEntity(worldIn, playerIn);
        	entity.setThrowData(slot, itemstack, isNetherite);
        	entity.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F + eff * 0.325F, 0F);
            worldIn.addEntity(entity);
        }

        if(!playerIn.abilities.isCreativeMode && !PickarangModule.noCooldown) {
        	int cooldown = 10 - eff;
        	if (cooldown > 0)
				playerIn.getCooldownTracker().setCooldown(this, cooldown);
		}
        
        playerIn.addStat(Stats.ITEM_USED.get(this));
        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }

	@Nonnull
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> multimap = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);

		if (slot == EquipmentSlotType.MAINHAND) {
			multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 2, AttributeModifier.Operation.ADDITION)); 
			multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.8, AttributeModifier.Operation.ADDITION)); 
		}

		return multimap;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return 0F;
	}

	@Override
	public boolean isRepairable(ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return repair.getItem() == (isNetherite ? Items.NETHERITE_INGOT : Items.DIAMOND);
	}
	
	@Override
	public int getItemEnchantability() {
		return isNetherite ? Items.NETHERITE_PICKAXE.getItemEnchantability() : Items.DIAMOND_PICKAXE.getItemEnchantability();
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(Enchantments.FORTUNE, Enchantments.SILK_TOUCH, Enchantments.EFFICIENCY).contains(enchantment);
	}
}
