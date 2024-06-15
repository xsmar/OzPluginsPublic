package com.ozplugins.AutoUtilitySpell;

import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.Collections.query.ItemQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.BankInventoryInteraction;
import com.example.InteractionApi.InventoryInteraction;
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
import net.runelite.client.util.Text;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.ozplugins.AutoUtilitySpell.AutoUtilitySpellState.*;

@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "<html><font color=\"#0394FC\">[OZ]</font> Auto Utility Spell</font></html>",
        enabledByDefault = false,
        description = "Auto cast various utility spells..",
        tags = {"oz"}
)
@Slf4j
public class AutoUtilitySpellPlugin extends Plugin {
    protected static final Random random = new Random();
    Instant botTimer;
    boolean enablePlugin;

    @Inject
    Client client;
    @Inject
    PluginManager pluginManager;
    @Inject
    AutoUtilitySpellConfiguration config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ConfigManager configManager;
    @Inject
    private AutoUtilitySpellOverlay overlay;
    @Inject
    private ClientThread clientThread;
    @Inject
    private KeyManager keyManager;
    @Inject
    private ChatMessageManager chatMessageManager;
    AutoUtilitySpellState state;
    int timeout = 0;
    UISettings uiSetting;
    private int nextRunEnergy;
    private int spellWidgetID;
    int idleCount = 0;


