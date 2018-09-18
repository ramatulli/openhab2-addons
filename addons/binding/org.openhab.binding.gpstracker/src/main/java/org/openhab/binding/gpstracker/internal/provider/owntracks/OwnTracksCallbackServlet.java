/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.provider.owntracks;

import org.openhab.binding.gpstracker.internal.discovery.TrackerDiscoveryService;
import org.openhab.binding.gpstracker.internal.provider.AbstractCallbackServlet;
import org.openhab.binding.gpstracker.internal.provider.TrackerRegistry;

/**
 * Callback servlet for OwnTracks trackers
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class OwnTracksCallbackServlet extends AbstractCallbackServlet {

    private static final long serialVersionUID = -4053305903339688036L;

    /**
     * Servlet path
     */
    private static final String CALLBACK_PATH = "/gpstracker/owntracks";

    /**
     * Provider name
     */
    private static final String PROVIDER = "OwnTracks";

    /**
     * Constructor called at binding startup.
     *
     * @param discoveryService Discovery service for new trackers.
     * @param trackerRegistry  Tracker registry
     */
    public OwnTracksCallbackServlet(TrackerDiscoveryService discoveryService, TrackerRegistry trackerRegistry) {
        super(discoveryService, trackerRegistry);
    }

    @Override
    public String getPath() {
        return CALLBACK_PATH;
    }

    @Override
    protected String getProvider() {
        return PROVIDER;
    }
}