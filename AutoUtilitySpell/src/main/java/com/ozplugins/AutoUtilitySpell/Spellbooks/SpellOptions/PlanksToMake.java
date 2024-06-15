package com.ozplugins.AutoUtilitySpell.Spellbooks.SpellOptions;

import net.runelite.api.ItemID;

public enum PlanksToMake {
    TEAKS(ItemID.TEAK_LOGS),
    MAHOGANY(ItemID.MAHOGANY_LOGS);

    public final int requiredItemID;

    PlanksToMake(int requiredItemID) {
        this.requiredItemID = requiredItemID;
    }
}
