package com.ozplugins.GuildWoodcutter;

import com.example.EthanApiPlugin.*;
import com.example.EthanApiPlugin.Collections.*;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.ObjectPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
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
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.ozplugins.GuildWoodcutter.GuildWoodcutterState.*;

@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "<html>[<font color=\"#0394FC\">OZ</font>] Auto Guild WCer</font></html>",
        enabledByDefault = false,
        description = "Woodcuts at the WC guild for you..",
        tags = {"Oz"}
)
@Slf4j
public class GuildWoodcutterPlugin extends Plugin {
    protected static final Random random = new Random();
    Instant botTimer;
    boolean enablePlugin;

    @Inject
    Client client;
    @Inject
    PluginManager pluginManager;
    @Inject
    MousePackets mousePackets;
    @Inject
    ObjectPackets objectPackets;
    @Inject
    GuildWoodcutterConfiguration config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ConfigManager configManager;
    @Inject
    private GuildWoodcutterOverlay overlay;
    @Inject
    private ClientThread clientThread;
    @Inject
    EthanApiPlugin api;
    @Inject
    private KeyManager keyManager;
    @Inject
    private ChatMessageManager chatMessageManager;
    GuildWoodcutterState state;
    int timeout = 0;
    boolean dropLogs;
    String uiSetting = "";

    @Provides
    GuildWoodcutterConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(GuildWoodcutterConfiguration.class);
    }

    @Override
    protected void startUp() {
        timeout = 0;
        enablePlugin = false;
        botTimer = Instant.now();
        dropLogs = false;
        state = null;
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
        dropLogs = false;
        enablePlugin = false;
        keyManager.unregisterKeyListener(pluginToggle);
        uiSetting = null;
        botTimer = null;
    }

    public GuildWoodcutterState getState() {
        Player player = client.getLocalPlayer();

        if (player == null) {
            return UNHANDLED_STATE;
        }
        if (timeout > 0) {
            return TIMEOUT;
        }
        if (isBankPinOpen()) {
            return BANK_PIN;
        }
        if (EthanApiPlugin.isMoving()) {
            overlay.infoStatus = "Moving";
            return MOVING;
        }

        if (client.getLocalPlayer().getAnimation() != -1) {
            overlay.infoStatus = "Chopping";
            return ANIMATING;
        }

        if (config.BirdNests() && !Inventory.full()) {
            Optional<ETileItem> nest = TileItems.search().withName("Bird nest").nearestToPlayer();
            if (nest.isPresent()) {
                return PICK_UP_NEST;
            }
        }
        if (!isBankOpen()) {
            if (dropLogs || (config.dropLogs() && Inventory.full())) {
                return DROP_LOGS;
            } else if (!Inventory.full()) {
                return CHOP_TREE;
            } else {
                return FIND_BANK;
            }
        } else {
            if (bankInventoryHasLogs()) {
                return DEPOSIT_LOGS;
            } else if (!bankInventoryFull()) {
                return CHOP_TREE;
            }
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

        state = getState();
        switch (state) {
            case TIMEOUT:
                timeout--;
                break;

            case FIND_BANK:
                openNearestBank();
                break;

            case DEPOSIT_LOGS:
                handleDepositLogs();
                break;

            case PICK_UP_NEST:
                handleNest();
                break;

            case CHOP_TREE:
                handleTree();
                break;

            case DROP_LOGS:
                handleDropLogs();
                break;

            case UNHANDLED_STATE:
                overlay.infoStatus = "Ded";
                break;

            case MOVING:
            case BANK_PIN:
            case IDLE:
                timeout = tickDelay();
                break;
        }
    }


    public void openNearestBank() {
        Optional<TileObject> bankChest = TileObjects.search().withName("Bank chest").nearestToPlayer();
        Optional<TileObject> ropeLadder = TileObjects.search().withName("Rope ladder").nearestToPlayer();

        //Opens bank
        if (!isBankOpen()) {
            overlay.infoStatus = "Finding bank";
            if (bankChest.isPresent()) {
                overlay.infoStatus = "Banking";
                TileObjectInteraction.interact(bankChest.get(), "Use");
            } else {
                if (ropeLadder.isPresent()) {
                    MousePackets.queueClickPacket();
                    TileObjectInteraction.interact(ropeLadder.get(), "Climb-down");
                } else {
                    sendGameMessage("Error: can't find bank");
                }
            }
            timeout = tickDelay();
        }
    }

    public void handleDepositLogs() {
        overlay.infoStatus = "Depositing";
        BankInventory.search().withId(config.Trees().getLogID()).first().ifPresent(x ->{
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(x, "Deposit-All");
        });

        BankInventory.search().withName("Bird nest").first().ifPresent(x ->{
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(x, "Deposit-All");
        });
        timeout = tickDelay();
    }

    public void handleDropLogs() {
        overlay.infoStatus = "Dropping logs";
        List<Widget> itemsToDrop = Inventory.search().withId(config.Trees().getLogID()).result();
        if (!itemsToDrop.isEmpty()) { dropLogs = true; }
        for (int i = 0; i < getRandomIntBetweenRange(config.minLogDropsPerTick(), config.maxLogDropsPerTick()); i++) {
            if (itemsToDrop.size() == 0) {
                dropLogs = false;
                return;
            }
            InventoryInteraction.useItem(itemsToDrop.get(i), "Drop");
        }
        if (itemsToDrop.isEmpty()) dropLogs = false;
    }

    public void handleTree() {
        Optional<TileObject> tree = TileObjects.search().withName(config.Trees().getName() + " tree").nearestToPlayer();
        Optional<TileObject> ropeLadder = TileObjects.search().withName("Rope ladder").nearestToPlayer();

        if (tree.isPresent()) {
            overlay.infoStatus = "Chop " + config.Trees().getName();
            TileObjectInteraction.interact(tree.get(), "Chop down", "Cut");
        } else {
            if (ropeLadder.isPresent() && config.Trees().getName() == "Redwood") {
                overlay.infoStatus = "Climbing ladder";
                MousePackets.queueClickPacket();
                TileObjectInteraction.interact(ropeLadder.get(), "Climb-up");
            } else {
                sendGameMessage("Error: No tree found.");
            }
        }
        timeout = tickDelay();
    }

    public void handleNest() {
        overlay.infoStatus = "Picking up nest";
        Optional<ETileItem> nest = TileItems.search().withName("Bird nest").nearestToPlayer();
        nest.ifPresent(item -> item.interact(false));
    }

    public boolean isBankOpen() {
        return (client.getWidget(WidgetInfo.BANK_CONTAINER) != null);
    }

    public boolean bankInventoryFull() {
        return (BankInventory.search().result().size() == 28);
    }

    public boolean bankInventoryHasLogs() {
        return (!BankInventory.search().withId(config.Trees().getLogID()).empty());
    }

    public boolean isBankPinOpen() {
        Widget bankPinWidget = client.getWidget(WidgetInfo.BANK_PIN_CONTAINER);
        return (bankPinWidget != null);
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
            sendGameMessage("Auto Guild Woodcutter disabled.");
        } else {
            sendGameMessage("Auto Guild Woodcutter enabled.");
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

        MessageNode messageNode = event.getMessageNode();
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
}
