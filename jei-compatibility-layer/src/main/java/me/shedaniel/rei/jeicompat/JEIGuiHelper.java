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

package me.shedaniel.rei.jeicompat;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.forge.api.PointHelper;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.gui.widgets.Panel;
import me.shedaniel.rei.api.gui.widgets.Widget;
import me.shedaniel.rei.api.gui.widgets.Widgets;
import me.shedaniel.rei.api.ingredient.EntryStack;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.TODO;
import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrap;

public enum JEIGuiHelper implements IGuiHelper {
    INSTANCE;
    
    @Override
    @NotNull
    public IDrawableBuilder drawableBuilder(@NotNull ResourceLocation resourceLocation, int u, int v, int width, int height) {
        return new IDrawableBuilder() {
            private int textureWidth = width;
            private int textureHeight = height;
            private int trimTop;
            private int trimBottom;
            private int trimLeft;
            private int trimRight;
            private int paddingTop;
            private int paddingBottom;
            private int paddingLeft;
            private int paddingRight;
            
            @Override
            @NotNull
            public IDrawableBuilder setTextureSize(int width, int height) {
                this.textureWidth = width;
                this.textureHeight = height;
                return this;
            }
            
            @Override
            @NotNull
            public IDrawableBuilder addPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
                this.paddingTop = paddingTop;
                this.paddingBottom = paddingBottom;
                this.paddingLeft = paddingLeft;
                this.paddingRight = paddingRight;
                return this;
            }
            
            @Override
            @NotNull
            public IDrawableBuilder trim(int trimTop, int trimBottom, int trimLeft, int trimRight) {
                this.trimTop = trimTop;
                this.trimBottom = trimBottom;
                this.trimLeft = trimLeft;
                this.trimRight = trimRight;
                return this;
            }
            
            @Override
            public IDrawableStatic build() {
                int tw = textureWidth + paddingLeft + paddingRight - trimLeft - trimRight;
                int th = textureHeight + paddingTop + paddingBottom - trimTop - trimBottom;
                Widget widget = Widgets.createTexturedWidget(resourceLocation, 0, 0, u, v, width - paddingRight, height - paddingBottom, tw, th);
                return new IDrawableStatic() {
                    @Override
                    public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
                        matrixStack.pushPose();
                        matrixStack.translate(xOffset + paddingLeft, yOffset + paddingTop, 0);
                        widget.render(matrixStack, PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
                        matrixStack.popPose();
                    }
                    
                    @Override
                    public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset) {
                        matrixStack.pushPose();
                        matrixStack.translate(xOffset + paddingLeft, yOffset + paddingTop, 0);
                        widget.render(matrixStack, PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
                        matrixStack.popPose();
                    }
                    
                    @Override
                    public int getWidth() {
                        return tw;
                    }
                    
                    @Override
                    public int getHeight() {
                        return th;
                    }
                };
            }
            
            @Override
            @NotNull
            public IDrawableAnimated buildAnimated(int ticksPerCycle, @NotNull IDrawableAnimated.StartDirection startDirection, boolean inverted) {
                return createAnimatedDrawable(build(), ticksPerCycle, startDirection, inverted);
            }
            
            @Override
            @NotNull
            public IDrawableAnimated buildAnimated(@NotNull ITickTimer tickTimer, @NotNull IDrawableAnimated.StartDirection startDirection) {
                throw TODO();
            }
        };
    }
    
    @Override
    @NotNull
    public IDrawableAnimated createAnimatedDrawable(@NotNull IDrawableStatic drawable, int ticksPerCycle, @NotNull IDrawableAnimated.StartDirection startDirection, boolean inverted) {
        // TODO Implement Animation
        return new IDrawableAnimated() {
            @Override
            public int getWidth() {
                return drawable.getWidth();
            }
            
            @Override
            public int getHeight() {
                return drawable.getHeight();
            }
            
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset) {
                drawable.draw(matrixStack, xOffset, yOffset);
            }
        };
    }
    
    @Override
    @NotNull
    public IDrawableStatic getSlotDrawable() {
        Panel base = Widgets.createSlotBase(new Rectangle(0, 0, 18, 18));
        return new IDrawableStatic() {
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
                throw TODO();
            }
            
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset) {
                base.getBounds().setLocation(xOffset, yOffset);
                base.render(matrixStack, PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
            }
            
            @Override
            public int getWidth() {
                return base.getBounds().getWidth();
            }
            
            @Override
            public int getHeight() {
                return base.getBounds().getHeight();
            }
        };
    }
    
    @Override
    @NotNull
    public IDrawableStatic createBlankDrawable(int width, int height) {
        return new IDrawableStatic() {
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
                
            }
            
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset) {
                
            }
            
            @Override
            public int getWidth() {
                return width;
            }
            
            @Override
            public int getHeight() {
                return height;
            }
        };
    }
    
    @Override
    @NotNull
    public <V> IDrawable createDrawableIngredient(@NotNull V ingredient) {
        EntryStack<?> stack = wrap(ingredient);
        return new IDrawable() {
            @Override
            public int getWidth() {
                return 16;
            }
            
            @Override
            public int getHeight() {
                return 16;
            }
            
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset) {
                stack.render(matrixStack, new Rectangle(xOffset, yOffset, getWidth(), getHeight()), PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
            }
        };
    }
    
    @Override
    @NotNull
    public ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1) {
        throw TODO();
    }
    
    @Override
    @NotNull
    public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
        throw TODO();
    }
}
