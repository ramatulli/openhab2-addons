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
package org.openhab.io.homekit.internal.accessories;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.SecuritySystem;
import com.beowulfe.hap.accessories.properties.CurrentSecuritySystemState;
import com.beowulfe.hap.accessories.properties.SecuritySystemAlarmType;
import com.beowulfe.hap.accessories.properties.TargetSecuritySystemState;

/**
 * Implements SecuritySystem as a GroupedAccessory made up of multiple items:
 * <ul>
 * <li>CurrentSecuritySystemState: String type</li>
 * <li>TargetSecuritySystemState: String type</li>
 * </ul>
 *
 * @author Cody Cutrer - Initial contribution
 */
class HomekitSecuritySystemImpl extends AbstractHomekitAccessoryImpl<GroupItem> implements SecuritySystem {

    @NonNull
    private StringItem currentSecuritySystemStateItem;
    private StringItem targetSecuritySystemStateItem;

    private Logger logger = LoggerFactory.getLogger(HomekitSecuritySystemImpl.class);

    public HomekitSecuritySystemImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, Map<HomekitCharacteristicType, Item> origCharacteristicItems)
            throws IncompleteAccessoryException {
        super(taggedItem, itemRegistry, updater, GroupItem.class);

        HashMap<HomekitCharacteristicType, Item> characteristicItems = new HashMap<>(origCharacteristicItems);

        this.currentSecuritySystemStateItem = Optional
                .ofNullable(characteristicItems.get(HomekitCharacteristicType.CURRENT_SECURITY_SYSTEM_STATE))
                .map(m -> (StringItem) m).orElseThrow(() -> new IncompleteAccessoryException(
                        HomekitCharacteristicType.CURRENT_SECURITY_SYSTEM_STATE));

        this.targetSecuritySystemStateItem = Optional
                .ofNullable(characteristicItems.get(HomekitCharacteristicType.TARGET_SECURITY_SYSTEM_STATE))
                .map(m -> (StringItem) m).orElseThrow(
                        () -> new IncompleteAccessoryException(HomekitCharacteristicType.TARGET_SECURITY_SYSTEM_STATE));

        characteristicItems.entrySet().stream().forEach(entry -> {
            logger.error("Item {} has unrecognized security system characteristic: {}", entry.getValue().getName(),
                    entry.getKey().getTag());
        });
    }

    @Override
    public CompletableFuture<CurrentSecuritySystemState> getCurrentSecuritySystemState() {
        CurrentSecuritySystemState state;

        String stringValue = currentSecuritySystemStateItem.getState().toString();
        if (stringValue.equalsIgnoreCase("DISARMED")) {
            state = CurrentSecuritySystemState.DISARMED;
        } else if (stringValue.equalsIgnoreCase("AWAY_ARM")) {
            state = CurrentSecuritySystemState.AWAY_ARM;
        } else if (stringValue.equalsIgnoreCase("STAY_ARM")) {
            state = CurrentSecuritySystemState.STAY_ARM;
        } else if (stringValue.equalsIgnoreCase("NIGHT_ARM")) {
            state = CurrentSecuritySystemState.NIGHT_ARM;
        } else if (stringValue.equalsIgnoreCase("TRIGGERED")) {
            state = CurrentSecuritySystemState.TRIGGERED;
        } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
            logger.debug("Security system target state not available. Relaying value of DISARM to Homekit");
            state = CurrentSecuritySystemState.DISARMED;
        } else {
            logger.error(
                    "Unrecognized security system target state: {}. Expected DISARM, AWAY_ARM, STAY_ARM, NIGHT_ARM strings in value.",
                    stringValue);
            state = CurrentSecuritySystemState.DISARMED;
        }
        return CompletableFuture.completedFuture(state);
    }

    @Override
    public void subscribeCurrentSecuritySystemState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(currentSecuritySystemStateItem, callback);
    }

    @Override
    public void unsubscribeCurrentSecuritySystemState() {
        getUpdater().unsubscribe(currentSecuritySystemStateItem);
    }

    @Override
    public void setTargetSecuritySystemState(TargetSecuritySystemState state) throws Exception {
        targetSecuritySystemStateItem.send(new StringType(state.toString()));
    }

    @Override
    public CompletableFuture<TargetSecuritySystemState> getTargetSecuritySystemState() {
        TargetSecuritySystemState state;

        String stringValue = targetSecuritySystemStateItem.getState().toString();
        if (stringValue.equalsIgnoreCase("DISARM")) {
            state = TargetSecuritySystemState.DISARM;
        } else if (stringValue.equalsIgnoreCase("AWAY_ARM")) {
            state = TargetSecuritySystemState.AWAY_ARM;
        } else if (stringValue.equalsIgnoreCase("STAY_ARM")) {
            state = TargetSecuritySystemState.STAY_ARM;
        } else if (stringValue.equalsIgnoreCase("NIGHT_ARM")) {
            state = TargetSecuritySystemState.NIGHT_ARM;
        } else if (stringValue.equals("UNDEF") || stringValue.equals("NULL")) {
            logger.debug("Security system target state not available. Relaying value of DISARM to Homekit");
            state = TargetSecuritySystemState.DISARM;
        } else {
            logger.error(
                    "Unrecognized security system target state: {}. Expected DISARM, AWAY_ARM, STAY_ARM, NIGHT_ARM strings in value.",
                    stringValue);
            state = TargetSecuritySystemState.DISARM;
        }
        return CompletableFuture.completedFuture(state);
    }

    @Override
    public void subscribeTargetSecuritySystemState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(targetSecuritySystemStateItem, callback);
    }

    @Override
    public void unsubscribeTargetSecuritySystemState() {
        getUpdater().unsubscribe(currentSecuritySystemStateItem);
    }

    @Override
    public CompletableFuture<SecuritySystemAlarmType> getAlarmTypeState() {
        return CompletableFuture.completedFuture(SecuritySystemAlarmType.NO_ALARM);
    }

    @Override
    public void subscribeAlarmTypeState(HomekitCharacteristicChangeCallback callback) {
    }

    @Override
    public void unsubscribeAlarmTypeState() {
    }
}
