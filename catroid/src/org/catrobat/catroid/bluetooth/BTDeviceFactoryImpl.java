/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2014 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.bluetooth;

import android.content.Context;

import org.catrobat.catroid.arduino.ArduinoImpl;
import org.catrobat.catroid.lego.mindstorm.nxt.LegoNXTImpl;

import java.util.UUID;

public class BTDeviceFactoryImpl implements BTDeviceFactory {

	@Override
	public <T extends BTDeviceService> BTDeviceService createDevice(Class<T> service, Context applicationContext) {

		if (service == BTDeviceService.LEGO_NXT) {
			return new LegoNXTImpl(applicationContext);
		}

//        if (service == BTDeviceService.ALBERT) {
//            return new Albert();
//        }

        if (service == BTDeviceService.ARDUINO) {
            return new ArduinoImpl(applicationContext);
        }

		return null; // may throw exception
	}

	@Override
	public <T extends BTDeviceService> BluetoothConnection createBTConnectionForDevice(Class<T> service,
			String address, UUID deviceUUID, Context applicationContext) {

		return new BluetoothConnection(address, deviceUUID);
	}

}