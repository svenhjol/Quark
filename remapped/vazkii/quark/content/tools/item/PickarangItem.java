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
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.handler.QuarkSounds;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.entity.PickarangEntity;
import vazkii.quark.content.tools.module.PickarangModule;

public class PickarangItem extends QuarkItem {

	public final boolean isNetherite;
	
	public PickarangItem(String regname, QuarkModule module, Settings properties, boolean isNetherite) {
		super(regname, module, properties);
		this.isNetherite = isNetherite;
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		stack.damage(2, attacker, (player) -> player.sendToolBreakStatus(Hand.MAIN_HAND));
		return true;
	}

	@Override
	public boolean isEffectiveOn(BlockState blockIn) {
		switch (isNetherite ? PickarangModule.netheriteHarvestLevel : PickarangModule.harvestLevel) {
			case 0:
				return Items.WOODEN_PICKAXE.isEffectiveOn(blockIn) ||
						Items.WOODEN_AXE.isEffectiveOn(blockIn) ||
						Items.WOODEN_SHOVEL.isEffectiveOn(blockIn);
			case 1:
				return Items.STONE_PICKAXE.isEffectiveOn(blockIn) ||
						Items.STONE_AXE.isEffectiveOn(blockIn) ||
						Items.STONE_SHOVEL.isEffectiveOn(blockIn);
			case 2:
				return Items.IRON_PICKAXE.isEffectiveOn(blockIn) ||
						Items.IRON_AXE.isEffectiveOn(blockIn) ||
						Items.IRON_SHOVEL.isEffectiveOn(blockIn);
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
	public boolean postMine(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
		if (state.getHardness(worldIn, pos) != 0)
			stack.damage(1, entityLiving, (player) -> player.sendToolBreakStatus(Hand.MAIN_HAND));
		return true;
	}

	@Nonnull
	@Override
	@SuppressWarnings("ConstantConditions")
    public TypedActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, @Nonnull Hand handIn) {
        ItemStack itemstack = playerIn.getStackInHand(handIn);
        playerIn.setStackInHand(handIn, ItemStack.EMPTY);
		int eff = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemstack);
		Vec3d pos = playerIn.getPos();
        worldIn.playSound(null, pos.x, pos.y, pos.z, QuarkSounds.ENTITY_PICKARANG_THROW, SoundCategory.NEUTRAL, 0.5F + eff * 0.14F, 0.4F / (worldIn.random.nextFloat() * 0.4F + 0.8F));

        if(!worldIn.isClient)  {
        	int slot = handIn == Hand.OFF_HAND ? playerIn.inventory.size() - 1 : playerIn.inventory.selectedSlot;
        	PickarangEntity entity = new PickarangEntity(worldIn, playerIn);
        	entity.setThrowData(slot, itemstack, isNetherite);
        	entity.shoot(playerIn, playerIn.pitch, playerIn.yaw, 0.0F, 1.5F + eff * 0.325F, 0F);
            worldIn.spawnEntity(entity);
        }

        if(!playerIn.abilities.creativeMode && !PickarangModule.noCooldown) {
        	int cooldown = 10 - eff;
        	if (cooldown > 0)
				playerIn.getItemCooldownManager().set(this, cooldown);
		}
        
        playerIn.incrementStat(Stats.USED.getOrCreateStat(this));
        return new TypedActionResult<>(ActionResult.SUCCESS, itemstack);
    }

	@Nonnull
	@Override
	public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, ItemStack stack) {
		Multimap<EntityAttribute, EntityAttributeModifier> multimap = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);

		if (slot == EquipmentSlot.MAINHAND) {
			multimap.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 2, EntityAttributeModifier.Operation.ADDITION)); 
			multimap.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -2.8, EntityAttributeModifier.Operation.ADDITION)); 
		}

		return multimap;
	}

	@Override
	public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
		return 0F;
	}

	@Override
	public boolean isRepairable(ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean canRepair(ItemStack toRepair, ItemStack repair) {
		return repair.getItem() == (isNetherite ? Items.NETHERITE_INGOT : Items.DIAMOND);
	}
	
	@Override
	public int getEnchantability() {
		return isNetherite ? Items.NETHERITE_PICKAXE.getEnchantability() : Items.DIAMOND_PICKAXE.getEnchantability();
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(Enchantments.FORTUNE, Enchantments.SILK_TOUCH, Enchantments.EFFICIENCY).contains(enchantment);
	}
}
