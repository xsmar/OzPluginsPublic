package com.ozplugins.AutoUtilitySpell.Spellbooks;
import lombok.Getter;

@Getter
public enum Lunar {

    HUMIDIFY(14286955),
    HUNTER_KIT(14286959),
    SPIN_FLAX(14286991),
    //SUPERGLASS_MAKE(14286966),//TODO GOTTA ADD SUPERGLASS MAKE
    TAN_LEATHER(14286967),
    STRING_JEWELRY(14286971),
    PLANK_MAKE(14286978);

    private final int widgetID;

    Lunar(int widgetID) {
        this.widgetID = widgetID;
    }
}