    @Provides
    AutoUtilitySpellConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoUtilitySpellConfiguration.class);
    }

    @Override
    protected void startUp() {
        timeout = 0;
        nextRunEnergy = 0;
        idleCount = 0;
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
        nextRunEnergy = 0;
        idleCount = 0;
        enablePlugin = false;
        keyManager.unregisterKeyListener(pluginToggle);
        uiSetting = null;
        botTimer = null;
    }

    public AutoUtilitySpellState getState() {
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

        switch (config.Spellbook()) {
            case STANDARD:
                return setupStandardSpellbook();

            case LUNAR:
                return setupLunarSpellbook();
        }
        return UNHANDLED_STATE;
    }

    private AutoUtilitySpellState setupLunarSpellbook() {
        switch (config.LunarSpell()) {
            case SPIN_FLAX:
                if (!isBankOpen() && Inventory.search().withId(ItemID.FLAX).first().isEmpty()) {
                    return FIND_BANK;
                }
                return HANDLE_SPIN_FLAX;

            case TAN_LEATHER:
                if (!isBankOpen() && Inventory.search().withId(config.HideToTan().requiredItemID).first().isEmpty()) {
                    return FIND_BANK;
                }
                return HANDLE_TAN_LEATHER;

            case STRING_JEWELRY:
                if (!isBankOpen() && Inventory.search().withId(config.JewelryToSTring().requiredItemID).first().isEmpty()) {
                    return FIND_BANK;
                }
                return HANDLE_STRING_JEWELRY;

            case PLANK_MAKE:
                if (!isBankOpen() && Inventory.search().withId(config.PlankToMake().requiredItemID).first().isEmpty()) {
                    return FIND_BANK;
                }
                return HANDLE_PLANK_MAKE;

            case HUNTER_KIT:
                if (!isBankOpen() && hasOpenHunterKit()) {
                    return FIND_BANK;
                }
                return HANDLE_HUNTER_KIT;

           // case SUPERGLASS_MAKE:
            //break;

            case HUMIDIFY:

                break;

        }
        return UNHANDLED_STATE;
    }


    private AutoUtilitySpellState setupStandardSpellbook() {
        switch (config.StandardSpell()) {
            case LOW_ALCH:
            case HIGH_ALCH:
                return HANDLE_ALCH;

            case VARROCK_TELEPORT:
            case CAMELOT_TELEPORT:
            case FALADOR_TELEPORT:
            case ARDOUGNE_TELEPORT:
            case LUMBRIDGE_TELEPORT:
                return HANDLE_TELEPORT;
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
        idleCount = (client.getLocalPlayer().getAnimation() == -1) ? idleCount + 1 : 0;
        spellWidgetID = getSpellWidgetID();
        state = getState();
        handleRun(5, 5);

        switch (state) {
            case TIMEOUT:
                timeout--;
                break;

            case FIND_BANK:
                openNearestBank();
                break;

            case HANDLE_ALCH:
                handleAlch();
                break;

            case HANDLE_TELEPORT:
                handleTeleport();
                break;

            case HANDLE_SPIN_FLAX:
                handleSpinFlax();
                break;

            case HANDLE_STRING_JEWELRY:
                handleStringJewelry();
                break;

            case HANDLE_TAN_LEATHER:
                handleTanHides();
                break;

            case HANDLE_PLANK_MAKE:
                handlePlankMake();
                break;

            case HANDLE_HUNTER_KIT:
                handleHunterKit();
                break;

            case UNHANDLED_STATE:
                overlay.infoStatus = "Ded";
                break;

            case MOVING:
            case ANIMATING:
            case BANK_PIN:
            case IDLE:
                timeout = tickDelay();
                break;
        }
    }

    private void handleHunterKit() {
        if (isBankOpen() && hasOpenHunterKit()) {
            BankInventory.search().filter(x -> !x.getName().contains("une")).result().forEach(x -> {
                overlay.infoStatus = "Depositing";
                BankInventoryInteraction.useItem(x, "Deposit-All");
            });
            return;
        }
        if (hasHunterKit()) {
            Inventory.search().matchesWildCardNoCase("*hunter kit*").first().ifPresent(x -> {
                overlay.infoStatus = "Opening Hunter kit";
                InventoryInteraction.useItem(x, "Open");
            });
        }
        overlay.infoStatus = "Casting Hunter kit";
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetAction(client.getWidget(spellWidgetID), "Cast");
        timeout = 1;
    }

    private void handlePlankMake() {
        Widget plank_make_spell = client.getWidget(spellWidgetID);

        if (isBankOpen()) {
            BankInventory.search().filter(x -> !x.getName().contains("logs") && !x.getName().contains("une") && !x.getName().contains("Coins")).first().ifPresent(x -> {
                overlay.infoStatus = "Depositing";
                BankInventoryInteraction.useItem(x, "Deposit-All");
            });

            Optional<Widget> required_item_bank = BankInventory.search().withId(config.PlankToMake().requiredItemID).first();
            if (required_item_bank.isEmpty()) {
                Bank.search().withId(config.PlankToMake().requiredItemID).first().ifPresentOrElse(x -> {
                    overlay.infoStatus = "Withdrawing logs";
                    BankInteraction.withdrawX(x, 30);
                }, () -> {
                    sendGameMessage("Out of logs, shutting down");
                    enablePlugin = false;
                });
                return;
            }
            client.runScript(29); //close bank
            return;
        }

        Inventory.search().withId(config.PlankToMake().requiredItemID).first().ifPresent(x -> {
            overlay.infoStatus = "Casting Contract make";
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnWidget(plank_make_spell, x);
            timeout = 2;
        });
    }


    private void handleStringJewelry() {
        if (isBankOpen()) {
            BankInventory.search().filter(x -> !x.getName().contains("une") && !x.getName().contains("(u)")).first().ifPresent(x -> {
                overlay.infoStatus = "Depositing";
                BankInventoryInteraction.useItem(x, "Deposit-All");
            });

            Optional<Widget> required_item_bank = BankInventory.search().withId(config.JewelryToSTring().requiredItemID).first();
            if (required_item_bank.isEmpty()) {
                Bank.search().withId(config.JewelryToSTring().requiredItemID).first().ifPresentOrElse(x -> {
                    overlay.infoStatus = "Withdrawing jewelry";
                    BankInteraction.withdrawX(x, 30);
                }, () -> {
                    sendGameMessage("Out of jewelry, shutting down");
                    enablePlugin = false;
                });
                return;
            }
        }
        if (idleCount > 3) {
            overlay.infoStatus = "Casting String jewelry";
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(client.getWidget(spellWidgetID), "Cast");
            timeout = tickDelay();
        }
    }

    private void handleSpinFlax() {
        if (isBankOpen()) {
            BankInventory.search().withName("Bow string").first().ifPresent(x -> {
                overlay.infoStatus = "Depositing bowstring";
                BankInventoryInteraction.useItem(x, "Deposit-All");
            });

            Optional<Widget> spin_flax_required_item_bank = BankInventory.search().withId(ItemID.FLAX).first();
            if (spin_flax_required_item_bank.isEmpty()) {
                Bank.search().withId(ItemID.FLAX).first().ifPresentOrElse(x -> {
                    overlay.infoStatus = "Withdrawing flax";
                    BankInteraction.withdrawX(x, 25);
                }, () -> {
                    sendGameMessage("Out of flax, shutting down");
                    enablePlugin = false;
                });
                return;
            }
        }
        overlay.infoStatus = "Casting Spin flax";
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetAction(client.getWidget(spellWidgetID), "Cast");
        timeout = 4;
    }

    private void handleTanHides() {
        if (isBankOpen()) {
            BankInventory.search().matchesWildCardNoCase("*dragon*").filter(x -> x.getName().contains("leather")).first().ifPresent(x -> {
                overlay.infoStatus = "Depositing bowstring";
                BankInventoryInteraction.useItem(x, "Deposit-All");
            });

            Optional<Widget> required_item_bank = BankInventory.search().withId(config.HideToTan().requiredItemID).first();
            if (required_item_bank.isEmpty()) {
                Bank.search().withId(config.HideToTan().requiredItemID).first().ifPresentOrElse(x -> {
                    overlay.infoStatus = "Withdrawing dragonhide";
                    BankInteraction.withdrawX(x, 25);
                }, () -> {
                    sendGameMessage("Out of dragonhide, shutting down");
                    enablePlugin = false;
                });
                return;
            }
        }
        overlay.infoStatus = "Casting Tan hide";
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetAction(client.getWidget(spellWidgetID), "Cast");
        timeout = 2;
    }

    private void handleTeleport() {
        overlay.infoStatus = "Casting teleport";
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetAction(client.getWidget(spellWidgetID), "Cast");
        timeout = 3;
    }


    private void handleAlch() {
        List<String> alch_whitelist = getListNames(config.alchWhitelist());
        Widget alch_spell = client.getWidget(spellWidgetID);

        for (String alchItem : getListNames(config.alchItemName())) {
            nameContainsNoCase(alchItem).filter(x -> !alch_whitelist.contains(Text.removeTags(x.getName().toLowerCase()))).first().ifPresentOrElse(item -> {
                overlay.infoStatus = "Alching: " + Text.removeTags(item.getName());
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetOnWidget(alch_spell, item);
                timeout = 5;
            }, () -> {
                overlay.infoStatus = "Nothing to alch";
                timeout = tickDelay();
            });
        }
    }

    private int getSpellWidgetID() {
        switch (config.Spellbook()) {
            case LUNAR:
                return config.LunarSpell().getWidgetID();
            case STANDARD:
                return config.StandardSpell().getWidgetID();
        }
        return 0;
    }

    private boolean hasHunterKit() {
        if (!isBankOpen()) {
            return Inventory.search().matchesWildCardNoCase("*hunter kit*").first().isPresent();
        }
        return BankInventory.search().matchesWildCardNoCase("*hunter kit*").first().isPresent();
    }

    private boolean hasOpenHunterKit() {
        if (!isBankOpen()) {
            return Inventory.search().filter(x -> !x.getName().contains("une")).first().isPresent()
                    && Inventory.search().matchesWildCardNoCase("*hunter kit*").first().isEmpty();
        }
        return BankInventory.search().filter(x -> !x.getName().contains("une")).first().isPresent()
                && BankInventory.search().matchesWildCardNoCase("*hunter kit*").first().isEmpty();

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
            sendGameMessage("Auto Utility Spell disabled.");
        } else {
            sendGameMessage("Auto Utility Spell enabled.");
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

        if (event.getMessage().startsWith("You do not have enough")) {
            sendGameMessage("Don't have required runes, shutting down.");
            enablePlugin = false;
        }
    }

    private List<String> getListNames(String list) {
        return Arrays.stream(list.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public static ItemQuery nameContainsNoCase(String name) {
        return Inventory.search().filter(widget -> widget.getName().toLowerCase().contains(name.toLowerCase()));
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
        if (!isBankOpen()) { //Opens bank
            TileObjects.search().withName("Bank booth").nearestToPlayer().ifPresentOrElse(x -> {
                overlay.infoStatus = "Banking";
                TileObjectInteraction.interact(x, "Bank", "Use");
            }, () -> {
                overlay.infoStatus = "Bank not found";
            });
        }
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

    public void handleRun(int minEnergy, int randMax) {
        if (nextRunEnergy < minEnergy || nextRunEnergy > minEnergy + randMax) {
            nextRunEnergy = getRandomIntBetweenRange(minEnergy, minEnergy + getRandomIntBetweenRange(0, randMax));
        }
        if ((client.getEnergy() / 100) > nextRunEnergy ||
                client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0) {
            if (!isRunEnabled()) {
                nextRunEnergy = 0;
                Widget runOrb = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB);

                if (runOrb != null) {
                    enableRun();
                }
            }
        }
    }

    public boolean isRunEnabled() {
        return client.getVarpValue(173) == 1;
    }


    public void enableRun() {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, 10485787, -1, -1);
    }

    public boolean isBankOpen() {
        return (client.getWidget(WidgetInfo.BANK_CONTAINER) != null);
    }

    public boolean isBankPinOpen() {
        return (client.getWidget(WidgetInfo.BANK_PIN_CONTAINER) != null);
    }

}
