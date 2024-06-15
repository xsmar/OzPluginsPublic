package com.ozplugins.AutoTeleTabs;

import lombok.Getter;

@Getter
public enum TeleTab {
    FALADOR(5177362),
    VARROCK(5177357),
    ARDOUGNE(5177363),
    CAMELOT(5177368),
    HOUSE_TELEPORT(5177359),
    WATCHTOWER(5177358),
    LUMBRIDGE(5177356),
    BONES_TO_BANANA(5177354),
    BONES_TO_PEACHES(5177369);

    private final int widgetID;

    TeleTab(int widgetID) {
        this.widgetID = widgetID;
    }
}
