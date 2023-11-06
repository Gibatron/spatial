package dev.mrturtle.spatial;

import dev.mrturtle.spatial.config.ConfigManager;
import dev.mrturtle.spatial.inventory.InventoryShape;
import dev.mrturtle.spatial.networking.SpatialNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
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

	public static final Event<RegisterShapeEventListener> SHAPES_LOADED = EventFactory.createArrayBacked(RegisterShapeEventListener.class, (listeners -> (server) -> {
		for (RegisterShapeEventListener listener : listeners) {
			listener.onShapesLoaded(server);
		}
	}));

	@Override
	public void onInitialize() {
		ConfigManager.loadConfig();
		ServerLifecycleEvents.SERVER_STARTED.register(Spatial::loadShapes);
		// Sync shapes with player when they connect to the server
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!server.isDedicated())
				return;
			PacketByteBuf buf = PacketByteBufs.create();
			for (Identifier ID : shapes.keySet()) {
				if (shapes.get(ID) == null)
					LOGGER.info(ID.toString());
			}
			buf.writeMap(shapes, PacketByteBuf::writeIdentifier, (bufx, shape) -> shape.writeTo(bufx));
			ServerPlayNetworking.send(handler.player, SpatialNetworking.SYNC_SHAPES_PACKET_ID, buf);
		});
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
		for (Map.Entry<String, String[]> entry : ConfigManager.config.shapeOverrides.entrySet()) {
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
		SHAPES_LOADED.invoker().onShapesLoaded(server);
	}

	private static void loadDefaultOverrides() {
		// Ingots
		addShapeOverride(Items.IRON_INGOT, new String[] {"xx"});
		addShapeOverrideFrom(Items.GOLD_INGOT, Items.IRON_INGOT);
		addShapeOverrideFrom(Items.NETHERITE_INGOT, Items.IRON_INGOT);
		// Netherite Stuff
		addShapeOverride(Items.ANCIENT_DEBRIS, new String[] {"xx", "xx"});
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
		addShapeOverride(Items.WATER_BUCKET, new String[] {"xxx", " x "});
		addShapeOverrideFrom(Items.LAVA_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.MILK_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.POWDER_SNOW_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.AXOLOTL_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.COD_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.PUFFERFISH_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.SALMON_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.TADPOLE_BUCKET, Items.WATER_BUCKET);
		addShapeOverrideFrom(Items.TROPICAL_FISH_BUCKET, Items.WATER_BUCKET);
		// Boats
		addShapeOverride(Items.OAK_CHEST_BOAT, new String[] {"xxx", "xxx"});
		addShapeOverrideFrom(Items.BIRCH_CHEST_BOAT, Items.OAK_CHEST_BOAT);
		addShapeOverrideFrom(Items.DARK_OAK_CHEST_BOAT, Items.OAK_CHEST_BOAT);
		addShapeOverrideFrom(Items.ACACIA_CHEST_BOAT, Items.OAK_CHEST_BOAT);
		addShapeOverrideFrom(Items.SPRUCE_CHEST_BOAT, Items.OAK_CHEST_BOAT);
		addShapeOverrideFrom(Items.CHERRY_CHEST_BOAT, Items.OAK_CHEST_BOAT);
		addShapeOverrideFrom(Items.MANGROVE_CHEST_BOAT, Items.OAK_CHEST_BOAT);
		addShapeOverrideFrom(Items.JUNGLE_CHEST_BOAT, Items.OAK_CHEST_BOAT);
		addShapeOverrideFrom(Items.BAMBOO_CHEST_RAFT, Items.OAK_CHEST_BOAT);
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
		addShapeOverride(Items.TRIDENT, new String[] {"x", "x", "x"});
		addShapeOverrideFrom(Items.TRAPPED_CHEST, Items.CHEST);
		addShapeOverrideFrom(Items.CARROT_ON_A_STICK, Items.FISHING_ROD);
		addShapeOverrideFrom(Items.WARPED_FUNGUS_ON_A_STICK, Items.FISHING_ROD);
	}

	/**This method can be used to add a custom shape override to an item
	 * This should be called after {@link Spatial#SHAPES_LOADED} otherwise it might get overwritten
	 *
	 * @param item  Item
	 * @param shape String Array, each entry is a row in the shape
	 */
	public static void addShapeOverride(Item item, String[] shape) {
		shapes.put(Registries.ITEM.getId(item), InventoryShape.fromString(shape));
	}

	/**This method can be used to add a custom shape override to an item
	 * This method copies it from an item that already has a registered shape
	 * This should be called after {@link Spatial#SHAPES_LOADED} otherwise it might get overwritten
	 *
	 * @param item Item
	 * @param from Item to copy shape from
	 */
	public static void addShapeOverrideFrom(Item item, Item from) {
		InventoryShape shape = shapes.get(Registries.ITEM.getId(from));
		if (shape == null) {
			LOGGER.warn("Attempted to copy a shape override from an item without a shape {}!", Registries.ITEM.getId(from));
			return;
		}
		shapes.put(Registries.ITEM.getId(item), shape);
	}

	/**This method can be used to add a custom shape override to a dyed variants of an item
	 * This method copies the shape from the Item passed in to all items that have an identifier such as "white_X" or "yellow_X" where X is the Item's ID
	 * If the base Item does not have a non-dyed variant such as wool you should use {@link Spatial#addDyedShapeOverride(Identifier, Item)} instead
	 * This should be called after {@link Spatial#SHAPES_LOADED} otherwise it might get overwritten
	 *
	 * @param from Item to copy shape from and base other item IDs off
	 */
	public static void addDyedShapeOverride(Item from) {
		addDyedShapeOverride(Registries.ITEM.getId(from), from);
	}

	/**This method can be used to add a custom shape override to a dyed variants of an item
	 * This method copies the shape from the Item passed in to all items that have an identifier such as "white_X" or "yellow_X" where X is the ID passed in
	 * If the base Item does have a non-dyed variant such as beds you should use {@link Spatial#addDyedShapeOverride(Item)} instead
	 * This should be called after {@link Spatial#SHAPES_LOADED} otherwise it might get overwritten
	 *
	 * @param fromID Identifier to base dyed IDs off
	 * @param from   Item to copy shape from
	 */
	public static void addDyedShapeOverride(Identifier fromID, Item from) {
		for (DyeColor color : DyeColor.values()) {
			Identifier toID = new Identifier(fromID.getNamespace(), String.format("%s_%s", color.getName().toLowerCase(), fromID.getPath()));
			addShapeOverrideFrom(Registries.ITEM.get(toID), from);
		}
	}

	/**This method replaces all current shapes loaded with the provided ones
	 * This shouldn't be used for adding overrides use {@link Spatial#addShapeOverride(Item, String[])} or {@link Spatial#addShapeOverrideFrom(Item, Item)} instead
	 *
	 * @param newShapes The shapes to replace the current with
	 */
	public static void setShapes(HashMap<Identifier, InventoryShape> newShapes) {
		shapes.clear();
		shapes.putAll(newShapes);
	}

	@FunctionalInterface
	public interface RegisterShapeEventListener {
		void onShapesLoaded(MinecraftServer server);
	}
}