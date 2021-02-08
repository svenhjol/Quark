package vazkii.quark.content.world.gen.structure.processor;

import java.util.Random;

import net.minecraft.block.SpawnerBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.spawner.AbstractSpawner;
import vazkii.quark.content.world.gen.structure.BigDungeonStructure;

public class BigDungeonSpawnerProcessor extends StructureProcessor {
	
    public BigDungeonSpawnerProcessor() { 
    	// NO-OP
    }
    
    @Override
    public BlockInfo process(IWorldReader worldReaderIn, BlockPos pos, BlockPos otherposidk, BlockInfo p_215194_3_, BlockInfo blockInfo, PlacementSettings placementSettingsIn, Template template) {
    	if(blockInfo.state.getBlock() instanceof SpawnerBlock) {
    		Random rand = placementSettingsIn.getRandom(blockInfo.pos);
    		TileEntity tile = TileEntity.readTileEntity(blockInfo.state, blockInfo.nbt);
    		
    		if(tile instanceof MobSpawnerTileEntity) {
    			MobSpawnerTileEntity spawner = (MobSpawnerTileEntity) tile;
    			AbstractSpawner logic = spawner.getSpawnerBaseLogic();
    			
    			double val = rand.nextDouble();
    			if(val > 0.95)
    				logic.setEntityType(EntityType.CREEPER);
    			else if(val > 0.5)
    				logic.setEntityType(EntityType.SKELETON);
    			else logic.setEntityType(EntityType.ZOMBIE);
    			
    			CompoundNBT nbt = new CompoundNBT();
    			spawner.write(nbt);
    			return new BlockInfo(blockInfo.pos, blockInfo.state, nbt);
    		}
    	}
    	
    	return blockInfo;
    }
    
	@Override
	protected IStructureProcessorType<?> getType() {
		return BigDungeonStructure.SPAWN_PROCESSOR_TYPE;
	}

}
