package com.ozplugins;

import com.example.EthanApiPlugin.Collections.BankInventory;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.ozplugins.AutoMinerState.*;

@PluginDescriptor(
        name = "<html>[<font color=\"#0394FC\">OZ</font>] Auto Miner</font></html>",
        enabledByDefault = false,
        description = "Miner plugin..",
        tags = {"Oz", "Ethan"}
)
@Slf4j
public class AutoMinerPlugin extends Plugin {
    protected static final Random random = new Random();
    Instant botTimer;
    boolean enablePlugin;

    @Inject
    Client client;
    @Inject
    PluginManager pluginManager;
    @Inject
    AutoMinerConfiguration config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ConfigManager configManager;
    @Inject
    private AutoMinerOverlay overlay;
    @Inject
    private ClientThread clientThread;
    @Inject
    private KeyManager keyManager;
    @Inject
    private ChatMessageManager chatMessageManager;
    AutoMinerState state;
    int timeout = 0;
    UISettings uiSetting;

    @Provides
    AutoMinerConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoMinerConfiguration.class);
    }

    @Override
    protected void startUp() {
        timeout = 0;
        enablePlugin = false;
        botTimer = Instant.now();
        state = null;
        uiSetting = config.UISettings();
        keyManager.registerKeyListener(pluginToggle);
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        resetVals();
    }

    private void resetVals() {
        overlayManager.remove(overlay);
        state = null;
        timeout = 0;
        enablePlugin = false;
        keyManager.unregisterKeyListener(pluginToggle);
        uiSetting = null;
        botTimer = null;
    }

    public AutoMinerState getState() {
        Player player = client.getLocalPlayer();

        if (player == null) {
            return UNHANDLED_STATE;
        }
        if (timeout > 0) {
            return TIMEOUT;
        }
        if (EthanApiPlugin.isMoving()) {
            return MOVING;
        }
        if (isBankPinOpen()) {
            overlay.infoStatus = "Bank Pin";
            return BANK_PIN;
        }

        if (client.getLocalPlayer().getAnimation() != -1) {
            return ANIMATING;
        }

        switch (config.Mode()) {
            case MINING_GUILD:
                if (isBankOpen() && Inventory.full()) {
                    return HANDLE_BANK;
                }
                if (!Inventory.full() || (isBankOpen() && BankInventory.search().empty())) {
                    return MINE;
                }
                if (Inventory.full()) {
                    return FIND_BANK;
                }
                break;

            case POWERMINE:

                break;
        }

        return UNHANDLED_STATE;
    }

    @Subscribe
    private void onGameTick(GameTick tick) {
        if (!enablePlugin) {
            return;
        }
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        uiSetting = config.UISettings();
        state = getState();

        switch (state) {
            case TIMEOUT:
                timeout--;
                break;

            case HANDLE_BANK:
                depositAll();
                break;

            case MINE:
                handleMine();
                break;

            case FIND_BANK:
                openNearestBank();
                break;

            case UNHANDLED_STATE:
                overlay.infoStatus = "ded";
                break;

            case MOVING:
            case ANIMATING:
            case BANK_PIN:
            case IDLE:
                timeout = tickDelay();
                break;
        }
    }

    private void handleMine() {
        Optional<TileObject> rock = TileObjects.search().withName(config.RockToMine().getRockName() + " rocks").withAction("Mine").nearestToPoint(config.RockToMine().getMinePoint());
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

        if (config.RockToMine().getRockName() == "Iron" && playerLocation.distanceTo(config.RockToMine().getMinePoint()) != 0) {
            overlay.infoStatus = "Moving to mine spot";
            MousePackets.queueClickPacket();
            MovementPackets.queueMovement(config.RockToMine().getMinePoint());
            return;
        }
        if (rock.isPresent()) {
            overlay.infoStatus = "Mining " + config.RockToMine().getRockName();
            TileObjectInteraction.interact(rock.get(), "Mine");
        } else {
            overlay.infoStatus = "Waiting for " + config.RockToMine().getRockName();
            timeout = tickDelay();
        }
    }

    private void depositAll() {
        overlay.infoStatus = "Depositing inventory";
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, 786474, -1, -1);
    }


    private final HotkeyListener pluginToggle = new HotkeyListener(() -> config.toggle()) {
        @Override
        public void hotkeyPressed() {
            togglePlugin();
        }
    };

    public void togglePlugin() {
        enablePlugin = !enablePlugin;
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        if (!enablePlugin) {
            sendGameMessage("Auto Miner disabled.");
        } else {
            sendGameMessage("Auto Miner enabled.");
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (!enablePlugin) {
            return;
        }

        ChatMessageType chatMessageType = event.getType();

        if (chatMessageType != ChatMessageType.GAMEMESSAGE && chatMessageType != ChatMessageType.SPAM) {
            return;
        }
    }

    //TODO Move all of this back into API
    private int tickDelay() {
        int tickLength = (int) randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
        log.debug("tick delay for {} ticks", tickLength);
        return tickLength;
    }

    public long randomDelay(boolean weightedDistribution, int min, int max, int deviation, int target) {
        if (weightedDistribution) {
            return (long) clamp((-Math.log(Math.abs(random.nextGaussian()))) * deviation + target, min, max);
        } else {
            /* generate a normal even distribution random */
            return (long) clamp(Math.round(random.nextGaussian() * deviation + target), min, max);
        }
    }

    private double clamp(double val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public void openNearestBank() {
        Optional<TileObject> bank = TileObjects.search().withName("Bank chest").nearestToPlayer();

        //Opens bank
        if (!isBankOpen()) {
            if (bank.isPresent()) {
                overlay.infoStatus = "Banking";
                TileObjectInteraction.interact(bank.get(), "Use");
            } else {
                overlay.infoStatus = "Bank not found";
            }
        }
        timeout = tickDelay();
    }

    public int getRandomIntBetweenRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public void sendGameMessage(String message) {
        String chatMessage = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(message)
                .build();

        chatMessageManager
                .queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
    }

    public boolean isBankOpen() {
        return (client.getWidget(WidgetInfo.BANK_CONTAINER) != null);
    }

    public boolean isBankPinOpen() {
        return (client.getWidget(WidgetInfo.BANK_PIN_CONTAINER) != null);
    }

}
