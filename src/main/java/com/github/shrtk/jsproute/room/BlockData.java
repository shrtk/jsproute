package com.github.shrtk.jsproute.room;

import net.minecraft.util.BlockPos;

public class BlockData {
    private int x;
    private int y;
    private int z;
    private String action; // ブロックIDやアクション（例: "VEIN", "stone_brick_stairs"）

    public BlockData() {
        // Gsonがオブジェクトを生成するために必要
    }

    public BlockData(int x, int y, int z, String action) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.action = action;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getAction() {
        return action;
    }

    public BlockPos toBlockPos() {
        return new BlockPos(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockData blockData = (BlockData) o;
        return x == blockData.x &&
                y == blockData.y &&
                z == blockData.z &&
                action.equals(blockData.action);
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        result = 31 * result + action.hashCode();
        return result;
    }
}