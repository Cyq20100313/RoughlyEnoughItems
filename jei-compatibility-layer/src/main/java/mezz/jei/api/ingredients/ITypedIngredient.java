package mezz.jei.api.ingredients;

import java.util.Optional;

/**
 * Ingredient with type information, for type safety.
 * These ingredients are validated by JEI, and only contain valid types and ingredients.
 *
 * @since 9.3.0
 */
public interface ITypedIngredient<T> {
    /**
     * @return the type of this ingredient
     * @see IIngredientType
     * @since 9.3.0
     */
    IIngredientType<T> getType();
    
    /**
     * @return the ingredient wrapped by this instance
     * @since 9.3.0
     */
    T getIngredient();
    
    /**
     * @return the ingredient wrapped by this instance, only if it matches the given type.
     * This is useful when handling a wildcard generic instance of `ITypedIngredient<?>`.
     * @since 9.3.3
     */
    <V> Optional<V> getIngredient(IIngredientType<V> ingredientType);
}
