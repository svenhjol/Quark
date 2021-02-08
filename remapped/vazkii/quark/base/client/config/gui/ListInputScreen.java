package vazkii.quark.base.client.config.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import vazkii.quark.base.client.config.gui.widget.ScrollableWidgetList;
import vazkii.quark.base.client.config.gui.widget.StringElementList;
import vazkii.quark.base.client.config.obj.ListObject;

public class ListInputScreen extends AbstractScrollingWidgetScreen {

	private final ListObject object;
	public final List<String> initialList;
	
	public List<String> list;
	
	public ListInputScreen(Screen parent, ListObject object) {
		super(parent);
		this.object = object;
		
		initialList = new ArrayList<>(object.getCurrentObj());
		list = new ArrayList<>(initialList);
	}

	@Override
	public void render(MatrixStack mstack, int mouseX, int mouseY, float pticks) {
		super.render(mstack, mouseX, mouseY, pticks);
		
		drawCenteredText(mstack, textRenderer, new LiteralText(object.getGuiDisplayName()).formatted(Formatting.BOLD), width / 2, 20, 0xFFFFFF);
	}
	
	public void addNew() {
		list.add("");
		refresh();
	}
	
	public void remove(int index) {
		list.remove(index);
		refresh();
	}
	
	@Override
	protected ScrollableWidgetList<?, ?> createWidgetList() {
		return new StringElementList(this);
	}
	
	@Override
	protected void onClickDone(ButtonWidget b) {
		object.setCurrentObj(list);
		super.onClickDone(b);
	}

	@Override
	protected void onClickDefault(ButtonWidget b) {
		list = new ArrayList<>(object.getCurrentObj());
		refresh();
	}

	@Override
	protected void onClickDiscard(ButtonWidget b) {
		list = new ArrayList<>(object.defaultObj);
		refresh();
	}

	@Override
	protected boolean isDirty() {
		if(initialList.size() != list.size())
			return true;
		
		for(int i = 0; i < list.size(); i++) {
			String a = list.get(i);
			String b = initialList.get(i);
			if(!a.equals(b))
				return true;
		}
			
		return false;
	}

}
