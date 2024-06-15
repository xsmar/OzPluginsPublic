package com.ozplugins.AutoEnchanter;

import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.BankInventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.ozplugins.AutoEnchanter.AutoEnchanterState.*;

@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "<html>[<font color=\"#0394FC\">OZ</font>] Auto Enchanter</font></html>",
        enabledByDefault = false,
        description = "Enchants various items for you..",
        tags = {"Oz", "Ethan"}
)
@Slf4j
public class AutoEnchanterPlugin extends Plugin {
    protected static final Random random = new Random();
    Instant botTimer;
    boolean enablePlugin;

    @Inject
    Client client;
    @Inject
    PluginManager pluginManager;
    @Inject
    AutoEnchanterConfiguration config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ConfigManager configManager;
    @Inject
    private AutoEnchanterOverlay overlay;
    @Inject
    private ClientThread clientThread;
    @Inject
    EthanApiPlugin api;
    @Inject
    private KeyManager keyManager;
    @Inject
    private ChatMessageManager chatMessageManager;
    AutoEnchanterState state;
    int timeout = 0;
    int idleCount = 0;
    Set<Integer> requiredRunes;
    int secondary = 0;

    @Provides
    AutoEnchanterConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoEnchanterConfiguration.class);
    }

    @Override
    protected void startUp() {
        timeout = 0;
        idleCount = 0;
        secondary = 0;
        enablePlugin = false;
        botTimer = Instant.now();
        state = null;
        requiredRunes = null;
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
        secondary = 0;
        enablePlugin = false;
        keyManager.unregisterKeyListener(pluginToggle);
        requiredRunes = null;
        botTimer = null;
    }

    public AutoEnchanterState getState() {
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

        if (config.enchantMode() == EnchantMode.JEWELRY && hasRequiredSecondary() && hasRunes() && hasMagicLevel()) {
            return ENCHANT_JEWELRY;
        } else if (hasRequiredSecondary() && hasRunes() && hasMagicLevel()) {
            return ENCHANT_BOLTS;
        } else if (isBankOpen() && (!hasRequiredSecondary() || !hasRunes())) {
            return HANDLE_BANK;
        } else if (!isBankOpen() && (!hasRequiredSecondary() || !hasRunes())) {
            return FIND_BANK;
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
        switch (config.enchantMode()) {
            case JEWELRY:
                requiredRunes = config.jewelryToMake().getRequirements();
                secondary = config.jewelryToMake().getRequiredJewelry();
                break;

            case BOLTS:
                requiredRunes = config.boltsToMake().getRequirements();
                secondary = config.boltsToMake().getRequiredBolts();
                break;
        }

        state = getState();
        switch (state) {
            case TIMEOUT:
                timeout--;
                break;

            case FIND_BANK:
                openNearestBank();
                break;

            case ENCHANT_JEWELRY:
                handleEnchantJewelry();
                break;

            case ENCHANT_BOLTS:
                handleEnchantBolts();
                break;

            case HANDLE_BANK:
                handleBank();
                break;

            case UNHANDLED_STATE:
            case MOVING:
            case ANIMATING:
            case BANK_PIN:
            case IDLE:
                overlay.infoStatus = "Oh no";
                timeout = tickDelay();
                break;
        }
    }

    private void handleEnchantBolts() {
        overlay.infoStatus = "Enchanting bolts";
        Optional<Widget> bolts = Inventory.search().withId(secondary).first();
        Widget enchant_spell = client.getWidget(14286857);
        Widget make_bolt = client.getWidget(17694734);

        if (make_bolt != null) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePause(17694734, 1);
        }
        if (bolts.isPresent() && enchant_spell != null) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(enchant_spell, "Cast");
        }
        if (!config.enableBolt1Tick()) { timeout = tickDelay(); }
    }

    private void handleEnchantJewelry() {
        overlay.infoStatus = "Enchanting jewelry";
        Optional<Widget> jewelry = Inventory.search().withId(secondary).first();
        Widget enchant_spell = client.getWidget(config.jewelryToMake().getWidgetID());

        if (jewelry.isPresent() && enchant_spell != null) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnWidget(enchant_spell, jewelry.get());
        }
        timeout = 2;
    }

    private void handleBank() {
        if (isBankOpen()) {
            BankInventory.search().filter(x -> !requiredRunes.contains(x.getItemId())
            && !x.getName().contains("(e)")).first().ifPresent(x ->
                    BankInventoryInteraction.useItem(x, "Deposit-All"));

            if (!hasRunes()) {
                overlay.infoStatus = "Withdrawing runes";
                //TODO Handle withdraw runes

                BankInventory.search().filter(x -> requiredRunes.contains(x.getItemId())
                        ).first().ifPresent(x ->
                        BankInventoryInteraction.useItem(x, "Deposit-All"));

            } else if (!hasRequiredSecondary()) {
                overlay.infoStatus = "Withdrawing items";
                if (config.enchantMode() == EnchantMode.JEWELRY) {
                    Bank.search().withId(secondary).first().ifPresent(x -> BankInteraction.withdrawX(x, 30));
                } else {
                    Bank.search().withId(secondary).first().ifPresent(x -> BankInteraction.withdrawX(x, 500));
                }
            }
        }
    }

    public boolean hasRunes() { //Clean up plox
        if (isBankOpen()) {
            return new HashSet<>(BankInventory.search().result().stream().map(Widget::getItemId).collect(Collectors.toList())).containsAll(requiredRunes)
                    || BankInventory.search().withName("Rune pouch").first().isPresent();
        }
        return new HashSet<>(Inventory.search().result().stream().map(Widget::getItemId).collect(Collectors.toList())).containsAll(requiredRunes)
                || Inventory.search().withName("Rune pouch").first().isPresent();
    }

    private boolean hasMagicLevel() {
        int magicLevel = client.getBoostedSkillLevel(Skill.MAGIC);
        return magicLevel >= config.jewelryToMake().getRequiredMagicLevel();
    }

    private boolean hasRequiredSecondary() {
        if (!isBankOpen()) {
            return Inventory.search().withId(secondary).first().isPresent();
        } else {
            return BankInventory.search().withId(secondary).first().isPresent();
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
            sendGameMessage("Auto Enchanter disabled.");
        } else {
            sendGameMessage("Auto Enchanter enabled.");
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
        if (!isBankOpen()) {
            Optional<TileObject> bank_chest = TileObjects.search().withName("Bank chest").withAction("Use").nearestToPlayer();
            Optional<TileObject> bank_booth = TileObjects.search().withName("Bank booth").withAction("Bank").nearestToPlayer();
            Optional<NPC> banker = NPCs.search().withName("Banker").withAction("Bank").nearestToPlayer();

            overlay.infoStatus = "Banking";
            if (bank_chest.isPresent()) {
                TileObjectInteraction.interact(bank_chest.get(), "Use");
            } else if (banker.isPresent()) {
                NPCInteraction.interact(banker.get(), "Bank");
            } else if (bank_booth.isPresent()) {
                TileObjectInteraction.interact(bank_booth.get(), "Bank");
            } else {
                overlay.infoStatus = "Bank not found";
                sendGameMessage("Bank not found, turning off plugin.");
                enablePlugin = false;
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
