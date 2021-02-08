package vazkii.quark.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author WireSegal
 * Created at 2:22 PM on 8/17/19.
 */
public interface IRuneColorProvider {

    @Environment(EnvType.CLIENT)
    int getRuneColor(ItemStack stack);
}
