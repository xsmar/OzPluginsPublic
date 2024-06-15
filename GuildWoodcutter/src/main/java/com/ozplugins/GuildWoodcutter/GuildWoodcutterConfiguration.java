package com.ozplugins.GuildWoodcutter;

import net.runelite.client.config.*;

@ConfigGroup("AutoGuildWoodcutter")
public interface GuildWoodcutterConfiguration extends Config
{
	String version = "v1.0";

	@ConfigItem(
			keyName = "instructions",
			name = "",
			description = "Instructions.",
			position = 1,
			section = "instructionsConfig"
	)
	default String instructions()
	{
		return "Start at Woodcutting guild bank and configure your preferences." +
				"Set hotkey up and activate plugin with the hotkey.";
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
			keyName = "TreeToChop",
			name = "Tree To Chop:",
			position = 9,
			section = "setupConfig",
			description = "Input the type of tree you'd like to chop."
	)
	default Trees Trees() {
		return Trees.YEW;
	}

	@ConfigItem(
			keyName = "dropLogs",
			name = "Drop Logs?",
			description = "Drops logs instead of banking",
			position = 10,
			section = "setupConfig"
	)
	default boolean dropLogs()
	{
		return false;
	}
	@Range(
			min = 1,
			max = 10
	)
	@ConfigItem(
			keyName = "minDropsPerTick",
			name = "Min Drops Per Tick:",
			description = "Minimum amount of logs to drop per tick if Drop Logs is active.",
			position = 11,
			section = "setupConfig"
	)
	default int minLogDropsPerTick()
	{
		return 1;
	}

	@Range(
			min = 2,
			max = 10
	)
	@ConfigItem(
			keyName = "maxDropsPerTick",
			name = "Max Drops Per Tick:",
			description = "Max amount of logs to drop per tick if Drop Logs is active.",
			position = 12,
			section = "setupConfig"
	)
	default int maxLogDropsPerTick()
	{
		return 2;
	}

	@ConfigItem(
			keyName = "BirdNests",
			name = "Pick up nests?",
			description = "Pick up bird nests?",
			position = 13,
			section = "setupConfig"
	)
	default boolean BirdNests() {
		return false;
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
