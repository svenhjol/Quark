package vazkii.quark.base.client.screen;

import java.util.function.Supplier;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import vazkii.quark.base.client.config.ConfigObject;

public class CheckboxButton extends ButtonWidget {

	private final Supplier<Boolean> checkedSupplier;
	
	public CheckboxButton(int x, int y, Supplier<Boolean> checkedSupplier, PressAction onClick) {
		super(x, y, 20, 20, new LiteralText(""), onClick);
		this.checkedSupplier = checkedSupplier;
	}
	
	public CheckboxButton(int x, int y, ConfigObject<Boolean> configObj) {
		this(x, y, () -> configObj.getCurrentObj(), (b) -> configObj.setCurrentObj(!configObj.getCurrentObj()));
	}
	
	// TODO proper icons
	
	@Override
	public int getFGColor() {
		return checkedSupplier.get() ? 0x00FF00 : 0xFF0000;
	}
	
	@Override
	public Text getMessage() {
		return new LiteralText(checkedSupplier.get() ? "V" : "X");
	}

}
