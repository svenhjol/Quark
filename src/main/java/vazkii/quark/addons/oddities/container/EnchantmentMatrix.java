package vazkii.quark.addons.oddities.container;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.registry.Registry;
import vazkii.quark.addons.oddities.module.MatrixEnchantingModule;

public class EnchantmentMatrix {
	
	public static final int MATRIX_WIDTH = 5;
	public static final int MATRIX_HEIGHT = 5;
	
	private static final int PIECE_VARIANTS = 8;
	
	private static final String TAG_PIECES = "pieces";
	private static final String TAG_PIECE_ID = "id";
	private static final String TAG_BENCHED_PIECES = "benchedPieces";
	private static final String TAG_PLACED_PIECES = "placedPieces";
	private static final String TAG_COUNT = "count";
	private static final String TAG_TYPE_COUNT = "typeCount";

	public final Map<Enchantment, Integer> totalValue = new HashMap<>();
	public final Map<Integer, Piece> pieces = new HashMap<>();
	public List<Integer> benchedPieces = new ArrayList<>();
	public List<Integer> placedPieces = new ArrayList<>();
	
	public int[][] matrix;
	public int count, typeCount;

	public final boolean book;
	public final ItemStack target;
	public final Random rng;
	
	public EnchantmentMatrix(ItemStack target, Random rng) {
		this.target = target;
		this.rng = rng;
		book = target.getItem() == Items.BOOK;
		computeMatrix();
	}

	public boolean canGeneratePiece(int bookshelfPower, int enchantability) {
		if(enchantability == 0)
			return false;
		
		if(book) {
			if(!MatrixEnchantingModule.allowBooks)
				return false;
			
			int bookshelfCount = Math.max(0, Math.min(bookshelfPower - 1, MatrixEnchantingModule.maxBookshelves)) / 7;
			int maxCount = MatrixEnchantingModule.baseMaxPieceCountBook + bookshelfCount;
			return count < maxCount;
		} else {
			int bookshelfCount = Math.min(bookshelfPower, MatrixEnchantingModule.maxBookshelves);
			int enchantabilityCount = Math.round((float) enchantability * ((float) bookshelfCount / (float) MatrixEnchantingModule.maxBookshelves));
			int maxCount = MatrixEnchantingModule.baseMaxPieceCount + ((bookshelfCount + 1) / 2) + (enchantabilityCount / 2);
			
			return count < maxCount;
		}
	}
	
	public boolean validateXp(PlayerEntity player, int bookshelfPower) {
		return player.isCreative() || (player.experienceLevel >= getMinXpLevel(bookshelfPower) && player.experienceLevel >= getNewPiecePrice());
	}
	
	public int getMinXpLevel(int bookshelfPower) {
		float scale = (float) MatrixEnchantingModule.minLevelScaleFactor;
		int cutoff = MatrixEnchantingModule.minLevelCutoff;
		
		if(book)
			return (int) (Math.min(bookshelfPower, MatrixEnchantingModule.maxBookshelves) * MatrixEnchantingModule.minLevelScaleFactorBook);
		else 
			return count > cutoff ? ((int) (cutoff * scale) - cutoff + count) : (int) (count * scale);
	}
	
	public int getNewPiecePrice() {
		return 1 + (MatrixEnchantingModule.piecePriceScale == 0 ? 0 : count / MatrixEnchantingModule.piecePriceScale); 
	}
	
	public boolean generatePiece(Map<Enchantment, Integer> influences, int bookshelfPower) {
		EnchantmentDataWrapper data = generateRandomEnchantment(influences, bookshelfPower);
		if (data == null)
			return false;
		
		int type = -1;
		for(Piece p : pieces.values())
			if(p.enchant == data.enchantment)
				type = p.type;
		
		if(type == -1) {
			type = typeCount % PIECE_VARIANTS;
			typeCount++;
		}

		Piece piece = new Piece(data, type);
		piece.generateBlocks();
		
		pieces.put(count, piece);
		totalValue.put(piece.enchant, totalValue.getOrDefault(piece.enchant, 0) + piece.getValue());
		benchedPieces.add(count);
		count++;
		
		if(book && count == 1) {
			for (int i = 0; i < 2; i++)
				if (rng.nextBoolean())
					count++;
		}

		return true;
	}
	
