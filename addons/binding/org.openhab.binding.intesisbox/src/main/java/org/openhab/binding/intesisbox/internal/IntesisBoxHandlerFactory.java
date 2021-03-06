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
package org.openhab.binding.intesisbox.internal;

import static org.openhab.binding.intesisbox.internal.IntesisBoxBindingConstants.THING_TYPE_INTESISBOX;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link IntesisBoxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Cody Cutrer - Initial contribution
 * @author Rocky Amatulli - Added stateDescriptionProvider
 */
@NonNullByDefault
@Component(configurationPid = "binding.intesisbox", service = ThingHandlerFactory.class)
public class IntesisBoxHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_INTESISBOX);
    private IntesisBoxStateDescriptionProvider stateDescriptionProvider = new IntesisBoxStateDescriptionProvider();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_INTESISBOX.equals(thingTypeUID)) {
            return new IntesisBoxHandler(thing, stateDescriptionProvider);
        }

        return null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(IntesisBoxStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    protected void unsetDynamicStateDescriptionProvider(IntesisBoxStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider.deactivate();
    }

}
