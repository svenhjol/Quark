package vazkii.quark.api.config;

import java.io.PrintStream;
import java.util.List;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Environment(EnvType.CLIENT)
public interface IConfigElement extends Comparable<IConfigElement> {

	public String getName();
	public String getGuiDisplayName();
	public List<String> getTooltip();
	public String getSubtitle();
	public @Nullable IConfigCategory getParent();
	public boolean isDirty();
	public void clean();
	
	public void refresh();
	public void reset(boolean hard);
	public void print(String pad, PrintStream out);
	
}
