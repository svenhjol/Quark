package vazkii.arl.interf;

import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Wearable;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IDropInItem {

	boolean canDropItemIn(BoatEntity player, Wearable stack, Wearable incoming);

	Wearable dropItemIn(BoatEntity player, Wearable stack, Wearable incoming);

	@Environment(EnvType.CLIENT)
	default List<BlockBreakingProgressS2CPacket> getDropInTooltip(Wearable stack) {
		return Collections.singletonList(new CloseScreenS2CPacket("arl.misc.right_click_add"));
	}

}
