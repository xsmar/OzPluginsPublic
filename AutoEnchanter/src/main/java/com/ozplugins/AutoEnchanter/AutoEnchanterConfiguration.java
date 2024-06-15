package com.ozplugins.AutoEnchanter;

import net.runelite.client.config.*;

@ConfigGroup("AutoEnchanter")
public interface AutoEnchanterConfiguration extends Config
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
		return "Supports various bolts and jewelry.\n\n" + "Supports casting with Runes or Rune Pouch only for now. Does not verify the runes in your Rune Pouch, make sure you have the right ones. \n\nStart at any bank. Choose your mode of enchantment and corresponding settings. \n\n"  +
				"Does not yet support withdrawing runes if you run out and doesn't support staffs. \n\nSet hotkey up and activate plugin with the hotkey.";
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
			keyName = "enchantMode",
			name = "Enchantment TeleTab:",
			position = 7,
			section = "setupConfig",
			description = "What jewelry do you want to make."
	)
	default EnchantMode enchantMode() {
		return EnchantMode.JEWELRY;
	}

	@ConfigSection(
			name = "Jewelry Configuration",
			description = "Jewelry configuration section.",
			position = 8
	)
	String jewelryConfig = "jewelryConfig";

	@ConfigItem(
			keyName = "jewelryToMake",
			name = "Jewelry",
			position = 9,
			section = "jewelryConfig",
			description = "What jewelry do you want to enchant."
	)
	default Jewelry jewelryToMake() {
		return Jewelry.RING_OF_DUELING;
	}


	@ConfigSection(
			name = "Bolt Configuration",
			description = "Bolt configuration section.",
			position = 10
	)
	String boltsConfig = "boltsConfig";

	@ConfigItem(
			keyName = "bolt1Tick",
			name = "Bolt 1 Tick Enchant",
			description = "Enable to 1 Tick bolt enchant.",
			section = "boltsConfig",
			position = 11
	)
	default boolean enableBolt1Tick()
	{
		return false;
	}

	@ConfigItem(
			keyName = "boltsToMake",
			name = "Bolts",
			position = 12,
			section = "boltsConfig",
			description = "What bolts do you want to enchant."
	)
	default Bolts boltsToMake() {
		return Bolts.RUBY_BOLTS_E;
	}

	@ConfigSection(
		//keyName = "delayTickConfig",
		name = "Game Tick Configuration",
		description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
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
			position = 80,
			closedByDefault = true
	)
	String UIConfig = "UIConfig";

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
