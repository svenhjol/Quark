package vazkii.quark.content.experimental.module;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.GenerationStage;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.util.WeightedSelector;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.BiomeTypeConfig;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.CombinedGenerator;
import vazkii.quark.content.world.config.UndergroundBiomeConfig;
import vazkii.quark.content.world.gen.UndergroundBiomeGenerator;
import vazkii.quark.content.world.gen.underground.CustomUndergroundBiome;

/**
 * @author WireSegal
 * Created at 8:01 PM on 10/1/19.
 */
@LoadModule(category = ModuleCategory.EXPERIMENTAL, enabledByDefault = false, description = "This feature is highly technical. Use only if you know what you're doing!")
public class CustomUndergroundBiomeModule extends QuarkModule {

    private static final String BIOME_OPTIONS = "HOT|COLD|SPARSE|DENSE|WET|SAVANNA|CONIFEROUS|JUNGLE|SPOOKY|DEAD|LUSH|NETHER|END|MUSHROOM|MAGICAL|RARE|OCEAN|RIVER|WATER|MESA|FOREST|PLAINS|MOUNTAIN|HILLS|SWAMP|SANDY|SNOWY|WASTELAND|BEACH|VOID";
    private static final String RL_PATTERN = "(?:\\w+:)?\\w+";
    private static final String BLOCKS_PATTERN = "(?:" + RL_PATTERN + "(?:@\\d+)?,)" + RL_PATTERN + "(?:@\\d+)?";

    public static final Pattern PATTERN = Pattern.compile("(?<dimensions>(?:(?:" + RL_PATTERN + ",)*" + RL_PATTERN + ")?);" +
            "(?<isDimensionBlacklist>true|false);" +
            "(?<biomeTypes>(?:(?:" + BIOME_OPTIONS + ",)*(?:" + BIOME_OPTIONS + "))?);" +
            "(?<isBiomeBlacklist>true|false);" +
            "(?<rarity>\\d+);" +
            "(?<minY>\\d+)\\.\\.(?<maxY>\\d+);" +
            "(?<horizontalSize>\\d+),(?<verticalSize>\\d+);" +
            "(?<horizontalVariation>\\d+),(?<verticalVariation>\\d+);" +
            "(?<floorBlocks>" + BLOCKS_PATTERN + ");" +
            "(?<ceilBlocks>" + BLOCKS_PATTERN + "|FLOOR);" +
            "(?<wallBlocks>" + BLOCKS_PATTERN + "|CEIL(?:ING)?|FLOOR);" +
            "(?<mimicInside>true|false);?");

    @Config(description = "The format for these definitions is:\n" +
            "dimensions;isDimensionBlacklist;biomeTypews;isBiomeBlacklist;rarity;minY..maxY;horizontalSize,verticalSize;horizontalVariation,verticalVariation;floorBlocks@weight;ceilingBlocks@weight;wallBlocks@weight;mimicInside\n" +
            "That's a lot to take in, so here's an example. This would be for the default config of the Slime underground biome. (Spaces are allowed.)\n" +
            "minecraft:overworld; false; SWAMP; false; 120; 10..40; 26,14; 14,6; minecraft:water@915, minecraft:slime_block@85; minecraft:green_terracotta@2, minecraft:lime_terracotta@3, minecraft:light_blue_terracotta@1; CEILING; false")
    public static List<String> biomeDefinitions = new ArrayList<>();

    private static List<UndergroundBiomeGenerator> generators = new ArrayList<>();

    @Override
    public void configChanged() {
        generators.clear();
        for (String definition : biomeDefinitions) {
            String strippedDef = definition.replaceAll("\\s", "");
            Matcher match = PATTERN.matcher(strippedDef);
            if (match.matches()) {
                DimensionConfig dimensions = extractDimensions(match.group("dimensions"),
                        match.group("isDimensionBlacklist"));
                BiomeTypeConfig biomes = extractBiomes(match.group("biomeTypes"),
                        match.group("isBiomeBlacklist"));
                int rarity = Integer.parseInt(match.group("rarity"));
                int minY = Integer.parseInt(match.group("minY"));
                int maxY = Integer.parseInt(match.group("maxY"));
                int horizontalSize = Integer.parseInt(match.group("horizontalSize"));
                int verticalSize = Integer.parseInt(match.group("verticalSize"));
                int horizontalVariation = Integer.parseInt(match.group("horizontalVariation"));
                int verticalVariation = Integer.parseInt(match.group("verticalVariation"));

                WeightedSelector<BlockState> floor = extractBlocks(match.group("floorBlocks"));

                WeightedSelector<BlockState> ceil;

                String ceilBlocks = match.group("ceilBlocks");
                switch (ceilBlocks) {
                    case "FLOOR":
                        ceil = floor.copy();
                        break;
                    default:
                        ceil = extractBlocks(ceilBlocks);
                }

                WeightedSelector<BlockState> wall;

                String wallBlocks = match.group("wallBlocks");
                switch (wallBlocks) {
                    case "FLOOR":
                        wall = floor.copy();
                        break;
                    case "CEIL":
                    case "CEILING":
                        wall = ceil.copy();
                        break;
                    default:
                        wall = extractBlocks(wallBlocks);
                        break;
                }

                boolean mimicInside = Boolean.parseBoolean(match.group("mimicInside"));

                CustomUndergroundBiome biome = new CustomUndergroundBiome(floor, ceil, wall, mimicInside);

                UndergroundBiomeConfig config = new UndergroundBiomeConfig(biome, Math.max(0, rarity));
                config.dimensions = dimensions;
                config.biomes = biomes;
                config.minYLevel = MathHelper.clamp(minY, 0, 255);
                config.maxYLevel = MathHelper.clamp(maxY, 0, 255);
                config.horizontalSize = Math.max(0, horizontalSize);
                config.verticalSize = Math.max(0, verticalSize);
                config.horizontalVariation = Math.max(0, horizontalVariation);
                config.verticalVariation = Math.max(0, verticalVariation);

                generators.add(new UndergroundBiomeGenerator(config, strippedDef));
            }
        }
    }

    private BiomeTypeConfig extractBiomes(String biomeTypes, String isBiomeBlacklist) {
        return new BiomeTypeConfig(Boolean.parseBoolean(isBiomeBlacklist), biomeTypes.split(","));
    }

    private DimensionConfig extractDimensions(String dimensions, String isDimensionBlacklist) {
        return new DimensionConfig(Boolean.parseBoolean(isDimensionBlacklist), dimensions.split(","));
    }

    private WeightedSelector<BlockState> extractBlocks(String blockString) {
        WeightedSelector<BlockState> list = new WeightedSelector<>();
        String[] entries = blockString.split(",");
        for (String entry : entries) {
            String[] split = entry.split("@");
            ResourceLocation loc = new ResourceLocation(split[0]);
            int weight = split.length == 1 ? 1 : Integer.parseInt(split[1]);
    
            Registry.BLOCK.getOptional(loc).ifPresent(block -> list.add(block.getDefaultState(), weight));
        }

        return list;
    }

    @Override
    public void setup() {
        WorldGenHandler.addGenerator(this, new CombinedGenerator(generators), GenerationStage.Decoration.UNDERGROUND_DECORATION, WorldGenWeights.UNDERGROUND_BIOMES);
    }
}
