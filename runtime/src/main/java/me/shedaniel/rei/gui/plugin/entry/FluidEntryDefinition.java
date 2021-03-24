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

package me.shedaniel.rei.gui.plugin.entry;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import me.shedaniel.architectury.fluid.FluidStack;
import me.shedaniel.architectury.hooks.FluidStackHooks;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.ingredient.entry.renderer.AbstractEntryRenderer;
import me.shedaniel.rei.api.client.ingredient.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.common.ingredient.EntryStack;
import me.shedaniel.rei.api.common.ingredient.entry.EntrySerializer;
import me.shedaniel.rei.api.common.ingredient.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.ingredient.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.ingredient.entry.type.EntryType;
import me.shedaniel.rei.api.common.ingredient.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.ingredient.util.EntryStacks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FluidEntryDefinition implements EntryDefinition<FluidStack>, EntrySerializer<FluidStack> {
    private static final String FLUID_AMOUNT = Platform.isForge() ? "tooltip.rei.fluid_amount.forge" : "tooltip.rei.fluid_amount";
    private final EntryRenderer<FluidStack> renderer = new FluidEntryRenderer();
    
    @Override
    public Class<FluidStack> getValueType() {
        return FluidStack.class;
    }
    
    @Override
    public EntryType<FluidStack> getType() {
        return VanillaEntryTypes.FLUID;
    }
    
    @Override
    public EntryRenderer<FluidStack> getRenderer() {
        return renderer;
    }
    
    @Override
    @Nullable
    public ResourceLocation getIdentifier(EntryStack<FluidStack> entry, FluidStack value) {
        return Registry.FLUID.getKey(value.getFluid());
    }
    
    @Override
    public boolean isEmpty(EntryStack<FluidStack> entry, FluidStack value) {
        return value.isEmpty();
    }
    
    @Override
    public FluidStack copy(EntryStack<FluidStack> entry, FluidStack value) {
        return value.copy();
    }
    
    @Override
    public FluidStack normalize(EntryStack<FluidStack> entry, FluidStack value) {
        FluidStack copy = value.copy();
        copy.setAmount(FluidStack.bucketAmount());
        return copy;
    }
    
    @Override
    public int hash(EntryStack<FluidStack> entry, FluidStack value, ComparisonContext context) {
        int code = 1;
        code = 31 * code + value.getFluid().hashCode();
        code = 31 * code + (context.isFuzzy() || !value.hasTag() ? 0 : value.getTag().hashCode());
        return code;
    }
    
    @Override
    public boolean equals(FluidStack o1, FluidStack o2, ComparisonContext context) {
        if (o1.getFluid() != o2.getFluid())
            return false;
        return context.isFuzzy() || isTagEqual(o1, o2);
    }
    
    private boolean isTagEqual(FluidStack o1, FluidStack o2) {
        CompoundTag tag1 = o1.getTag();
        CompoundTag tag2 = o2.getTag();
        return Objects.equals(tag1, tag2);
    }
    
    @Override
    @Nullable
    public EntrySerializer<FluidStack> getSerializer() {
        return this;
    }
    
    @Override
    public boolean supportSaving() {
        return true;
    }
    
    @Override
    public boolean supportReading() {
        return true;
    }
    
    @Override
    public CompoundTag save(EntryStack<FluidStack> entry, FluidStack value) {
        return value.write(new CompoundTag());
    }
    
    @Override
    public FluidStack read(CompoundTag tag) {
        return FluidStack.read(tag);
    }
    
    @Override
    public Component asFormattedText(EntryStack<FluidStack> entry, FluidStack value) {
        return value.getFluid().defaultFluidState().createLegacyBlock().getBlock().getName();
    }
    
    @Override
    public Collection<ResourceLocation> getTagsFor(EntryStack<FluidStack> entry, FluidStack value) {
        TagCollection<Fluid> collection = Minecraft.getInstance().getConnection().getTags().getFluids();
        return collection == null ? Collections.emptyList() : collection.getMatchingTags(value.getFluid());
    }
    
    public static class FluidEntryRenderer extends AbstractEntryRenderer<FluidStack> {
        @Override
        public void render(EntryStack<FluidStack> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            FluidStack stack = entry.getValue();
            TextureAtlasSprite sprite = FluidStackHooks.getStillTexture(stack);
            int color = FluidStackHooks.getColor(stack);
            int a = 255;
            int r = (color >> 16 & 255);
            int g = (color >> 8 & 255);
            int b = (color & 255);
            Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            Matrix4f matrix = matrices.last().pose();
            builder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            int z = entry.getZ();
            builder.vertex(matrix, bounds.getMaxX(), bounds.y, z).uv(sprite.getU1(), sprite.getV0()).color(r, g, b, a).endVertex();
            builder.vertex(matrix, bounds.x, bounds.y, z).uv(sprite.getU0(), sprite.getV0()).color(r, g, b, a).endVertex();
            builder.vertex(matrix, bounds.x, bounds.getMaxY(), z).uv(sprite.getU0(), sprite.getV1()).color(r, g, b, a).endVertex();
            builder.vertex(matrix, bounds.getMaxX(), bounds.getMaxY(), z).uv(sprite.getU1(), sprite.getV1()).color(r, g, b, a).endVertex();
            tesselator.end();
        }
        
        @Override
        public @Nullable Tooltip getTooltip(EntryStack<FluidStack> entry, Point mouse) {
            if (entry.isEmpty())
                return null;
            List<Component> toolTip = Lists.newArrayList(entry.asFormattedText());
            Fraction amount = entry.getValue().getAmount();
            if (!amount.isLessThan(Fraction.zero())) {
                String amountTooltip = I18n.get(FLUID_AMOUNT, EntryStacks.simplifyAmount(entry).getValue().getAmount());
                if (amountTooltip != null)
                    toolTip.addAll(Stream.of(amountTooltip.split("\n")).map(TextComponent::new).collect(Collectors.toList()));
            }
            if (Minecraft.getInstance().options.advancedItemTooltips) {
                ResourceLocation fluidId = Registry.FLUID.getKey(entry.getValue().getFluid());
                toolTip.add((new TextComponent(fluidId.toString())).withStyle(ChatFormatting.DARK_GRAY));
            }
            return Tooltip.create(toolTip);
        }
    }
}
