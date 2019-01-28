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
package org.openhab.binding.monoprice10761.internal.config;

/**
 * Configuration class for the Monoprice 10761 Zone Thing.
 *
 * @author Cody Cutrer - Initial contribution
 */

public class Monoprice10761ZoneConfiguration {

    // Zone Thing constants
    public static final String ZONE_NUMBER = "zoneNumber";

    /**
     * The Zone Number. Can be in the range of 11-16, 21-26, 31-36. This is a required parameter for a zone.
     */
    public Integer zoneNumber;
}
