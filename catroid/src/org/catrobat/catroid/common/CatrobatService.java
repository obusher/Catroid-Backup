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
package org.catrobat.catroid.common;

import org.catrobat.catroid.bluetooth.BTDeviceConnector;
import org.catrobat.catroid.lego.mindstorm.nxt.LegoNXT;
import org.catrobat.catroid.arduino.Arduino;
import org.catrobat.catroid.devices.arduino.kodey.Kodey;


// CHECKSTYLE DISABLE InterfaceIsType FOR 1 LINES
public interface CatrobatService {

	public static final Class<LegoNXT> LEGO_NXT = LegoNXT.class;
	public static final Class<Kodey> KODEY = Kodey.class;
    public static final Class<Arduino> ARDUINO = Arduino.class;
//    public static final Class<Albert> ALBERT = Albert.class;


	// Common services - gets created by ServiceProvider if needed
	public static final Class<BTDeviceConnector> BLUETOOTH_DEVICE_CONNECTOR = BTDeviceConnector.class;

}