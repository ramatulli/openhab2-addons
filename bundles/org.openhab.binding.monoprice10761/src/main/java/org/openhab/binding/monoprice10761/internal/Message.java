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
package org.openhab.binding.monoprice10761.internal;

/**
 * Class that parses messages from the amp.
 *
 * @author Cody Cutrer - Initial contribution
 */
public class Message {

    private String message = "";
    public int zoneId;
    public int paStatus;
    public int powerStatus;
    public int muteStatus;
    public int dndStatus;
    public int volumeStatus;
    public int trebleStatus;
    public int bassStatus;
    public int balanceStatus;
    public int sourceStatus;
    public int keypadStatus;

    /**
     * Constructor.
     *
     * @param message
     *            - the message received
     */
    public Message(String message) {
        this.message = message;
        zoneId = -1;
        processMessage();
    }

    /**
     * Processes the incoming message and extracts the information.
     */
    private void processMessage() {
        if (message.length() != 24 || !message.substring(0, 2).equals("#>")) {
            return;
        }

        zoneId = Integer.valueOf(message.substring(2, 4));
        paStatus = Integer.valueOf(message.substring(4, 6));
        powerStatus = Integer.valueOf(message.substring(6, 8));
        muteStatus = Integer.valueOf(message.substring(8, 10));
        dndStatus = Integer.valueOf(message.substring(10, 12));
        volumeStatus = Integer.valueOf(message.substring(12, 14));
        trebleStatus = Integer.valueOf(message.substring(14, 16));
        bassStatus = Integer.valueOf(message.substring(16, 18));
        balanceStatus = Integer.valueOf(message.substring(18, 20));
        sourceStatus = Integer.valueOf(message.substring(20, 22));
        keypadStatus = Integer.valueOf(message.substring(22, 24));
    }
}
