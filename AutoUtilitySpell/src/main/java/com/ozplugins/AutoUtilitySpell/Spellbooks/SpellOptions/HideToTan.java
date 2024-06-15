package com.ozplugins.AutoUtilitySpell.Spellbooks.SpellOptions;

import net.runelite.api.ItemID;

public enum HideToTan {
        GREEN_DRAGONHIDE(ItemID.GREEN_DRAGONHIDE),
        BLUE_DRAGONHIDE(ItemID.BLUE_DRAGONHIDE),
        RED_DRAGONHIDE(ItemID.RED_DRAGONHIDE),
        BLACK_DRAGONHIDE(ItemID.BLACK_DRAGONHIDE);

        public final int requiredItemID;

        HideToTan(int requiredItemID) {
                this.requiredItemID = requiredItemID;
        }
}
