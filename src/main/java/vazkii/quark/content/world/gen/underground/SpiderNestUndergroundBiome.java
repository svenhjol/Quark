package vazkii.quark.content.world.gen.underground;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator.Context;
import vazkii.quark.content.world.module.underground.SpiderNestUndergroundBiomeModule;

public class SpiderNestUndergroundBiome extends BasicUndergroundBiome {

	public SpiderNestUndergroundBiome() {
		super(Blocks.COBBLESTONE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState());
	}

	@Override
	public void fillCeiling(Context context, BlockPos pos, BlockState state) {
		super.fillCeiling(context, pos, state);
		placeCobweb(context, pos, Direction.DOWN, 0.1);
	}
	
	private void placeCobweb(Context context, BlockPos pos, Direction off, double chance) {
		if(context.random.nextDouble() < chance) {
			BlockPos placePos = off == null ? pos : pos.offset(off);
			context.world.setBlockState(placePos, Blocks.COBWEB.getDefaultState(), 2);
		}
	}
	
	@Override
	public void fillWall(Context context, BlockPos pos, BlockState state) {
		if(context.random.nextDouble() < 0.3)
			context.world.setBlockState(pos, SpiderNestUndergroundBiomeModule.cobbedstone.getDefaultState(), 2);
		else super.fillWall(context, pos, state);
	}
	
	@Override
	public void fillFloor(Context context, BlockPos pos, BlockState state) {
		if(context.random.nextDouble() < 0.3)
			context.world.setBlockState(pos, SpiderNestUndergroundBiomeModule.cobbedstone.getDefaultState(), 2);
		else super.fillFloor(context, pos, state);
		
		placeCobweb(context, pos, Direction.UP, 0.033);
	}
	
//	@Override
//	public boolean hasDungeon() {
//		return true;
//	}
//
//	@Override
//	public void spawnDungeon(ServerWorld world, BlockPos pos, Direction face) {
//		BlockPos spawnerPos = pos.offset(face);
//		world.setBlockState(spawnerPos, Blocks.MOB_SPAWNER.getDefaultState());
//		
//		Class<? extends Entity> e = SpiderEntity.class;
//		if(world.rand.nextDouble() < caveSpiderSpawnerChance)
//			e = CaveSpiderEntity.class;
//		MobSpawnerTileEntity spawner = (MobSpawnerTileEntity) world.getTileEntity(spawnerPos);
//		if (spawner != null)
//		spawner.getSpawnerBaseLogic().setEntityId(EntityList.getKey(e));
//		
//		int range = 3;
//		for(int x = -range; x < range + 1; x++)
//			for(int y = -range; y < range + 1; y++)
//				for(int z = -range; z < range + 1; z++) {
//					BlockPos cobwebPos = spawnerPos.add(x, y, z);
//					BlockState stateAt = world.getBlockState(cobwebPos);
//					if(stateAt.getBlock().isAir(stateAt, world, cobwebPos) || stateAt.getBlock().isReplaceable(world, cobwebPos))
//						placeCobweb(world, cobwebPos, null, nestCobwebChance);
//				}
//	}
	
}
