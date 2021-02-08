package vazkii.quark.content.world.gen.underground;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator.Context;

public class SandstoneUndergroundBiome extends BasicUndergroundBiome {

	public SandstoneUndergroundBiome() {
		super(Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState());
	}
	
	@Override
	public void fillCeiling(Context context, BlockPos pos, BlockState state) {
		if(context.random.nextDouble() < 0.1)
			context.world.setBlockState(pos.down(), ceilingState, 2);
		
		super.fillCeiling(context, pos, state);
	}
	
	@Override
	public void fillFloor(Context context, BlockPos pos, BlockState state) {
		if(context.random.nextBoolean()) {
			context.world.setBlockState(pos, Blocks.SAND.getDefaultState(), 2);
			if(context.random.nextDouble() < 0.05)
				context.world.setBlockState(pos.up(), Blocks.DEAD_BUSH.getDefaultState(), 2);
		} else super.fillFloor(context, pos, state);
	}
	
	@Override
	public void fillWall(Context context, BlockPos pos, BlockState state) {
		if(context.random.nextDouble() < 0.1)
			context.world.setBlockState(pos, Blocks.CHISELED_SANDSTONE.getDefaultState(), 2);
		else super.fillWall(context, pos, state);
	}
	
//	@Override
//	public boolean hasDungeon() {
//		return true;
//	}
//	
//	@Override
//	public void spawnDungeon(ServerWorld world, BlockPos pos, Direction side) {
//		if(side == null)
//			side = Direction.NORTH;
//		
//		switch(side) {
//		case NORTH:
//			pos = pos.add(3, -7, 6);
//			break;
//		case SOUTH:
//			pos = pos.add(-3, -7, -6);
//			break;
//		case EAST:
//			pos = pos.add(-6, -7, 3);
//			break;
//		case WEST:
//			pos = pos.add(6, -7, -3);
//			break;
//		default: break; 
//		}
//		
//		MinecraftServer server = world.getMinecraftServer();
//		Template template = world.getStructureTemplateManager().getTemplate(server, HUSK_GRAVE_STRUCTURE);
//		PlacementSettings settings = new PlacementSettings();
//		settings.setRotation(rotationFromFacing(side.getOpposite()));
//		
//		template.addBlocksToWorld(world, pos, settings);
//
//		Map<BlockPos, String> dataBlocks = template.getDataBlocks(pos, settings);
//		for(Entry<BlockPos, String> entry : dataBlocks.entrySet()) {
//			BlockPos dataPos = entry.getKey();
//			switch(entry.getValue()) {
//			case "spawner":
//				world.setBlockState(dataPos, Blocks.MOB_SPAWNER.getDefaultState(), 2);
//				TileEntity spawner = world.getTileEntity(dataPos);
//
//				if(spawner instanceof MobSpawnerTileEntity)
//					((MobSpawnerTileEntity) spawner).getSpawnerBaseLogic().setEntityId(EntityList.getKey(ZombieEntity.class));
//				break;
//			case "chest":
//				BlockState chestState = Blocks.CHEST.getDefaultState().withProperty(ChestBlock.FACING, Direction.WEST);
//				world.setBlockState(dataPos, chestState);
//
//				TileEntity chest = world.getTileEntity(dataPos);
//				if(chest instanceof LockableLootTileEntity)
//					((LockableLootTileEntity) chest).setLootTable(LootTables.CHESTS_DESERT_PYRAMID, world.rand.nextLong());
//				break;
//			}
//		}
//	}

}
