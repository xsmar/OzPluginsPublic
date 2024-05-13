package com.ozplugins;

import net.runelite.client.config.*;

@ConfigGroup("AutoNMZ")
public interface AutoNMZConfiguration extends Config {
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
		return "Start outside NMZ.\n\n" + "Flick Prayers will flick Quick Prayers. Set up offensive/defensive before turning on.\n\n" +
				 "If Power Surge is enabled, make sure your Main gear and Spec gear is configured. Item names are required, separated by a comma e.g 'Dharok's greataxe, Berserker ring'";
	}

	@ConfigSection(
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
			keyName = "FlickPrayer",
			name = "Flick Prayers",
			description = "Should quick prayer be flicked?",
			position = 9,
			section = "setupConfig"
	)
	default boolean FlickPrayer() {
		return false;
	}

	@ConfigItem(
			keyName = "RockCake",
			name = "Rock Cake",
			description = "Use Dwarven rock cake?",
			position = 10,
			section = "setupConfig"
	)
	default boolean RockCake() {
		return false;
	}

	@Range(
			min = 50,
			max = 999
	)
	@ConfigItem(
			keyName = "AbsorptionLowAmount",
			name = "Maintain Absorb at:",
			position = 11,
			section = "setupConfig",
			description = "Will consume absorptions if available at set amount."
	)
	default int AbsorptionLowAmount()
	{
		return 100;
	}

	@ConfigSection(
			name = "Automated Prep Config",
			description = "Automated NMZ Prep configuration section.",
			position = 13
	)
	String autoNMZPrepConfig = "autoNMZPrepConfig";

	@ConfigItem(
			keyName = "AutomatePrep",
			name = "Automate dream/prep",
			description = "Check this if you do want to automate buying pots and starting dream.",
			position = 14,
			section = "autoNMZPrepConfig"
	)
	default boolean AutomatePrep() {
		return false;
	}

	@Range(
			min = 0,
			max = 28
	)
	@ConfigItem(
			keyName = "AbsorptionPotions",
			name = "# of Absorptions",
			description = "# of full (4) Absorption potions to withdraw from reserve.",
			position = 15,
			section = "autoNMZPrepConfig"
	)
	default int AbsorptionPotions()
	{
		return 10;
	}

	@Range(
			min = 0,
			max = 28
	)
	@ConfigItem(
			keyName = "OverloadPotions",
			name = "# of Overloads",
			description = "# of full (4) Overload potions to withdraw from reserve.",
			position = 16,
			section = "autoNMZPrepConfig"
	)
	default int OverloadPotions()
	{
		return 10;
	}

	@ConfigSection(
			name = "Power Surge Config",
			description = "Power surge spec gear swap.",
			position = 17
	)
	String powerSurgeConfig = "powerSurgeConfig";


	@ConfigItem(
			keyName = "PowerSurge",
			name = "Power Surge",
			description = "Use Power Surge power-up?",
			position = 18,
			section = "powerSurgeConfig"
	)
	default boolean PowerSurge() {
		return false;
	}

	@ConfigItem(
			keyName = "Main Gear",
			name = "Main Gear",
			description = "Main gear names",
			position = 19,
			section = "powerSurgeConfig"
	)
	default String MainWeapons() {
		return "";
	}

	@ConfigItem(
			keyName = "Spec Attack Gear Swap",
			name = "Spec Attack Gear Swap",
			description = "Gear to use special attack with during power surge.",
			position = 20,
			section = "powerSurgeConfig"
	)
	default String SpecWeapons() {
		return "";
	}

	@ConfigSection(
		name = "Game Tick Configuration",
		description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
		position = 57,
		closedByDefault = true
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


	@ConfigItem(
		keyName = "enableUI",
		name = "Enable UI",
		description = "Enable to turn on in game UI",
			position = 140
	)
	default boolean enableUI()
	{
		return true;
	}

}
