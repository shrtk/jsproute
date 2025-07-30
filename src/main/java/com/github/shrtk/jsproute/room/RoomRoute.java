package com.github.shrtk.jsproute.room;

import java.util.List;

public class RoomRoute {
    private String name; // ルートの名前 (例: "mansion")
    private String type; // ルートのタイプ (例: "gemstone")
    private List<BlockData> positions; // 攻略ルートの座標とアクションのリスト
    private List<Object> targets; // 今回は使用しないが、JSONにあるので定義

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<BlockData> getPositions() {
        return positions;
    }

    public void setPositions(List<BlockData> positions) {
        this.positions = positions;
    }

    public List<Object> getTargets() {
        return targets;
    }

    public void setTargets(List<Object> targets) {
        this.targets = targets;
    }
}