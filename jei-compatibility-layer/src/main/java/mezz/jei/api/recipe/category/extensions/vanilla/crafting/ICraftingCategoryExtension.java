package mezz.jei.api.recipe.category.extensions.vanilla.crafting;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

/**
 * Implement this interface instead of just {@link IRecipeCategoryExtension}
 * to have your recipe extension work as part of {@link RecipeTypes#CRAFTING} as a shapeless recipe.
 * <p>
 * For shaped recipes, override {@link #getWidth()} and {@link #getHeight()}.
 * <p>
 * Register this extension by getting the extendable crafting category from:
 * {@link IVanillaCategoryExtensionRegistration#getCraftingCategory()}
 * and then registering it with {@link IExtendableRecipeCategory#addCategoryExtension}.
 */
public interface ICraftingCategoryExtension extends IRecipeCategoryExtension {
    /**
     * Override the default {@link IRecipeCategory} behavior.
     *
     * @see IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)
     * @since 9.4.0
     */
    void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses);
    
    /**
     * Return the registry name of the recipe here.
     * With advanced tooltips on, this will show on the output item's tooltip.
     * <p>
     * This will also show the modId when the recipe modId and output item modId do not match.
     * This lets the player know where the recipe came from.
     *
     * @return the registry name of the recipe, or null if there is none
     */
    @Nullable
    default ResourceLocation getRegistryName() {
        return null;
    }
    
    /**
     * @return the width of a shaped recipe, or 0 for a shapeless recipe
     * @since 9.3.0
     */
    default int getWidth() {
        return 0;
    }
    
    /**
     * @return the height of a shaped recipe, or 0 for a shapeless recipe
     * @since 9.3.0
     */
    default int getHeight() {
        return 0;
    }
}
