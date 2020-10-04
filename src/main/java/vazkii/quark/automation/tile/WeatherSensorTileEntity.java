package vazkii.quark.automation.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Tickable;
import vazkii.quark.automation.block.WeatherSensorBlock;
import vazkii.quark.automation.module.WeatherSensorModule;

/**
 * @author WireSegal
 * Created at 9:12 AM on 8/26/19.
 */
public class WeatherSensorTileEntity extends BlockEntity implements Tickable {
    public WeatherSensorTileEntity() {
        super(WeatherSensorModule.weatherSensorTEType);
    }

    @Override
    public void tick() {
        if (this.world != null && !this.world.isClient && this.world.getTime() % 20L == 0L) {
            BlockState state = this.getCachedState();
            Block block = state.getBlock();
            if (block instanceof WeatherSensorBlock) {
                WeatherSensorBlock.updatePower(state, this.world, this.pos);
            }
        }

    }
}
