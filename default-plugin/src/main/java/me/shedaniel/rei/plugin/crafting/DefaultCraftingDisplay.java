/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.plugin.crafting;

import me.shedaniel.rei.api.registry.display.TransferDisplay;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.server.ContainerInfo;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public interface DefaultCraftingDisplay extends TransferDisplay {
    @Override
    default @NotNull ResourceLocation getCategoryIdentifier() {
        return DefaultPlugin.CRAFTING;
    }
    
    @Override
    default int getWidth() {
        return 2;
    }
    
    @Override
    default int getHeight() {
        return 2;
    }
    
    Optional<Recipe<?>> getOptionalRecipe();
    
    @Override
    default List<EntryIngredient> getOrganisedInputEntries(ContainerInfo<AbstractContainerMenu> containerInfo, AbstractContainerMenu container) {
        List<EntryIngredient> list = new ArrayList<>(containerInfo.getCraftingWidth(container) * containerInfo.getCraftingHeight(container));
        for (int i = 0; i < containerInfo.getCraftingWidth(container) * containerInfo.getCraftingHeight(container); i++) {
            list.add(EntryIngredient.empty());
        }
        for (int i = 0; i < getInputEntries().size(); i++) {
            EntryIngredient stacks = getInputEntries().get(i);
            list.set(DefaultCraftingCategory.getSlotWithSize(this, i, containerInfo.getCraftingWidth(container)), stacks);
        }
        return list;
    }
}
