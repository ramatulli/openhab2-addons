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

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.Lightbulb;
import com.beowulfe.hap.accessories.characteristics.Brightness;
import com.beowulfe.hap.accessories.characteristics.Color;

/**
 * Implementing a Homekit Lightbulb using a SwitchItem, DimmerItem, or ColorItem
 *
 * @author Andy Lintner - Initial contribution
 */
class HomekitLightbulbImpl extends AbstractHomekitAccessoryImpl<GenericItem> implements Lightbulb, Brightness, Color {

    public HomekitLightbulbImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater, GenericItem.class);
    }

    @Override
    public CompletableFuture<Boolean> getLightbulbPowerState() {
        OnOffType state = getItem().getStateAs(OnOffType.class);
        return CompletableFuture.completedFuture(state == OnOffType.ON);
    }

    @Override
    public CompletableFuture<Void> setLightbulbPowerState(boolean value) throws Exception {
        GenericItem item = getItem();
        if (item instanceof SwitchItem) {
            ((SwitchItem) item).send(value ? OnOffType.ON : OnOffType.OFF);
        } else if (item instanceof GroupItem) {
            ((GroupItem) item).send(value ? OnOffType.ON : OnOffType.OFF);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeLightbulbPowerState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeLightbulbPowerState() {
        getUpdater().unsubscribe(getItem());
    }

    @Override
    public CompletableFuture<Integer> getBrightness() {
        State state = getItem().getStateAs(PercentType.class);
        if (state instanceof PercentType) {
            PercentType brightness = (PercentType) state;
            return CompletableFuture.completedFuture(brightness.intValue());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> setBrightness(Integer value) throws Exception {
        GenericItem item = getItem();
        if (item instanceof DimmerItem) {
            ((DimmerItem) item).send(new PercentType(value));
        } else if (item instanceof GroupItem) {
            ((GroupItem) item).send(new PercentType(value));
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeBrightness(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), "brightness", callback);
    }

    @Override
    public void unsubscribeBrightness() {
        getUpdater().unsubscribe(getItem(), "brightness");
    }

    @Override
    public CompletableFuture<Double> getHue() {
        State state = getItem().getStateAs(HSBType.class);
        if (state instanceof HSBType) {
            HSBType hsb = (HSBType) state;
            return CompletableFuture.completedFuture(hsb.getHue().doubleValue());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> setHue(Double value) throws Exception {
        if (value == null) {
            value = 0.0;
        }
        State state = getItem().getStateAs(HSBType.class);
        if (state instanceof HSBType) {
            HSBType hsb = (HSBType) state;
            HSBType newState = new HSBType(new DecimalType(value), hsb.getSaturation(), hsb.getBrightness());
            ((ColorItem) getItem()).send(newState);
            return CompletableFuture.completedFuture(null);
        } else {
            // state is undefined (light is not connected)
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public void subscribeHue(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), "hue", callback);
    }

    @Override
    public void unsubscribeHue() {
        getUpdater().unsubscribe(getItem(), "hue");
    }

    @Override
    public CompletableFuture<Double> getSaturation() {
        State state = getItem().getStateAs(HSBType.class);
        if (state instanceof HSBType) {
            HSBType hsb = (HSBType) state;
            return CompletableFuture.completedFuture(hsb.getSaturation().doubleValue());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> setSaturation(Double value) throws Exception {
        if (value == null) {
            value = 0.0;
        }
        State state = getItem().getStateAs(HSBType.class);
        if (state instanceof HSBType) {
            HSBType hsb = (HSBType) state;
            HSBType newState = new HSBType(hsb.getHue(), new PercentType(value.intValue()), hsb.getBrightness());
            ((ColorItem) getItem()).send(newState);
            return CompletableFuture.completedFuture(null);
        } else {
            // state is undefined (light is not connected)
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public void subscribeSaturation(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), "saturation", callback);
    }

    @Override
    public void unsubscribeSaturation() {
        getUpdater().unsubscribe(getItem(), "saturation");
    }

    @Override
    public Optional<Brightness> getBrightnessCharacteristic() {
        if (getItem() instanceof DimmerItem) {
            return Optional.of(this);
        } else {
            Optional<Brightness> result = Optional.empty();
            return result;
        }
    }

    @Override
    public Optional<Color> getColorCharacteristics() {
        if (getItem() instanceof ColorItem) {
            return Optional.of(this);
        } else {
            Optional<Color> result = Optional.empty();
            return result;
        }
    }
}
