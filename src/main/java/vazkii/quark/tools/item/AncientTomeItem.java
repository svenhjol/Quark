package vazkii.quark.tools.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.Module;
import vazkii.quark.tools.module.AncientTomesModule;

import javax.annotation.Nonnull;
import java.util.List;

public class AncientTomeItem extends QuarkItem {

	public AncientTomeItem(Module module) {
		super("ancient_tome", module, 
				new Item.Settings().maxCount(1));
	}
	
	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack) {
		return false;
	}
	
	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Nonnull
	@Override
	public Rarity getRarity(ItemStack stack) {
		return EnchantedBookItem.getEnchantmentTag(stack).isEmpty() ? super.getRarity(stack) : Rarity.UNCOMMON;
	}
	
	public static ItemStack getEnchantedItemStack(EnchantmentLevelEntry ench) {
		ItemStack newStack = new ItemStack(AncientTomesModule.ancient_tome);
		EnchantedBookItem.addEnchantment(newStack, ench);
		return newStack;
	}
	
	@Override
	public void appendStacks(@Nonnull ItemGroup group, @Nonnull DefaultedList<ItemStack> items) {
		if (isEnabled() || group == ItemGroup.SEARCH) {
			if (group == ItemGroup.SEARCH || group.getEnchantments().length != 0) {
				for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
					if ((group == ItemGroup.SEARCH && ench.getMaxLevel() != 1) ||
							AncientTomesModule.validEnchants.contains(ench)) {
						if ((group == ItemGroup.SEARCH && ench.type != null) || group.containsEnchantments(ench.type)) {
							items.add(getEnchantedItemStack(new EnchantmentLevelEntry(ench, ench.getMaxLevel())));
						}
					}
				}
			}
		}
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, World worldIn, List<Text> tooltip, TooltipContext flagIn) {
		super.appendTooltip(stack, worldIn, tooltip, flagIn);
	    ItemStack.appendEnchantments(tooltip, EnchantedBookItem.getEnchantmentTag(stack));
	}

}
