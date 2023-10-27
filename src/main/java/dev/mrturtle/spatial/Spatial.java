package dev.mrturtle.spatial;

import dev.mrturtle.spatial.config.ConfigManager;
import dev.mrturtle.spatial.inventory.InventoryShape;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Spatial implements ModInitializer {
	private static final HashMap<Identifier, InventoryShape> shapes = new HashMap<>();

    public static final Logger LOGGER = LoggerFactory.getLogger("Spatial");

	@Override
	public void onInitialize() {
		ConfigManager.loadConfig();
		ServerLifecycleEvents.SERVER_STARTED.register(Spatial::loadShapes);
	}

	public static InventoryShape getShape(ItemStack stack) {
		Identifier ID = Registries.ITEM.getId(stack.getItem());
		if (shapes.containsKey(ID))
			return shapes.get(ID);
		else
			return new InventoryShape();
	}

	private static void loadShapes(MinecraftServer server) {
		shapes.clear();
		for (RecipeEntry<?> entry : server.getRecipeManager().values()) {
			Recipe<?> recipe = entry.value();
			ItemStack output = recipe.getResult(server.getRegistryManager());
			InventoryShape shape = null;
			if (recipe instanceof ShapedRecipe shapedRecipe) {
				DefaultedList<Ingredient> ingredients = shapedRecipe.getIngredients();
				shape = InventoryShape.fromIngredientList(ingredients, shapedRecipe.getWidth(), shapedRecipe.getHeight());
			}
			Identifier ID = Registries.ITEM.getId(output.getItem());
			if (shape != null)
				shapes.put(ID, shape);
		}
		int shapesFromRecipes = shapes.size();
		LOGGER.info("Loaded {} item shapes from recipes", shapesFromRecipes);
		// Builtin overrides
		loadDefaultOverrides();
		LOGGER.info("Loaded item shapes from builtin overrides");
		// Load overrides from config
		int shapesFromConfig = 0;
		for (Map.Entry<String, String> entry : ConfigManager.config.shapeOverrides.entrySet()) {
			if (!Identifier.isValid(entry.getKey())) {
				LOGGER.warn("Failed to load invalid override \"{}\" from config", entry.getKey());
				continue;
			}
			Identifier ID = new Identifier(entry.getKey());
			InventoryShape shape = InventoryShape.fromString(entry.getValue());
			shapes.put(ID, shape);
			shapesFromConfig += 1;
		}
		LOGGER.info("Loaded {} item shapes from config overrides", shapesFromConfig);
	}

	public static void loadDefaultOverrides() {
		// Ingots
		addShapeOverride(Items.IRON_INGOT, "xx");
		addShapeOverrideFrom(Items.GOLD_INGOT, Items.IRON_INGOT);
		addShapeOverrideFrom(Items.NETHERITE_INGOT, Items.IRON_INGOT);
		// Netherite Stuff
		addShapeOverride(Items.ANCIENT_DEBRIS, "xx\nxx");
		addShapeOverrideFrom(Items.NETHERITE_SWORD, Items.IRON_SWORD);
		addShapeOverrideFrom(Items.NETHERITE_PICKAXE, Items.IRON_PICKAXE);
		addShapeOverrideFrom(Items.NETHERITE_AXE, Items.IRON_AXE);
		addShapeOverrideFrom(Items.NETHERITE_SHOVEL, Items.IRON_SHOVEL);
		addShapeOverrideFrom(Items.NETHERITE_HOE, Items.IRON_HOE);
		addShapeOverrideFrom(Items.NETHERITE_HELMET, Items.IRON_HELMET);
		addShapeOverrideFrom(Items.NETHERITE_CHESTPLATE, Items.IRON_CHESTPLATE);
		addShapeOverrideFrom(Items.NETHERITE_LEGGINGS, Items.IRON_LEGGINGS);
		addShapeOverrideFrom(Items.NETHERITE_BOOTS, Items.IRON_BOOTS);
		// Buckets
		addShapeOverride(Items.WATER_BUCKET, "xxx\n x ");
		addShapeOverrideFrom(Items.LAVA_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.MILK_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.POWDER_SNOW_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.AXOLOTL_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.COD_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.PUFFERFISH_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.SALMON_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.TADPOLE_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.TROPICAL_FISH_BUCKET, Items.WATER_BUCKET);
		// Potions
		addShapeOverrideFrom(Items.POTION, Items.GLASS_BOTTLE);
		addShapeOverrideFrom(Items.SPLASH_POTION, Items.GLASS_BOTTLE);
		addShapeOverrideFrom(Items.LINGERING_POTION, Items.GLASS_BOTTLE);
		addShapeOverrideFrom(Items.HONEY_BOTTLE, Items.GLASS_BOTTLE);
		// Anvils
		addShapeOverrideFrom(Items.CHIPPED_ANVIL, Items.ANVIL);
		addShapeOverrideFrom(Items.DAMAGED_ANVIL, Items.ANVIL);
		// Arrows
		addShapeOverrideFrom(Items.SPECTRAL_ARROW, Items.ARROW);
		addShapeOverrideFrom(Items.TIPPED_ARROW, Items.ARROW);
		// Dyed Variants
		addDyedShapeOverride(Items.SHULKER_BOX);
        addDyedShapeOverride(Items.CANDLE);
		addDyedShapeOverride(new Identifier("minecraft", "wool"), Items.WHITE_WOOL);
		// Other
		addShapeOverride(Items.TRIDENT, "x\nx\nx");
		addShapeOverrideFrom(Items.TRAPPED_CHEST, Items.CHEST);
	}

	private static void addShapeOverride(Item item, String shape) {
		shapes.put(Registries.ITEM.getId(item), InventoryShape.fromString(shape));
	}

	private static void addShapeOverrideFrom(Item item, Item from) {
		shapes.put(Registries.ITEM.getId(item), shapes.get(Registries.ITEM.getId(from)));
	}

	private static void addDyedShapeOverride(Item from) {
		addDyedShapeOverride(Registries.ITEM.getId(from), from);
	}

	private static void addDyedShapeOverride(Identifier fromID, Item from) {
		for (DyeColor color : DyeColor.values()) {
			Identifier toID = new Identifier(fromID.getNamespace(), String.format("%s_%s", color.getName().toLowerCase(), fromID.getPath()));
			addShapeOverrideFrom(Registries.ITEM.get(toID), from);
		}
	}
}