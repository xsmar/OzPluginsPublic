package com.ozplugins.constants;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
public enum MineArea {
    UPPER_1(new WorldPoint(3760, 5671, 0)),
    UPPER_2(new WorldPoint(3752, 5679, 0)),
    LOWER_1(new WorldPoint(3746, 5652, 0)),
    LOWER_2(new WorldPoint(3737, 5665, 0));

    private final WorldPoint minePoint;

    MineArea(WorldPoint minePoint) {
        this.minePoint = minePoint;
    }
}
