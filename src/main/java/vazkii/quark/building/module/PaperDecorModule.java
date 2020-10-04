/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [24/03/2016, 03:18:35 (GMT)]
 */
package vazkii.quark.building.module;

import vazkii.quark.base.block.IQuarkBlock;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.building.block.PaperLanternBlock;
import vazkii.quark.building.block.PaperWallBlock;

@LoadModule(category = ModuleCategory.BUILDING)
public class PaperDecorModule extends Module {

	@Override
	public void construct() {
		IQuarkBlock parent = new PaperLanternBlock("paper_lantern", this);
		new PaperLanternBlock("paper_lantern_sakura", this);

		new PaperWallBlock(parent, "paper_wall");
		new PaperWallBlock(parent, "paper_wall_big");
		new PaperWallBlock(parent, "paper_wall_sakura");
	}
	
}

