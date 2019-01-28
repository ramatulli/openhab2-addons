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
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.monoprice10761.internal.config.AmpConfiguration;
import org.openhab.binding.monoprice10761.internal.config.Monoprice10761ZoneConfiguration;
import org.openhab.binding.monoprice10761.internal.handler.AmpHandler;
import org.openhab.binding.monoprice10761.internal.handler.ZoneThingHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link Monoprice10761HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.monoprice10761", service = ThingHandlerFactory.class)
public class Monoprice10761HandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return Monoprice10761BindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (Monoprice10761BindingConstants.AMP_THING_TYPE.equals(thingTypeUID)) {
            ThingUID it100BridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, it100BridgeUID, null);
        } else if (Monoprice10761BindingConstants.ZONE_THING_TYPE.equals(thingTypeUID)) {
            ThingUID zoneThingUID = getZoneUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, zoneThingUID, bridgeUID);
        }

        throw new IllegalArgumentException(
                "createThing(): The thing type " + thingTypeUID + " is not supported by the DSC Alarm binding.");
    }

    /**
     * Get the Bridge Thing UID.
     *
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @return thingUID
     */
    private @Nullable ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration) {
        if (thingUID == null) {
            String serialPort = (String) configuration.get(AmpConfiguration.SERIAL_PORT);
            String bridgeID = serialPort.replace('.', '_');
            return new ThingUID(thingTypeUID, bridgeID);
        }
        return thingUID;
    }

    /**
     * Get the Zone Thing UID.
     *
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @param bridgeUID
     * @return thingUID
     */
    private @Nullable ThingUID getZoneUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration, @Nullable ThingUID bridgeUID) {
        if (thingUID == null && bridgeUID != null) {
            String zoneId = "zone" + (String) configuration.get(Monoprice10761ZoneConfiguration.ZONE_NUMBER);
            return new ThingUID(thingTypeUID, zoneId, bridgeUID.getId());
        }
        return thingUID;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (Monoprice10761BindingConstants.AMP_THING_TYPE.equals(thingTypeUID)) {
            AmpHandler handler = new AmpHandler((Bridge) thing);
            registerDiscoveryService(handler);
            return handler;
        } else if (Monoprice10761BindingConstants.ZONE_THING_TYPE.equals(thingTypeUID)) {
            return new ZoneThingHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        ServiceRegistration<?> discoveryServiceRegistration = discoveryServiceRegistrations
                .get(thingHandler.getThing().getUID());

        Monoprice10761DiscoveryService discoveryService = (Monoprice10761DiscoveryService) bundleContext
                .getService(discoveryServiceRegistration.getReference());
        discoveryService.deactivate();
        discoveryServiceRegistration.unregister();
        discoveryServiceRegistration = null;
        discoveryServiceRegistrations.remove(thingHandler.getThing().getUID());

        super.removeHandler(thingHandler);
    }

    /**
     * Register the Thing Discovery Service for a bridge.
     *
     * @param bridgeHandler
     */
    private void registerDiscoveryService(AmpHandler bridgeHandler) {
        Monoprice10761DiscoveryService discoveryService = new Monoprice10761DiscoveryService(bridgeHandler);
        discoveryService.activate();

        ServiceRegistration<?> discoveryServiceRegistration = bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
        discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), discoveryServiceRegistration);
    }
}
