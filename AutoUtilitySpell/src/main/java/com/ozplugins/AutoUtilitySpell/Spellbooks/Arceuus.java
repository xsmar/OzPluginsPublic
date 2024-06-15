package com.ozplugins.AutoUtilitySpell.Spellbooks;
import lombok.Getter;

@Getter
public enum Arceuus {
    COMING_SOON(1234);

    private final int widgetID;

    Arceuus(int widgetID) {
        this.widgetID = widgetID;
    }
}
