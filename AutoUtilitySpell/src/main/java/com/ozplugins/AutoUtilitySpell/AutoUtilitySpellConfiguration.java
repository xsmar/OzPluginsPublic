package com.ozplugins.AutoUtilitySpell;

import com.ozplugins.AutoUtilitySpell.Spellbooks.Lunar;
import com.ozplugins.AutoUtilitySpell.Spellbooks.Spellbooks;
import com.ozplugins.AutoUtilitySpell.Spellbooks.Standard;
import com.ozplugins.AutoUtilitySpell.Spellbooks.SpellOptions.HideToTan;
import com.ozplugins.AutoUtilitySpell.Spellbooks.SpellOptions.JewelryToString;
import com.ozplugins.AutoUtilitySpell.Spellbooks.SpellOptions.PlanksToMake;
import net.runelite.client.config.*;

@ConfigGroup("AutoUtilitySpell")
public interface AutoUtilitySpellConfiguration extends Config
{
	String version = "v0.1";

	@ConfigItem(
			keyName = "instructions",
			name = "",
			description = "Instructions.",
			position = 1,
			section = "instructionsConfig"
	)
	default String instructions()
	{
		return "This plugin assumes you have the rune requirements already. \n\nSet hotkey up and activate plugin with the hotkey.";
	}

	@ConfigSection(
			//keyName = "delayTickConfig",
			name = "Instructions",
			description = "Plugin instructions.",
			position = 2
	)
	String instructionsConfig = "instructionsConfig";

	@ConfigSection(
			name = "Setup",
			description = "Plugin setup.",
			position = 5
	)
	String setupConfig = "setupConfig";

	@ConfigItem(
			keyName = "start/stop hotkey",
			name = "Start/Stop Hotkey",
			description = "Toggle for turning plugin on and off.",
			position = 6,
			section = "setupConfig"
	)
	default Keybind toggle()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Spellbook",
			name = "Spellbook:",
			position = 7,
			section = "setupConfig",
			description = "What spellbook do you want to use."
	)
	default Spellbooks Spellbook() {
		return Spellbooks.STANDARD;
	}

	@ConfigSection(
			name = "Standard Spellbook Config",
			description = "Standard Spellbook Configuration section.",
			closedByDefault = true,
			position = 13
	)
	String standardSpellbookConfig = "standardSpellbookConfig";

	@ConfigItem(
			keyName = "alchInstructions",
			name = "",
			description = "Instructions.",
			position = 14,
			section = "standardSpellbookConfig"
	)
	default String standardSpellbook()
	{
		return "If alching, make sure you fill out the Do not alch list first. \n\nNot case sensitive, separate by comma.";
	}

	@ConfigItem(
			keyName = "standardSpell",
			name = "Spell:",
			description = "Input the name of the standard spell you want to cast.",
			position = 15,
			section = "standardSpellbookConfig"
	)
	default Standard StandardSpell() {
		return Standard.HIGH_ALCH;
	}

	@ConfigItem(
			keyName = "AlchWhitelistItemName",
			name = "Don't Alch Items:",
			description = "Input the name of the items you want to alch, separated by a comma.",
			position = 16,
			section = "standardSpellbookConfig"
	)
	default String alchWhitelist() {
		return "rune pouch,nature rune,";
	}
	@ConfigItem(
			keyName = "AlchItemName",
			name = "Alch Item Name Contains:",
			description = "Input the name of the item you want to alch. Can use wildcard.",
			position = 17,
			section = "standardSpellbookConfig"
	)
	default String alchItemName() {
		return "rune p";
	}

	@ConfigSection(
			name = "Lunar Spellbook Config",
			description = "Lunar Spellbook Configuration section.",
			closedByDefault = true,
			position = 22
	)
	String lunarSpellbookConfig = "lunarSpellbookConfig";

	@ConfigItem(
			keyName = "lunarSpell",
			name = "Spell:",
			description = "Input the name of the lunar spell you want to cast.",
			position = 23,
			section = "lunarSpellbookConfig"
	)
	default Lunar LunarSpell() {
		return Lunar.PLANK_MAKE;
	}


	@ConfigItem(
			keyName = "HideToTan",
			name = "Hide To Tan:",
			position = 24,
			section = "lunarSpellbookConfig",
			description = "What hide do you want to tan?."
	)
	default HideToTan HideToTan() {
		return HideToTan.GREEN_DRAGONHIDE;
	}

	@ConfigItem(
			keyName = "PlankToMake",
			name = "Contract To Make",
			position = 26,
			section = "lunarSpellbookConfig",
			description = "What plank do you want to make?."
	)
	default PlanksToMake PlankToMake() {
		return PlanksToMake.TEAKS;
	}

	@ConfigItem(
			keyName = "JewelryToString",
			name = "Jewelry",
			position = 27,
			section = "lunarSpellbookConfig",
			description = "What jewelry do you want to string?"
	)
	default JewelryToString JewelryToSTring() {
		return JewelryToString.JADE_AMULET;
	}

	@ConfigItem(
			keyName = "pickUpGlass",
			name = "Pick Up Glass",
			description = "Enable to pick up glass from floor when using Superglass make spell.",
			section = "lunarSpellbookConfig",
			position = 27
	)
	default boolean pickUpGlass()
	{
		return false;
	}


	@ConfigSection(
		//keyName = "delayTickConfig",
		name = "Game Tick Configuration",
		description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
		closedByDefault = true,
		position = 57
	)
	String delayTickConfig = "delayTickConfig";

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayMin",
		name = "Game Tick Min",
		description = "",
		position = 58,
		section = "delayTickConfig"
	)
	default int tickDelayMin()
	{
		return 1;
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayMax",
		name = "Game Tick Max",
		description = "",
		position = 59,
		section = "delayTickConfig"
	)
	default int tickDelayMax()
	{
		return 3;
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayTarget",
		name = "Game Tick Target",
		description = "",
		position = 60,
		section = "delayTickConfig"
	)
	default int tickDelayTarget()
	{
		return 2;
	}

	@Range(
		min = 0,
		max = 10
	)
	@ConfigItem(
		keyName = "tickDelayDeviation",
		name = "Game Tick Deviation",
		description = "",
		position = 61,
		section = "delayTickConfig"
	)
	default int tickDelayDeviation()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "tickDelayWeightedDistribution",
		name = "Game Tick Weighted Distribution",
		description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
		position = 62,
		section = "delayTickConfig"
	)
	default boolean tickDelayWeightedDistribution()
	{
		return false;
	}

	@ConfigSection(
			name = "UI Settings",
			description = "UI settings.",
			closedByDefault = true,
			position = 80
	)
	String UIConfig = "UIConfig";
	@ConfigItem(
			keyName = "UISetting",
			name = "UI Layout: ",
			description = "Choose what UI layout you'd like.",
			position = 81,
			section = "UIConfig",
			hidden = false
	)
	default UISettings UISettings() {
		return UISettings.FULL;
	}

	@ConfigItem(
		keyName = "enableUI",
		name = "Enable UI",
		description = "Enable to turn on in game UI",
			section = "UIConfig",
			position = 140
	)
	default boolean enableUI()
	{
		return true;
	}

}
