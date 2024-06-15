package com.ozplugins;

import net.runelite.client.config.*;

@ConfigGroup("AutoMiner")
public interface AutoMinerConfiguration extends Config
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
		return "Miner plugin. Set hotkey up and activate plugin with the hotkey.";
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
			keyName = "MineMode",
			name = "Mine mode:",
			position = 7,
			section = "setupConfig",
			description = "Select your mining method."
	)
	default Mode Mode() {
		return Mode.MINING_GUILD;
	}


	@ConfigSection(
			name = "Guild Configuration",
			description = "Mining Guild configuration section.",
			position = 8
	)
	String guildConfig = "guildConfig";

	@ConfigItem(
			keyName = "guildRock",
			name = "Rock",
			position = 9,
			section = "guildConfig",
			description = "What rock do you want to mine."
	)
	default MiningGuildRocks RockToMine() {
		return MiningGuildRocks.IRON_1;
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
