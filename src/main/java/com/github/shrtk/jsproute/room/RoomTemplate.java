package com.github.shrtk.jsproute.room;

import java.util.List;

public class RoomTemplate {
    private String name; // テンプレートの名前 (例: "mansion")
    private List<BlockData> blocks; // テンプレートを構成するブロックのリスト

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BlockData> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BlockData> blocks) {
        this.blocks = blocks;
    }

    // JSONの "positions" に対応するために、このクラスを調整します。
    // 今回のJSON例では "positions" の配列が直接テンプレートになっているため、
    // RoomTemplateのblocksフィールドをJSONのルート配列に合わせます。
    // JSONの形式に合わせて、以下のように修正します。
    // JSONの例が {"name": "mansion", "type": "gemstone", "positions": [...]}
    // と {"x": ..., "y": ..., "z": ..., "action": ...} の2種類あるので、
    // テンプレート側は後者のリストとして読み込みます。
    // 最初に提示されたmansion_template.jsonの例は配列なので、それに合わせます。
}