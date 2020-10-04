package vazkii.quark.integration.jei;

import javax.annotation.Nullable;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import vazkii.quark.tweaks.recipe.ElytraDuplicationRecipe;

public class ElytraDuplicationExtension implements ICraftingCategoryExtension {
	private final ElytraDuplicationRecipe recipe;

	ElytraDuplicationExtension(ElytraDuplicationRecipe recipe) {
		this.recipe = recipe;
	}

	@Override
	public void setIngredients(IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getPreviewInputs());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
	}

	@Override
	public void drawInfo(int recipeWidth, int recipeHeight, MatrixStack matrixStack, double mouseX, double mouseY) {
		MinecraftClient.getInstance().textRenderer.draw(matrixStack, I18n.translate("quark.jei.makes_copy"), 60, 46, 0x555555);
	}

	@Nullable
	@Override
	public Identifier getRegistryName() {
		return recipe.getId();
	}
}
