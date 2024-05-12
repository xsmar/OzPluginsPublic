package com.ozplugins;

import com.example.EthanApiPlugin.Collections.BankInventory;
import com.example.EthanApiPlugin.Collections.Equipment;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInventoryInteraction;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.ozplugins.constants.AutoMLMState;
import com.ozplugins.constants.Hopper;
import com.ozplugins.constants.MineArea;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.ozplugins.constants.AutoMLMState.*;

@PluginDescriptor(
        name = "<html><font color=\"#0394FC\">[OZ]</font> Auto MLM</font></html>",
        enabledByDefault = false,
        description = "Mines shit in the motherlode mine for you..",
        tags = {"Oz"}
)
@Slf4j
public class AutoMLMPlugin extends Plugin {
    protected static final Random random = new Random();
    Instant botTimer;
    boolean enablePlugin;

    @Inject
    Client client;
    @Inject
    AutoMLMConfiguration config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AutoMLMOverlay overlay;
    @Inject
    private KeyManager keyManager;
    @Inject
    private ChatMessageManager chatMessageManager;
    AutoMLMState state;
    int timeout = 0;
    int idleCount = 0;
    boolean depositOres = false;
    WorldPoint MinePoint;

    @Provides
    AutoMLMConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoMLMConfiguration.class);
    }

    @Override
    protected void startUp() {
        timeout = 0;
        idleCount = 0;
        enablePlugin = false;
        depositOres = false;
        botTimer = Instant.now();
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
        idleCount = 0;
        enablePlugin = false;
        depositOres = false;
        keyManager.unregisterKeyListener(pluginToggle);
        botTimer = null;
    }

    public AutoMLMState getState() {
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
        if (client.getLocalPlayer().getAnimation() == 6752) {
            overlay.infoStatus = "Mining";
            return ANIMATING;
        }
        if (idleCount < 3) {
            return IDLE;
        }
        if (needsToDepositOres()) {
            depositOres = true;
        }
        if (depositOres) {
            return DEPOSIT_BANK;
        }
        if (!Inventory.full()) {
            return MINE;
        }
        if (Inventory.full()) {
            return DEPOSIT_HOPPER;
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
        idleCount = (client.getLocalPlayer().getAnimation() == -1) ? idleCount + 1 : 0;
        MinePoint = config.mineArea().getMinePoint();

        state = getState();
        switch (state) {
            case TIMEOUT:
                timeout--;
                break;

            case FIND_BANK:
                openNearestBank();
                break;

            case MINE:
                handleGem();
                handleMineOre();
                break;

            case DEPOSIT_HOPPER:
                handleHopper();
                break;

            case DEPOSIT_BANK:
                handleDepositOres();
                break;

            case UNHANDLED_STATE:
                overlay.infoStatus = "Shit";
                break;

            case MOVING:
            case ANIMATING:
            case BANK_PIN:
            case IDLE:
                timeout = tickDelay();
                break;
        }
    }

    public void handleDepositOres() {
        if (isOnUpperFloor()) {
            handleLadder();
            return;
        }
        if (isBankOpen()) {
            overlay.infoStatus = "Banking ores";
            BankInventory.search().result().stream()
                    .filter(x -> x.getItemId() != ItemID.HAMMER && !x.getName().contains("pickaxe") && x.getItemId() != ItemID.PAYDIRT)
                    .forEach(x -> BankInventoryInteraction.useItem(x, "Deposit-All"));

            if (client.getVarbitValue(Varbits.SACK_NUMBER) > 0) {
                handleSack();
            } else {
                depositOres = false;
                return;
            }
        }

        if (Inventory.getItemAmount(12011) > 1) {
            handleHopper();
            return;
        }
        if (Inventory.search().filter(x -> x.getName().contains("ore") || x.getName().contains("Coal")).first().isPresent()) {
            openNearestBank();
            return;
        }
        if (!isBankOpen() && Inventory.search().filter(x -> x.getName().contains("ore") || x.getName().contains("Coal")).first().isEmpty()
                && client.getVarbitValue(Varbits.SACK_NUMBER) > 0) {
            handleSack();
        }
    }

    public void handleSack() {
        Optional<TileObject> sack = TileObjects.search().withName("Sack").withAction("Search").nearestToPlayer();
        if (sack.isPresent() && !Inventory.full()) {
            overlay.infoStatus = "Searching sack";
            TileObjectInteraction.interact(sack.get(), "Search");
        }
    }

    public void handleGem() {
        overlay.infoStatus = "Dropping gem";
        Inventory.search().filter(gem -> gem.getName().contains("Uncut")).first().ifPresent(x -> InventoryInteraction.useItem(x, "Drop"));
    }

    public void handleMineOre() {
        overlay.infoStatus = "Mine ore";
        if (config.useSpec() && hasSpec()) {
            useSpec();
        }
        if (((config.mineArea() == MineArea.UPPER_1 || config.mineArea() == MineArea.UPPER_2) && !isOnUpperFloor())
                || ((config.mineArea() == MineArea.LOWER_1 || config.mineArea() == MineArea.LOWER_2) && isOnUpperFloor())) {
            handleLadder();
            return;
        }
        TileObjects.search().withName("Ore vein").withAction("Mine").nearestToPoint(MinePoint).ifPresent(x -> TileObjectInteraction.interact(x, "Mine"));
    }

    private void useSpec() {
        if (!Equipment.search().matchesWildCardNoCase("*Dragon pickaxe*").empty() || !Equipment.search().matchesWildCardNoCase("*infernal pickaxe*").empty()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, 38862885, -1, -1);
        }
    }

    private boolean hasSpec() {
        return client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) == 1000;
    }

    public void handleHopper() {
        if (isOnUpperFloor()) {
            if (config.hopper() == Hopper.UPPER) {
                overlay.infoStatus = "Deposit hopper";
                TileObjects.search().withName("Hopper").withAction("Deposit").nearestByPath().ifPresent(x -> TileObjectInteraction.interact(x, "Deposit"));
                return;
            }
            handleLadder();
            return;
        }

        Inventory.search().withId(ItemID.HAMMER).first().ifPresentOrElse(x -> {
            Optional<TileObject> brokenWheel = TileObjects.search().withAction("Hammer").nearestToPlayer();
            if (brokenWheel.isPresent()) {
                overlay.infoStatus = "Fixing wheel";
                TileObjectInteraction.interact(brokenWheel.get(), "Hammer");
                return;
            }
            overlay.infoStatus = "Deposit hopper";
            TileObjects.search().withName("Hopper").withAction("Deposit").nearestByPath().ifPresent(y -> TileObjectInteraction.interact(y, "Deposit"));
        }, () -> {
            TileObjects.search().withName("Crate").withAction("Search").nearestToPoint(new WorldPoint(3752, 5674, 0)).ifPresent(x -> {
                overlay.infoStatus = "Getting Hammer";
                if (Inventory.full()) {
                    Inventory.search().withId(ItemID.PAYDIRT).first().ifPresent(z -> {
                        InventoryInteraction.useItem(z, "Drop");
                    });
                }
                TileObjectInteraction.interact(x, "Search");
            });
        });
    }

    public void handleLadder() {
        overlay.infoStatus = "Climb ladder";
        TileObjects.search().withName("Ladder").withAction("Climb").nearestToPlayer().ifPresent(x -> TileObjectInteraction.interact(x, "Climb"));
    }

    public boolean needsToDepositOres() {
        return config.sack().getSize() - (client.getVarbitValue(Varbits.SACK_NUMBER) + Inventory.getItemAmount(12011)) <= 0
                || Inventory.search().filter(x -> x.getName().contains("ore") || x.getName().contains("Coal")).first().isPresent();
    }

    public boolean isOnUpperFloor() {
        return client.getVarbitValue(2086) == 1;
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
            sendGameMessage("Auto MLM disabled.");
        } else {
            sendGameMessage("Auto MLM enabled.");
        }
    }

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
        String chatMessage = new ChatMessageBuilder().append(ChatColorType.HIGHLIGHT).append(message).build();

        chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(chatMessage).build());
    }

    public boolean isBankOpen() {
        return (client.getWidget(WidgetInfo.BANK_CONTAINER) != null);
    }

    public boolean isBankPinOpen() {
        return (client.getWidget(WidgetInfo.BANK_PIN_CONTAINER) != null);
    }

}
