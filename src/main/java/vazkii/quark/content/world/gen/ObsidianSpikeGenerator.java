package vazkii.quark.content.world.gen;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.Generator;
import vazkii.quark.content.building.module.CompressedBlocksModule;
import vazkii.quark.content.world.module.NetherObsidianSpikesModule;

public class ObsidianSpikeGenerator extends Generator {

	public ObsidianSpikeGenerator(DimensionConfig dimConfig) {
		super(dimConfig);
	}

	@Override
	public void generateChunk(WorldGenRegion world, ChunkGenerator generator, Random rand, BlockPos chunkCorner) {
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
	
	public static void placeSpikeAt(IWorld world, BlockPos pos, Random rand) {
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
					if(!(world.isAirBlock(checkPos) || world.getBlockState(checkPos).getMaterial() == Material.LAVA))
						return;
				}
		
		BlockState obsidian = Blocks.OBSIDIAN.getDefaultState();
		
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				for(int k = 0; k < heightBottom + heightBelow; k++) {
					BlockPos placePos = pos.add(i - 1, k - heightBelow, j - 1);

					if(world.getBlockState(placePos).getBlockHardness(world, placePos) != -1)
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
				((MobSpawnerTileEntity) world.getTileEntity(placePos)).getSpawnerBaseLogic().setEntityType(EntityType.BLAZE);
				
				placePos = placePos.down();
				world.setBlockState(placePos, Blocks.CHEST.getDefaultState(), 0);
				((ChestTileEntity) world.getTileEntity(placePos)).setLootTable(new ResourceLocation("minecraft", "chests/nether_bridge"), rand.nextLong());
			}
		}
	}

}
