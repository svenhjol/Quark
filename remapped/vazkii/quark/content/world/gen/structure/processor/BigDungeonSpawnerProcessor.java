package vazkii.quark.content.world.gen.structure.processor;

import java.util.Random;

import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.Structure;
import net.minecraft.structure.Structure.StructureBlockInfo;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.WorldView;
import vazkii.quark.content.world.gen.structure.BigDungeonStructure;

public class BigDungeonSpawnerProcessor extends StructureProcessor {
	
    public BigDungeonSpawnerProcessor() { 
    	// NO-OP
    }
    
    @Override
    public StructureBlockInfo process(WorldView worldReaderIn, BlockPos pos, BlockPos otherposidk, StructureBlockInfo p_215194_3_, StructureBlockInfo blockInfo, StructurePlacementData placementSettingsIn, Structure template) {
    	if(blockInfo.state.getBlock() instanceof SpawnerBlock) {
    		Random rand = placementSettingsIn.getRandom(blockInfo.pos);
    		BlockEntity tile = BlockEntity.createFromTag(blockInfo.state, blockInfo.tag);
    		
    		if(tile instanceof MobSpawnerBlockEntity) {
    			MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) tile;
    			MobSpawnerLogic logic = spawner.getLogic();
    			
    			double val = rand.nextDouble();
    			if(val > 0.95)
    				logic.setEntityId(EntityType.CREEPER);
    			else if(val > 0.5)
    				logic.setEntityId(EntityType.SKELETON);
    			else logic.setEntityId(EntityType.ZOMBIE);
    			
    			CompoundTag nbt = new CompoundTag();
    			spawner.toTag(nbt);
    			return new StructureBlockInfo(blockInfo.pos, blockInfo.state, nbt);
    		}
    	}
    	
    	return blockInfo;
    }
    
	@Override
	protected StructureProcessorType<?> getType() {
		return BigDungeonStructure.SPAWN_PROCESSOR_TYPE;
	}

}
