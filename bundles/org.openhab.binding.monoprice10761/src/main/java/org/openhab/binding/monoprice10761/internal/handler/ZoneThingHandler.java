/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.monoprice10761.internal.handler;

import static org.openhab.binding.monoprice10761.internal.Monoprice10761BindingConstants.*;

import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.monoprice10761.internal.Message;
import org.openhab.binding.monoprice10761.internal.config.Monoprice10761ZoneConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for handling a Zone type Thing.
 *
 * @author Cody Cutrer - Initial Contribution
 */
public class ZoneThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ZoneThingHandler.class);

    /** Bridge Handler for the Thing. */
    public AmpHandler bridgeHandler = null;

    /** Alarm Properties. */

    private boolean thingHandlerInitialized = false;

    /** Zone Number. */
    private int zoneNumber;

    /**
     * Constructor.
     *
     * @param thing
     */
    public ZoneThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Thing handler - Thing ID: {}.", this.getThing().getUID());

        getConfiguration();

        // set the Thing offline for now
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());

        this.setThingHandlerInitialized(false);

        super.dispose();
    }

    /**
     * Method to Initialize Thing Handler.
     */
    public void initializeThingHandler() {
        if (getBridgeHandler() != null) {
            if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                Thing thing = getThing();
                List<Channel> channels = thing.getChannels();
                logger.debug("initializeThingHandler(): Initialize Thing Handler - {}", thing.getUID());

                for (Channel channel : channels) {
                    updateChannel(channel.getUID(), 0);
                }

                this.setThingHandlerInitialized(true);

                logger.debug("initializeThingHandler(): Thing Handler Initialized - {}", thing.getUID());
            } else {
                logger.debug("initializeThingHandler(): Thing '{}' Unable To Initialize Thing Handler!: Status - {}",
                        thing.getUID(), thing.getStatus());
            }
        }
    }

    /**
     * Get the Bridge Handler.
     *
     * @return brdigeHandler
     */
    public synchronized AmpHandler getBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();

            if (bridge == null) {
                logger.debug("getBridgeHandler(): Unable to get bridge!");
                return null;
            }

            logger.debug("getBridgeHandler(): Bridge for '{}' - '{}'", getThing().getUID(), bridge.getUID());

            ThingHandler handler = bridge.getHandler();

            if (handler instanceof AmpHandler) {
                this.bridgeHandler = (AmpHandler) handler;
            } else {
                logger.debug("getBridgeHandler(): Unable to get bridge handler!");
            }
        }

        return this.bridgeHandler;
    }

    public void updateChannel(ChannelUID channelUID, int state) {
        logger.debug("updateChannel(): Zone Channel UID: {} {}", channelUID, state);

        OnOffType onOffType;

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case VOLUME:
                    double scaledVolume = state;
                    updateState(channelUID, new PercentType((int) Math.round(scaledVolume * 2.63d)));
                    break;
                case POWER:
                case MUTE:
                case DND:
                case PA:
                case KEYPAD:
                    onOffType = (state > 0) ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case SOURCE:
                case TREBLE:
                case BASS:
                case BALANCE:
                    updateState(channelUID, new DecimalType(state));
                    break;
                default:
                    logger.debug("updateChannel(): Zone Channel not updated - {}.", channelUID);
                    break;
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand(): Command Received - {} {}.", channelUID, command);

        if (command instanceof RefreshType) {
            return;
        }

        if (bridgeHandler != null && bridgeHandler.isConnected()) {
            String ampCommand = "";
            int param = -1;

            switch (channelUID.getId()) {
                case POWER:
                case MUTE:
                case DND:
                    if (command instanceof OnOffType) {
                        param = ((OnOffType) command == OnOffType.ON) ? 1 : 0;
                    }
                    break;
                case VOLUME:
                    if (command instanceof DecimalType) {
                        param = (int) Math.round(((DecimalType) command).doubleValue() / 2.63);
                    } else if (command instanceof OnOffType) {
                        param = ((OnOffType) command == OnOffType.ON) ? 19 : 0;
                    }

                    break;
                case SOURCE:
                case TREBLE:
                case BASS:
                case BALANCE:
                    if (command instanceof DecimalType) {
                        param = ((DecimalType) command).intValue();
                    }
                    break;
            }

            switch (channelUID.getId()) {
                case POWER:
                    ampCommand = "PR";
                    break;
                case SOURCE:
                    ampCommand = "CH";
                    if (param < 1 || param > 6) {
                        param = -1;
                    }
                    break;
                case VOLUME:
                    ampCommand = "VO";
                    if (param < 0) {
                        param = 0;
                    } else if (param > 38) {
                        param = 38;
                    }
                    break;
                case MUTE:
                    ampCommand = "MU";
                    break;
                case DND:
                    ampCommand = "DT";
                    break;
                case TREBLE:
                    ampCommand = "TR";
                    if (param < 0) {
                        param = 0;
                    } else if (param > 14) {
                        param = 14;
                    }
                    break;
                case BASS:
                    ampCommand = "BS";
                    if (param < 0) {
                        param = 0;
                    } else if (param > 14) {
                        param = 14;
                    }
                    break;
                case BALANCE:
                    ampCommand = "BL";
                    if (param < 0) {
                        param = 0;
                    } else if (param > 20) {
                        param = 20;
                    }
                    break;
            }

            if (param != -1) {
                bridgeHandler.sendCommand(getZoneNumber(), ampCommand, param);
                updateChannel(channelUID, param);
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(bridgeStatusInfo.getStatus());
            this.initializeThingHandler();
        } else {
            this.setThingHandlerInitialized(false);
        }

        logger.debug("bridgeStatusChanged(): Bridge Status: '{}' - Thing '{}' Status: '{}'!", bridgeStatusInfo,
                getThing().getUID(), getThing().getStatus());
    }

    /**
     * Get the thing configuration.
     */
    private void getConfiguration() {
        Monoprice10761ZoneConfiguration zoneConfiguration = getConfigAs(Monoprice10761ZoneConfiguration.class);
        setZoneNumber(zoneConfiguration.zoneNumber.intValue());
    }

    /**
     * Get Zone Number.
     *
     * @return zoneNumber
     */
    public int getZoneNumber() {
        return zoneNumber;
    }

    /**
     * Set Zone Number.
     *
     * @param zoneNumber
     */
    public void setZoneNumber(int zoneNumber) {
        this.zoneNumber = zoneNumber;
    }

    /**
     * Get Channel by ChannelUID.
     *
     * @param channelUID
     */
    public Channel getChannel(ChannelUID channelUID) {
        Channel channel = null;

        List<Channel> channels = getThing().getChannels();

        for (Channel ch : channels) {
            if (channelUID == ch.getUID()) {
                channel = ch;
                break;
            }
        }

        return channel;
    }

    /**
     * Get Thing Handler refresh status.
     *
     * @return thingRefresh
     */
    public boolean isThingHandlerInitialized() {
        return thingHandlerInitialized;
    }

    /**
     * Set Thing Handler refresh status.
     *
     * @param deviceInitialized
     */
    public void setThingHandlerInitialized(boolean refreshed) {
        this.thingHandlerInitialized = refreshed;
    }

    public void eventReceived(Message message, Thing thing) {
        if (thing != null) {
            if (getThing().equals(thing)) {
                ChannelUID channelUID = null;

                channelUID = new ChannelUID(getThing().getUID(), PA);
                updateChannel(channelUID, message.paStatus);
                channelUID = new ChannelUID(getThing().getUID(), POWER);
                updateChannel(channelUID, message.powerStatus);
                channelUID = new ChannelUID(getThing().getUID(), MUTE);
                updateChannel(channelUID, message.muteStatus);
                channelUID = new ChannelUID(getThing().getUID(), DND);
                updateChannel(channelUID, message.dndStatus);
                channelUID = new ChannelUID(getThing().getUID(), VOLUME);
                updateChannel(channelUID, message.volumeStatus);
                channelUID = new ChannelUID(getThing().getUID(), TREBLE);
                updateChannel(channelUID, message.trebleStatus);
                channelUID = new ChannelUID(getThing().getUID(), BASS);
                updateChannel(channelUID, message.bassStatus);
                channelUID = new ChannelUID(getThing().getUID(), BALANCE);
                updateChannel(channelUID, message.balanceStatus);
                channelUID = new ChannelUID(getThing().getUID(), SOURCE);
                updateChannel(channelUID, message.sourceStatus);
                channelUID = new ChannelUID(getThing().getUID(), KEYPAD);
                updateChannel(channelUID, message.keypadStatus);
            }
        }
    }
}
