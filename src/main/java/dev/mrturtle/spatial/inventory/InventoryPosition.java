package dev.mrturtle.spatial.inventory;

import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class InventoryPosition {
    public int x;
    public int y;

    public InventoryPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static List<InventoryPosition> fromIngredientList(DefaultedList<Ingredient> ingredients, int width, int height) {
        List<InventoryPosition> shape = new ArrayList<>();
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                Ingredient ingredient = ingredients.get(i + j * width);
                if (!ingredient.isEmpty())
                    shape.add(new InventoryPosition(i, j));
            }
        }
        return shape;
    }
}
