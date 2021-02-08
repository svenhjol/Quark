package vazkii.quark.base.client.config.gui;

import org.apache.commons.lang3.text.WordUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import vazkii.quark.api.config.IConfigCategory;
import vazkii.quark.base.Quark;
import vazkii.quark.base.client.config.ConfigObject;
import vazkii.quark.base.client.config.external.ExternalCategory;
import vazkii.quark.base.client.config.gui.widget.ConfigElementList;
import vazkii.quark.base.client.config.gui.widget.ScrollableWidgetList;

public class CategoryScreen extends AbstractScrollingWidgetScreen {

	public final IConfigCategory category;
	private String breadcrumbs;
	
	public CategoryScreen(Screen parent, IConfigCategory category) {
		super(parent);
		this.category = category;
	}
	
	@Override
	protected void init() {
		super.init();
		
		breadcrumbs = category.getName();
		IConfigCategory currCategory = category.getParent();
		while(currCategory != null && !(currCategory instanceof ExternalCategory)) {
			breadcrumbs = String.format("%s > %s", currCategory.getName(), breadcrumbs);
			currCategory = currCategory.getParent();
		}
		breadcrumbs = String.format("> %s", breadcrumbs);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void render(MatrixStack mstack, int mouseX, int mouseY, float pticks) {
		super.render(mstack, mouseX, mouseY, pticks);
		
		int left = 20;
		
		// change name for externals
		String modName = WordUtils.capitalizeFully(Quark.MOD_ID);
		IConfigCategory currCategory = category;
		while(currCategory != null && !(currCategory instanceof ExternalCategory))
			currCategory = currCategory.getParent();
		
		if(currCategory != null) {
			modName = currCategory.getName();
			if(modName.matches("common|client")) {
				currCategory = currCategory.getParent();
				modName = currCategory.getName();
			}
		}
		
		textRenderer.draw(mstack, Formatting.BOLD + I18n.translate("quark.gui.config.header", modName), left, 10, 0x48ddbc);
		textRenderer.draw(mstack, breadcrumbs, left, 20, 0xFFFFFF);
	}

	@Override
	protected ScrollableWidgetList<?, ?> createWidgetList() {
		return new ConfigElementList<ConfigObject<?>>(this);
	}

	@Override
	protected void onClickDefault(ButtonWidget b) {
		category.reset(true);
	}

	@Override
	protected void onClickDiscard(ButtonWidget b) {
		category.reset(false);
	}

	@Override
	protected boolean isDirty() {
		return category.isDirty();
	}

}
