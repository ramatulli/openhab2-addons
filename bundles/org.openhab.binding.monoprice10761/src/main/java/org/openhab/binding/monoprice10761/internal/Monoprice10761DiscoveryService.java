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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.monoprice10761.internal.config.Monoprice10761ZoneConfiguration;
import org.openhab.binding.monoprice10761.internal.handler.AmpHandler;

/**
 * This class is responsible for discovering zones via the bridge.
 *
 * @author Cody Cutrer - Initial Contribution
 *
 */
public class Monoprice10761DiscoveryService extends AbstractDiscoveryService {

    /**
     * Bridge handler.
     */
    AmpHandler bridgeHandler;

    /**
     * Constructor.
     *
     * @param ampHandler
     */
    public Monoprice10761DiscoveryService(AmpHandler ampHandler) {
        super(Monoprice10761BindingConstants.SUPPORTED_THING_TYPES_UIDS, 15, true);
        this.bridgeHandler = ampHandler;
    }

    /**
     * Activates the Discovery Service.
     */
    public void activate() {
        bridgeHandler.registerDiscoveryService(this);
    }

    /**
     * Deactivates the Discovery Service.
     */
    @Override
    public void deactivate() {
        bridgeHandler.unregisterDiscoveryService();
    }

    /**
     * Method to add a Thing to the Smarthome Inbox.
     *
     * @param bridge
     * @param bridgeThingType
     * @param event
     */
    public void addThing(Bridge bridge, int zoneNumber) {
        ThingUID thingUID = null;
        String thingID = "";
        String thingLabel = "";
        Map<String, Object> properties = null;

        if (zoneNumber >= 11 && zoneNumber <= 16 || zoneNumber >= 21 && zoneNumber <= 26
                || zoneNumber >= 31 && zoneNumber <= 36) {
            thingID = "zone" + String.valueOf(zoneNumber);
            thingLabel = "Zone " + String.valueOf(zoneNumber);

            properties = new HashMap<>(0);
            thingUID = new ThingUID(Monoprice10761BindingConstants.ZONE_THING_TYPE, bridge.getUID(), thingID);
            properties.put(Monoprice10761ZoneConfiguration.ZONE_NUMBER, zoneNumber);
        }

        if (thingUID != null) {
            DiscoveryResult discoveryResult;

            if (properties != null) {
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridge.getUID()).withLabel(thingLabel).build();
            } else {
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridge.getUID())
                        .withLabel(thingLabel).build();
            }

            thingDiscovered(discoveryResult);
        }

    }

    @Override
    protected void startScan() {
        // Can be ignored here as discovery is via the bridge
    }
}
