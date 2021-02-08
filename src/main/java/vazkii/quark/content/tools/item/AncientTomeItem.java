package vazkii.quark.content.tools.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tools.module.AncientTomesModule;

import javax.annotation.Nonnull;
import java.util.List;

public class AncientTomeItem extends QuarkItem {

	public AncientTomeItem(QuarkModule module) {
		super("ancient_tome", module, 
				new Item.Properties().maxStackSize(1));
	}
	
	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) {
		return false;
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Nonnull
	@Override
	public Rarity getRarity(ItemStack stack) {
		return EnchantedBookItem.getEnchantments(stack).isEmpty() ? super.getRarity(stack) : Rarity.UNCOMMON;
	}
	
	public static ItemStack getEnchantedItemStack(EnchantmentData ench) {
		ItemStack newStack = new ItemStack(AncientTomesModule.ancient_tome);
		EnchantedBookItem.addEnchantment(newStack, ench);
		return newStack;
	}
	
	@Override
	public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
		if (isEnabled() || group == ItemGroup.SEARCH) {
			if (group == ItemGroup.SEARCH || group.getRelevantEnchantmentTypes().length != 0) {
				Registry.ENCHANTMENT.forEach(ench -> {
					if ((group == ItemGroup.SEARCH && ench.getMaxLevel() != 1) ||
							AncientTomesModule.validEnchants.contains(ench)) {
						if ((group == ItemGroup.SEARCH && ench.type != null) || group.hasRelevantEnchantmentType(ench.type)) {
							items.add(getEnchantedItemStack(new EnchantmentData(ench, ench.getMaxLevel())));
						}
					}
				});
			}
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
	    ItemStack.addEnchantmentTooltips(tooltip, EnchantedBookItem.getEnchantments(stack));
	}

}