	private EnchantmentDataWrapper generateRandomEnchantment(Map<Enchantment, Integer> influences, int bookshelfPower) {
		int level = book ? (MatrixEnchantingModule.bookEnchantability + rng.nextInt(Math.max(1, bookshelfPower) * 2)) : 0;
		
		List<Piece> marked = pieces.values().stream().filter(p -> p.marked).collect(Collectors.toList());
		
		List<EnchantmentDataWrapper> validEnchants = new ArrayList<>();
		Registry.ENCHANTMENT.forEach(enchantment -> {
			if ((!enchantment.isTreasureEnchantment() || MatrixEnchantingModule.allowTreasures)
					&& !MatrixEnchantingModule.disallowedEnchantments.contains(Objects.toString(enchantment.getRegistryName()))
					&& (enchantment.canApplyAtEnchantingTable(target) || (book && enchantment.isAllowedOnBooks()))) {
				int enchantLevel = 1;
				if (book) {
					for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
						if (level >= enchantment.getMinEnchantability(i) && level <= enchantment.getMaxEnchantability(i)) {
							enchantLevel = i;
							break;
						}
					}
				}

				int valueAdded = getValue(enchantment, enchantLevel);
				int currentValue = totalValue.getOrDefault(enchantment, 0);
				
				if (valueAdded + currentValue > getValue(enchantment, enchantment.getMaxLevel()) + getMaxXP(enchantment, enchantment.getMaxLevel()))
					return;
				EnchantmentDataWrapper wrapper = new EnchantmentDataWrapper(enchantment, enchantLevel);
				wrapper.normalizeRarity(influences, marked);
				validEnchants.add(wrapper);
			}
		});

		if (validEnchants.isEmpty())
			return null;

		int total = 0;

		for (EnchantmentDataWrapper wrapper : validEnchants)
			total += wrapper.itemWeight;

		if (total == 0) {
			for (EnchantmentDataWrapper wrapper : validEnchants)
				wrapper.itemWeight++;
		} 

