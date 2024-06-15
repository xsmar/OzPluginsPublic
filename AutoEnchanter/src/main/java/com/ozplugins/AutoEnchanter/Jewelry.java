package com.ozplugins.AutoEnchanter;

import lombok.Getter;
import net.runelite.api.ItemID;

import java.util.Set;

@Getter
public enum Jewelry {
    //This is all prolly very unnecessary but im trying to learn more about enums so fuck it
    RING_OF_RECOIL(14286859, 7, ItemID.SAPPHIRE_RING, Set.of(ItemID.COSMIC_RUNE,ItemID.WATER_RUNE)),
    GAMES_NECKLACE(14286859, 7, ItemID.SAPPHIRE_NECKLACE, Set.of(ItemID.COSMIC_RUNE,ItemID.WATER_RUNE)),
    DODGY_NECKLACE(14286859, 7, ItemID.OPAL_NECKLACE, Set.of(ItemID.COSMIC_RUNE,ItemID.WATER_RUNE)),
    EXPEDITIOUS_BRACELET( 14286859, 7, ItemID.OPAL_BRACELET, Set.of(ItemID.COSMIC_RUNE,ItemID.WATER_RUNE)),
    RING_OF_DUELING(14286870, 27, ItemID.EMERALD_RING, Set.of(ItemID.COSMIC_RUNE,ItemID.AIR_RUNE)),
    NECKLACE_OF_PASSAGE(14286870, 27, ItemID.EMERALD_NECKLACE, Set.of(ItemID.COSMIC_RUNE,ItemID.AIR_RUNE)),
    AMULET_OF_CHEMISTRY(14286870, 27, ItemID.JADE_AMULET, Set.of(ItemID.COSMIC_RUNE,ItemID.AIR_RUNE)),
    RING_OF_FORGING(14286882, 49, ItemID.RUBY_RING, Set.of(ItemID.COSMIC_RUNE,ItemID.FIRE_RUNE)),
    DIGSITE_PENDANT(14286882, 49, ItemID.RUBY_AMULET, Set.of(ItemID.COSMIC_RUNE,ItemID.FIRE_RUNE)),
    BURNING_AMULET(14286882, 49, ItemID.TOPAZ_AMULET, Set.of(ItemID.COSMIC_RUNE,ItemID.FIRE_RUNE)),
    BRACELET_OF_SLAUGHTER( 14286882, 49, ItemID.TOPAZ_BRACELET, Set.of(ItemID.COSMIC_RUNE,ItemID.FIRE_RUNE)),
    PHOENIX_NECKLACE(14286890, 57, ItemID.DIAMOND_NECKLACE, Set.of(ItemID.COSMIC_RUNE,ItemID.EARTH_RUNE)),
    ABYSSAL_BRACELET(14286890, 57, ItemID.DIAMOND_BRACELET, Set.of(ItemID.COSMIC_RUNE,ItemID.EARTH_RUNE)),
    RING_OF_WEALTH( 14286905, 68, ItemID.DRAGONSTONE_RING, Set.of(ItemID.COSMIC_RUNE,ItemID.EARTH_RUNE,ItemID.WATER_RUNE)),
    SKILLS_NECKLACE( 14286905, 68, ItemID.DRAGON_NECKLACE, Set.of(ItemID.COSMIC_RUNE,ItemID.EARTH_RUNE,ItemID.WATER_RUNE)),
    AMULET_OF_GLORY(14286905, 68, ItemID.DRAGONSTONE_AMULET, Set.of(ItemID.COSMIC_RUNE,ItemID.EARTH_RUNE,ItemID.WATER_RUNE)),
    COMBAT_BRACELET(14286905, 68, ItemID.DRAGON_BRACELET, Set.of(ItemID.COSMIC_RUNE,ItemID.EARTH_RUNE,ItemID.WATER_RUNE));

    private final int widgetID;
    private final int requiredMagicLevel;
    private final int requiredJewelry;
    private final Set<Integer> requirements;


    Jewelry(int widgetID, int requiredMagicLevel, int requiredJewelry, Set<Integer> requirements) {
        this.widgetID = widgetID;
        this.requiredMagicLevel = requiredMagicLevel;
        this.requiredJewelry = requiredJewelry;
        this.requirements = requirements;
    }
}
