package com.ozplugins;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InteractionHelper;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.ozplugins.util.Utils;
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

import static com.ozplugins.AutoNMZState.*;

@PluginDescriptor(
        name = "<html><font color=\"#0394FC\">[OZ]</font> Auto NMZ</font></html>",
        enabledByDefault = false,
        description = "Does NMZ for you..",
        tags = { "Oz" }
)
public class AutoNMZPlugin extends Plugin {
    protected static final Random random = new Random();
    Instant botTimer;
    boolean enablePlugin;

    @Inject
    Client client;
    @Inject
    AutoNMZConfiguration config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AutoNMZOverlay overlay;
    @Inject
    private KeyManager keyManager;
    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    public Utils util;

    AutoNMZState state;
    int timeout = 0;
    boolean forceTab = false;
    int absorptionPoints = 0;
    int overloadRefreshRemaining = 0;
    int overloadCakeTimeout = 16;
    int hitpoints = 0;
    int overloadSipsInReserve = 0;
    int absorptionSipsInReserve = 0;
    int specialAttack = 0;
    boolean specialAttackEnabled = false;
    boolean powerSurgeEffect = false;
    boolean hasOverload = true;
    int POWER_SURGE = 26264;

    @Provides
    AutoNMZConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoNMZConfiguration.class);
    }

    @Override
    protected void startUp() {
        timeout = 0;
        absorptionPoints = 0;
        overloadRefreshRemaining = 0;
        hitpoints = 0;
        overloadCakeTimeout = 16;
        specialAttack = 0;
        overloadSipsInReserve = 0;
        absorptionSipsInReserve = 0;
        specialAttackEnabled = false;
        powerSurgeEffect = false;
        hasOverload = true;
        enablePlugin = false;
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
        absorptionPoints = 0;
        overloadRefreshRemaining = 0;
        specialAttack = 0;
        overloadSipsInReserve = 0;
        absorptionSipsInReserve = 0;
        specialAttackEnabled = false;
        overloadCakeTimeout = 16;
        powerSurgeEffect = false;
        hasOverload = true;
        hitpoints = 0;
        enablePlugin = false;
        keyManager.unregisterKeyListener(pluginToggle);
        botTimer = null;
    }

    public AutoNMZState getState() {
        Player player = client.getLocalPlayer();

        if (player == null) {
            return UNHANDLED_STATE;
        }
        if (overloadCakeTimeout > 0) {
            overloadCakeTimeout--;
        }

        if (timeout > 0) {
            return TIMEOUT;
        }
        if (isBankPinOpen()) {
            return BANK_PIN;
        }

        if (inNMZ()) {
            return FIGHT;
        }
        if (!inNMZ() && !config.AutomatePrep()) {
            overlay.infoStatus = "Not in NMZ";
            return IDLE;
        }
        return HANDLE_PREP;
    }

    @Subscribe
    private void onGameTick(GameTick tick) {
        if (!enablePlugin) {
            return;
        }

        if (client.getGameState() != GameState.LOGGED_IN) { //This even needed? prolly not
            forceTab = false;
            return;
        }
        hitpoints = client.getBoostedSkillLevel(Skill.HITPOINTS);
        specialAttack = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
        specialAttackEnabled = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 1;
        state = getState();

        switch (state) {
            case TIMEOUT:
                timeout--;
                break;

            case FIGHT:
                //Sloppy, to redo. Do all of this elsewhere
                absorptionPoints = client.getVarbitValue(Varbits.NMZ_ABSORPTION);
                overloadRefreshRemaining = client.getVarbitValue(Varbits.NMZ_OVERLOAD_REFRESHES_REMAINING);

                if (config.FlickPrayer()) {
                    handlePrayFlick();
                } else {
                    //Deactivates prayer in case you uncheck Flick Prayers in config
                    if (EthanApiPlugin.isQuickPrayerEnabled()) {
                        overlay.infoStatus = "Deactivating prayer";
                        InteractionHelper.togglePrayer();
                    }
                }

                //Handle pots and cake
                if (absorptionPoints < config.AbsorptionLowAmount()) {
                    handleAbsorption();
                }
                if (overloadRefreshRemaining == 0 && hasOverload && hitpoints > 50) {
                    handleOverload();
                } else if (config.RockCake() && hitpoints > 1) {
                    handleRockCake();
                }
                handleFight();
                break;

            case HANDLE_PREP:
                handlePrep();
                break;

            case UNHANDLED_STATE:
                overlay.infoStatus = "Ded";
                break;

            case BANK_PIN:
            case MOVING:
            case ANIMATING:
            case IDLE:
                timeout = util.tickDelay();
                break;
        }
    }

    public void handlePrep() {
        //TODO Needs some cleanup, eyes hurt
        overloadSipsInReserve = client.getVarbitValue(3953);
        absorptionSipsInReserve = client.getVarbitValue(3954);

        timeout = util.tickDelay();
        if ((Inventory.search().matchesWildCardNoCase("*absorption*").result().size() >= config.AbsorptionPotions()
                && Inventory.search().matchesWildCardNoCase("*overload*").result().size() >= config.OverloadPotions()) || Inventory.full()) {
            handleDream();
            return;
        }
        if ((Inventory.search().matchesWildCardNoCase("*overload*").result().size() + (overloadSipsInReserve / 4)) < config.OverloadPotions()) {
            overlay.infoStatus = "Buying Overloads";
            handleBuyOverloads();
            return;
        }
        if ((Inventory.search().matchesWildCardNoCase("*absorption*").result().size() + (absorptionSipsInReserve / 4)) < config.AbsorptionPotions()) {
            overlay.infoStatus = "Buying Absorptions";
            handleBuyAbsorption();
            return;
        }

        if (config.AbsorptionPotions() - Inventory.search().matchesWildCardNoCase("*absorption*").result().size() > 0) {
            overlay.infoStatus = "Withdrawing absorption";
            if (Widgets.search().hiddenState(false).withTextContains("How many doses of absorption potion will you withdraw?").empty()) {
                TileObjectInteraction.interact(26280, "Take");
                return;
            }
            util.sendIntValue((config.AbsorptionPotions() * 4) - Inventory.search().matchesWildCardNoCase("*absorption*").result().size());
            return;
        }

        if (config.OverloadPotions() - Inventory.search().matchesWildCardNoCase("*overload*").result().size() > 0) {
            overlay.infoStatus = "Withdrawing overdose";
            if (Widgets.search().hiddenState(false).withTextContains("How many doses of overload potion will you withdraw?").empty()) {
                TileObjectInteraction.interact(26279, "Take");
                return;
            }
            util.sendIntValue((config.OverloadPotions() * 4) - Inventory.search().matchesWildCardNoCase("*overload*").result().size());
        }
    }

    public void handleBuyOverloads() {
        if (Widgets.search().hiddenState(false).withTextContains("Dom Onion's Reward Shop").empty() && !EthanApiPlugin.isMoving()) {
            TileObjectInteraction.interact(26273, "Search");
            return;
        }
        int amountToBuy = (config.OverloadPotions() - Inventory.search().matchesWildCardNoCase("*overload*").result().size() - (absorptionSipsInReserve / 4)) * 4;
        buyOverload(amountToBuy);
    }

    public void handleBuyAbsorption() {
        if (Widgets.search().hiddenState(false).withTextContains("Dom Onion's Reward Shop").empty() && !EthanApiPlugin.isMoving()) {
            TileObjectInteraction.interact(26273, "Search");
            return;
        }
        int amountToBuy = (config.AbsorptionPotions() - Inventory.search().matchesWildCardNoCase("*absorption*").result().size() - (absorptionSipsInReserve / 4)) * 4;
        buyAbsorption(amountToBuy);
    }

    public void buyOverload(int amount) {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(5, 13500422, 11733, 6);
        util.sendIntValue(amount);
    }

    public void buyAbsorption(int amount) {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(5, 13500422, 11733, 9);
        util.sendIntValue(amount);
    }

    public void handleDream() {
        if (isDreamReady()) {
            overlay.infoStatus = "Setting up dream";
            if (Widgets.search().hiddenState(false).withTextContains("Previous: Customisable Rumble (hard)").empty()) {
                NPCInteraction.interact("Dominic Onion", "Dream");
                return;
            }
            selectDream();
            return;
        }
        if (Widgets.search().hiddenState(false).withTextContains("Nazastarool").empty()) {
            overlay.infoStatus = "Entering dream";
            TileObjectInteraction.interact(26291, "Drink");
            return;
        }
        enterDream();
    }

    public void enterDream() {
        MousePackets.queueClickPacket();
        WidgetPackets.queueResumePause(8454150, -1);
    }

    public void selectDream() {
        MousePackets.queueClickPacket();
        WidgetPackets.queueResumePause(14352385, 4);
        MousePackets.queueClickPacket();
        WidgetPackets.queueResumePause(15138821, -1);
        MousePackets.queueClickPacket();
        WidgetPackets.queueResumePause(14352385, 1);
    }

    public void handleFight() {
        if (config.PowerSurge()) {
            Optional<TileObject> power = TileObjects.search().withId(POWER_SURGE).nearestToPlayer();
            if (!powerSurgeEffect && power.isPresent()) {
                getPowerSurge();
                return;
            }
            if (powerSurgeEffect) {
                //Handle Special Attack. Switch to spec gear first.
                util.toggleGear(util.getGearNames(config.SpecWeapons()));
                if (!specialAttackEnabled && specialAttack > 20) { //Over 20 since most specs are 25% or more
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetActionPacket(1, 38862885, -1, -1);
                }
            } else {
                //Swap back to main weapons if not in power surge
                util.toggleGear(util.getGearNames(config.MainWeapons()));
            }
        }

        if (!EthanApiPlugin.isMoving() && !client.getLocalPlayer().isInteracting()) {
            NPCs.search().nearestToPlayer().ifPresent(x -> {
                overlay.infoStatus = "Attacking";
                NPCInteraction.interact(x, "Attack");
            });
        } else {
            overlay.infoStatus = "Fighting";
        }
    }

    public void getPowerSurge() {
        TileObjects.search().withId(POWER_SURGE).nearestToPlayer().ifPresent(x -> {
            overlay.infoStatus = "Grabbing power surge";
            TileObjectInteraction.interact(x, "Activate");
        });
    }

    public void handleRockCake() {
        if (overloadCakeTimeout == 0 || !hasOverload) {
            Inventory.search().matchesWildCardNoCase("Dwarven*").first().ifPresent(x -> {
                overlay.infoStatus = "Guzzling cake";
                InventoryInteraction.useItem(x, "Guzzle");
            });
        }
    }

    public void handleOverload() {
        Inventory.search().matchesWildCardNoCase("*Overload*").first().ifPresentOrElse(x -> {
            overlay.infoStatus = "Drinking overload";
            hasOverload = true;
            overloadCakeTimeout = 16;
            InventoryInteraction.useItem(x, "Drink");
        }, () -> hasOverload = false);
    }

    public void handleAbsorption() {
        Inventory.search().matchesWildCardNoCase("*absorption*").first().ifPresent(x -> {
            overlay.infoStatus = "Drinking absorption";
            InventoryInteraction.useItem(x, "Drink");
        });
    }

    public void handlePrayFlick() { //Thanx ethan
        if (forceTab) {
            client.runScript(915, 3);
            forceTab = false;
        }
        if (client.getWidget(5046276) == null) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(client.getWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB), "Setup");
            forceTab = true;
        }

        if (EthanApiPlugin.isQuickPrayerEnabled()) {
            InteractionHelper.togglePrayer();
        }
        InteractionHelper.togglePrayer();
    }

    public boolean inNMZ() {
        return Arrays.stream(client.getMapRegions()).anyMatch(x -> x == 9033);
    }

    public boolean isDreamReady() {
        return client.getVarbitValue(3946) == 0;
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
            sendGameMessage("Auto NMZ disabled.");
        } else {
            sendGameMessage("Auto NMZ enabled.");
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

        if (event.getMessage().endsWith(" special attack power!")) {
            powerSurgeEffect = true;
        }
        if (event.getMessage().endsWith(" power has ended.")) {
            powerSurgeEffect = false;
        }

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
