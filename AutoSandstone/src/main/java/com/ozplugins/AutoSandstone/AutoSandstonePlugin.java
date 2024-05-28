package com.ozplugins.AutoSandstone;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.PacketUtils.WidgetInfoExtended;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
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
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import org.apache.commons.lang3.ArrayUtils;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.EthanApiPlugin.EthanApiPlugin.isMoving;
import static com.ozplugins.AutoSandstone.AutoSandstoneState.*;

@PluginDescriptor(
        name = "<html><font color=\"#0394FC\">[OZ]</font> Auto Sandstone</html>",
        enabledByDefault = false,
        description = "Mines and deposits sandstone",
        tags = {"oz"}
)

public class AutoSandstonePlugin extends Plugin {
    protected static final Random random = new Random();
    Instant timer;
    boolean enablePlugin;
    @Inject
    Client client;
    @Inject
    AutoSandstoneConfiguration config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AutoSandstoneOverlay overlay;
    @Inject
    private KeyManager keyManager;
    @Inject
    private ChatMessageManager chatMessageManager;
    AutoSandstoneState state;
    int timeout = 0;
    boolean needsToCastHumidify;
    int bucketsCollected = 0;
    private static final int MINE_REGION = 12589;

    @Provides
    AutoSandstoneConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoSandstoneConfiguration.class);
    }

    @Override
    protected void startUp() {
        timeout = 0;
        enablePlugin = false;
        needsToCastHumidify = false;
        bucketsCollected = 0;
        timer = Instant.now();
        state = null;
        keyManager.registerKeyListener(pluginToggle);
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        state = null;
        timeout = 0;
        enablePlugin = false;
        needsToCastHumidify = false;
        bucketsCollected = 0;
        keyManager.unregisterKeyListener(pluginToggle);
        timer = null;
    }

    public AutoSandstoneState getState() {
        Player player = client.getLocalPlayer();

        if (!isAtMine()) {
            return UNHANDLED_STATE;
        }

        if (timeout > 0) {
            return TIMEOUT;
        }

        if (isMoving()) {
            return MOVING;
        }

        if (client.getLocalPlayer().getAnimation() == 624) {
            overlay.infoStatus = "Mining";
            return ANIMATING;
        }

        humidifyCheck();
        if (config.Humidify() && needsToCastHumidify) {
            return CAST_HUMIDIFY;
        }
        if (Inventory.full()) {
            return DEPOSIT_SANDSTONE;
        }
        if (!Inventory.full()) {
            return MINE;
        }

        return UNHANDLED_STATE;
    }

    @Subscribe
    public void onGameTick(GameTick e) {
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

            case MINE:
                handleMineSandstone();
                break;

            case DEPOSIT_SANDSTONE:
                handleDepositSandstone();
                break;

            case CAST_HUMIDIFY:
                handleHumidify();
                break;

            case UNHANDLED_STATE:
                overlay.infoStatus = "Dead";
                break;

            case ANIMATING:
            case MOVING:
            case IDLE:
                timeout = tickDelay();
                break;
        }
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
            sendGameMessage("Auto Sandstone disabled.");
        } else {
            sendGameMessage("Auto Sandstone enabled.");
        }
    }

    public void handleMineSandstone() {
        TileObjects.search().withName("Sandstone rocks").nearestToPoint(new WorldPoint(3165, 2914, 0)).ifPresent(x -> {
            overlay.infoStatus = "Mine sandstone";
            TileObjectInteraction.interact(x, "Mine");
        });
    }

    public void handleDepositSandstone() {
        TileObjects.search().withId(26199).nearestByPath().ifPresent(x -> {
            overlay.infoStatus = "Depositing Sandstone";
            TileObjectInteraction.interact(x, "Deposit");
            int s1kg = Inventory.search().withId(ItemID.SANDSTONE_1KG).result().size();
            int s2kg = Inventory.search().withId(ItemID.SANDSTONE_2KG).result().size() * 2;
            int s5kg = Inventory.search().withId(ItemID.SANDSTONE_5KG).result().size() * 4;
            int s10kg = Inventory.search().withId(ItemID.SANDSTONE_10KG).result().size() * 8;
            bucketsCollected += s1kg + s2kg + s5kg + s10kg;
            timeout = tickDelay();
        });
    }

    public void handleHumidify() {
        overlay.infoStatus = "Casting humidify";
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetAction(client.getWidget(WidgetInfoExtended.SPELL_HUMIDIFY.getPackedId()), "Cast");
        timeout = tickDelay();
    }

    public void humidifyCheck() {
        int sizeEmpty = Inventory.search().withId(ItemID.WATERSKIN0).result().size();
        int sizeFilled = Inventory.search().nameContains("Waterskin").result().size();

        if (sizeEmpty > 0) {
            if (sizeEmpty == sizeFilled) {
                needsToCastHumidify = true;
                return;
            }
        }
        needsToCastHumidify = false;
    }

    public boolean isAtMine() {
        return ArrayUtils.contains(client.getMapRegions(), MINE_REGION);
    }


    private int tickDelay() {
        int tickLength = (int) randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
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
