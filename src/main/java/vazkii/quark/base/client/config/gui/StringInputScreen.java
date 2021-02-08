package vazkii.quark.base.client.config.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import vazkii.quark.base.client.config.obj.AbstractStringInputObject;

public class StringInputScreen<T> extends AbstractQScreen {

	private final AbstractStringInputObject<T> object;
	
	private TextFieldWidget input;
	private Button resetButton, doneButton;
	private boolean errored = false;
	
	public StringInputScreen(Screen parent, AbstractStringInputObject<T> object) {
		super(parent);
		this.object = object;
	}
	
	@Override
	public void render(MatrixStack mstack, int mouseX, int mouseY, float pticks) {
		renderBackground(mstack);

		super.render(mstack, mouseX, mouseY, pticks);
		
		drawCenteredString(mstack, font, new StringTextComponent(object.getGuiDisplayName()).mergeStyle(TextFormatting.BOLD), width / 2, 20, 0xFFFFFF);
		drawCenteredString(mstack, font, I18n.format("quark.gui.config.defaultvalue", object.defaultObj),  width / 2, 30, 0xFFFFFF);
		
		input.render(mstack, mouseX, mouseY, pticks);
	}
	
	@Override
	protected void init() {
		super.init();
		
		int pad = 3;
		int bWidth = 121;
		int left = (width - (bWidth + pad) * 3) / 2;
		int vStart = height - 30;
		
		addButton(new Button(left, vStart, bWidth, 20, new TranslationTextComponent("quark.gui.config.default"), this::setDefault));
		addButton(resetButton = new Button(left + bWidth + pad, vStart, bWidth, 20, new TranslationTextComponent("quark.gui.config.discard"), this::reset));
		addButton(doneButton = new Button(left + (bWidth + pad) * 2, vStart, bWidth, 20, new TranslationTextComponent("gui.done"), this::save));
		
		input = new TextFieldWidget(font, width / 2 - 100, 60, 200, 20, new StringTextComponent(""));
		input.setValidator(object::isStringValid);
		input.setMaxStringLength(object.getMaxStringLength());
		input.setResponder(this::update);
		setFocusedDefault(input);
		children.add(input);
		
		update();
	}
	
	@Override
	public void tick() {
		input.tick();
	}
	
	@Override
	public boolean keyPressed(int key, int mouseX, int mouseY) {
		switch(key) {
		case 256: // esc
			reset(null);
			return true;
		case 257: // enter
			if(!errored) {
				save(null);
				return true;
			}
		}
		
		return super.keyPressed(key, mouseX, mouseY);
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if(input.mouseClicked(x, y, button))
			return true;
		
		return super.mouseClicked(x, y, button);
	}
	
	private void setDefault(Button button) {
		object.reset(true);
		update();
		save(button);
	}
	
	private void reset(Button button) {
		object.reset(false);
		update();
		save(button);
	}
	
	private void save(Button button) {
		if(!errored) {
			object.setCurrentObj(object.fromString(input.getText()));
			returnToParent(button);
		}
	}
	
	private void update() {
		input.setText(object.getCurrentObj().toString());
		update(input.getText());
	}
	
	private void update(String s) {
		T val = object.fromString(s);
		errored = val == null || (object.restriction != null && !object.restriction.test(val));
		input.setTextColor(errored ? 0xFF0000 : 0xFFFFFF);
		
		resetButton.active = errored || object.wouldBeDirty(val);
		doneButton.active = !errored;
	}

}
