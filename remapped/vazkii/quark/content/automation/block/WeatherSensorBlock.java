package vazkii.quark.content.automation.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.automation.tile.WeatherSensorTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author WireSegal
 * Created at 9:01 AM on 8/26/19.
 */
public class WeatherSensorBlock extends QuarkBlock {
    public static final IntProperty POWER = IntProperty.of("power", 0, 2);
    public static final BooleanProperty INVERTED = Properties.INVERTED;
    public static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);

    public WeatherSensorBlock(String regname, QuarkModule module, ItemGroup creativeTab, Settings properties) {
        super(regname, module, creativeTab, properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWER, 0).with(INVERTED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWER, INVERTED);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockView world) {
        return new WeatherSensorTileEntity();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext selection) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasSidedTransparency(BlockState p_220074_1_) { // blocksLight
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction face) {
        return (int) (state.get(POWER) * 7.5f);
    }

    public static void updatePower(BlockState state, World world, BlockPos pos) {
        if (world.getDimension().hasSkyLight()) {
            boolean inverted = state.get(INVERTED);

            if (world.isThundering())
                world.setBlockState(pos, state.with(POWER, inverted ? 0 : 2));
            else if (world.isRaining() && world.getBiome(pos).getPrecipitation() != Biome.Precipitation.NONE)
                world.setBlockState(pos, state.with(POWER, 1));
            else
                world.setBlockState(pos, state.with(POWER, inverted ? 2 : 0));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult ray) {
        if (player.canModifyBlocks()) {
            if (world.isClient) {
                return ActionResult.SUCCESS;
            } else {
                BlockState inverted = state.cycle(INVERTED); // cycle
                world.setBlockState(pos, inverted, 4);
                updatePower(inverted, world, pos);
                return ActionResult.SUCCESS;
            }
        } else {
            return super.onUse(state, world, pos, player, hand, ray);
        }
    }
}
