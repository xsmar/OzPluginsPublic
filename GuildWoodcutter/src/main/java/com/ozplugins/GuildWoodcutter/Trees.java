package com.ozplugins.GuildWoodcutter;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum Trees {
        YEW("Yew", ItemID.YEW_LOGS),
        MAGIC("Magic", ItemID.MAGIC_LOGS),
        REDWOOD("Redwood", ItemID.REDWOOD_LOGS);

    private final String name;
    private final int logID;

    Trees(String name, int logID) {
            this.name = name;
            this.logID = logID;
        }
}
