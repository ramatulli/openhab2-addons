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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.openhab.io.homekit.internal.battery.BatteryStatus;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.AbstractSensor;
import com.beowulfe.hap.accessories.characteristics.LowBatteryStatus;

/**
 *
 * @author Cody Cutrer - Initial contribution
 */
public abstract class AbstractHomekitSensorImpl extends AbstractHomekitAccessoryImpl<GenericItem>
        implements AbstractSensor, LowBatteryStatus {

    @NonNull
    private BatteryStatus batteryStatus;

    public AbstractHomekitSensorImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            MetadataRegistry metadataRegistry, HomekitAccessoryUpdater updater, BatteryStatus batteryStatus) {
        super(taggedItem, itemRegistry, metadataRegistry, updater, GenericItem.class);
        this.batteryStatus = batteryStatus;
    }

    @Override
    public CompletableFuture<Boolean> getLowBatteryState() {
        return CompletableFuture.completedFuture(batteryStatus.isLow());
    }

    @Override
    public void subscribeLowBatteryState(HomekitCharacteristicChangeCallback callback) {
        batteryStatus.subscribe(getUpdater(), callback);
    }

    @Override
    public void unsubscribeLowBatteryState() {
        batteryStatus.unsubscribe(getUpdater());
    }

    @Override
    public Optional<LowBatteryStatus> getLowBatteryStatusCharacteristic() {
        if (batteryStatus == null) {
            Optional<LowBatteryStatus> result = Optional.empty();
            return result;
        } else {
            return Optional.of(this);
        }
    }
}
