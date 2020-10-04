package vazkii.quark.oddities.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vazkii.quark.base.Quark;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.HandleBackpackMessage;
import vazkii.quark.oddities.container.BackpackContainer;
import vazkii.quark.oddities.module.BackpackModule;

public class BackpackInventoryScreen extends InventoryScreen {
	
	private static final Identifier BACKPACK_INVENTORY_BACKGROUND = new Identifier(Quark.MOD_ID, "textures/misc/backpack_gui.png");
	
	private final PlayerEntity player;
	private ButtonWidget recipeButton;
	private int recipeButtonY;
	
	private boolean closeHack = false;
	private static PlayerScreenHandler oldContainer;
	
	public BackpackInventoryScreen(PlayerScreenHandler backpack, PlayerInventory inventory, Text component) {
		super(setBackpackContainer(inventory.player, backpack));
		
		this.player = inventory.player;
		setBackpackContainer(player, oldContainer);
	}
	
	public static PlayerEntity setBackpackContainer(PlayerEntity entity, PlayerScreenHandler container) {
		oldContainer = entity.playerScreenHandler;
		entity.playerScreenHandler = container;
		
		return entity;
	}

	@Override
	public void init(MinecraftClient mc, int width, int height) {
		backgroundHeight = 224;
		super.init(mc, width, height);
		
		for(AbstractButtonWidget widget : buttons)
			if(widget instanceof TexturedButtonWidget) {
				widget.y -= 29;
				
				recipeButton = (ButtonWidget) widget;
				recipeButtonY = widget.y;
			}
	}

	@Override
	public void tick() {
		super.tick();
		
		if(recipeButton != null)
			recipeButton.y = recipeButtonY;
		
		if(!BackpackModule.isEntityWearingBackpack(player)) {
			ItemStack curr = player.inventory.getCursorStack();
			BackpackContainer.saveCraftingInventory(player);
			closeHack = true;
			QuarkNetwork.sendToServer(new HandleBackpackMessage(false));
			client.openScreen(new InventoryScreen(player));
			player.inventory.setCursorStack(curr);
		}
	}
	
	@Override
	public void onClose() {
		if(closeHack) {
			closeHack = false;
			return;
		}
			
		super.onClose();
	}
	
	@Override // drawContainerGui
	protected void drawBackground(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		client.getTextureManager().bindTexture(BACKPACK_INVENTORY_BACKGROUND);
		int i = x;
		int j = y;
		drawTexture(stack, i, j, 0, 0, backgroundWidth, backgroundHeight);
		drawEntity(i + 51, j + 75, 30, i + 51 - mouseX, j + 75 - 50 - mouseY, client.player);
	}
	
}