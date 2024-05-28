package com.ozplugins;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.PathingTesting.PathingTesting;
import com.ozplugins.AutoCombat.AutoCombatPlugin;
import com.ozplugins.AutoSandstone.AutoSandstonePlugin;
import com.ozplugins.AutoScurrius.AutoScurriusPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PluginTester {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(EthanApiPlugin.class, PacketUtilsPlugin.class, PathingTesting.class,
                AutoMLMPlugin.class,
                AutoNMZPlugin.class,
                AutoCombatPlugin.class,
                AutoScurriusPlugin.class,
                AutoSandstonePlugin.class);
        RuneLite.main(args);
    }
}