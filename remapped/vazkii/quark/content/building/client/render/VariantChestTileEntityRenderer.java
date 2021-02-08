package vazkii.quark.content.building.client.render;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.event.TextureStitchEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.client.render.GenericChestTERenderer;
import vazkii.quark.content.building.module.VariantChestsModule.IChestTextureProvider;

public class VariantChestTileEntityRenderer extends GenericChestTERenderer<ChestBlockEntity> {

	private static Map<Block, ChestTextureBatch> chestTextures = new HashMap<>();
	
	public static Block invBlock = null; 

	public VariantChestTileEntityRenderer(BlockEntityRenderDispatcher disp) {
		super(disp);
	}

	@Override
	public SpriteIdentifier getMaterial(ChestBlockEntity t, ChestType type) {
		Block block = invBlock;
		if(block == null)
			block = t.getCachedState().getBlock();
		
		ChestTextureBatch batch = chestTextures.get(block);
		if(batch == null)
			return null;
		
		switch(type) {
		case LEFT: return batch.left;
		case RIGHT: return batch.right;
		default: return batch.normal;
		}
	}

	public static void accept(TextureStitchEvent.Pre event, Block chest) {
		Identifier atlas = event.getMap().getId();

		if(chest instanceof IChestTextureProvider) {
			IChestTextureProvider prov = (IChestTextureProvider) chest;

			String path = prov.getChestTexturePath();
			if(!prov.isTrap())
				add(event, atlas, chest, path, "normal", "left", "right");
			else
				add(event, atlas, chest, path, "trap", "trap_left", "trap_right");
		}
	}

	private static void add(TextureStitchEvent.Pre event, Identifier atlas, Block chest, String path, String normal, String left, String right) {
		Identifier resNormal = new Identifier(Quark.MOD_ID, path + normal);
		Identifier resLeft = new Identifier(Quark.MOD_ID, path + left);
		Identifier resRight = new Identifier(Quark.MOD_ID, path + right);

		ChestTextureBatch batch = new ChestTextureBatch(atlas, resNormal, resLeft, resRight);
		chestTextures.put(chest, batch);

		event.addSprite(resNormal);
		event.addSprite(resLeft);
		event.addSprite(resRight);
	}

	private static class ChestTextureBatch {
		public final SpriteIdentifier normal, left, right;

		public ChestTextureBatch(Identifier atlas, Identifier normal, Identifier left, Identifier right) {
			this.normal = new SpriteIdentifier(atlas, normal);
			this.left = new SpriteIdentifier(atlas, left);
			this.right = new SpriteIdentifier(atlas, right);
		}

	}

}
