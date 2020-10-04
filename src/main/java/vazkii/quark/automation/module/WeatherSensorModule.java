package vazkii.quark.automation.module;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.automation.block.WeatherSensorBlock;
import vazkii.quark.automation.tile.WeatherSensorTileEntity;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

/**
 * @author WireSegal
 * Created at 9:11 AM on 8/26/19.
 */
@LoadModule(category = ModuleCategory.AUTOMATION)
public class WeatherSensorModule extends Module {
    public static BlockEntityType<WeatherSensorTileEntity> weatherSensorTEType;

    @Override
    public void construct() {
        Block weatherSensor = new WeatherSensorBlock("weather_sensor", this, ItemGroup.REDSTONE,
                Block.Properties.of(Material.STONE, MaterialColor.MAGENTA)
                .requiresTool() // needs tool
        		.harvestTool(ToolType.PICKAXE)
        		.strength(0.2F));
        
        weatherSensorTEType = BlockEntityType.Builder.create(WeatherSensorTileEntity::new, weatherSensor).build(null);
        RegistryHelper.register(weatherSensorTEType, "weather_sensor");
    }

}
