package dev.mrturtle.spatial;

import dev.mrturtle.spatial.inventory.InventoryShape;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Spatial implements ModInitializer {
	private static final HashMap<Identifier, InventoryShape> shapes = new HashMap<>();

    public static final Logger LOGGER = LoggerFactory.getLogger("spatial");

	@Override
	public void onInitialize() {
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
	}
}