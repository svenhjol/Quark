package vazkii.quark.world.gen;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.Generator;
import vazkii.quark.building.module.CompressedBlocksModule;
import vazkii.quark.world.module.NetherObsidianSpikesModule;

public class ObsidianSpikeGenerator extends Generator {

	public ObsidianSpikeGenerator(DimensionConfig dimConfig) {
		super(dimConfig);
	}

	@Override
	public void generateChunk(ChunkRegion world, ChunkGenerator generator, StructureAccessor structureManager, Random rand, BlockPos chunkCorner) {
		if(rand.nextFloat() < NetherObsidianSpikesModule.chancePerChunk) {
			for(int i = 0; i < NetherObsidianSpikesModule.triesPerChunk; i++) {
				BlockPos pos = chunkCorner.add(rand.nextInt(16), 50, rand.nextInt(16));
				
				while(pos.getY() > 10) {
					BlockState state = world.getBlockState(pos);
					if(state.getBlock() == Blocks.LAVA) {
						placeSpikeAt(world, pos, rand);
						break;
					}
					pos = pos.down();
				}
			}
		}
	}
	
	public static void placeSpikeAt(WorldAccess world, BlockPos pos, Random rand) {
		int heightBelow = 10;
		int heightBottom = 3 + rand.nextInt(3);
		int heightMiddle = 2 + rand.nextInt(4);
		int heightTop = 2 + rand.nextInt(3);
		
		boolean addSpawner = false;
		if(rand.nextFloat() < NetherObsidianSpikesModule.bigSpikeChance) {
			heightBottom += 7;
			heightMiddle += 8;
			heightTop += 4;
			addSpawner = NetherObsidianSpikesModule.bigSpikeSpawners;
		}
		
		int checkHeight = heightBottom + heightMiddle + heightTop + 2;
		for(int i = 0; i < 5; i++)
			for(int j = 0; j < 5; j++)
				for(int k = 0; k < checkHeight; k++) {
					BlockPos checkPos = pos.add(i - 2, k, j - 2);
					if(!(world.isAir(checkPos) || world.getBlockState(checkPos).getMaterial() == Material.LAVA))
						return;
				}
		
		BlockState obsidian = Blocks.OBSIDIAN.getDefaultState();
		
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				for(int k = 0; k < heightBottom + heightBelow; k++) {
					BlockPos placePos = pos.add(i - 1, k - heightBelow, j - 1);

					if(world.getBlockState(placePos).getHardness(world, placePos) != -1)
						world.setBlockState(placePos, obsidian, 0);
				}
		
		for(int i = 0; i < heightMiddle; i++) {
			BlockPos placePos = pos.add(0, heightBottom + i, 0);
			
			world.setBlockState(placePos, obsidian, 0);
			for(Direction face : MiscUtil.HORIZONTALS)
				world.setBlockState(placePos.offset(face), obsidian, 0);
		}
		
		for(int i = 0; i < heightTop; i++) {
			BlockPos placePos = pos.add(0, heightBottom + heightMiddle + i, 0);
			world.setBlockState(placePos, obsidian, 0);
			
			if(addSpawner && i == 0) {
				boolean useBlazeLantern = ModuleLoader.INSTANCE.isModuleEnabled(CompressedBlocksModule.class) && CompressedBlocksModule.enableBlazeLantern;
				world.setBlockState(placePos, useBlazeLantern ? CompressedBlocksModule.blaze_lantern.getDefaultState() : Blocks.GLOWSTONE.getDefaultState(), 0);
				
				placePos = placePos.down();
				world.setBlockState(placePos, Blocks.SPAWNER.getDefaultState(), 0);
				((MobSpawnerBlockEntity) world.getBlockEntity(placePos)).getLogic().setEntityId(EntityType.BLAZE);
				
				placePos = placePos.down();
				world.setBlockState(placePos, Blocks.CHEST.getDefaultState(), 0);
				((ChestBlockEntity) world.getBlockEntity(placePos)).setLootTable(new Identifier("minecraft", "chests/nether_bridge"), rand.nextLong());
			}
		}
	}

}
