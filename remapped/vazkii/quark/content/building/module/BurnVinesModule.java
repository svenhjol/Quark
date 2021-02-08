package vazkii.quark.content.building.module;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.block.BurntVineBlock;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.BUILDING, hasSubscriptions = true)
public class BurnVinesModule extends QuarkModule {

	public static Block burnt_vine;
	
	@Override
	public void construct() {
		burnt_vine = new BurntVineBlock(this);
	}
	
	@SubscribeEvent
	public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		ItemStack stack = event.getItemStack();
		if(stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE) {
			BlockPos pos = event.getPos();
			World world = event.getWorld();
			BlockState state = world.getBlockState(pos);
			
			if(state.getBlock() == Blocks.VINE) {
				BlockState newState = burnt_vine.getDefaultState();
				Map<Direction, BooleanProperty> map = VineBlock.FACING_PROPERTIES;
				for(Direction d : map.keySet()) {
					BooleanProperty prop = map.get(d);
					newState = newState.with(prop, state.get(prop));
				}
				
				world.setBlockState(pos, newState);
				
				BlockPos testPos = pos.down();
				BlockState testState = world.getBlockState(testPos);
				while(testState.getBlock() == Blocks.VINE) {
					world.removeBlock(testPos, false);
					testPos = testPos.down();
					testState = world.getBlockState(testPos);
				}
				
				world.playSound(event.getPlayer(), pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 0.5F, 1F);
				if(world instanceof ServerWorld) {
					ServerWorld sworld = (ServerWorld) world;
					sworld.spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.25, 0.25, 0.25, 0.01);
					sworld.spawnParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.25, 0.25, 0.25, 0.01);
				}
				event.setCancellationResult(ActionResult.SUCCESS);
				event.setCanceled(true);
			}
		}
	}
	
}
