package com.ozplugins.AutoEnchanter;

import lombok.Getter;
import net.runelite.api.ItemID;

import java.util.Set;

@Getter
public enum Bolts {
    SAPPHIRE_BOLTS_E( 7, ItemID.SAPPHIRE_BOLTS, Set.of(ItemID.COSMIC_RUNE, ItemID.WATER_RUNE, ItemID.MIND_RUNE)),
    RUBY_BOLTS_E(49, ItemID.RUBY_BOLTS, Set.of(ItemID.COSMIC_RUNE, ItemID.BLOOD_RUNE, ItemID.FIRE_RUNE)),
    DIAMOND_BOLTS_E(57, ItemID.DIAMOND_BOLTS, Set.of(ItemID.COSMIC_RUNE, ItemID.EARTH_RUNE, ItemID.LAW_RUNE)),
    DRAGONSTONE_BOLTS_E(68, ItemID.DRAGONSTONE_BOLTS, Set.of(ItemID.COSMIC_RUNE, ItemID.EARTH_RUNE, ItemID.SOUL_RUNE));

    private final int requiredMagicLevel;
    private final int requiredBolts;
    private final Set<Integer> requirements;

    Bolts(int requiredMagicLevel, int requiredBolts, Set<Integer> requirements) {
        this.requiredMagicLevel = requiredMagicLevel;
        this.requiredBolts = requiredBolts;
        this.requirements = requirements;
    }
}
