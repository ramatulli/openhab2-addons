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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link Monoprice10761BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Monoprice10761BindingConstants {

    private static final String BINDING_ID = "monoprice10761";

    // List of all Thing Type UIDs
    public static final ThingTypeUID AMP_THING_TYPE = new ThingTypeUID(BINDING_ID, "amp");
    public static final ThingTypeUID ZONE_THING_TYPE = new ThingTypeUID(BINDING_ID, "zone");

    // List of all Channel ids
    public static final String BRIDGE_RESET = "bridge_reset";

    public static final String POWER = "power";
    public static final String SOURCE = "source";
    public static final String VOLUME = "volume";
    public static final String MUTE = "mute";
    public static final String DND = "dnd";
    public static final String TREBLE = "treble";
    public static final String BASS = "bass";
    public static final String BALANCE = "balance";
    public static final String PA = "pa";
    public static final String KEYPAD = "keypad";

    // Set of all supported Thing Type UIDs
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(Monoprice10761BindingConstants.AMP_THING_TYPE, Monoprice10761BindingConstants.ZONE_THING_TYPE)
                    .collect(Collectors.toSet()));

}
