package dev.mrturtle.spatial;

import dev.mrturtle.spatial.inventory.InventoryPosition;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Spatial implements ModInitializer {
	public static final HashMap<Identifier, List<InventoryPosition>> shapes = new HashMap<>();

    public static final Logger LOGGER = LoggerFactory.getLogger("spatial");

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(Spatial::loadShapes);
	}

	private static void loadShapes(MinecraftServer server) {
		shapes.clear();
		for (RecipeEntry<?> entry : server.getRecipeManager().values()) {
			Recipe<?> recipe = entry.value();
			ItemStack output = recipe.getResult(server.getRegistryManager());
			List<InventoryPosition> shape = null;
			if (recipe instanceof ShapedRecipe shapedRecipe) {
				DefaultedList<Ingredient> ingredients = shapedRecipe.getIngredients();
				shape = InventoryPosition.fromIngredientList(ingredients, shapedRecipe.getWidth(), shapedRecipe.getHeight());
			}
			Identifier ID = Registries.ITEM.getId(output.getItem());
			if (shape != null)
				shapes.put(ID, shape);
		}
	}
}