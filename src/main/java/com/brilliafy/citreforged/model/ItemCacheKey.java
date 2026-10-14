package com.brilliafy.citreforged.model;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;

public class ItemCacheKey {
    protected int damage;
    protected int stackSize;
    protected NBTTagCompound nbtData;
    protected int hash;

    public ItemCacheKey(ItemStack stack) {
        this.damage = stack.getItemDamage();
        this.stackSize = stack.getCount();
        this.nbtData = stack.hasTagCompound() ? stack.getTagCompound() : null;
        this.hash = computeHash(this.damage, this.stackSize, this.nbtData);
    }

    protected ItemCacheKey() {}

    public static int computeHash(int damage, int stackSize, NBTTagCompound nbtData) {
        return (damage * 31 + stackSize) * 31 + (nbtData != null ? nbtData.hashCode() : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemCacheKey)) return false;
        ItemCacheKey that = (ItemCacheKey) o;
        return damage == that.damage && stackSize == that.stackSize && Objects.equals(nbtData, that.nbtData);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public static class LookupKey extends ItemCacheKey {
        public void reset(ItemStack stack) {
            this.damage = stack.getItemDamage();
            this.stackSize = stack.getCount();
            this.nbtData = stack.hasTagCompound() ? stack.getTagCompound() : null;
            this.hash = computeHash(this.damage, this.stackSize, this.nbtData);
        }
    }
}
