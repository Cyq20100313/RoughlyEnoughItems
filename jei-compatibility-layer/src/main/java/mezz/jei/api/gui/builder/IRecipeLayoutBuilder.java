package mezz.jei.api.gui.builder;

import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;

/**
 * A builder passed to plugins that implement
 * {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}.
 * <p>
 * Plugins add slot locations and ingredients to JEI using this interface.
 *
 * @since 9.3.0
 */
public interface IRecipeLayoutBuilder {
    /**
     * Add a slot that will be drawn at the given position relative to the recipe layout.
     *
     * @param recipeIngredientRole the {@link RecipeIngredientRole} of this slot (for lookups).
     * @param x                    relative x position of the slot on the recipe layout.
     * @param y                    relative y position of the slot on the recipe layout.
     * @return a {@link IRecipeSlotBuilder} that has further methods for adding ingredients, etc.
     * @apiNote When porting from {@link mezz.jei.api.gui.ingredient.IGuiItemStackGroup} to this new method,
     * in most cases you will need to add 1 pixel x and y to your coordinates here.
     * For ItemStack ingredients, JEI used to automatically add a 1 pixel offset on all sides,
     * so that a 16x16 item would be centered on an 18x18 slot texture background.
     * This automatic behavior was confusing and inconsistent with other ingredient types, so
     * this new method does not have a hidden automatic 1 pixel offset. Sorry!
     * @since 9.3.0
     */
    IRecipeSlotBuilder addSlot(RecipeIngredientRole recipeIngredientRole, int x, int y);
    
    /**
     * Add ingredients that are important for recipe lookup, but are not displayed on the recipe layout.
     *
     * @param recipeIngredientRole the {@link RecipeIngredientRole} of these ingredients (for lookups).
     * @return an {@link IIngredientAcceptor} to add ingredients to.
     * @since 9.3.0
     */
    IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole recipeIngredientRole);
    
    /**
     * Moves the recipe transfer button's position relative to the recipe layout.
     * By default, the recipe transfer button is at the bottom, to the right of the recipe.
     * If it doesn't fit there, you can use this to move it when you init the recipe layout.
     *
     * @since 9.3.0
     */
    void moveRecipeTransferButton(int posX, int posY);
    
    /**
     * Adds a shapeless icon to the top right of the recipe,
     * that shows a tooltip saying "shapeless" when hovered over.
     *
     * @since 9.3.0
     */
    void setShapeless();
    
    /**
     * Adds a shapeless icon to the specified position relative to the recipe layout,
     * that shows a tooltip saying "shapeless" when hovered over.
     *
     * @since 9.3.0
     */
    void setShapeless(int posX, int posY);
    
    /**
     * Link slots together so that if one slot matches the current focus, the others will be limited too.
     * This can only be set on slots that contains the same number of ingredients.
     * <p>
     * For example:
     * Consider a recipe that has an input slot with every plank type
     * and an output slot with stairs for each plank type.
     * <p>
     * The number of inputs and outputs are the same,
     * and when the full recipe is displayed it rotates through all the different pairs of planks and their stairs.
     * <p>
     * If the slots are focus-linked, when the player focuses on acacia planks,
     * the linked output slot will only display acacia stairs.
     *
     * @since 9.5.1
     */
    void createFocusLink(IIngredientAcceptor<?>... slots);
    
    /**
     * Link slots together so that if one slot matches the current focus, the others will be limited too.
     * This can only be set on slots that contains the same number of ingredients.
     * <p>
     * For example:
     * Consider a recipe that has an input slot with every plank type
     * and an output slot with stairs for each plank type.
     * <p>
     * The number of inputs and outputs are the same,
     * and when the full recipe is displayed it rotates through all the different pairs of planks and their stairs.
     * <p>
     * If the slots are focus-linked, when the player focuses on acacia planks,
     * the linked output slot will only display acacia stairs.
     *
     * @since 9.4.1
     */
    void createFocusLink(IRecipeSlotBuilder... slots);
}
