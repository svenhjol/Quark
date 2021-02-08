package vazkii.quark.content.client.tooltip;

import java.util.List;

import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TextFormatting;

/**
 * @author WireSegal
 * Created at 10:40 AM on 9/1/19.
 */
public class TooltipUtils {

    public static int shiftTextByLines(List<? extends ITextProperties> lines, int y) {
        for(int i = 1; i < lines.size(); i++) {
            String s = lines.get(i).getString();
            s = TextFormatting.getTextWithoutFormattingCodes(s);
            if(s != null && s.trim().isEmpty()) {
                y += 10 * (i - 1) + 1;
                break;
            }
        }
        return y;
    }
}
