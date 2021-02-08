package vazkii.quark.content.management.module;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.client.handler.InventoryButtonHandler;
import vazkii.quark.base.client.handler.ModKeybindHandler;
import vazkii.quark.base.client.handler.InventoryButtonHandler.ButtonProvider;
import vazkii.quark.base.client.handler.InventoryButtonHandler.ButtonTargetType;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.SortInventoryMessage;
import vazkii.quark.content.management.client.gui.MiniInventoryButton;

import java.util.function.BooleanSupplier;

@LoadModule(category = ModuleCategory.MANAGEMENT)
public class InventorySortingModule extends QuarkModule {

	@Config
	public static boolean enablePlayerInventory = true;
	@Config
	public static boolean enablePlayerInventoryInChests = true;
	@Config
	public static boolean enableChests = true;

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		KeyBinding sortPlayer = ModKeybindHandler.init("sort_player", null, ModKeybindHandler.INV_GROUP);

		InventoryButtonHandler.addButtonProvider(this, ButtonTargetType.PLAYER_INVENTORY, 0,
				sortPlayer,
				(screen) -> {
					if (enablePlayerInventory)
						QuarkNetwork.sendToServer(new SortInventoryMessage(true));
				},
				provider("sort", true, () -> enablePlayerInventory));
		InventoryButtonHandler.addButtonProvider(this, ButtonTargetType.CONTAINER_PLAYER_INVENTORY, 0,
				sortPlayer,
				(screen) -> {
					if (enablePlayerInventoryInChests)
						QuarkNetwork.sendToServer(new SortInventoryMessage(true));
				},
				provider("sort_inventory", true, () -> enablePlayerInventoryInChests));
		InventoryButtonHandler.addButtonProvider(this, ButtonTargetType.CONTAINER_INVENTORY, 0,
				"sort_container",
				(screen) -> {
					if (enableChests)
						QuarkNetwork.sendToServer(new SortInventoryMessage(false));
				},
				provider("sort_container", false, () -> enableChests));
	}

	@OnlyIn(Dist.CLIENT)
	private ButtonProvider provider(String tooltip, boolean forcePlayer, BooleanSupplier condition) {
		return (parent, x, y) -> !condition.getAsBoolean() ? null :
				new MiniInventoryButton(parent, 0, x, y, "quark.gui.button." + tooltip, (b) -> QuarkNetwork.sendToServer(new SortInventoryMessage(forcePlayer)));
	}

}
