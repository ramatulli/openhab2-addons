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

import static org.openhab.binding.monoprice10761.internal.Monoprice10761BindingConstants.BRIDGE_RESET;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.monoprice10761.internal.Message;
import org.openhab.binding.monoprice10761.internal.Monoprice10761DiscoveryService;
import org.openhab.binding.monoprice10761.internal.config.AmpConfiguration;
import org.openhab.binding.monoprice10761.internal.config.Monoprice10761ZoneConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bridge handler for the Monoprice 10761 Amp
 *
 * @author Cody Cutrer - Initial Contribution
 */

public class AmpHandler extends BaseBridgeHandler implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(AmpHandler.class);

    /** The Discovery Service. */
    private Monoprice10761DiscoveryService discoveryService = null;

    /** Connection status for the bridge. */
    private boolean connected = false;

    /** Determines if things have changed. */
    private boolean thingsHaveChanged = false;

    /** Determines if all things have been initialized. */
    private boolean allThingsInitialized = false;

    /** Thing count. */
    private int thingCount = 0;

    // Polling variables
    public int pollPeriod = 0;

    private ScheduledFuture<?> pollingTask;

    private int numUnits = 1;

    private String serialPortName = "";
    private int baudRate;
    private SerialPort serialPort = null;
    private OutputStreamWriter serialOutput = null;
    private BufferedReader serialInput = null;
    private ArrayList<String> queuedWrites = new ArrayList<String>();
     private boolean isReady = false;
     private StringBuilder pendingMessage = new StringBuilder();

    private final Supplier<SerialPortManager> serialPortManagerSupplier;

    /**
     * Constructor.
     *
     * @param bridge
     */
    public AmpHandler(Bridge bridge, Supplier<SerialPortManager> serialPortManagerSupplier) {
        super(bridge);
        this.serialPortManagerSupplier = serialPortManagerSupplier;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the Bridge handler.");

        AmpConfiguration configuration = getConfigAs(AmpConfiguration.class);

        serialPortName = configuration.serialPort;

        if (serialPortName != null) {
            baudRate = configuration.baud.intValue();
            pollPeriod = configuration.pollPeriod.intValue();

            if (this.pollPeriod > 60) {
                this.pollPeriod = 60;
            } else if (this.pollPeriod < 1) {
                this.pollPeriod = 1;
            }

            logger.debug("Bridge Handler Initialized.");
            logger.debug("   Serial Port: {},", serialPortName);
            logger.debug("   Baud:        {},", baudRate);
            logger.debug("   PollPeriod:  {},", pollPeriod);

            updateStatus(ThingStatus.OFFLINE);
            startPolling();
        }
    }

    @Override
    public void dispose() {
        stopPolling();
        closeConnection();
        super.dispose();
    }

    /**
     * Register the Discovery Service.
     *
     * @param discoveryService
     */
    public void registerDiscoveryService(Monoprice10761DiscoveryService discoveryService) {
        if (discoveryService == null) {
            throw new IllegalArgumentException("registerDiscoveryService(): Illegal Argument. Not allowed to be Null!");
        } else {
            this.discoveryService = discoveryService;
            logger.trace("registerDiscoveryService(): Discovery Service Registered!");
        }
    }

    /**
     * Unregister the Discovery Service.
     */
    public void unregisterDiscoveryService() {
        discoveryService = null;
        logger.trace("unregisterDiscoveryService(): Discovery Service Unregistered!");
    }

    /**
     * Connect The Bridge.
     */
    private void connect() {
        openConnection();

        if (isConnected()) {
            onConnected();
        }
    }

    /**
     * Runs when connected.
     */
    public void onConnected() {
        logger.debug("onConnected(): Bridge Connected!");

        setBridgeStatus(true);
        // request initial status. send first newline to clear any
        // interrupted communication, then request stats for all
        // three units to discover how many units there are
        write("\r\n");
        queueWrite("?10\r\n");
        queueWrite("?20\r\n");
        queueWrite("?30\r\n");

        thingsHaveChanged = true;
    }

    /**
     * Disconnect The Bridge.
     */
    private void disconnect() {
        closeConnection();

        if (!isConnected()) {
            setBridgeStatus(false);
        }
    }

    /**
     * Returns Connected.
     */
    public boolean isConnected() {
        return this.connected;
    }

    /**
     * Sets Connected.
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Set Bridge Status.
     *
     * @param isOnline
     */
    public void setBridgeStatus(boolean isOnline) {
        logger.debug("setBridgeConnection(): Setting Bridge to {}",
                isOnline ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        updateStatus(isOnline ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        ChannelUID channelUID = new ChannelUID(getThing().getUID(), BRIDGE_RESET);
        updateState(channelUID, isOnline ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Method to start the polling task.
     */
    public void startPolling() {
        logger.debug("Starting Polling Task.");
        if (pollingTask == null || pollingTask.isCancelled()) {
            pollingTask = scheduler.scheduleWithFixedDelay(this::polling, 0, pollPeriod * 1000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Method to stop the polling task.
     */
    public void stopPolling() {
        logger.debug("Stopping Polling Task.");
        if (pollingTask != null && !pollingTask.isCancelled()) {
            pollingTask.cancel(true);
            pollingTask = null;
        }
    }

    /**
     * Method for polling the amp.
     */
    public synchronized void polling() {
        logger.debug("Polling Task - '{}' is {}", getThing().getUID(), getThing().getStatus());

        if (isConnected()) {
            for (int i = 1; i <= numUnits; i++) {
                queueWrite(String.format("?%d0\r\n", i));
            }

            checkThings();

            if (thingsHaveChanged) {
                if (allThingsInitialized) {
                    this.setBridgeStatus(isConnected());
                    thingsHaveChanged = false;
                }
            }
        } else {
            logger.error("Not Connected to the Amp!");
            connect();
        }
    }

    /**
     * Check if things have changed.
     */
    public void checkThings() {
        logger.debug("Checking Things!");

        allThingsInitialized = true;

        List<Thing> things = getThing().getThings();

        if (things.size() != thingCount) {
            thingsHaveChanged = true;
            thingCount = things.size();
        }

        for (Thing thing : things) {

            ZoneThingHandler handler = (ZoneThingHandler) thing.getHandler();

            if (handler != null) {
                logger.debug("***Checking '{}' - Status: {}, Initialized: {}", thing.getUID(), thing.getStatus(),
                        handler.isThingHandlerInitialized());

                if (!handler.isThingHandlerInitialized() || !thing.getStatus().equals(ThingStatus.ONLINE)) {
                    if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                        handler.bridgeStatusChanged(getThing().getStatusInfo());
                    }

                    allThingsInitialized = false;
                }
            } else {
                logger.error("checkThings(): Thing handler not found!");
            }
        }
    }

    /**
     * Find a Thing.
     *
     * @param zoneId
     * @return thing
     */
    public Thing findThing(int zoneId) {
        List<Thing> things = getThing().getThings();

        Thing thing = null;

        for (Thing t : things) {
            try {
                Configuration config = t.getConfiguration();
                ZoneThingHandler handler = (ZoneThingHandler) t.getHandler();

                if (handler != null) {
                    BigDecimal zoneNumber = (BigDecimal) config.get(Monoprice10761ZoneConfiguration.ZONE_NUMBER);
                    if (zoneId == zoneNumber.intValue()) {
                        thing = t;
                        logger.debug("findThing(): Thing Found - {}, {}", t, handler);
                        return thing;
                    }
                }
            } catch (Exception e) {
                logger.debug("findThing(): Error Seaching Thing - {} ", e.getMessage(), e);
            }
        }

        return thing;
    }

    public void openConnection() {
        try {
            logger.debug("openConnection(): Connecting to Amp ");

            SerialPortIdentifier portIdentifier = serialPortManagerSupplier.get().getIdentifier(serialPortName);
            if (portIdentifier == null) {
                logger.error("openConnection();: No Such Port");
                setConnected(false);
                return;
            }
            serialPort = portIdentifier.open(this.getClass().getName(), 2000);

            serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.enableReceiveThreshold(1);
            serialPort.disableReceiveTimeout();

            serialOutput = new OutputStreamWriter(serialPort.getOutputStream(), "US-ASCII");
            serialInput = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

            setSerialEventHandler(this);

            setConnected(true);
        } catch (PortInUseException portInUseException) {
            logger.error("openConnection(): Port in Use Exception: {}", portInUseException.getMessage());
            setConnected(false);
        } catch (UnsupportedCommOperationException unsupportedCommOperationException) {
            logger.error("openConnection(): Unsupported Comm Operation Exception: {}",
                    unsupportedCommOperationException.getMessage());
            setConnected(false);
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            logger.error("openConnection(): Unsupported Encoding Exception: {}",
                    unsupportedEncodingException.getMessage());
            setConnected(false);
        } catch (IOException ioException) {
            logger.error("openConnection(): IO Exception: {}", ioException.getMessage());
            setConnected(false);
        }
    }

     public synchronized void queueWrite(String writeString) {
         if (isReady) {
             logger.debug("doing immediate write");
             isReady = false;
             write(writeString);
         } else {
             if (!queuedWrites.contains(writeString)) {
                 queuedWrites.add(writeString);
             }
         }
     }

     public void write(String writeString) {
        try {
            serialOutput.write(writeString);
            serialOutput.flush();
            logger.debug("write(): Message Sent: {}", writeString);
        } catch (IOException ioException) {
            logger.error("write(): {}", ioException.getMessage());
            setConnected(false);
        } catch (Exception exception) {
            logger.error("write(): Unable to write to serial port: {} ", exception.getMessage(), exception);
            setConnected(false);
        }
    }

    public void closeConnection() {
        logger.debug("closeConnection(): Closing Serial Connection!");

        isReady = false;
        queuedWrites.clear();

        if (serialPort == null) {
            setConnected(false);
            return;
        }

        serialPort.removeEventListener();

        if (serialInput != null) {
            IOUtils.closeQuietly(serialInput);
            serialInput = null;
        }

        if (serialOutput != null) {
            IOUtils.closeQuietly(serialOutput);
            serialOutput = null;
        }

        serialPort.close();
        serialPort = null;

        setConnected(false);
        logger.debug("close(): Serial Connection Closed!");
    }

    /**
     * Gets the Serial Port Name of the amp
     *
     * @return serialPortName
     */
    public String getSerialPortName() {
        return serialPortName;
    }

    /**
     * Receives Serial Port Events and reads Serial Port Data.
     *
     * @param serialPortEvent
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                int nextChar = 0;
                 while (serialInput.ready()) {
                     nextChar = serialInput.read();
                     if (nextChar == '\r' || nextChar == '\n') {
                         String messageLine = pendingMessage.toString();
                         pendingMessage.setLength(0);
                         handleIncomingMessage(messageLine);
                     } else {
                         pendingMessage.append((char) nextChar);
                     }
                 }
                 // No more data, and the last line is a bare # with no delimiter,
                 // then it's asking for the next command
                 if (nextChar == '#' && pendingMessage.length() == 1) {
                     logger.debug("ready for next command");
                     if (queuedWrites.isEmpty()) {
                         logger.debug("nothing to write");
                         isReady = true;
                     } else {
                         write(queuedWrites.remove(0));
                     }
                 }
                 logger.debug("nothing more to read");
            } catch (IOException ioException) {
                logger.error("serialEvent(): IO Exception: {}", ioException.getMessage());
            } catch (Exception e) {
                logger.error("serialEvent(): Exception: {}", e.getMessage());
            }
        }
    }

    /**
     * Set the serial event handler.
     *
     * @param serialPortEventListenser
     */
    private void setSerialEventHandler(SerialPortEventListener serialPortEventListenser) {
        try {
            // Add the serial port event listener
            serialPort.addEventListener(serialPortEventListenser);
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException tooManyListenersException) {
            logger.error("setSerialEventHandler(): Too Many Listeners Exception: {}",
                    tooManyListenersException.getMessage());
        }
    }

    /**
     * Handles an incoming message from the amp.
     *
     * @param incomingMessage
     */
    public synchronized void handleIncomingMessage(String incomingMessage) {
        if (incomingMessage != null && !incomingMessage.isEmpty()) {
            Message message = new Message(incomingMessage);

            logger.debug("handleIncomingMessage(): Message received: {} - {}", incomingMessage, message.toString());

            if (message.zoneId == -1) {
                return;
            }

            if (message.zoneId / 10 > numUnits) {
                numUnits = message.zoneId / 10;
            }

            Thing thing = findThing(message.zoneId);

            logger.debug("handleIncomingMessage(): Thing Search - '{}'", thing);

            if (thing != null) {
                ZoneThingHandler thingHandler = (ZoneThingHandler) thing.getHandler();

                if (thingHandler != null) {
                    if (thingHandler.isThingHandlerInitialized()) {
                        thingHandler.eventReceived(message, thing);
                    } else {
                        logger.debug("handleIncomingMessage(): Thing '{}' Not Refreshed!", thing.getUID());
                    }
                }
            } else {
                logger.debug("handleIncomingMessage(): Thing Not Found! Send to Discovery Service!");

                if (discoveryService != null) {
                    discoveryService.addThing(getThing(), message.zoneId);
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand(): Command Received - {} {}.", channelUID, command);

        if (command instanceof RefreshType) {
            return;
        }

        if (isConnected()) {
            switch (channelUID.getId()) {
                case BRIDGE_RESET:
                    if (command == OnOffType.OFF) {
                        disconnect();
                    }
                    break;
            }
        }
    }

    /**
     * Send an API command to the Amp
     *
     * @param zone
     * @param command
     * @param param
     * @return successful
     */
    public boolean sendCommand(int zone, String command, int param) {
        String data = String.format("<%02d%s%02d\r\n", zone, command, param);

        queueWrite(data);
        logger.debug("sendCommand(): '{}' Command Sent - {} {}", zone, command, param);

        return true;
    }
}
