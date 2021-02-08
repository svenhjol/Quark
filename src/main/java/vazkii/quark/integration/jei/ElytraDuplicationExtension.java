package vazkii.quark.integration.jei;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import vazkii.quark.content.tweaks.recipe.ElytraDuplicationRecipe;

public class ElytraDuplicationExtension implements ICraftingCategoryExtension {
	private final ElytraDuplicationRecipe recipe;

	ElytraDuplicationExtension(ElytraDuplicationRecipe recipe) {
		this.recipe = recipe;
	}

	@Override
	public void setIngredients(IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void drawInfo(int recipeWidth, int recipeHeight, MatrixStack matrixStack, double mouseX, double mouseY) {
		Minecraft.getInstance().fontRenderer.drawString(matrixStack, I18n.format("quark.jei.makes_copy"), 60, 46, 0x555555);
		RenderSystem.enableAlphaTest();
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return recipe.getId();
	}
}
