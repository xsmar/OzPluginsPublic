package com.ozplugins.AutoUtilitySpell.Spellbooks;

import lombok.Getter;

@Getter
public enum Standard {
    LOW_ALCH(14286867),
    HIGH_ALCH(14286888),
    VARROCK_TELEPORT(14286869),
    LUMBRIDGE_TELEPORT(14286872),
    FALADOR_TELEPORT(14286875),
    CAMELOT_TELEPORT(14286880),
    ARDOUGNE_TELEPORT(14286886);

    private final int widgetID;

    Standard(int widgetID) {
        this.widgetID = widgetID;
    }

}
