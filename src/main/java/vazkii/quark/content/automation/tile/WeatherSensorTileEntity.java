package vazkii.quark.content.automation.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import vazkii.quark.content.automation.block.WeatherSensorBlock;
import vazkii.quark.content.automation.module.WeatherSensorModule;

/**
 * @author WireSegal
 * Created at 9:12 AM on 8/26/19.
 */
public class WeatherSensorTileEntity extends TileEntity implements ITickableTileEntity {
    public WeatherSensorTileEntity() {
        super(WeatherSensorModule.weatherSensorTEType);
    }

    @Override
    public void tick() {
        if (this.world != null && !this.world.isRemote && this.world.getGameTime() % 20L == 0L) {
            BlockState state = this.getBlockState();
            Block block = state.getBlock();
            if (block instanceof WeatherSensorBlock) {
                WeatherSensorBlock.updatePower(state, this.world, this.pos);
            }
        }

    }
}
