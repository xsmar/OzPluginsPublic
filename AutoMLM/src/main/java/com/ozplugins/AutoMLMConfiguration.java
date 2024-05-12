package com.ozplugins;

import com.ozplugins.constants.Hopper;
import com.ozplugins.constants.MineArea;
import com.ozplugins.constants.Sack;
import net.runelite.client.config.*;

@ConfigGroup("AutoMLM")
public interface AutoMLMConfiguration extends Config
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
		return "Start at Motherlode mine. \n\nMust have hammer in inventory if you have the Fix Wheel option activated."
				+ "\n\nSet hotkey up and activate plugin with the hotkey.";
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
			keyName = "mineArea",
			name = "What area to mine:",
			position = 7,
			section = "setupConfig",
			description = "Input the area where you want to mine at."
	)
	default MineArea mineArea() {
		return MineArea.UPPER_1;
	}

	@ConfigItem(
			keyName = "hopper",
			name = "Hopper:",
			position = 8,
			section = "setupConfig",
			description = "Input the area where you want to mine at."
	)
	default Hopper hopper() {
		return Hopper.LOWER;
	}

	@ConfigItem(
			keyName = "sackSize",
			name = "Sack:",
			position = 9,
			section = "setupConfig",
			description = "Input the area where you want to mine at."
	)
	default Sack sack() {
		return Sack.REGULAR;
	}

	@ConfigItem(
			keyName = "useSpec",
			name = "Use Spec",
			description = "Use Dragon pickaxe spec?",
			position = 10,
			section = "setupConfig"
	)
	default boolean useSpec() { return false; }

	@ConfigSection(
		//keyName = "delayTickConfig",
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
