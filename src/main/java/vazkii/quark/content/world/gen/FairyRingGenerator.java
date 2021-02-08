package vazkii.quark.content.world.gen;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraftforge.common.Tags;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.Generator;
import vazkii.quark.content.world.module.FairyRingsModule;

public class FairyRingGenerator extends Generator {

	public FairyRingGenerator(DimensionConfig dimConfig) {
		super(dimConfig);
	}
	
	@Override
	public void generateChunk(WorldGenRegion worldIn, ChunkGenerator generator, Random rand, BlockPos corner) {
		int x = corner.getX() + rand.nextInt(16);
		int z = corner.getZ() + rand.nextInt(16);
		BlockPos center = new BlockPos(x, 128, z);
		
		Biome biome = getBiome(worldIn, center);
		
		Biome.Category category = biome.getCategory();
		double chance = 0;
		if(category == Category.FOREST)
			chance = FairyRingsModule.forestChance;
		else if(category == Category.PLAINS)
			chance = FairyRingsModule.plainsChance;
		
		if(rand.nextDouble() < chance) {
			BlockPos pos = center;
			BlockState state = worldIn.getBlockState(pos);
			
			while(state.getMaterial() != Material.ORGANIC && pos.getY() > 30) {
				pos = pos.down();
				state = worldIn.getBlockState(pos);
			}
			
			if(state.getMaterial() == Material.ORGANIC)
				spawnFairyRing(worldIn, pos.down(), rand);
		}		
	}
	
	public static void spawnFairyRing(IWorld world, BlockPos pos, Random rand) {
		BlockState flower = Blocks.OXEYE_DAISY.getDefaultState();
		
		for(int i = -3; i <= 3; i++)
			for(int j = -3; j <= 3; j++) {
				float dist = (i * i) + (j * j);
				if(dist < 7 || dist > 10)
					continue;
				
				for(int k = 5; k > -4; k--) {
					BlockPos fpos = pos.add(i, k, j);
					BlockPos fposUp = fpos.up();
					BlockState state = world.getBlockState(fpos);	
					if(state.getMaterial() == Material.ORGANIC && world.isAirBlock(fposUp)) {
						world.setBlockState(fpos.up(), flower, 2);
						break;
					}
				}
			}
		
		BlockPos orePos = pos.down(rand.nextInt(10) + 25);
		BlockState stoneState = world.getBlockState(orePos);
		int down = 0;
		while(!stoneState.getBlock().isIn(Tags.Blocks.STONE) && down < 10) {
			orePos = orePos.down();	
			stoneState = world.getBlockState(orePos);	
			down++;
		}
		
		if(stoneState.getBlock().isIn(Tags.Blocks.STONE)) {
			BlockState ore = FairyRingsModule.ores.get(rand.nextInt(FairyRingsModule.ores.size()));
			world.setBlockState(orePos, ore, 2);
			for(Direction face : Direction.values())
				if(rand.nextBoolean())
					world.setBlockState(orePos.offset(face), ore, 2);
		}
	}
	
}
