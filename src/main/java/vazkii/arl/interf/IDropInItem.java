package vazkii.arl.interf;

import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.TranslatableText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IDropInItem {

	boolean canDropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming);

	ItemStack dropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming);

	@Environment(EnvType.CLIENT)
	default List<StringVisitable> getDropInTooltip(ItemStack stack) {
		return Collections.singletonList(new TranslatableText("arl.misc.right_click_add"));
	}

}