		return WeightedRandom.getRandomItem(rng, validEnchants);
	}
	
	public boolean place(int id, int x, int y) {
		Piece p = pieces.get(id);
		if(p != null && benchedPieces.contains(id) && canPlace(p, x, y)) {
			p.x = x;
			p.y = y;
			
			benchedPieces.remove((Integer) id);
			placedPieces.add(id);
			
			computeMatrix();
			return true;
		}
		
		return false;
	}
	
	public boolean remove(int id) {
		Piece p = pieces.get(id);
		if(p != null && placedPieces.contains(id)) {
			placedPieces.remove((Integer) id);
			benchedPieces.add(id);
			
			computeMatrix();
			return true;
		}
		
		return false;
	}
	
	public boolean rotate(int id) {
		Piece p = pieces.get(id);
		if(p != null && benchedPieces.contains(id)) {
			p.rotate();
			return true;
		}
		
		return false;
	}
	
	public boolean merge(int placed, int hover) {
		Piece placedPiece = pieces.get(placed);
		Piece hoveredPiece = pieces.get(hover);
		if(placedPiece != null && hoveredPiece != null && placedPieces.contains(placed) && benchedPieces.contains(hover)) {
			Enchantment enchant = placedPiece.enchant;
			if(hoveredPiece.enchant == enchant && placedPiece.level < enchant.getMaxLevel()) {
				placedPiece.xp += hoveredPiece.getValue();

				int max = placedPiece.getMaxXP();
				while(placedPiece.xp >= max) {
					if(placedPiece.level >= enchant.getMaxLevel())
						break;
					
					placedPiece.level++;
					placedPiece.xp -= max;
					max = placedPiece.getMaxXP();
				}

				if(hoveredPiece.marked)
					placedPiece.marked = true;
				
				benchedPieces.remove((Integer) hover);
				pieces.remove(hover);
				return true;
			}
		}
		
		return false;
	}
	
	public void writeToNBT(CompoundNBT cmp) {
		ListNBT list = new ListNBT();
		for(Integer i : pieces.keySet()) {
			CompoundNBT pieceTag = new CompoundNBT();
			
			pieceTag.putInt(TAG_PIECE_ID, i);
			if (pieces.get(i).enchant != null) {
				pieces.get(i).writeToNBT(pieceTag);
				list.add(pieceTag);
			}
		}
		
		cmp.put(TAG_PIECES, list);
		cmp.putIntArray(TAG_BENCHED_PIECES, packList(benchedPieces));
		cmp.putIntArray(TAG_PLACED_PIECES, packList(placedPieces));
		cmp.putInt(TAG_COUNT, count);
		cmp.putInt(TAG_TYPE_COUNT, typeCount);
	}
	
	public void readFromNBT(CompoundNBT cmp) {
		pieces.clear();
		totalValue.clear();
		ListNBT plist = cmp.getList(TAG_PIECES, cmp.getId());
		for(int i = 0; i < plist.size(); i++) {
			CompoundNBT pieceTag = plist.getCompound(i);
			
			int id = pieceTag.getInt(TAG_PIECE_ID);
			Piece piece = new Piece();
			piece.readFromNBT(pieceTag);
			pieces.put(id, piece);
			totalValue.put(piece.enchant, totalValue.getOrDefault(piece.enchant, 0) + piece.getValue());
		}
		
		benchedPieces = unpackList(cmp.getIntArray(TAG_BENCHED_PIECES));
		placedPieces = unpackList(cmp.getIntArray(TAG_PLACED_PIECES));
		count = cmp.getInt(TAG_COUNT);
		typeCount = cmp.getInt(TAG_TYPE_COUNT);

		computeMatrix();
	}
	
	private void computeMatrix() {
		matrix = new int[MATRIX_WIDTH][MATRIX_HEIGHT];
		
		for(int i = 0; i < MATRIX_WIDTH; i++)
			for(int j = 0; j < MATRIX_HEIGHT; j++)
				matrix[i][j] = -1;
		
		for(Integer i : placedPieces) {
			Piece p = pieces.get(i);
			for(int[] b : p.blocks)
				matrix[p.x + b[0]][p.y + b[1]] = i;
		}
	}
	
	public boolean canPlace(Piece p, int x, int y) {
		for(int[] b : p.blocks) {
			int bx = b[0] + x;
			int by = b[1] + y;
			if(bx < 0 || by < 0 || bx >= MATRIX_WIDTH || by >= MATRIX_HEIGHT)
				return false;
			
			if(matrix[bx][by] != -1)
				return false;
		}
		
		return true;
	}
	
	private int[] packList(List<Integer> list) {
		int[] arr = new int[list.size()];
		for(int i = 0; i < arr.length; i++)
			arr[i] = list.get(i);
		return arr;
	}
	
	private List<Integer> unpackList(int[] arr) {
		List<Integer> list = new ArrayList<>(arr.length);
		for (int anArr : arr) list.add(anArr);
		
		return list;
	}

	public static int getMaxXP(Enchantment enchantment, int level) {
		if(level >= enchantment.getMaxLevel())
			return 0;

		switch(enchantment.getRarity()) {
			case COMMON:
				return level;
			case UNCOMMON:
				return level / 2 + 1;
			default:
				return 1;
		}
	}

	public static int getValue(Enchantment enchantment, int level) {
		int total = 1;
		for (int i = 1; i < level; i++)
			total += getMaxXP(enchantment, i);
		return total;
	}
	
	public static class Piece {
		
		private static final int[][][] PIECE_TYPES = new int[][][] {
			{{0,0},	{-1,0},	{1,0},	{0,-1},	{0,1}}, // Plus
			{{0,0},	{-1,0},	{1,0},	{-1,-1},{0,-1}}, // Block
			{{0,0},	{-1,0},	{1,0},	{-1,1},	{1,1}}, // U
			{{0,0}, {-1,0},	{1,0},	{-1,-1},{1,1}}, // S
			{{0,0}, {-1,0},	{1,0},	{1,-1},	{1,1}}, // T
			{{0,0}, {-1,0},	{1,0},	{0,-1},	{1,1}}, // Twig
			{{0,0}, {-1,0},	{0,-1},	{-1,-1},{1,1}}, // Squiggle
			{{0,0},	{-1,0},	{1,0},	{0,-1},	{0,1},	{1,1}}, // Fish
			{{0,0}, {-1,0},	{0,-1},	{-1,-1},{-1,1},	{1,-1}}, // Stairs
			{{0,0},	{-1,0},	{0,-1},	{-1,-1},{-1,1},	{1,1}}, // J
			{{0,0},	{-1,0},	{1,0},	{-1,-1},{1,-1},	{1,1}}, // H
			{{0,0},	{-1,0},	{1,0},	{0,-1},	{-1,-1}, {1,1}} // weird block thing idk
		};
		
		private static final String TAG_COLOR = "color";
		private static final String TAG_TYPE = "type";
		private static final String TAG_ENCHANTMENT = "enchant";
		private static final String TAG_LEVEL = "level";
		private static final String TAG_BLOCK_COUNT = "blockCount";
		private static final String TAG_BLOCK = "block";
		private static final String TAG_X = "x";
		private static final String TAG_Y = "y";
		private static final String TAG_XP = "xp";
		private static final String TAG_MARKED = "marked";
		private static final String TAG_INFLUENCE = "influence";

		public Enchantment enchant;
		public int level, color, type, x, y, xp;
		public int[][] blocks;
		public boolean marked;
		public int influence;
		
		public Piece() { }
		
		public Piece(EnchantmentDataWrapper wrapper, int type) {
			this.enchant = wrapper.enchantment;
			this.level = wrapper.enchantmentLevel;
			this.marked = wrapper.marked;
			this.influence = wrapper.influence;
			this.type = type;
			
			Random rng = new Random(Objects.toString(enchant.getRegistryName()).hashCode());
			float h = rng.nextFloat();
			float s = rng.nextFloat() * 0.2F + 0.8F;
			float b = rng.nextFloat() * 0.25F + 0.75F;
			this.color = Color.HSBtoRGB(h, s, b);
		}
		
		public void generateBlocks() {
			int type = (int) (Math.random() * PIECE_TYPES.length);
			int[][] copyPieces = PIECE_TYPES[type];
			blocks = new int[copyPieces.length][2];
			
			for(int i = 0; i < blocks.length; i++) {
				blocks[i][0] = copyPieces[i][0]; 
				blocks[i][1] = copyPieces[i][1];
			}
			
			int rotations = (int) (Math.random() * 4);
			for(int i = 0; i < rotations; i++)
				rotate();
		}
		
		public void rotate() {
			for (int[] b : blocks) {
				int x = b[0];
				int y = b[1];
				b[0] = y;
				b[1] = -x;
			}
		}
		
		public int getMaxXP() {
			return EnchantmentMatrix.getMaxXP(enchant, level);
		}

		public int getValue() {
			return EnchantmentMatrix.getValue(enchant, level) + xp;
		}
		
		public void writeToNBT(CompoundNBT cmp) {
			cmp.putInt(TAG_COLOR, color);
			cmp.putInt(TAG_TYPE, type);
			if (enchant != null)
				cmp.putString(TAG_ENCHANTMENT, Objects.toString(enchant.getRegistryName()));
			cmp.putInt(TAG_LEVEL, level);
			cmp.putInt(TAG_X, x);
			cmp.putInt(TAG_Y, y);
			cmp.putInt(TAG_XP, xp);
			cmp.putBoolean(TAG_MARKED, marked);
			cmp.putInt(TAG_INFLUENCE, influence);

			cmp.putInt(TAG_BLOCK_COUNT, blocks.length);
			for(int i = 0; i < blocks.length; i++)
				cmp.putIntArray(TAG_BLOCK + i, blocks[i]);
		}
		
		public void readFromNBT(CompoundNBT cmp) {
			color = cmp.getInt(TAG_COLOR);
			type = cmp.getInt(TAG_TYPE);
			enchant = Registry.ENCHANTMENT.getOrDefault(new ResourceLocation(cmp.getString(TAG_ENCHANTMENT)));
			level = cmp.getInt(TAG_LEVEL);
			x = cmp.getInt(TAG_X);
			y = cmp.getInt(TAG_Y);
			xp = cmp.getInt(TAG_XP);
			marked = cmp.getBoolean(TAG_MARKED);
			influence = cmp.getInt(TAG_INFLUENCE);
			
			blocks = new int[cmp.getInt(TAG_BLOCK_COUNT)][2];
			for(int i = 0; i < blocks.length; i++)
				blocks[i] = cmp.getIntArray(TAG_BLOCK + i);
		}
	}
	
	private static class EnchantmentDataWrapper extends EnchantmentData {

		private boolean marked;
		private int influence;
		
		public EnchantmentDataWrapper(Enchantment enchantmentObj, int enchLevel) {
			super(enchantmentObj, enchLevel);
		}
		
		public void normalizeRarity(Map<Enchantment, Integer> influences, List<Piece> markedEnchants) {
			if(MatrixEnchantingModule.normalizeRarity) {
				switch(enchantment.getRarity()) {
				case COMMON:
					itemWeight = 80000;
					break;
				case UNCOMMON:
					itemWeight = 40000;
					break;
				case RARE:
					itemWeight = 25000;
					break;
				case VERY_RARE:
					itemWeight = 5000; 
					break;
				default: 
					break;
				}
				
				influence = influences.getOrDefault(enchantment, 0);
				float multiplier = 1F + influence * (float) MatrixEnchantingModule.influencePower;
				itemWeight *= multiplier;
				
				boolean mark = true;
				
				for(Piece other : markedEnchants) {
					if(other.enchant == enchantment) {
						itemWeight *= MatrixEnchantingModule.dupeMultiplier;
						mark = false;
						break;
					} else if(!other.enchant.isCompatibleWith(enchantment) || !enchantment.isCompatibleWith(other.enchant)) {
						itemWeight *= MatrixEnchantingModule.incompatibleMultiplier;
						mark = false;
						break;
					}
				}
				
				if(mark)
					marked = true;
			}
		}
		
	}
	
}
