/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [03/07/2016, 17:24:22 (GMT)]
 */
package vazkii.quark.base.util;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import vazkii.arl.util.RegistryHelper;

public class QuarkEffect extends Effect {

	protected final String bareName;

	public QuarkEffect(String name, EffectType type, int color) {
		super(type, color);
		RegistryHelper.register(this, name);
		bareName = name;
	}
}
