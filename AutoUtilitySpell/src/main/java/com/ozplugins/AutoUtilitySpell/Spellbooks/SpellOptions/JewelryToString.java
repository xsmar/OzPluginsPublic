package com.ozplugins.AutoUtilitySpell.Spellbooks.SpellOptions;

import net.runelite.api.ItemID;

public enum JewelryToString {
        OPAL_AMULET(ItemID.OPAL_AMULET_U),
        JADE_AMULET(ItemID.JADE_AMULET_U),
        TOPAZ_AMULET(ItemID.TOPAZ_AMULET_U),
        SAPPHIRE_AMULET(ItemID.SAPPHIRE_AMULET_U),
        EMERALD_AMULET(ItemID.EMERALD_AMULET_U),
        RUBY_AMULET(ItemID.RUBY_AMULET_U),
        DIAMOND_AMULET(ItemID.DIAMOND_AMULET_U),
        DRAGONSTONE_AMULET(ItemID.DRAGONSTONE_AMULET_U);

        public final int requiredItemID;

        JewelryToString(int requiredItemID) {
                this.requiredItemID = requiredItemID;
        }
}
